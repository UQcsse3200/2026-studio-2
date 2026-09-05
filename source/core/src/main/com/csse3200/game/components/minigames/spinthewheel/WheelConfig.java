package com.csse3200.game.components.minigames.spinthewheel;

import com.csse3200.game.components.item.ItemType;
import java.util.List;

public final class WheelConfig {
  /** The items on the wheel*/
  public static final List<WheelItem> ITEMS =
      List.of(
          new WheelItem(ItemType.ARROW, 10),
          new WheelItem(ItemType.FireArrow, 5),
          new WheelItem(ItemType.ColdArrow, 5),
          new WheelItem(ItemType.CONSUMABLE, 1));

  public static final String[] TEXTURES = SpinTheWheelDisplay.texturesFor(ITEMS);

  private WheelConfig() {}
}

