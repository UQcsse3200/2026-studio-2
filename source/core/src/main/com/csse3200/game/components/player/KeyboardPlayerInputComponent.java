package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.input.InputComponent;

/** Input handler for player keyboard and mouse controls. */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private static final int SPEED = 1;
  private static final int LEFT = 0;
  private static final int RIGHT = 1;
  private final boolean[] keysHeld = new boolean[2];
  private boolean sprintHeld;
  private CameraComponent cameraComponent;

  public KeyboardPlayerInputComponent() {
    super(5);
  }

  /**
   * Sets the camera used to convert screen coordinates to world-space aim directions.
   *
   * @param cameraComponent active camera component
   */
  public void setCameraComponent(CameraComponent cameraComponent) {
    this.cameraComponent = cameraComponent;
  }

  /**
   * Triggers player events on specific keycodes.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Keys.A:
      case Keys.LEFT:
        keysHeld[LEFT] = true;
        triggerWalkEvent();
        return true;
      case Keys.D:
      case Keys.RIGHT:
        keysHeld[RIGHT] = true;
        triggerWalkEvent();
        return true;
      case Keys.SPACE:
        triggerJumpEvent();
        return true;
      case Keys.SHIFT_LEFT:
      case Keys.SHIFT_RIGHT:
        sprintHeld = true;
        triggerSprintEvent();
        return true;
      case Keys.Q:
        entity.getEvents().trigger("cycleArrow");
        return true;
      default:
        return false;
    }
  }

  /**
   * Triggers player events on specific keycodes.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyUp(int)
   */
  @Override
  public boolean keyUp(int keycode) {
    switch (keycode) {
      case Keys.A:
      case Keys.LEFT:
        keysHeld[LEFT] = false;
        triggerWalkEvent();
        return true;
      case Keys.D:
      case Keys.RIGHT:
        keysHeld[RIGHT] = false;
        triggerWalkEvent();
        return true;
      case Keys.SHIFT_LEFT:
      case Keys.SHIFT_RIGHT:
        sprintHeld = false;
        triggerSprintEvent();
        return true;
      default:
        return false;
    }
  }

  /**
   * Fires the currently selected arrow toward the clicked world position.
   *
   * @return whether the input was processed
   * @see InputProcessor#touchDown(int, int, int, int)
   */
  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button != Buttons.LEFT) {
      return false;
    }
    Vector2 aimDirection = getAimDirection(screenX, screenY);
    if (aimDirection == null || aimDirection.isZero()) {
      return false;
    }
    entity.getEvents().trigger("shoot", aimDirection);
    return true;
  }

  /**
   * Signals that the fire button was let go, so the selected arrow can react. Carries the aim
   * direction resolved at release time, so a charged bow shot fires toward wherever the cursor
   * currently is rather than where it was when charging started.
   *
   * @return whether the input was processed
   * @see InputProcessor#touchUp(int, int, int, int)
   */
  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (button != Buttons.LEFT) {
      return false;
    }
    entity.getEvents().trigger("stopShoot", getAimDirection(screenX, screenY));
    return true;
  }

  private void triggerSprintEvent() {
    if (sprintHeld) {
      entity.getEvents().trigger("sprint");
    } else {
      entity.getEvents().trigger("sprintStop");
    }
  }

  private void triggerJumpEvent() {
    entity.getEvents().trigger("jump");
  }

  private Vector2 getAimDirection(int screenX, int screenY) {
    if (cameraComponent == null) {
      return null;
    }
    Camera camera = cameraComponent.getCamera();
    Vector3 worldPosition = camera.unproject(new Vector3(screenX, screenY, 0f));
    return new Vector2(worldPosition.x, worldPosition.y).sub(entity.getCenterPosition());
  }

  private void triggerWalkEvent() {
    float x = 0;
    if (keysHeld[LEFT]) x -= SPEED;
    if (keysHeld[RIGHT]) x += SPEED;

    walkDirection.set(x, 0);

    if (walkDirection.epsilonEquals(Vector2.Zero, 0.01f)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection.cpy());
    }
  }
}
