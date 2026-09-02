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
    inventory.addItem(ItemType.ARROW, 3);

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
    assertEquals(2, inventory.getItemCount(ItemType.ARROW));
    assertTrue(fired[0]);
  }

  @Test
  void shouldFireStandardArrowBeforeConsumingAmmo() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ARROW, 3);

    player
        .getEvents()
        .addListener(
            "primaryAttack",
            (Vector2 ignored) -> assertEquals(3, inventory.getItemCount(ItemType.ARROW)));

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(2, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldUseStandardArrowWhenAttackEventFires() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ARROW, 1);

    int[] fired = {0};
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> fired[0]++);

    player.getEvents().trigger("attack");
    assertEquals(1, fired[0]);
    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
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
    inventory.addItem(ItemType.CONSUMABLE, 2);
    inventory.selectNext();
    inventory.selectNext();

    assertEquals(ItemType.CONSUMABLE, inventory.getSelectedItem());
    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(40 + HealthPotion.HEAL_AMOUNT, combat.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.CONSUMABLE));
  }

  @Test
  void shouldClampPotionHealToMaxHealth() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    combat.setHealth(90);
    inventory.addItem(ItemType.CONSUMABLE, 1);

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(combat.getMaxHealth(), combat.getHealth());
    assertEquals(0, inventory.getItemCount(ItemType.CONSUMABLE));
  }

  @Test
  void shouldNotUsePotionAtFullHealth() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    inventory.addItem(ItemType.CONSUMABLE, 1);

    boolean[] failed = {false};
    player.getEvents().addListener("itemUseFailed", (ItemType type) -> failed[0] = true);

    assertTrue(combat.isHealthFull());
    assertFalse(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(combat.getMaxHealth(), combat.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.CONSUMABLE));
    assertTrue(failed[0]);
  }

  @Test
  void shouldNotConsumeRopeArrowAndApplyCooldown() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventory.selectNext();

    ItemUseComponent use = player.getComponent(ItemUseComponent.class);
    boolean[] grappled = {false};
    player.getEvents().addListener("grappleFire", (Vector2 ignored) -> grappled[0] = true);

    assertEquals(ItemType.RopeArrow, inventory.getSelectedItem());
    assertTrue(use.useSelectedItem());
    assertEquals(1, inventory.getItemCount(ItemType.RopeArrow));
    assertTrue(grappled[0]);
    assertFalse(use.isRopeReady());
    assertEquals(5f, use.getRopeCooldownRemaining(), 0.001f);
  }

  @Test
  void shouldStartRopeCooldownAfterGrappleFires() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventory.selectNext();

    ItemUseComponent use = player.getComponent(ItemUseComponent.class);
    player
        .getEvents()
        .addListener("grappleFire", (Vector2 ignored) -> assertTrue(use.isRopeReady()));

    assertTrue(use.useSelectedItem());
    assertFalse(use.isRopeReady());
  }

  @Test
  void shouldRejectRopeArrowDuringCooldownThenAllowAfter() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventory.selectNext();

    ItemUseComponent use = player.getComponent(ItemUseComponent.class);
    int[] uses = {0};
    player.getEvents().addListener("grappleFire", (Vector2 ignored) -> uses[0]++);

    assertTrue(use.useSelectedItem());

    ItemType[] failedType = {null};
    player.getEvents().addListener("itemUseFailed", (ItemType type) -> failedType[0] = type);

    when(time.getTime()).thenReturn(1000L);
    assertFalse(use.useSelectedItem());
    assertEquals(1, uses[0]);
    assertEquals(ItemType.RopeArrow, failedType[0]);
    assertEquals(4f, use.getRopeCooldownRemaining(), 0.001f);

    when(time.getTime()).thenReturn(5000L);
    assertTrue(use.useSelectedItem());
    assertEquals(2, uses[0]);
    assertEquals(5f, use.getRopeCooldownRemaining(), 0.001f);
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
