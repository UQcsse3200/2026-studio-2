package com.csse3200.game.components.minigames.cyclopsMinigame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mathematical state of the components in the timing bar. Keeps track of size and location of
 * scoring bar. Keeps track of the location of the sweeping marker.
 */
public class TimingBarLogic {
  private static final Logger logger = LoggerFactory.getLogger(TimingBarLogic.class);

  static final float BAR_START = 0f;
  static final float BAR_WIDTH = 1f; // Acts as 100% etc.
  private float scoringAreaSize;

  static final float MARKER_SPEED = 1f;
  int direction = 1;

  private float markerX = BAR_START;
  float greenStart;
  float greenEnd;

  boolean isStopped = true;

  /**
   * Timing bar logic component to be the mathematical state of a timing bar and a sweeping marker
   * A scoring area is centred in the area to act as the scoring zone
   *
   * @param scoringAreaWidth percentage of timing bar to cover with a "scoring zone" (0-100%)
   */
  public TimingBarLogic(float scoringAreaWidth) {
    this.changeScoringAreaWidth(scoringAreaWidth);
  }

  /**
   * Get the current X location of the marker that needs to be scaled
   * to the visual width of the timing bar
   * @return float - between 0 and 1 of the current position of the marker.
   */
  public float getMarkerX() {
    return this.markerX;
  }

  /**
   * Get the current size of the scoring area
   * @return float - percentage of timing bar covered
   */
  public float getScoringAreaSize() {
    return this.scoringAreaSize;
  }

  /**
   * Change the width of the scoring area to the given width.
   * And centres it within the timing bar
   *
   * @param width - float value as a percentage, 0-100%
   */
  public void changeScoringAreaWidth(float width) {
    float barCenter = BAR_WIDTH / 2;

    float coveredArea = width / 100;
    scoringAreaSize = coveredArea;

    this.greenStart = barCenter - (coveredArea / 2);
    this.greenEnd = barCenter + (coveredArea / 2);

    logger.debug("scoring area size changed to {}", scoringAreaSize);
  }

  /**
   * Update the marker to move along the bar.
   *
   * @param deltaTime - time passed since last frame
   */
  public void update(float deltaTime) {
    if (isStopped) {
      return;
    }

    markerX += MARKER_SPEED * direction * deltaTime;

    if (markerX >= BAR_WIDTH) {
      markerX = BAR_WIDTH;
      direction = -1;
    } else if (markerX <= 0) {
      markerX = 0;
      direction = 1;
    }
  }

  /** Stops the sliding marker */
  public void stopMarker() {
    this.isStopped = true;
    logger.trace("sliding marker stopped");
  }

  /** Starts the sliding marker */
  public void startMarker() {
    this.isStopped = false;
    logger.trace("sliding marker started");
  }

  /** Resets the sliding marker to the start of the timing bar */
  public void resetMarker() {
    this.markerX = 0;
    logger.trace("sliding marker position set to 0");
  }

  /**
   * Checks whether the marker is in the scoring green area.
   *
   * @return boolean - True when the marker is in the scoring area, otherwise False.
   */
  public boolean checkHit() {
    return (markerX >= greenStart && markerX <= greenEnd);
  }
}
