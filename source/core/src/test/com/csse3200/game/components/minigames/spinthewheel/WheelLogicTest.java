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
          new WheelItem(ItemType.STANDARD_ARROW, 1),
          new WheelItem(ItemType.FIRE_ARROW, 2),
          new WheelItem(ItemType.ICE_ARROW, 3));

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

    assertEquals(ItemType.FIRE_ARROW, result.type());
    assertEquals(2, result.value());
  }

  @Test
  void shouldRejectAngleBeforeSpin() {
    WheelLogic wheel = new WheelLogic(List.of(new WheelItem(ItemType.STANDARD_ARROW, 1)));
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

  @Test
  void shouldGiveAReadOnlyCopyOfItsItems() {
    WheelLogic wheel = new WheelLogic(THREE_ITEMS);

    assertEquals(THREE_ITEMS, wheel.getItems());
    assertThrows(UnsupportedOperationException.class, () -> wheel.getItems().clear());
  }

  @Test
  void shouldCentreTheWinningAngleInItsSegment() {
    WheelLogic wheel = wheelLandingOn(1);
    wheel.spin();

    // Three segments of 120 degrees, so the second is centred on 180
    assertEquals(180f, wheel.getWinningAngle());
  }

  @Test
  void shouldLandTheWinnerUnderThePointer() {
    WheelLogic wheel = wheelLandingOn(1);
    wheel.spin();

    float target = wheel.getTargetRotation(30f, 90f, 2);

    // From 30, turning 240 brings the winner at 180 round to the pointer at 90, then 2 full turns
    assertEquals(990f, target);
    assertEquals(90f, (target + wheel.getWinningAngle()) % 360f);
  }

  @Test
  void shouldRejectNegativeTurns() {
    WheelLogic wheel = wheelLandingOn(1);
    wheel.spin();

    assertThrows(IllegalArgumentException.class, () -> wheel.getTargetRotation(0f, 90f, -1));
  }

  private static WheelLogic wheelLandingOn(int index) {
    Random random = mock(Random.class);
    when(random.nextInt(THREE_ITEMS.size())).thenReturn(index);
    return new WheelLogic(THREE_ITEMS, random);
  }
}
