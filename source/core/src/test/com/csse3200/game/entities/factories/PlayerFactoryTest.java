package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.itemdictionary.ItemDictionaryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.PlayerConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputService;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.ParticleEffectsRenderingComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

/** Tests factory assembly without starting the inventory/shop UI or loading GPU textures. */
@ExtendWith(GameExtension.class)
class PlayerFactoryTest {
  private PhysicsService physics;
  private GameTime time;
  private InputProcessor previousInput;

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);

    LightingEngine lightingEngine = mock(LightingEngine.class);
    when(lightingEngine.getRayHandler()).thenReturn(mock(box2dLight.RayHandler.class));
    LightingService lightingService = mock(LightingService.class);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerLightingService(lightingService);

    previousInput = Gdx.input.getInputProcessor();
    ServiceLocator.registerInputService(new InputService());
    TextureAtlas atlas = mock(TextureAtlas.class);
    AtlasRegion region = mock(AtlasRegion.class);
    when(region.getRegionWidth()).thenReturn(32);
    when(region.getRegionHeight()).thenReturn(64);
    when(atlas.findRegion("default")).thenReturn(region);
    when(atlas.findRegions(anyString()))
        .thenAnswer(invocation -> new Array<>(new AtlasRegion[] {region}));
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset("images/player/player.atlas", TextureAtlas.class)).thenReturn(atlas);
    ServiceLocator.registerResourceService(resources);
  }

  @AfterEach
  void tearDown() {
    physics.getPhysics().dispose();
    Gdx.input.setInputProcessor(previousInput);
  }

  @Test
  void shouldEquipAttachedBowAndRegisterCombatAnimations() {
    try (MockedConstruction<ParticleEffectsRenderingComponent> particles =
        mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity player = PlayerFactory.createPlayer();
      BowComponent bow = player.getComponent(BowComponent.class);
      assertNotNull(bow);
      assertSame(bow, player.getComponent(WeaponComponent.class).getPrimaryWeapon());
      assertNotNull(player.getComponent(GrappleComponent.class));
      assertNotNull(player.getComponent(PlayerActions.class));
      AnimationRenderComponent animator = player.getComponent(AnimationRenderComponent.class);
      for (String clip :
          new String[] {
            "idle",
            "walk",
            "sprint",
            "jump",
            "hurt",
            "death",
            "air_dash",
            "bow_draw",
            "bow_hold",
            "bow_shoot"
          }) {
        assertTrue(animator.hasAnimation(clip), "Missing player animation: " + clip);
      }
      player.getComponent(PlayerAnimationController.class).create();
      assertEquals("idle", animator.getCurrentAnimation());
      assertEquals(0.6f, player.getScale().x, 0.001f);
      assertEquals(1.2f, player.getScale().y, 0.001f);
      player.getComponent(PhysicsComponent.class).create();
      player.getComponent(ColliderComponent.class).create();
      player.getComponent(HitboxComponent.class).create();
      assertEquals(PhysicsLayer.PLAYER, player.getComponent(HitboxComponent.class).getLayer());
      assertTrue(player.getComponent(HitboxComponent.class).getFixture().isSensor());
      assertFalse(player.getComponent(ColliderComponent.class).getFixture().isSensor());
    }
  }

  @Test
  void shouldCreateDisplayPlayerWithoutPlayerActions() {
    Entity player = PlayerFactory.createPlayerDisplay();

    assertNotNull(player.getComponent(AnimationRenderComponent.class));
    org.junit.jupiter.api.Assertions.assertNull(player.getComponent(PlayerActions.class));
  }

  @Test
  void shouldApplyConfiguredHealthDamageAndInvulnerabilityToCreatedPlayer() {
    PlayerConfig config = FileLoader.readClass(PlayerConfig.class, "configs/player.json");
    CombatStatsComponent stats;
    try (MockedConstruction<ParticleEffectsRenderingComponent> particles =
        mockConstruction(ParticleEffectsRenderingComponent.class)) {
      stats = PlayerFactory.createPlayer().getComponent(CombatStatsComponent.class);
    }
    assertEquals(config.health, stats.getHealth());
    assertEquals(config.baseAttack, stats.getBaseAttack());
    CombatStatsComponent attacker = new CombatStatsComponent(10, 1);
    stats.hit(attacker);
    assertEquals(config.health - 1, stats.getHealth());
    when(time.getTime()).thenReturn(config.invulnerabilityDuration - 1);
    stats.hit(attacker);
    assertEquals(config.health - 1, stats.getHealth());
    when(time.getTime()).thenReturn(config.invulnerabilityDuration);
    stats.hit(attacker);
    assertEquals(config.health - 2, stats.getHealth());
  }

  @Test
  void shouldCreateFullPlayerWithBowComponent() {
    try (MockedConstruction<ParticleEffectsRenderingComponent> particles =
        mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity player = PlayerFactory.createPlayer();

      assertNotNull(player.getComponent(BowComponent.class));
    }
  }

  @Test
  void shouldCreateDisplayPlayerWithoutMovementWeaponsOrGrapple() {
    Entity display = PlayerFactory.createPlayerDisplay();
    assertNull(display.getComponent(PlayerActions.class));
    assertNull(display.getComponent(BowComponent.class));
    assertNull(display.getComponent(WeaponComponent.class));
    assertNull(display.getComponent(GrappleComponent.class));
    PlayerAnimationController controller = display.getComponent(PlayerAnimationController.class);
    controller.create();
    display.getEvents().trigger("death");
    assertEquals(
        "death", display.getComponent(AnimationRenderComponent.class).getCurrentAnimation());
    assertEquals(0.6f, display.getScale().x, 0.001f);
    assertEquals(1.2f, display.getScale().y, 0.001f);
  }

  @Test
  void giveStartingLoadoutAddsRopeArrow() {
    Entity player =
        new Entity()
            .addComponent(new InventoryComponent(50))
            .addComponent(new ItemDictionaryComponent());
    player.create();

    PlayerFactory.giveStartingLoadout(player);

    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    assertEquals(1, inventory.getItemCount(ItemType.ROPE_ARROW));

    ItemDictionaryComponent dictionary = player.getComponent(ItemDictionaryComponent.class);
    assertTrue(dictionary.isDiscovered(ItemType.ROPE_ARROW));
  }

  @Test
  void giveStartingLoadoutIgnoresNullPlayer() {
    assertDoesNotThrow(() -> PlayerFactory.giveStartingLoadout(null));
  }
}
