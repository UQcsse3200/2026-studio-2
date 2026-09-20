package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class CameraComponent extends Component {
  private final Camera camera;
  private Entity target;
  private CaveCameraController lookahead;

  public CameraComponent() {
    this(new OrthographicCamera());
  }

  public CameraComponent(Camera camera) {
    this.camera = camera;
  }

  public void setTarget(Entity target) {
    this.target = target;
    if (target != null) {
      Vector2 position = target.getPosition();
      lookahead().snapToSpawn(position.x, position.y, isTargetFacingRight());
    }
  }

  /**
   * Defines the playable room/level bounds, in world units, so the camera won't show outside
   * them. Call once the level's dimensions are known (e.g. after terrain is spawned).
   */
  public void setRoomBounds(float minX, float minY, float maxX, float maxY) {
    lookahead().setRoomBounds(minX, minY, maxX, maxY);
  }

  @Override
  public void update() {
    if (this.target == null) {
      return;
    }
    Vector2 position = target.getPosition();
    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    lookahead().update(delta, position.x, position.y, isTargetFacingRight());
  }

  private boolean isTargetFacingRight() {
    PlayerActions playerActions = target.getComponent(PlayerActions.class);
    return playerActions == null || playerActions.getFacingDirection() >= 0;
  }

  private CaveCameraController lookahead() {
    if (lookahead == null) {
      lookahead =
          new CaveCameraController((OrthographicCamera) camera, camera.viewportWidth, camera.viewportHeight);
    }
    return lookahead;
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
    lookahead().setViewportSize(camera.viewportWidth, camera.viewportHeight);
  }
}
