package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

public class CameraComponent extends Component {
  private final Camera camera;
  private Vector2 lastPosition;
  private Entity target;

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
    if (this.target == null) {
      return;
    }
    Vector2 position = target.getPosition();
    if (!lastPosition.epsilonEquals(target.getPosition())) {
      camera.position.set(position.x, position.y, 0f);
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
