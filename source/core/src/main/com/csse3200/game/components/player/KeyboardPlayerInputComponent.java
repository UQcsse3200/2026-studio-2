package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;

/**
 * Input handler for the player for keyboard and touch (mouse) input. This input handler only uses
 * keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private static final int SPEED = 1;
  private static final int LEFT = 0;
  private static final int RIGHT = 1;
  private final boolean[] keysHeld = new boolean[2];
  private Entity cameraEntity;

  public KeyboardPlayerInputComponent() {
    super(5);
  }

  /**
   * Sets the camera entity used to convert screen coordinates to world coordinates when aiming.
   *
   * @param cameraEntity entity holding a CameraComponent
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
        entity.getEvents().trigger("jump");
        return true;
      case Keys.SHIFT_LEFT:
      case Keys.SHIFT_RIGHT:
        entity.getEvents().trigger("sprint");
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
        entity.getEvents().trigger("sprintStop");
        return true;
      default:
        return false;
    }
  }

  /**
   * Fires the grapple toward the clicked world position.
   *
   * @return whether the input was processed
   * @see InputProcessor#touchDown(int, int, int, int)
   */
  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button != Buttons.LEFT || cameraEntity == null) {
      return false;
    }
    CameraComponent cameraComponent = cameraEntity.getComponent(CameraComponent.class);
    if (cameraComponent == null) {
      return false;
    }
    Camera camera = cameraComponent.getCamera();
    Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
    Vector2 aim = new Vector2(world.x, world.y).sub(entity.getCenterPosition());
    entity.getEvents().trigger("grappleFire", aim);
    return true;
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
