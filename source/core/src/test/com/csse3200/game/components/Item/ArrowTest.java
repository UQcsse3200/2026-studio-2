package com.csse3200.game.components.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowTest {
  @Test
  void shouldGiveEachArrowItsOwnItemType() {
    assertEquals(ItemType.ARROW, new StandardArr(1).getItemType());
    assertEquals(ItemType.RopeArrow, new RopeArr().getItemType());
  }

  @Test
  void shouldNotStackDifferentArrowTypesTogether() {
    InventoryComponent inventory = new InventoryComponent(0);
    StandardArr standard = new StandardArr(3);
    RopeArr rope = new RopeArr();

    assertTrue(inventory.addItem(standard.getItemType(), standard.getQuantity()));
    assertTrue(inventory.addItem(rope.getItemType(), rope.getQuantity()));

    assertEquals(3, inventory.getItemCount(ItemType.ARROW));
    assertEquals(1, inventory.getItemCount(ItemType.RopeArrow));
  }

  @Test
  void shouldSelectBetweenBothArrowTypes() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(new StandardArr(3).getItemType(), 3);
    inventory.addItem(new RopeArr().getItemType(), 1);

    assertEquals(ItemType.ARROW, inventory.getSelectedItem());
    assertEquals(ItemType.RopeArrow, inventory.selectNext());
    assertEquals(ItemType.ARROW, inventory.selectNext());
  }

  @Test
  void shouldKeepArrowSpecificAttributes() {
    RopeArr rope = new RopeArr();
    StandardArr standard = new StandardArr(1);

    assertEquals(0, rope.getDamage());
    assertFalse(rope.isConsumeAmmo());
    assertEquals(5f, rope.getCooldown(), 0.001f);

    assertEquals(10, standard.getDamage());
    assertTrue(standard.isConsumeAmmo());
    assertEquals(0f, standard.getCooldown(), 0.001f);
  }
}
