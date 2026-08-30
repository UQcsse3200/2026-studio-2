package com.csse3200.game.components.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class HealthPotionTest {
  @Test
  void shouldUseAndConsumePotion() {
    HealthPotion potion = new HealthPotion(2);

    assertEquals(ItemType.CONSUMABLE, potion.getItemType());
    assertEquals(HealthPotion.HEAL_AMOUNT, potion.getTreatment());
    assertTrue(potion.useConsumable());
    assertEquals(1, potion.getQuantity());
    assertTrue(potion.useConsumable());
    assertEquals(0, potion.getQuantity());
    assertFalse(potion.useConsumable());
  }
}
