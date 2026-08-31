package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;

/** Input handler for player keyboard and mouse controls. */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private static final int SPEED = 1;
  private static final int LEFT = 0;
  private static final int RIGHT = 1;
  private final boolean[] keysHeld = new boolean[2];
  private boolean sprintHeld;
  private Entity cameraEntity;

  public KeyboardPlayerInputComponent() {
    super(5);
  }

  /**
   * Sets the camera entity used to convert screen coordinates to world-space aim directions.
   *
   * @param cameraEntity entity holding the active {@link CameraComponent}
   */
  public void setCameraEntity(Entity cameraEntity) {
    this.cameraEntity = cameraEntity;
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
      case Keys.W:
        entity.getEvents().trigger("jump");
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
    return triggerAimedEvent("shoot", screenX, screenY);
  }

  private void triggerSprintEvent() {
    if (sprintHeld) {
      entity.getEvents().trigger("sprint");
    } else {
      entity.getEvents().trigger("sprintStop");
    }
  }

  /** Fires an event aimed at wherever the mouse cursor currently sits. */
  private boolean triggerAimedEvent(String eventName) {
    return triggerAimedEvent(eventName, Gdx.input.getX(), Gdx.input.getY());
  }

  /** Fires an event aimed at the given screen position. */
  private boolean triggerAimedEvent(String eventName, int screenX, int screenY) {
    Vector2 aim = getAimDirection(screenX, screenY);
    if (aim == null || aim.isZero()) {
      return false;
    }
    entity.getEvents().trigger(eventName, aim);
    return true;
  }

  private Vector2 getAimDirection(int screenX, int screenY) {
    if (cameraEntity == null) {
      return null;
    }
    CameraComponent cameraComponent = cameraEntity.getComponent(CameraComponent.class);
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
