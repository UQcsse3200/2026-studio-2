package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.ItemId;
import com.csse3200.game.components.item.ItemType;

/** A grappling arrow. Deals no damage and is limited by a cooldown rather than ammunition. */
public class RopeArr extends Arrow {
  public RopeArr() {
    this(1);
  }

  public RopeArr(int quantity) {
    super(
        ItemType.RopeArrow,
        ItemId.ROPE_ARROW.getId(),
        "Rope Arrow",
        "An arrow used for grappling.",
        quantity,
        0,
        15f,
        false, // consumeAmmo
        5f); // cooldown
  }
}
