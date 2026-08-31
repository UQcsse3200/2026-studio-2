package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.ItemId;
import com.csse3200.game.components.item.ItemType;

public class FireArr extends Arrow {
  private static final int BASE_DAMAGE = 5;
  private static final float BURN_DAMAGE_PER_SECOND = 3f;
  private static final float BURN_TIME = 5f;

  public FireArr(int quantity) {
    super(
        ItemType.FireArrow,
        ItemId.FIRE_ARROW.getId(),
        "Fire Arrow",
        "An arrow that burns enemies over time.",
        quantity,
        BASE_DAMAGE,
        new StandardArr(1).getRange() + 1,
        true,
        0f);
  }

  public float getBurnDamagePerSecond() {
    return BURN_DAMAGE_PER_SECOND;
  }

  public float getBurnTime() {
    return BURN_TIME;
  }
}
