package com.csse3200.game.components.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.weapons.ColdArr;
import com.csse3200.game.components.item.weapons.FireArr;
import com.csse3200.game.components.item.weapons.RopeArr;
import com.csse3200.game.components.item.weapons.StandardArr;
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

  @Test
  void shouldKeepFireArrowSpecificAttributes() {
    FireArr fire = new FireArr(1);

    assertEquals(ItemType.FireArrow, fire.getItemType());
    assertEquals(ItemId.FIRE_ARROW.getId(), fire.getItemId());
    assertEquals("Fire Arrow", fire.getItemName());
    assertEquals(1, fire.getQuantity());
    assertEquals(5, fire.getDamage());
    assertEquals(new StandardArr(1).getRange() + 1, fire.getRange(), 0.001f);
    assertTrue(fire.isConsumeAmmo());
    assertEquals(0f, fire.getCooldown(), 0.001f);
    assertEquals(3f, fire.getBurnDamagePerSecond(), 0.001f);
    assertEquals(5f, fire.getBurnTime(), 0.001f);
  }

  @Test
  void shouldKeepColdrrowSpecificAttributes() {
    ColdArr cold = new ColdArr(1);

    assertEquals(ItemType.ColdArrow, cold.getItemType());
    assertEquals(ItemId.COLD_ARROW.getId(), cold.getItemId());
    assertEquals("Cold Arrow", cold.getItemName());
    assertEquals(1, cold.getQuantity());
    assertEquals(8, cold.getDamage());
    assertEquals(new StandardArr(1).getRange() + 1, cold.getRange(), 0.001f);
    assertTrue(cold.isConsumeAmmo());
    assertEquals(0f, cold.getCooldown(), 0.001f);
    assertEquals(0.75f, cold.getSlowSpeed(), 0.001f);
    assertEquals(5f, cold.getSlowTime(), 0.001f);
  }
}
