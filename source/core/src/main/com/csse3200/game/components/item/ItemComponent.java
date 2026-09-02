package com.csse3200.game.components.item;

import com.csse3200.game.components.Component;

/**
 * Marks an entity as a collectable item lying in the world, and stores the item that entity grants
 * when it is collected.
 */
public class ItemComponent extends Component {
  private final Item item;

  /**
   * @param item the item granted when this entity is collected
   */
  public ItemComponent(Item item) {
    this.item = item;
  }

  /**
   * @return the item this entity represents
   */
  public Item getItem() {
    return item;
  }
}
