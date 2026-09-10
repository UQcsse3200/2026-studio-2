package com.csse3200.game.components.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.components.item.ItemType;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WheelLogicTest {
  private static final List<WheelItem> THREE_ITEMS =
      List.of(
          new WheelItem(ItemType.ARROW, 1),
          new WheelItem(ItemType.FireArrow, 2),
          new WheelItem(ItemType.ColdArrow, 3));

  @Test
  void shouldRejectEmptyWheel() {
    assertThrows(IllegalArgumentException.class, () -> new WheelLogic(List.of()));
  }

  @Test
  void shouldReturnItemAtChosenIndex() {
    Random random = mock(Random.class);
    when(random.nextInt(3)).thenReturn(1);

    WheelLogic wheel = new WheelLogic(THREE_ITEMS, random);
    WheelItem result = wheel.spin();

    assertEquals(ItemType.FireArrow, result.type());
    assertEquals(2, result.value());
  }

  @Test
  void shouldRejectAngleBeforeSpin() {
    WheelLogic wheel = new WheelLogic(List.of(new WheelItem(ItemType.ARROW, 1)));
    assertThrows(IllegalStateException.class, wheel::getWinningAngle);
  }

  @Test
  void shouldReachEveryItem() {
    WheelLogic wheel = new WheelLogic(THREE_ITEMS);

    Set<ItemType> seen = new HashSet<>();
    for (int i = 0; i < 200; i++) {
      seen.add(wheel.spin().type());
    }

    assertEquals(3, seen.size());
  }
}
