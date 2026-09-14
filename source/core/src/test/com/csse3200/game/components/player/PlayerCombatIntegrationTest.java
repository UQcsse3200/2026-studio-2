package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.extensions.GameExtension;
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
  private MockedStatic<ProjectileFactory> factory;
  private EventListener1<ItemType> itemUsed;
  private EventListener1<ItemType> itemFailed;
  private EventListener1<Vector2> primaryAttack;
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

    KeyboardPlayerInputComponent input = mock(KeyboardPlayerInputComponent.class);
    when(input.getMouseAimDirection()).thenReturn(new Vector2(3f, 4f));
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
    animation = mock(EventListener1.class);
    player.getEvents().addListener("itemUsed", itemUsed);
    player.getEvents().addListener("itemUseFailed", itemFailed);
    player.getEvents().addListener("primaryAttack", primaryAttack);
    player.getEvents().addListener("attackAnimation", animation);

    factory = mockStatic(ProjectileFactory.class);
    factory
        .when(() -> ProjectileFactory.createPlayerArrow(eq(player), any(), any()))
        .thenReturn(projectile);
    factory
        .when(() -> ProjectileFactory.createFireArrow(eq(player), any(), any()))
        .thenReturn(projectile);
    factory
        .when(() -> ProjectileFactory.createColdArrow(eq(player), any(), any()))
        .thenReturn(projectile);
  }

  @AfterEach
  void tearDown() {
    if (factory != null) {
      factory.close();
    }
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
                eq(player), position.capture(), direction.capture()));
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

    factory.verify(() -> ProjectileFactory.createFireArrow(eq(player), any(), any()));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    assertEquals(0, inventory.getItemCount(ItemType.FIRE_ARROW));
    verify(itemUsed).handle(ItemType.FIRE_ARROW);
    verifyNoInteractions(itemFailed);
  }

  @Test
  void shouldSelectColdFactoryAndConsumeOneArrow() {
    inventory.addItem(ItemType.COLD_ARROW, 2);

    assertTrue(itemUse.useSelectedItem());

    factory.verify(() -> ProjectileFactory.createColdArrow(eq(player), any(), any()));
    factory.verifyNoMoreInteractions();
    verify(entities).register(projectile);
    assertEquals(1, inventory.getItemCount(ItemType.COLD_ARROW));
    verify(itemUsed).handle(ItemType.COLD_ARROW);
    verifyNoInteractions(itemFailed);
  }

  @Test
  void shouldNotFireWithEmptyInventory() {
    assertFalse(itemUse.useSelectedItem());

    factory.verifyNoInteractions();
    verifyNoInteractions(entities, sound, primaryAttack, itemUsed, animation);
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }
}
