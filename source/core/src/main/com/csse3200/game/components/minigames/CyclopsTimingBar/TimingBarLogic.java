package com.csse3200.game.components.minigames.CyclopsTimingBar;

/**
 * Mathematical state of the components in the timing bar.
 * Keeps track of size and location of scoring bar.
 * Keeps track of the location of the sweeping marker.
 */
public class TimingBarLogic {

    private float barStart = 2f;
    private float barWidth = 16f;
    private float greenStart = 10f;
    private float greenEnd = 13f;

    private float markerX = 2f;
    private float markerSpeed = 8f;
    private int direction = 1;

    public void changeScoringAreaSize(float width) {
        float barCenter = barWidth / 2;

        this.greenStart = barCenter - (width / 2);
        this.greenEnd = barCenter + (width / 2);
    }

    /**
     * Update the marker to move along the bar.
     * @param deltaTime
     */
    public void update(float deltaTime) {
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

    /**
     * Checks whether the marker is in the scoring green area.
     * @return True when the marker is in the scoring area, otherwise False.
     */
    public boolean checkHit() {
        return (markerX >= greenStart && markerX <= greenEnd);
    }

}
