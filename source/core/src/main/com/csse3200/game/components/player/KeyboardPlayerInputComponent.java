package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

/** Input handler for player keyboard and mouse controls. */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private static final int SPEED = 1;
  private static final int LEFT = 0;
  private static final int RIGHT = 1;
  private static final int UP = 2;
  private static final int DOWN = 3;
  private final boolean[] keysHeld = new boolean[4];
  private boolean sprintHeld;
  private CameraComponent cameraComponent;
  private boolean attackHeld;
  private boolean dead;
  private boolean rightMouseHeld;
  private boolean cheats = false;

  public KeyboardPlayerInputComponent() {
    super(5);
  }

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("togglePause", this::unpause);
    entity.getEvents().addListener("death", () -> dead = true);
    entity.getEvents().addListener("openShop", this::releaseHeldGameplayInput);
    entity.getEvents().addListener("closeShop", this::syncReleasedShootButton);
    entity.getEvents().addListener("releaseHeldGameplayInput", this::releaseHeldGameplayInput);
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
    if (dead) {
      return false;
    }
    if (isShopOpen() && keycode != Keys.F) {
      return true;
    }
    switch (keycode) {
      // Hotbar number keys
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
      case Keys.NUM_9:
        entity.getEvents().trigger("selectQuickSlot", 8);
        return true;
      case Keys.W:
        entity.getEvents().trigger("grappleClimbStart");
        keysHeld[UP] = true;
        triggerWalkEvent();
        return true;
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
      case Keys.E:
        triggerAttackOrItemUse();
        return true;
      case Keys.F:
        entity.getEvents().trigger("interact");
        return true;
      case Keys.B:
        entity.getEvents().trigger("toggleBackpack");
        return true;
      case Keys.R:
        entity.getEvents().trigger("dropItem");
        return true;
      case Keys.FORWARD_DEL:
        entity.getEvents().trigger("deleteItem");
        return true;
      case Keys.PERIOD:
        entity.getEvents().trigger("switchItem", 1);
        return true;
      case Keys.COMMA:
        entity.getEvents().trigger("switchItem", -1);
        return true;
      case Keys.S:
        entity.getEvents().trigger("grappleDescendStart");
        entity.getEvents().trigger("updateLedgeDrop", true);
        keysHeld[DOWN] = true;
        triggerWalkEvent();
        return true;
      case Keys.TAB:
        entity.getEvents().trigger("openArrowWheel");
        return true;
      case Keys.ESCAPE:
        entity.getEvents().trigger("togglePause");
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
    if (dead) {
      return false;
    }
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
      case Keys.W:
        entity.getEvents().trigger("grappleClimbStop");
        keysHeld[UP] = false;
        triggerWalkEvent();
        return true;
      case Keys.UP:
        keysHeld[UP] = false;
        triggerWalkEvent();
        return true;
      case Keys.S:
        entity.getEvents().trigger("grappleDescendStop");
        keysHeld[DOWN] = false;
        triggerWalkEvent();
        return true;
      case Keys.DOWN:
        keysHeld[DOWN] = false;
        triggerWalkEvent();
        return true;
      case Keys.SHIFT_LEFT:
      case Keys.SHIFT_RIGHT:
        sprintHeld = false;
        triggerSprintEvent();
        return true;
      case Keys.E:
        attackHeld = false;
        return true;
      case Keys.TAB:
        entity.getEvents().trigger("closeArrowWheel");
        return true;
      default:
        return false;
    }
  }

  public void toggleCheats() {
    cheats = !cheats;
  }

  /**
   * Left click swings the melee weapon, right click fires the selected arrow. Both aim toward the
   * clicked world position.
   *
   * @return whether the input was processed
   * @see InputProcessor#touchDown(int, int, int, int)
   */
  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (isShopOpen()) {
      return true;
    }
    if (dead || isArrowWheelOpen()) {
      return false;
    }
    if (button == Buttons.RIGHT) {
      rightMouseHeld = true;
      return triggerAimedEvent("shoot", screenX, screenY);
    }
    return false;
  }

  /**
   * @return true while the right mouse button is being held down
   */
  public boolean isRightMouseHeld() {
    return rightMouseHeld;
  }

  private boolean triggerAimedEvent(String eventName, int screenX, int screenY) {
    Vector2 aim = getAimDirection(screenX, screenY);
    if (aim == null || aim.isZero()) {
      return false;
    }
    entity.getEvents().trigger(eventName, aim);
    return true;
  }

  /**
   * Signals that the fire button was let go, so the selected weapon or arrow can react.
   *
   * @return whether the input was processed
   * @see InputProcessor#touchUp(int, int, int, int)
   */
  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (dead) {
      return isShopOpen();
    }

    if (button == Buttons.RIGHT) {
      // Clear even if the shop is open. The overlay may still deliver this event, and swallowing
      // it without cancelling leaves the bow stuck charging after the shop closes.
      clearHeldShootButton(isShopOpen());
      return true;
    }

    return isShopOpen();
  }

  /** Reports the pointer's offset from the centre of the screen, where the wheel is drawn. */
  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    if (Gdx.graphics == null) {
      return false;
    }

    float centreX = Gdx.graphics.getWidth() / 2f;
    float centreY = Gdx.graphics.getHeight() / 2f;
    // Screen y grows downwards, so flip it to match the wheel's y-up directions.
    Vector2 offsetFromCentre = new Vector2(screenX - centreX, centreY - screenY);
    entity.getEvents().trigger("arrowWheelPointerMoved", offsetFromCentre);

    // Reported, not consumed, so other handlers still see the movement.
    return false;
  }

  private boolean isShopOpen() {
    PlayerInteractionComponent interaction = entity.getComponent(PlayerInteractionComponent.class);
    return interaction != null && interaction.isShopOpen();
  }

  private void releaseHeldGameplayInput() {
    keysHeld[LEFT] = false;
    keysHeld[RIGHT] = false;
    sprintHeld = false;
    attackHeld = false;
    triggerWalkEvent();
    entity.getEvents().trigger("sprintStop");
    // Opening a UI can steal the mouse-up, so drop the charge immediately instead of firing.
    clearHeldShootButton(true);
  }

  /**
   * If the shop consumed the mouse-up, right-click still looks held here. After close, fire/cancel
   * based on whether the button is actually down.
   */
  private void syncReleasedShootButton() {
    if (!rightMouseHeld) {
      return;
    }
    if (Gdx.input != null && Gdx.input.isButtonPressed(Buttons.RIGHT)) {
      return;
    }
    clearHeldShootButton(true);
  }

  /**
   * Drops the right-mouse held flag. {@code cancelCharge} skips firing so a UI overlay cannot spawn
   * an arrow; otherwise this is a normal shoot release.
   */
  private void clearHeldShootButton(boolean cancelCharge) {
    rightMouseHeld = false;
    if (cancelCharge) {
      entity.getEvents().trigger("chargeCancel");
    } else {
      entity.getEvents().trigger("stopShoot");
    }
  }

  private boolean isArrowWheelOpen() {
    ArrowWheelComponent wheel = entity.getComponent(ArrowWheelComponent.class);
    return wheel != null && wheel.isOpen();
  }

  private void triggerAttackOrItemUse() {
    if (attackHeld || isArrowWheelOpen()) {
      return;
    }
    attackHeld = true;

    ItemUseComponent itemUse = entity.getComponent(ItemUseComponent.class);
    if (itemUse != null) {
      itemUse.useSelectedItem();
    }
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

  /**
   * Aim direction from the current mouse position to the player, in world space.
   *
   * @return aim vector, or null if the camera is unavailable
   */
  public Vector2 getMouseAimDirection() {
    return getAimDirection(Gdx.input.getX(), Gdx.input.getY());
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
    float y = 0;
    if (keysHeld[LEFT]) x -= SPEED;
    if (keysHeld[RIGHT]) x += SPEED;
    walkDirection.set(x, y);

    if (cheats) {
      Body body = entity.getComponent(PhysicsComponent.class).getBody();
      if (keysHeld[UP]) {
        body.applyLinearImpulse(new Vector2(0, 10f), body.getWorldCenter(), true);
      }
      if (keysHeld[DOWN]) {
        body.applyLinearImpulse(new Vector2(0, -10f), body.getWorldCenter(), true);
      }
    }

    if (walkDirection.epsilonEquals(Vector2.Zero, 0.01f)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection.cpy());
    }
  }

  public void unpause() {
    triggerWalkEvent();
    triggerSprintEvent();
  }
}
