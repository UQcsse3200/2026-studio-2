package com.csse3200.game.components.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowTest {
  @Test
  void shouldGiveEachArrowItsOwnItemType() {
    assertEquals(ItemType.STANDARD_ARROW, new Arrow(ItemType.STANDARD_ARROW, 1).getItemType());
    assertEquals(ItemType.ROPE_ARROW, new Arrow(ItemType.ROPE_ARROW, 1).getItemType());
  }

  @Test
  void shouldNotStackDifferentArrowTypesTogether() {
    InventoryComponent inventory = new InventoryComponent(0);
    Arrow standard = new Arrow(ItemType.STANDARD_ARROW, 3);
    Arrow rope = new Arrow(ItemType.ROPE_ARROW, 1);

    assertTrue(inventory.addItem(standard.getItemType(), standard.getQuantity()));
    assertTrue(inventory.addItem(rope.getItemType(), rope.getQuantity()));

    assertEquals(3, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(1, inventory.getItemCount(ItemType.ROPE_ARROW));
  }

  @Test
  void shouldSelectBetweenBothArrowTypes() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.STANDARD_ARROW, 3);
    inventory.addItem(ItemType.ROPE_ARROW, 1);

    assertEquals(ItemType.STANDARD_ARROW, inventory.getSelectedItem());
    assertEquals(ItemType.ROPE_ARROW, inventory.selectNext());
    assertEquals(ItemType.STANDARD_ARROW, inventory.selectNext());
  }

  @Test
  void shouldKeepArrowSpecificAttributes() {
    Arrow rope = new Arrow(ItemType.ROPE_ARROW, 1);
    Arrow standard = new Arrow(ItemType.STANDARD_ARROW, 1);

    assertEquals(0, rope.getDamage());
    assertFalse(rope.isConsumeAmmo());
    assertEquals(5f, rope.getCooldown(), 0.001f);

    assertEquals(10, standard.getDamage());
    assertTrue(standard.isConsumeAmmo());
    assertEquals(0f, standard.getCooldown(), 0.001f);
  }

  @Test
  void shouldKeepFireArrowSpecificAttributes() {
    Arrow fire = new Arrow(ItemType.FIRE_ARROW, 1);

    assertEquals(ItemType.FIRE_ARROW, fire.getItemType());
    assertEquals(ItemType.FIRE_ARROW.getId(), fire.getItemId());
    assertEquals("Fire Arrow", fire.getItemName());
    assertEquals(1, fire.getQuantity());
    assertEquals(5, fire.getDamage());
    assertEquals(ItemType.STANDARD_ARROW.getRange() + 1, fire.getRange(), 0.001f);
    assertTrue(fire.isConsumeAmmo());
    assertEquals(0f, fire.getCooldown(), 0.001f);
    assertEquals(3f, fire.getItemType().getBurnDamagePerSecond(), 0.001f);
    assertEquals(5f, fire.getItemType().getBurnTime(), 0.001f);
  }

  @Test
  void shouldKeepColdArrowSpecificAttributes() {
    Arrow cold = new Arrow(ItemType.COLD_ARROW, 1);

    assertEquals(ItemType.COLD_ARROW, cold.getItemType());
    assertEquals(ItemType.COLD_ARROW.getId(), cold.getItemId());
    assertEquals("Cold Arrow", cold.getItemName());
    assertEquals(1, cold.getQuantity());
    assertEquals(8, cold.getDamage());
    assertEquals(ItemType.STANDARD_ARROW.getRange() + 1, cold.getRange(), 0.001f);
    assertTrue(cold.isConsumeAmmo());
    assertEquals(0f, cold.getCooldown(), 0.001f);
    assertEquals(0.75f, cold.getItemType().getSlowSpeed(), 0.001f);
    assertEquals(5f, cold.getItemType().getSlowTime(), 0.001f);
  }
}
