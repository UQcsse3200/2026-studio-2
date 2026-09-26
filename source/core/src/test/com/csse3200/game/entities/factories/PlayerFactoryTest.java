package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.itemdictionary.ItemDictionaryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerFactoryTest {
  @Test
  void giveStartingLoadoutAddsRopeArrowSwordAndSpear() {
    Entity player =
        new Entity()
            .addComponent(new InventoryComponent(50))
            .addComponent(new ItemDictionaryComponent());
    player.create();

    PlayerFactory.giveStartingLoadout(player);

    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    assertEquals(1, inventory.getItemCount(ItemType.ROPE_ARROW));

    ItemDictionaryComponent dictionary = player.getComponent(ItemDictionaryComponent.class);
    assertTrue(dictionary.isDiscovered(ItemType.ROPE_ARROW));
  }

  @Test
  void giveStartingLoadoutIgnoresNullPlayer() {
    assertDoesNotThrow(() -> PlayerFactory.giveStartingLoadout(null));
  }
}
