package com.csse3200.game.components.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WheelLogicTest {

  @Test
  void shouldRejectEmptyWheel() {
    try {
      new WheelLogic(Map.of());
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  void shouldReturnItemAtChosenIndex() {
    Map<String, Integer> source = new LinkedHashMap<>();
    source.put("A", 1);
    source.put("B", 2);
    source.put("C", 3);

    Random random = mock(Random.class);
    when(random.nextInt(3)).thenReturn(1);

    WheelLogic wheel = new WheelLogic(source, random);
    Map.Entry<String, Integer> result = wheel.spin();

    assertEquals("B", result.getKey());
    assertEquals(2, result.getValue());
  }

  @Test
  void shouldRejectAngleBeforeSpin() {
    Map<String, Integer> source = new LinkedHashMap<>();
    source.put("A", 1);

    WheelLogic wheel = new WheelLogic(source);

    try {
      wheel.getWinningAngle();
    } catch (IllegalStateException e) {
    }
  }

  @Test
  void shouldReachEveryItem() {
    Map<String, Integer> source = new LinkedHashMap<>();
    source.put("A", 1);
    source.put("B", 2);
    source.put("C", 3);

    WheelLogic wheel = new WheelLogic(source);

    Set<String> seen = new HashSet<>();
    for (int i = 0; i < 200; i++) {
      seen.add(wheel.spin().getKey());
    }

    assertEquals(3, seen.size());
  }
}
