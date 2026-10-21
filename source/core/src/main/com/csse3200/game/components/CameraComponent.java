package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class CameraComponent extends Component {
  private final Camera camera;
  private Vector2 lastPosition;
  private Entity target;

  private float ZOOM_TIME = 0.75f;
  private float currentZoomTime = 0f;
  private boolean requestedZoom = false;
  private boolean zoomingCamera = false;

  public float targetGameWidth;
  public float targetGameHeight;
  public Vector2 targetPosition = new Vector2();

  public float startGameWidth;
  public float startGameHeight;
  private Vector2 startPosition = new Vector2();

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

  @Override
  public void update() {
    //    if (this.target == null) {
    //      return;
    //    }

    if (requestedZoom) {
      startZoom();
    }

    if (zoomingCamera) {
      currentZoomTime += ServiceLocator.getTimeSource().getDeltaTime();
      float progress = Math.min(1f, currentZoomTime / ZOOM_TIME);

      camera.viewportWidth = MathUtils.lerp(startGameWidth, targetGameWidth, progress);
      camera.viewportHeight = MathUtils.lerp(startGameHeight, targetGameHeight, progress);
      camera.position.x = MathUtils.lerp(startPosition.x, targetPosition.x, progress);
      camera.position.y = MathUtils.lerp(startPosition.y, targetPosition.y, progress);

      camera.update();

      if (progress >= 1f) {
        zoomingCamera = false;
      }
    } else if (this.target != null) {
      Vector2 position = target.getPosition();
      if (!lastPosition.epsilonEquals(target.getPosition())) {
        camera.position.set(position.x, position.y, 0f);
        lastPosition = position;
        camera.update();
      }
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

  public boolean getZoomingCamera() {
    return zoomingCamera;
  }

  public void requestZoom() {
    if (!zoomingCamera) {
      requestedZoom = true;
    }
  }

  private void startZoom() {
    currentZoomTime = 0f;
    zoomingCamera = true;
    requestedZoom = false;

    startGameWidth = camera.viewportWidth;
    startGameHeight = camera.viewportHeight;
    startPosition.set(camera.position.x, camera.position.y);
  }
}
