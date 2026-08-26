package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.utils.math.Vector2Utils;

/**
 * Input handler for the player for keyboard and touch (mouse) input. This input handler only uses
 * keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private Entity cameraEntity;
  private boolean attackHeld;

  public KeyboardPlayerInputComponent() {
    super(5);
  }

  /**
   * Sets the camera entity used to convert the mouse cursor to a world-space attack direction.
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
      case Keys.W:
        walkDirection.add(Vector2Utils.UP);
        triggerWalkEvent();
        return true;
      case Keys.A:
        walkDirection.add(Vector2Utils.LEFT);
        triggerWalkEvent();
        return true;
      case Keys.S:
        walkDirection.add(Vector2Utils.DOWN);
        triggerWalkEvent();
        return true;
      case Keys.D:
        walkDirection.add(Vector2Utils.RIGHT);
        triggerWalkEvent();
        return true;
      case Keys.E:
        if (!attackHeld) {
          Vector2 aimDirection = getMouseAimDirection();
          if (aimDirection != null && !aimDirection.isZero()) {
            entity.getEvents().trigger("fireArrow", aimDirection);
          }
          attackHeld = true;
        }
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
      case Keys.W:
        walkDirection.sub(Vector2Utils.UP);
        triggerWalkEvent();
        return true;
      case Keys.A:
        walkDirection.sub(Vector2Utils.LEFT);
        triggerWalkEvent();
        return true;
      case Keys.S:
        walkDirection.sub(Vector2Utils.DOWN);
        triggerWalkEvent();
        return true;
      case Keys.D:
        walkDirection.sub(Vector2Utils.RIGHT);
        triggerWalkEvent();
        return true;
      case Keys.E:
        attackHeld = false;
        return true;
      default:
        return false;
    }
  }

  private Vector2 getMouseAimDirection() {
    if (cameraEntity == null) {
      return null;
    }
    CameraComponent cameraComponent = cameraEntity.getComponent(CameraComponent.class);
    if (cameraComponent == null) {
      return null;
    }
    Camera camera = cameraComponent.getCamera();
    Vector3 worldPosition = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f));
    return new Vector2(worldPosition.x, worldPosition.y).sub(entity.getCenterPosition());
  }

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
  }
}
