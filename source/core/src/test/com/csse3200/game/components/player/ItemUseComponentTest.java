package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemUseComponentTest {
  private GameTime time;

  @BeforeEach
  void beforeEach() {
    time = mock(GameTime.class);
    when(time.getTime()).thenReturn(0L);
    ServiceLocator.registerTimeSource(time);
  }

  @Test
  void shouldConsumeStandardArrowAndFireEvent() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 3);

    boolean[] fired = {false};
    player
        .getEvents()
        .addListener(
            "primaryAttack",
            (Vector2 direction) -> {
              fired[0] = true;
              assertFalse(direction.isZero());
            });

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertTrue(fired[0]);
  }

  @Test
  void shouldFireStandardArrowBeforeConsumingAmmo() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 3);

    player
        .getEvents()
        .addListener(
            "primaryAttack",
            (Vector2 ignored) -> assertEquals(3, inventory.getItemCount(ItemType.STANDARD_ARROW)));

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldUseStandardArrowWhenAttackEventFires() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 1);

    int[] fired = {0};
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> fired[0]++);

    player.getEvents().trigger("attack");
    assertEquals(1, fired[0]);
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldRejectAttackWhenNoArrowSelected() {
    Entity player = createPlayer();
    AtomicReference<Vector2> attackDirection = new AtomicReference<>();
    player.getEvents().addListener("primaryAttack", attackDirection::set);

    assertFalse(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(null, attackDirection.get());
  }

  @Test
  void shouldHealPlayerWhenUsingConsumable() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    combat.setHealth(40);
    inventory.addItem(ItemType.HEALTH_POTION, 2);
    inventory.selectNext();
    inventory.selectNext();

    assertEquals(ItemType.HEALTH_POTION, inventory.getSelectedItem());
    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(40 + HealthPotion.HEAL_AMOUNT, combat.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
  }

  @Test
  void shouldClampPotionHealToMaxHealth() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    combat.setHealth(90);
    inventory.addItem(ItemType.HEALTH_POTION, 1);

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(CombatStatsComponent.MAX_HEALTH, combat.getHealth());
    assertEquals(0, inventory.getItemCount(ItemType.HEALTH_POTION));
  }

  @Test
  void shouldNotUsePotionAtFullHealth() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    inventory.addItem(ItemType.HEALTH_POTION, 1);

    boolean[] failed = {false};
    player.getEvents().addListener("itemUseFailed", (ItemType type) -> failed[0] = true);

    assertTrue(combat.isHealthFull());
    assertFalse(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(CombatStatsComponent.MAX_HEALTH, combat.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
    assertTrue(failed[0]);
  }

  @Test
  void shouldFireGrappleAndNotConsumeRopeArrow() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ROPE_ARROW, 1);
    inventory.selectNext();

    ItemUseComponent use = player.getComponent(ItemUseComponent.class);
    AtomicReference<Vector2> grappleDir = new AtomicReference<>();
    player.getEvents().addListener("grappleFire", (Vector2 dir) -> grappleDir.set(dir));

    assertEquals(ItemType.ROPE_ARROW, inventory.getSelectedItem());
    assertTrue(use.useSelectedItem());
    assertEquals(1, inventory.getItemCount(ItemType.ROPE_ARROW));
    assertFalse(grappleDir.get().isZero());
  }

  @Test
  void shouldTriggerItemUsedForRopeArrow() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ROPE_ARROW, 1);
    inventory.selectNext();

    ItemType[] used = {null};
    player.getEvents().addListener("itemUsed", (ItemType type) -> used[0] = type);

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(ItemType.ROPE_ARROW, used[0]);
  }

  @Test
  void shouldFireSelectedArrowOnShootEvent() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 2);

    int[] fired = {0};
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> fired[0]++);

    player.getEvents().trigger("shoot", new Vector2(1f, 0f));

    assertEquals(1, fired[0]);
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldIgnoreShootEventWhenConsumableSelected() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    combat.setHealth(40);
    inventory.addItem(ItemType.HEALTH_POTION, 1);

    assertEquals(ItemType.HEALTH_POTION, inventory.getSelectedItem());

    player.getEvents().trigger("shoot", new Vector2(1f, 0f));

    assertEquals(40, combat.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
  }

  @Test
  void shouldReleaseGrappleOnStopShootWhenRopeArrowSelected() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ROPE_ARROW, 1);
    inventory.selectNext();

    assertEquals(ItemType.ROPE_ARROW, inventory.getSelectedItem());

    int[] released = {0};
    player.getEvents().addListener("grappleRelease", () -> released[0]++);

    player.getEvents().trigger("stopShoot");

    assertEquals(1, released[0]);
  }

  @Test
  void shouldNotReleaseGrappleOnStopShootForOtherArrows() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 1);

    int[] released = {0};
    player.getEvents().addListener("grappleRelease", () -> released[0]++);

    player.getEvents().trigger("stopShoot");

    assertEquals(0, released[0]);
  }

  @Test
  void shouldFireFireArrowThroughBowAndConsumeAmmo() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.FIRE_ARROW, 2);

    AtomicReference<ArrowType> bowType = new AtomicReference<>();
    int[] shots = {0};
    player.getEvents().addListener("setArrowType", (ArrowType t) -> bowType.set(t));
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> shots[0]++);

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(ArrowType.FIRE, bowType.get());
    assertEquals(1, shots[0]);
    assertEquals(1, inventory.getItemCount(ItemType.FIRE_ARROW));
  }

  @Test
  void shouldFireColdArrowThroughBowAndConsumeAmmo() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.COLD_ARROW, 2);

    AtomicReference<ArrowType> bowType = new AtomicReference<>();
    int[] shots = {0};
    player.getEvents().addListener("setArrowType", (ArrowType t) -> bowType.set(t));
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> shots[0]++);

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(ArrowType.COLD, bowType.get());
    assertEquals(1, shots[0]);
    assertEquals(1, inventory.getItemCount(ItemType.COLD_ARROW));
  }

  private Entity createPlayer() {
    Entity player =
        new Entity()
            .addComponent(new InventoryComponent(0))
            .addComponent(new CombatStatsComponent(100, 10))
            .addComponent(new ItemUseComponent());
    player.create();
    return player;
  }
}
