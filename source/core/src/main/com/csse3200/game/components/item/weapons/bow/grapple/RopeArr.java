package com.csse3200.game.components.item.weapons.bow.grapple;

import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;

/** A grappling arrow. Deals no damage and is limited by a cooldown rather than ammunition. */
public class RopeArr extends Arrow {
  public RopeArr() {
    this(1);
  }

  public RopeArr(int quantity) {
    super(ItemType.ROPE_ARROW, quantity);
  }
}
