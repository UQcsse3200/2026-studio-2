package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.utils.math.Vector2Utils;

/**
 * Input handler for the player for keyboard and touch (mouse) input. This input handler only uses
 * keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();

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
      case Keys.NUM_1:
        entity.getEvents().trigger("selectQuickSlot", 0);
        return true;
      case Keys.NUM_2:
        entity.getEvents().trigger("selectQuickSlot", 1);
        return true;
      case Keys.NUM_3:
        entity.getEvents().trigger("selectQuickSlot", 2);
        return true;
      case Keys.NUM_4:
        entity.getEvents().trigger("selectQuickSlot", 3);
        return true;
      case Keys.NUM_5:
        entity.getEvents().trigger("selectQuickSlot", 4);
        return true;
      case Keys.NUM_6:
        entity.getEvents().trigger("selectQuickSlot", 5);
        return true;
      case Keys.NUM_7:
        entity.getEvents().trigger("selectQuickSlot", 6);
        return true;
      case Keys.NUM_8:
        entity.getEvents().trigger("selectQuickSlot", 7);
        return true;
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
      case Keys.SPACE:
        entity.getEvents().trigger("attack");
        return true;
      case Keys.E:
        entity.getEvents().trigger("interact");
        return true;
      case Keys.B:
        entity.getEvents().trigger("toggleBackpack");
        return true;
      case Keys.Q:
        entity.getEvents().trigger("dropItem");
        return true;
      case Keys.X:
        entity.getEvents().trigger("deleteItem");
        return true;
      case Keys.PERIOD:
        entity.getEvents().trigger("switchItem", 1);
        return true;
      case Keys.COMMA:
        entity.getEvents().trigger("switchItem", -1);
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
      default:
        return false;
    }
  }

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
  }
}
