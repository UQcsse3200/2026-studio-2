package com.csse3200.game.components.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.extensions.GameExtension;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class WheelConfigTest {

  @Test
  void shouldHaveItems() {
    assertFalse(WheelConfig.ITEMS.isEmpty());
  }

  @Test
  void shouldLoadASpriteForEveryItem() {
    List<String> paths = Arrays.asList(WheelConfig.TEXTURES);

    for (WheelItem item : WheelConfig.ITEMS) {
      assertTrue(paths.contains(item.type().getTexturePath()));
    }
  }
}
