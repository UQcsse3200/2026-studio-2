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
import java.util.concurrent.atomic.AtomicInteger;
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
  void shouldFireStandardArrowBeforeConsumingAmmo() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 3);

    AtomicReference<ArrowType> bowType = new AtomicReference<>();
    player.getEvents().addListener("setArrowType", bowType::set);
    player
        .getEvents()
        .addListener(
            "primaryAttack",
            (Vector2 ignored) -> assertEquals(3, inventory.getItemCount(ItemType.STANDARD_ARROW)));

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(ArrowType.STANDARD, bowType.get());
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldUseStandardArrowWhenAttackEventFires() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 1);

    AtomicInteger fired = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> fired.incrementAndGet());

    player.getEvents().trigger("attack");

    assertEquals(1, fired.get());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldRejectAttackWhenNoItemSelected() {
    Entity player = createPlayer();
    AtomicReference<Vector2> attackDirection = new AtomicReference<>();
    player.getEvents().addListener("primaryAttack", attackDirection::set);

    assertFalse(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(null, attackDirection.get());
  }

  @Test
  void shouldHealPlayerWhenUsingHealthPotion() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    combat.setHealth(40);
    inventory.addItem(ItemType.HEALTH_POTION, 2);

    assertEquals(ItemType.HEALTH_POTION, inventory.getSelectedItem());
    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(40 + HealthPotion.HEAL_AMOUNT, combat.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
  }

  @Test
  void shouldClampHealthPotionHealToMaxHealth() {
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
  void shouldNotUseHealthPotionAtFullHealth() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    CombatStatsComponent combat = player.getComponent(CombatStatsComponent.class);
    inventory.addItem(ItemType.HEALTH_POTION, 1);

    AtomicReference<ItemType> failed = new AtomicReference<>();
    player.getEvents().addListener("itemUseFailed", failed::set);

    assertTrue(combat.isHealthFull());
    assertFalse(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(CombatStatsComponent.MAX_HEALTH, combat.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
    assertEquals(ItemType.HEALTH_POTION, failed.get());
  }

  @Test
  void shouldFireGrappleAndNotConsumeRopeArrow() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ROPE_ARROW, 1);

    AtomicReference<Vector2> grappleDir = new AtomicReference<>();
    player.getEvents().addListener("grappleFire", grappleDir::set);

    assertEquals(ItemType.ROPE_ARROW, inventory.getSelectedItem());
    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(1, inventory.getItemCount(ItemType.ROPE_ARROW));
    assertFalse(grappleDir.get().isZero());
  }

  @Test
  void shouldReleaseGrappleOnStopShootWhenRopeArrowSelected() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ROPE_ARROW, 1);

    AtomicInteger released = new AtomicInteger();
    player.getEvents().addListener("grappleRelease", released::incrementAndGet);

    player.getEvents().trigger("stopShoot");

    assertEquals(1, released.get());
  }

  @Test
  void shouldNotReleaseGrappleOnStopShootForOtherArrows() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.STANDARD_ARROW, 1);

    AtomicInteger released = new AtomicInteger();
    player.getEvents().addListener("grappleRelease", released::incrementAndGet);

    player.getEvents().trigger("stopShoot");

    assertEquals(0, released.get());
  }

  @Test
  void shouldFireFireArrowThroughBowAndConsumeAmmo() {
    assertArrowUsesBowType(ItemType.FIRE_ARROW, ArrowType.FIRE);
  }

  @Test
  void shouldFireIceArrowThroughBowAndConsumeAmmo() {
    assertArrowUsesBowType(ItemType.ICE_ARROW, ArrowType.ICE);
  }

  @Test
  void shouldUseSwordThroughMeleeAttackEvent() {
    assertMeleeItemUsesDamageAndRange(ItemType.Sword);
  }

  @Test
  void shouldUseSpearThroughMeleeAttackEvent() {
    assertMeleeItemUsesDamageAndRange(ItemType.Spear);
  }

  @Test
  void shouldTriggerSpeedPotionBuffAndConsumePotion() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.SpeedPotion, 1);

    AtomicReference<Float> boost = new AtomicReference<>();
    AtomicReference<Float> duration = new AtomicReference<>();
    player
        .getEvents()
        .addListener(
            "speedPotionUsed",
            (Float value, Float seconds) -> {
              boost.set(value);
              duration.set(seconds);
            });

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());

    assertEquals(ItemType.SpeedPotion.getSpeedBoost(), boost.get(), 0.001f);
    assertEquals(ItemType.SpeedPotion.getDuration(), duration.get(), 0.001f);
    assertEquals(0, inventory.getItemCount(ItemType.SpeedPotion));
  }

  @Test
  void shouldNotUseSecondPoisonPotionWhileBuffIsActive() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    ItemUseComponent itemUse = player.getComponent(ItemUseComponent.class);

    inventory.addItem(ItemType.PoisonPotion, 2);

    assertTrue(itemUse.useSelectedItem());
    assertEquals(1, inventory.getItemCount(ItemType.PoisonPotion));

    assertFalse(itemUse.useSelectedItem());
    assertEquals(1, inventory.getItemCount(ItemType.PoisonPotion));
  }

  @Test
  void shouldAllowPoisonPotionAfterBuffExpires() {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    ItemUseComponent itemUse = player.getComponent(ItemUseComponent.class);

    inventory.addItem(ItemType.PoisonPotion, 2);

    assertTrue(itemUse.useSelectedItem());
    assertEquals(1, inventory.getItemCount(ItemType.PoisonPotion));

    when(time.getTime()).thenReturn(5000L);

    assertTrue(itemUse.useSelectedItem());
    assertEquals(0, inventory.getItemCount(ItemType.PoisonPotion));
  }

  private void assertArrowUsesBowType(ItemType itemType, ArrowType expectedArrowType) {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(itemType, 2);

    AtomicReference<ArrowType> bowType = new AtomicReference<>();
    AtomicInteger shots = new AtomicInteger();
    player.getEvents().addListener("setArrowType", bowType::set);
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> shots.incrementAndGet());

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertEquals(expectedArrowType, bowType.get());
    assertEquals(1, shots.get());
    assertEquals(1, inventory.getItemCount(itemType));
  }

  private void assertMeleeItemUsesDamageAndRange(ItemType itemType) {
    Entity player = createPlayer();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(itemType, 1);

    AtomicReference<Vector2> direction = new AtomicReference<>();
    AtomicInteger damage = new AtomicInteger();
    AtomicReference<Float> range = new AtomicReference<>();
    player
        .getEvents()
        .addListener(
            "meleeAttack",
            (Vector2 aim, Integer itemDamage, Float itemRange) -> {
              direction.set(aim);
              damage.set(itemDamage);
              range.set(itemRange);
            });

    assertTrue(player.getComponent(ItemUseComponent.class).useSelectedItem());
    assertFalse(direction.get().isZero());
    assertEquals(itemType.getDamage(), damage.get());
    assertEquals(itemType.getRange(), range.get(), 0.001f);
  }

  private Entity createPlayer() {
    Entity player =
        new Entity()
            .addComponent(new InventoryComponent(0))
            .addComponent(new CombatStatsComponent(100, CombatStatsComponent.MAX_HEALTH, 10))
            .addComponent(new ItemUseComponent())
            .addComponent(new PoisonBuff());
    player.create();
    return player;
  }
}
