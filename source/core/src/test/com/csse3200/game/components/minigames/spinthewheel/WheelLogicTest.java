package com.csse3200.game.components.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WheelLogicTest {

  @Test
  void shouldRejectEmptyWheel() {
    assertThrows(IllegalArgumentException.class, () -> new WheelLogic(List.of()));
  }

  @Test
  void shouldReturnItemAtChosenIndex() {
    List<WheelItem> source =
        List.of(new WheelItem("A", 1), 
        new WheelItem("B", 2), 
        new WheelItem("C", 3));

    Random random = mock(Random.class);
    when(random.nextInt(3)).thenReturn(1);

    WheelLogic wheel = new WheelLogic(source, random);
    WheelItem result = wheel.spin();

    assertEquals("B", result.name());
    assertEquals(2, result.value());
  }

  @Test
  void shouldRejectAngleBeforeSpin() {
    WheelLogic wheel = new WheelLogic(List.of(new WheelItem("A", 1)));
    assertThrows(IllegalStateException.class, wheel::getWinningAngle);
  }

  @Test
  void shouldReachEveryItem() {
    List<WheelItem> source =
        List.of(new WheelItem("A", 1), new WheelItem("B", 2), new WheelItem("C", 3));

    WheelLogic wheel = new WheelLogic(source);

    Set<String> seen = new HashSet<>();
    for (int i = 0; i < 200; i++) {
      seen.add(wheel.spin().name());
    }

    assertEquals(3, seen.size());
  }
}
