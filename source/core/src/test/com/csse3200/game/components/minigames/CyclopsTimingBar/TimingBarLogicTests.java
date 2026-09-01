package com.csse3200.game.components.minigames.CyclopsTimingBar;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TimingBarLogicTests {

  @Test
  void scoringAreaWidthChanges() {
    float defaultWidth = 0f;
    TimingBarLogic logic = new TimingBarLogic(defaultWidth);
    logic.changeScoringAreaWidth(100f);

    assertEquals(1.0f, logic.scoringAreaSize, 0.001f);
    assertEquals(0.0f, logic.greenStart, 0.001f);
    assertEquals(1.0f, logic.greenEnd, 0.001f);
  }

  @Test
  void markerStopsAndStartsOnCall() {
    TimingBarLogic logic = new TimingBarLogic(0f);

    logic.stopMarker();
    assertTrue(logic.isStopped);

    logic.startMarker();
    assertFalse(logic.isStopped);
  }

  @Test
  void markerDoesNotMoveWhenStopped() {
    TimingBarLogic logic = new TimingBarLogic(0f);
    logic.stopMarker();

    float deltaTime = 0.5f; // Smaller number to avoid bounce
    float previousLocation = logic.markerX;

    logic.update(deltaTime);
    assertEquals(previousLocation, logic.markerX, 0.001f);
  }

  @Test
  void markerMovesWhenNotStopped() {
    TimingBarLogic logic = new TimingBarLogic(0f);
    logic.startMarker();

    float deltaTime = 0.5f; // Smaller number to avoid bounce
    float previousLocation = logic.markerX;
    float expectedNextLocation =
        previousLocation + (logic.markerSpeed * logic.direction * deltaTime);

    logic.update(deltaTime);
    assertEquals(expectedNextLocation, logic.markerX, 0.001f);
  }

  @Test
  void markerBouncesBackWhenHitsEdge() {
    TimingBarLogic logic = new TimingBarLogic(0f);
    logic.startMarker();

    float deltaTime = 5f;
    logic.update(deltaTime);
    assertEquals(logic.barWidth, logic.markerX, 0.001f);
    assertEquals(-1, logic.direction);

    logic.update(deltaTime);
    assertEquals(0, logic.markerX, 0.001f);
    assertEquals(1, logic.direction);
  }

  @Test
  void checkHitReturnsTrueWhenMarkerInScoringArea() {
    TimingBarLogic logic = new TimingBarLogic(100f);
    assertTrue(logic.checkHit());
  }

  @Test
  void checkHitReturnsFalseWhenMarkerNotInScoringArea() {
    TimingBarLogic logic = new TimingBarLogic(0f);
    assertFalse(logic.checkHit());
  }
}
