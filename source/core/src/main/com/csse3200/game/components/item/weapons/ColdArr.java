package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.ItemId;
import com.csse3200.game.components.item.ItemType;

public class ColdArr extends Arrow {
  private static final int BASE_DAMAGE = 8;
  private static final float SLOW_SPEED = 0.75f;
  private static final float SLOW_TIME = 5f;

  public ColdArr(int quantity) {
    super(
        ItemType.ColdArrow,
        ItemId.COLD_ARROW.getId(),
        "Cold Arrow",
        "An arrow that slows enemies,",
        quantity,
        BASE_DAMAGE,
        new StandardArr(1).getRange() + 1,
        true,
        0f);
  }

  public float getSlowSpeed() {
    return SLOW_SPEED;
  }

  public float getSlowTime() {
    return SLOW_TIME;
  }
}
