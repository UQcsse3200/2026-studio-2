package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.input.InputComponent;

/**
 * Input handler for the player for keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private static final int SPEED = 1;

  public KeyboardPlayerInputComponent() {
    super(5);
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
        walkDirection.add(-SPEED, 0);
        triggerWalkEvent();
        return true;
      case Keys.D:
      case Keys.RIGHT:
        walkDirection.add(SPEED, 0);
        triggerWalkEvent();
        return true;
      case Keys.SPACE:
      case Keys.W:
        entity.getEvents().trigger("jump");
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
        walkDirection.add(SPEED, 0);
        triggerWalkEvent();
        return true;
      case Keys.D:
      case Keys.RIGHT:
        walkDirection.add(-SPEED, 0);
        triggerWalkEvent();
        return true;
      default:
        return false;
    }
  }

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero, 0.01f)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection.cpy());
    }
  }
}
