package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.ItemId;
import com.csse3200.game.components.item.ItemType;

public class StandardArr extends Arrow {
  public StandardArr(int quantity) {
    super(
        ItemType.ARROW,
        ItemId.STANDARD_ARROW.getId(),
        "Standard Arrow",
        "A basic arrow used as ammunition.",
        quantity,
        10,
        15f,
        true, // consumeAmmo
        0f); // cooldown
  }

  public boolean useArr() {
    return removeQuantity(1);
  }
}
