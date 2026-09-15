package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/** Exercises inventory-to-bow behaviour with Mockito boundaries for time, input and projectiles. */
@ExtendWith(GameExtension.class)
class PlayerCombatIntegrationTest {
  private Entity player;
  private InventoryComponent inventory;
  private ItemUseComponent itemUse;
  private BowComponent bow;
  private WeaponComponent weapon;
  private GameTime time;
  private EntityService entities;
  private Entity projectile;
  private Sound sound;
  private Input previousInput;
  private KeyboardPlayerInputComponent input;
  private MockedStatic<ProjectileFactory> factory;
  private EventListener1<ItemType> itemUsed;
  private EventListener1<ItemType> itemFailed;
  private EventListener1<Vector2> primaryAttack;
  private EventListener1<Vector2> chargeStart;
  private EventListener1<Vector2> animation;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    time = mock(GameTime.class);
    entities = mock(EntityService.class);
    ResourceService resources = mock(ResourceService.class);
    sound = mock(Sound.class);
    projectile = mock(Entity.class);
    ServiceLocator.registerTimeSource(time);
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerResourceService(resources);
    when(resources.containsAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(true);
    when(resources.getAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(sound);

    previousInput = Gdx.input;
    Gdx.input = mock(Input.class);
    when(Gdx.input.getX()).thenReturn(40);
    when(Gdx.input.getY()).thenReturn(60);
    ServiceLocator.registerInputService(mock(InputService.class));
    Camera camera = mock(Camera.class);
    when(camera.unproject(any(Vector3.class))).thenAnswer(ignored -> new Vector3(5f, 7f, 0f));
    input = new KeyboardPlayerInputComponent();
    input.setCameraComponent(new CameraComponent(camera));
    inventory = new InventoryComponent(0);
    itemUse = new ItemUseComponent();
    bow = new BowComponent();
    weapon = new WeaponComponent(bow);
    player =
        new Entity()
            .addComponent(new CombatStatsComponent(100, 10))
            .addComponent(inventory)
            .addComponent(bow)
            .addComponent(weapon)
            .addComponent(itemUse)
            .addComponent(input);
    player.setPosition(1f, 2f);
    player.setScale(2f, 2f);
    player.create();

    itemUsed = mock(EventListener1.class);
    itemFailed = mock(EventListener1.class);
    primaryAttack = mock(EventListener1.class);
    chargeStart = mock(EventListener1.class);
    animation = mock(EventListener1.class);
    player.getEvents().addListener("itemUsed", itemUsed);
    player.getEvents().addListener("itemUseFailed", itemFailed);
    player.getEvents().addListener("primaryAttack", primaryAttack);
    player.getEvents().addListener("chargeStart", chargeStart);
    player.getEvents().addListener("attackAnimation", animation);

    factory = mockStatic(ProjectileFactory.class);
    factory
        .when(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), any(Float.class)))
        .thenReturn(projectile);
    factory
        .when(() -> ProjectileFactory.createFireArrow(eq(player), any(), any(), any(Float.class)))
        .thenReturn(projectile);
    factory
        .when(() -> ProjectileFactory.createIceArrow(eq(player), any(), any(), any(Float.class)))
        .thenReturn(projectile);
  }

  @AfterEach
  void tearDown() {
    if (factory != null) {
      factory.close();
    }
    Gdx.input = previousInput;
    ServiceLocator.clear();
  }

  @Test
  void shouldFireOneStandardArrowWithNormalizedAimAndConsumeOneItem() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);

    assertTrue(itemUse.useSelectedItem());

    ArgumentCaptor<Vector2> position = ArgumentCaptor.forClass(Vector2.class);
    ArgumentCaptor<Vector2> direction = ArgumentCaptor.forClass(Vector2.class);
    factory.verify(
        () ->
            ProjectileFactory.createPlayerArrow(
                eq(player), position.capture(), direction.capture(), eq(1f)));
    factory.verifyNoMoreInteractions();
    assertTrue(direction.getValue().epsilonEquals(new Vector2(0.6f, 0.8f)));
    assertTrue(position.getValue().epsilonEquals(new Vector2(2.96f, 4.28f)));
    verify(entities).register(projectile);
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(primaryAttack).handle(new Vector2(3f, 4f));
    verify(itemUsed).handle(ItemType.STANDARD_ARROW);
    verify(animation).handle(new Vector2(0.6f, 0.8f));
    verify(sound).play();
    verifyNoInteractions(itemFailed);
  }

  @Test
  void shouldSelectFireFactoryAndConsumeLastArrow() {
    inventory.addItem(ItemType.FIRE_ARROW, 1);

    assertTrue(itemUse.useSelectedItem());

    factory.verify(() -> ProjectileFactory.createFireArrow(eq(player), any(), any(), eq(1f)));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    assertEquals(0, inventory.getItemCount(ItemType.FIRE_ARROW));
    verify(itemUsed).handle(ItemType.FIRE_ARROW);
    verifyNoInteractions(itemFailed);
  }

  @Test
  void shouldSelectIceFactoryAndConsumeOneArrow() {
    inventory.addItem(ItemType.ICE_ARROW, 2);

    assertTrue(itemUse.useSelectedItem());

    factory.verify(() -> ProjectileFactory.createIceArrow(eq(player), any(), any(), eq(1f)));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    assertEquals(1, inventory.getItemCount(ItemType.ICE_ARROW));
    verify(itemUsed).handle(ItemType.ICE_ARROW);
    verifyNoInteractions(itemFailed);
  }

  @Test
  void shouldNotFireWithEmptyInventory() {
    assertFalse(itemUse.useSelectedItem());

    factory.verifyNoInteractions();
    verifyNoInteractions(entities, sound, primaryAttack, itemUsed, animation);
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldPreserveAmmoAndRejectSecondShotDuringCooldown() {
    inventory.addItem(ItemType.STANDARD_ARROW, 3);
    assertTrue(itemUse.useSelectedItem());

    boolean accepted = itemUse.useSelectedItem();

    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertFalse(accepted);
    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(1f)));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    verify(primaryAttack).handle(any());
    verify(itemUsed).handle(ItemType.STANDARD_ARROW);
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
    verify(animation).handle(any());
    verify(sound).play();
  }

  @Test
  void shouldFireAgainOnlyAfterCooldownExpires() {
    inventory.addItem(ItemType.STANDARD_ARROW, 3);
    assertTrue(itemUse.useSelectedItem());
    when(time.getDeltaTime()).thenReturn(0.39f);
    bow.update();
    assertFalse(itemUse.useSelectedItem());
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));

    when(time.getDeltaTime()).thenReturn(0.02f);
    bow.update();
    assertTrue(itemUse.useSelectedItem());

    factory.verify(
        () -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(1f)), times(2));
    factory.verifyNoMoreInteractions();
    verify(entities, times(2)).register(projectile);
    verify(itemUsed, times(2)).handle(ItemType.STANDARD_ARROW);
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldNotBypassCooldownByChangingArrowType() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    inventory.addItem(ItemType.FIRE_ARROW, 2);
    assertTrue(itemUse.useSelectedItem());
    player.getEvents().trigger("selectQuickSlot", 1);

    assertFalse(itemUse.useSelectedItem());

    assertEquals(2, inventory.getItemCount(ItemType.FIRE_ARROW));
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(1f)));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    verify(primaryAttack).handle(any());
    verify(itemUsed).handle(ItemType.STANDARD_ARROW);
    verify(itemFailed).handle(ItemType.FIRE_ARROW);

    when(time.getDeltaTime()).thenReturn(0.4f);
    bow.update();
    assertTrue(itemUse.useSelectedItem());
    factory.verify(() -> ProjectileFactory.createFireArrow(eq(player), any(), any(), eq(1f)));
    verify(entities, times(2)).register(projectile);
    verify(itemUsed).handle(ItemType.FIRE_ARROW);
    assertEquals(1, inventory.getItemCount(ItemType.FIRE_ARROW));
  }

  @Test
  void shouldPreserveAmmoWithoutEquippedWeapon() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    weapon.setPrimaryWeapon(null);

    assertFalse(itemUse.useSelectedItem());

    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    factory.verifyNoInteractions();
    verifyNoInteractions(entities, sound, primaryAttack, itemUsed, animation);
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
  }

  @Test
  void shouldPreserveAmmoWithoutWeaponCoordinator() {
    InventoryComponent isolatedInventory = new InventoryComponent(0);
    ItemUseComponent isolatedItemUse = new ItemUseComponent();
    Entity incompletePlayer =
        new Entity().addComponent(isolatedInventory).addComponent(isolatedItemUse);
    incompletePlayer.create();
    incompletePlayer.getEvents().addListener("primaryAttack", primaryAttack);
    incompletePlayer.getEvents().addListener("itemUsed", itemUsed);
    incompletePlayer.getEvents().addListener("itemUseFailed", itemFailed);
    isolatedInventory.addItem(ItemType.STANDARD_ARROW, 2);

    assertFalse(isolatedItemUse.useSelectedItem());

    assertEquals(2, isolatedInventory.getItemCount(ItemType.STANDARD_ARROW));
    factory.verifyNoInteractions();
    verifyNoInteractions(entities, sound, primaryAttack, itemUsed);
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
  }

  @Test
  void shouldRejectChargeAndPreserveAmmoWithoutEquippedWeapon() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    weapon.setPrimaryWeapon(null);

    player.getEvents().trigger("shoot", new Vector2(3f, 4f));

    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
    verifyNoInteractions(chargeStart, itemUsed, entities, primaryAttack, animation, sound);
    factory.verifyNoInteractions();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldRejectChargeAndPreserveAmmoWithoutWeaponCoordinator() {
    InventoryComponent isolatedInventory = new InventoryComponent(0);
    ItemUseComponent isolatedItemUse = new ItemUseComponent();
    Entity incompletePlayer =
        new Entity().addComponent(isolatedInventory).addComponent(isolatedItemUse);
    EventListener1<ItemType> isolatedItemFailed = mock(EventListener1.class);
    EventListener1<Vector2> isolatedChargeStart = mock(EventListener1.class);
    incompletePlayer.getEvents().addListener("itemUseFailed", isolatedItemFailed);
    incompletePlayer.getEvents().addListener("chargeStart", isolatedChargeStart);
    incompletePlayer.create();
    isolatedInventory.addItem(ItemType.STANDARD_ARROW, 2);

    incompletePlayer.getEvents().trigger("shoot", new Vector2(3f, 4f));

    assertEquals(2, isolatedInventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(isolatedItemFailed).handle(ItemType.STANDARD_ARROW);
    verifyNoInteractions(isolatedChargeStart, entities);
    factory.verifyNoInteractions();
  }

  @Test
  void shouldRequireEReleaseEvenAfterBowCooldownExpires() {
    inventory.addItem(ItemType.STANDARD_ARROW, 3);
    assertTrue(input.keyDown(Keys.E));
    assertTrue(input.keyDown(Keys.E));
    when(time.getDeltaTime()).thenReturn(0.4f);
    bow.update();
    assertTrue(input.keyDown(Keys.E));
    verify(entities).register(projectile);
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verifyNoInteractions(itemFailed);

    assertTrue(input.keyUp(Keys.E));
    assertTrue(input.keyDown(Keys.E));

    verify(entities, times(2)).register(projectile);
    verify(itemUsed, times(2)).handle(ItemType.STANDARD_ARROW);
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldShareECooldownWithRightClick() {
    inventory.addItem(ItemType.STANDARD_ARROW, 3);
    input.keyDown(Keys.E);

    assertTrue(input.touchDown(40, 60, 0, Buttons.RIGHT));

    verify(entities).register(projectile);
    verify(primaryAttack).handle(any());
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    input.touchUp(40, 60, 0, Buttons.RIGHT);
    when(time.getDeltaTime()).thenReturn(0.4f);
    bow.update();
    input.touchDown(40, 60, 0, Buttons.RIGHT);
    verify(chargeStart).handle(new Vector2(3f, 4f));
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(entities).register(projectile);

    input.touchUp(40, 60, 0, Buttons.RIGHT);

    verify(entities, times(2)).register(projectile);
    verify(itemUsed, times(2)).handle(ItemType.STANDARD_ARROW);
    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(0.3f)));
  }

  @Test
  void shouldShareRightClickCooldownWithE() {
    inventory.addItem(ItemType.STANDARD_ARROW, 3);
    input.touchDown(40, 60, 0, Buttons.RIGHT);
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(chargeStart).handle(new Vector2(3f, 4f));
    verifyNoInteractions(entities, primaryAttack, itemFailed);

    input.keyDown(Keys.E);

    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verifyNoInteractions(entities, primaryAttack);

    input.touchUp(40, 60, 0, Buttons.RIGHT);

    verify(entities).register(projectile);
    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(0.3f)));
    when(time.getDeltaTime()).thenReturn(0.4f);
    bow.update();
    input.keyUp(Keys.E);
    input.keyDown(Keys.E);
    verify(entities, times(2)).register(projectile);
    verify(itemUsed, times(2)).handle(ItemType.STANDARD_ARROW);
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldRejectRepeatedChargeStartsWithoutConsumingAnotherArrow() {
    inventory.addItem(ItemType.STANDARD_ARROW, 3);

    assertTrue(input.touchDown(40, 60, 0, Buttons.RIGHT));
    assertTrue(input.touchDown(40, 60, 0, Buttons.RIGHT));

    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(chargeStart).handle(new Vector2(3f, 4f));
    verify(itemUsed).handle(ItemType.STANDARD_ARROW);
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
    verifyNoInteractions(entities, primaryAttack, animation, sound);

    input.touchUp(40, 60, 0, Buttons.RIGHT);

    verify(entities).register(projectile);
    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(0.3f)));
  }

  @Test
  void shouldReleaseReservedArrowTypeAfterSelectionChangesDuringCharge() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    inventory.addItem(ItemType.FIRE_ARROW, 2);
    input.touchDown(40, 60, 0, Buttons.RIGHT);

    input.keyDown(Keys.NUM_2);
    input.touchUp(40, 60, 0, Buttons.RIGHT);

    assertEquals(ItemType.FIRE_ARROW, inventory.getSelectedItem());
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(2, inventory.getItemCount(ItemType.FIRE_ARROW));
    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(0.3f)));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    verify(itemUsed).handle(ItemType.STANDARD_ARROW);
    verifyNoInteractions(itemFailed);
  }

  @Test
  void shouldReleaseLastReservedArrowAfterInventoryAutoSelectsNextType() {
    inventory.addItem(ItemType.STANDARD_ARROW, 1);
    inventory.addItem(ItemType.FIRE_ARROW, 1);

    input.touchDown(40, 60, 0, Buttons.RIGHT);

    assertEquals(ItemType.FIRE_ARROW, inventory.getSelectedItem());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verifyNoInteractions(entities);

    input.touchUp(40, 60, 0, Buttons.RIGHT);

    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(0.3f)));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    verify(itemUsed).handle(ItemType.STANDARD_ARROW);
    verifyNoInteractions(itemFailed);
  }

  @Test
  void shouldCancelReservedChargeOnDeathWithoutGhostFiring() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    input.touchDown(40, 60, 0, Buttons.RIGHT);

    player.getEvents().trigger("death");
    player.getEvents().trigger("chargeRelease", new Vector2(3f, 4f));

    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(itemUsed).handle(ItemType.STANDARD_ARROW);
    verifyNoInteractions(entities, primaryAttack, animation, sound, itemFailed);
    factory.verifyNoInteractions();
  }

  @Test
  void shouldRejectInvalidChargeAimBeforeConsumingAmmo() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);

    player.getEvents().trigger("shoot", Vector2.Zero.cpy());

    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    verify(itemFailed).handle(ItemType.STANDARD_ARROW);
    verifyNoInteractions(chargeStart, itemUsed, entities, primaryAttack, animation, sound);
    factory.verifyNoInteractions();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldRouteRopeArrowWithoutConsumingItAndReleaseOnMouseUp() {
    EventListener1<Vector2> grappleFire = mock(EventListener1.class);
    EventListener0 grappleRelease = mock(EventListener0.class);
    player.getEvents().addListener("grappleFire", grappleFire);
    player.getEvents().addListener("grappleRelease", grappleRelease);
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    inventory.addItem(ItemType.ROPE_ARROW, 1);
    input.keyDown(Keys.E);
    input.keyDown(Keys.NUM_2);

    input.touchDown(40, 60, 0, Buttons.RIGHT);
    assertTrue(input.isRightMouseHeld());
    input.touchUp(40, 60, 0, Buttons.RIGHT);

    verify(grappleFire).handle(new Vector2(3f, 4f));
    verify(grappleRelease).handle();
    assertFalse(input.isRightMouseHeld());
    assertEquals(1, inventory.getItemCount(ItemType.ROPE_ARROW));
    verify(itemUsed).handle(ItemType.ROPE_ARROW);
    verifyNoInteractions(itemFailed);
    factory.verify(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any(), eq(1f)));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
  }

  @Test
  void shouldUsePotionWithEButNotRightClickAndPreserveItAtFullHealth() {
    CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
    stats.setHealth(75);
    inventory.addItem(ItemType.HEALTH_POTION, 2);
    input.touchDown(40, 60, 0, Buttons.RIGHT);
    assertEquals(75, stats.getHealth());
    assertEquals(2, inventory.getItemCount(ItemType.HEALTH_POTION));

    input.keyDown(Keys.E);
    assertEquals(100, stats.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
    input.keyUp(Keys.E);
    input.keyDown(Keys.E);

    assertEquals(100, stats.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
    verify(itemUsed).handle(ItemType.HEALTH_POTION);
    verify(itemFailed).handle(ItemType.HEALTH_POTION);
    verifyNoInteractions(entities, primaryAttack, animation, sound);
    factory.verifyNoInteractions();
  }
}
