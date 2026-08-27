package com.csse3200.game.components.minigames.CyclopsTimingBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mathematical state of the components in the timing bar. Keeps track of size and location of
 * scoring bar. Keeps track of the location of the sweeping marker.
 */
public class TimingBarLogic {
  private static final Logger logger = LoggerFactory.getLogger(TimingBarLogic.class);

  public float barStart = 0f;
  public float barWidth = 1f; // Acts as 100% etc.
  public float scoringAreaSize;

  private final float markerSpeed = 1f;
  private int direction = 1;

  public float markerX = barStart;
  public float greenStart;
  public float greenEnd;

  public boolean isStopped = false;

  /**
   * Scoring area width is designed to be a float as a percentage (e.g 0-100)
   *
   * @param scoringAreaWidth
   */
  public TimingBarLogic(float scoringAreaWidth) {
    this.changeScoringAreaWidth(scoringAreaWidth);
  }

  /**
   * Change the width of the scoring area to the given width
   *
   * @param width - float value as a percentage, 0-100%
   */
  public void changeScoringAreaWidth(float width) {
    float barCenter = barWidth / 2;

    float covered_area = width / 100;
    scoringAreaSize = covered_area;

    this.greenStart = barCenter - (covered_area / 2);
    this.greenEnd = barCenter + (covered_area / 2);
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

    // logger.info("Marker: {}", markerX);

    if (markerX >= barWidth) {
      markerX = barWidth;
      direction = -1;
    } else if (markerX <= 0) {
      markerX = 0;
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
