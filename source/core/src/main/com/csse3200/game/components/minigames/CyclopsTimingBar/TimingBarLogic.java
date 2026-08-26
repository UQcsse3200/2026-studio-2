package com.csse3200.game.components.minigames.CyclopsTimingBar;

/**
 * Mathematical state of the components in the timing bar. Keeps track of size and location of
 * scoring bar. Keeps track of the location of the sweeping marker.
 */
public class TimingBarLogic {

  public float barStart = 2f;
  public float barWidth;
  public float greenStart;
  public float greenEnd;

  public float markerX = barStart;
  private final float markerSpeed = 10f;
  private int direction = 1;

  public boolean isStopped = false;

  public TimingBarLogic(float barWidth, float scoringAreaWidth) {
    this.barWidth = barWidth;
    this.changeScoringAreaWidth(scoringAreaWidth);
  }

  /**
   * Change the width of the scoring area to the given width
   *
   * @param width - float value of the new width of the scoring area
   */
  public void changeScoringAreaWidth(float width) {
    float barCenter = barWidth / 2;

    this.greenStart = barCenter - (width / 2);
    this.greenEnd = barCenter + (width / 2);
  }

  /**
   * Update the marker to move along the bar.
   *
   * @param deltaTime
   */
  public void update(float deltaTime) {
    if (isStopped) {
      return;
    }

    markerX += markerSpeed * direction * deltaTime;

    float rightEdge = barStart + barWidth;
    if (markerX >= rightEdge) {
      markerX = rightEdge;
      direction = -1;
    } else if (markerX <= barStart) {
      markerX = barStart;
      direction = 1;
    }
  }

  /** Stops the sliding marker */
  public void stopMarker() {
    this.isStopped = true;
  }

  /** Starts the sliding marker */
  public void startMarker() {
    this.isStopped = false;
  }

  /**
   * Checks whether the marker is in the scoring green area.
   *
   * @return True when the marker is in the scoring area, otherwise False.
   */
  public boolean checkHit() {
    return (markerX >= greenStart && markerX <= greenEnd);
  }
}
