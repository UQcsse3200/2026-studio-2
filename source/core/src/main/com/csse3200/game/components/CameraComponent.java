package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

public class CameraComponent extends Component {
  private final Camera camera;
  private Vector2 lastPosition;
  private Entity target;
  private boolean horizontalBoundsSet = false;
  private float minX;
  private float maxX;
  private boolean verticalBoundsSet = false;
  private float minY;
  private float maxY;
  private float verticalFramingOffset = 0f;

  public CameraComponent() {
    this(new OrthographicCamera());
  }

  public CameraComponent(Camera camera) {
    this.camera = camera;
    lastPosition = Vector2.Zero.cpy();
  }

  public void setTarget(Entity target) {
    this.target = target;
  }

  /**
   * Stops the camera's viewport from panning past the given world-x bounds (e.g. level walls),
   * instead of showing empty space beyond the edge.
   *
   * @param minX left edge the camera's viewport won't pan past
   * @param maxX right edge the camera's viewport won't pan past
   */
  public void setHorizontalBounds(float minX, float maxX) {
    this.minX = minX;
    this.maxX = maxX;
    this.horizontalBoundsSet = true;
  }

  /**
   * Stops the camera's viewport from panning past the given world-y bounds.
   *
   * @param minY bottom edge the camera's viewport won't pan past
   * @param maxY top edge the camera's viewport won't pan past
   */
  public void setVerticalBounds(float minY, float maxY) {
    this.minY = minY;
    this.maxY = maxY;
    this.verticalBoundsSet = true;
  }

  /**
   * Frames this far above the target instead of centering exactly on it, so the ground sits lower
   * on screen (more sky/level visible above) instead of the target sitting dead-centre.
   *
   * @param offset world units above the target to frame; 0 (default) centres on the target
   */
  public void setVerticalFramingOffset(float offset) {
    this.verticalFramingOffset = offset;
  }

  @Override
  public void update() {
    if (this.target == null) {
      return;
    }
    Vector2 position = target.getPosition();
    if (!lastPosition.epsilonEquals(target.getPosition())) {
      float x = position.x;
      float y = position.y + verticalFramingOffset;

      if (horizontalBoundsSet) {
        float halfWidth = camera.viewportWidth / 2f;
        float lo = minX + halfWidth;
        float hi = maxX - halfWidth;
        x = lo <= hi ? MathUtils.clamp(x, lo, hi) : (lo + hi) / 2f;
      }
      if (verticalBoundsSet) {
        float halfHeight = camera.viewportHeight / 2f;
        float lo = minY + halfHeight;
        float hi = maxY - halfHeight;
        y = lo <= hi ? MathUtils.clamp(y, lo, hi) : (lo + hi) / 2f;
      }

      camera.position.set(x, y, 0f);
      lastPosition = position;
      camera.update();
    }
  }

  public Matrix4 getProjectionMatrix() {
    return camera.combined;
  }

  public Camera getCamera() {
    return camera;
  }

  public void resize(int screenWidth, int screenHeight, float gameWidth) {
    float ratio = (float) screenHeight / screenWidth;
    camera.viewportWidth = gameWidth;
    camera.viewportHeight = gameWidth * ratio;
    camera.update();
  }
}
