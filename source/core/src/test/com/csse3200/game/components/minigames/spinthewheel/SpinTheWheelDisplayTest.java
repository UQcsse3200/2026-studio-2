package com.csse3200.game.components.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.extensions.GameExtension;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SpinTheWheelDisplayTest {
  private static final List<WheelItem> TWO_ITEMS =
      List.of(new WheelItem(ItemType.ARROW, 1), new WheelItem(ItemType.CONSUMABLE, 2));

  @Test
  void shouldIncludeASpriteForEveryItem() {
    List<String> paths = Arrays.asList(SpinTheWheelDisplay.texturesFor(TWO_ITEMS));

    for (WheelItem item : TWO_ITEMS) {
      assertTrue(paths.contains(item.type().getTexturePath()));
    }
  }

  @Test
  void shouldAddOnePathPerItem() {
    int shared = SpinTheWheelDisplay.texturesFor(List.of()).length;

    assertEquals(shared + TWO_ITEMS.size(), SpinTheWheelDisplay.texturesFor(TWO_ITEMS).length);
  }
}
