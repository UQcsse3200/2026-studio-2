package com.csse3200.game.components.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemComponentTest {
  @Test
  void shouldReturnTheItemItWasGiven() {
    Item item = new Arrow(ItemType.ROPE_ARROW, 1);
    ItemComponent component = new ItemComponent(item);

    assertSame(item, component.getItem());
  }

  @Test
  void shouldBeRetrievableFromEntity() {
    Item item = new Arrow(ItemType.STANDARD_ARROW, 4);
    Entity entity = new Entity().addComponent(new ItemComponent(item));
    entity.create();

    assertSame(item, entity.getComponent(ItemComponent.class).getItem());
  }

  @Test
  void shouldKeepItemAttributes() {
    Entity entity = new Entity().addComponent(new ItemComponent(new Arrow(ItemType.STANDARD_ARROW, 4)));
    entity.create();

    Item stored = entity.getComponent(ItemComponent.class).getItem();

    assertEquals(ItemType.STANDARD_ARROW, stored.getItemType());
    assertEquals("Standard Arrow", stored.getItemName());
    assertEquals(4, stored.getQuantity());
  }
}
