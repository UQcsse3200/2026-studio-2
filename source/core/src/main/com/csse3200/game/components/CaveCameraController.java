package com.csse3200.game.components;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

/**
 * Lookahead camera controller used by {@link CameraComponent}. Eases the camera ahead of the
 * target in its facing direction, and (once room bounds are known) clamps it so it never shows
 * outside the playable area.
 */
public class CaveCameraController {

  private final OrthographicCamera camera;
  private float viewportWidth;
  private float viewportHeight;

  // How far the camera should lead the player in the facing direction
  private final float lookaheadDistance = 4f; // in world units, tune to your scale

  // How quickly the camera eases toward its target offset (higher = snappier)
  private final float smoothSpeed = 3f;

  // Tracks current lookahead offset for smoothing
  private float currentOffsetX = 0f;

  // Level/room bounds so the camera doesn't show outside the playable area
  private boolean roomBoundsSet = false;
  private float roomMinX;
  private float roomMinY;
  private float roomMaxX;
  private float roomMaxY;

  private float minCameraX;
  private float maxCameraX;
  private float minCameraY;
  private float maxCameraY;

  public CaveCameraController(OrthographicCamera camera, float viewportWidth, float viewportHeight) {
    this.camera = camera;
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
  }

  public void setRoomBounds(float roomMinX, float roomMinY, float roomMaxX, float roomMaxY) {
    this.roomMinX = roomMinX;
    this.roomMinY = roomMinY;
    this.roomMaxX = roomMaxX;
    this.roomMaxY = roomMaxY;
    this.roomBoundsSet = true;
    recomputeCameraBounds();
  }

  /**
   * Updates the viewport size this controller clamps against, e.g. after a window resize.
   */
  public void setViewportSize(float viewportWidth, float viewportHeight) {
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
    if (roomBoundsSet) {
      recomputeCameraBounds();
    }
  }

  private void recomputeCameraBounds() {
    minCameraX = roomMinX + viewportWidth / 2f;
    maxCameraX = roomMaxX - viewportWidth / 2f;
    minCameraY = roomMinY + viewportHeight / 2f;
    maxCameraY = roomMaxY - viewportHeight / 2f;
  }

  /**
   * Call once when the player spawns/enters a room, so the very first
   * frame already has them biased toward the left edge instead of centered.
   */
  public void snapToSpawn(float playerX, float playerY, boolean facingRight) {
    currentOffsetX = facingRight ? lookaheadDistance : -lookaheadDistance;

    // Player spawns near the left third of the screen: push the camera
    // further right than a centered view would, so the player sits left.
    float spawnBias = viewportWidth * 0.2f; // 20% of screen width toward the left edge
    float targetX = playerX + spawnBias + currentOffsetX;
    float targetY = playerY;

    camera.position.set(clampX(targetX), clampY(targetY), 0);
    camera.update();
  }

  /**
   * Call every frame in update/render, passing the target's position
   * and facing direction.
   */
  public void update(float delta, float playerX, float playerY, boolean facingRight) {
    float targetOffset = facingRight ? lookaheadDistance : -lookaheadDistance;

    // Smoothly ease the lookahead offset toward the target (avoids snapping
    // the camera instantly when the player turns around)
    currentOffsetX = MathUtils.lerp(currentOffsetX, targetOffset, smoothSpeed * delta);

    float targetX = playerX + currentOffsetX;
    float targetY = playerY;

    float newX = MathUtils.lerp(camera.position.x, targetX, smoothSpeed * delta);
    float newY = MathUtils.lerp(camera.position.y, targetY, smoothSpeed * delta);

    camera.position.set(clampX(newX), clampY(newY), 0);
    camera.update();
  }

  private float clampX(float value) {
    return roomBoundsSet ? clamp(value, minCameraX, maxCameraX) : value;
  }

  private float clampY(float value) {
    return roomBoundsSet ? clamp(value, minCameraY, maxCameraY) : value;
  }

  private float clamp(float value, float min, float max) {
    if (min > max) return (min + max) / 2f; // room smaller than viewport
    return Math.max(min, Math.min(max, value));
  }
}
