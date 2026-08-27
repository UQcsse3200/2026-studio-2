package com.csse3200.game.components.item;

/** A grappling arrow. Deals no damage and is limited by a cooldown rather than ammunition. */
public class RopeArr extends Arrow {
  public RopeArr() {
    super(
        ItemType.RopeArrow,
        "Rope Arrow",
        "An arrow used for grappling.",
        1,
        0,
        15f,
        false, // consumeAmmo
        5f); // cooldown
  }
}
