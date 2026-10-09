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
import com.csse3200.game.services.ServiceLocator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Input handler for player keyboard and mouse controls. */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private static final String SELECT_QUICK_SLOT = "selectQuickSlot";
  private static final int SPEED = 1;
  private static final int LEFT = 0;
  private static final int RIGHT = 1;
  private static final int UP = 2;
  private static final int DOWN = 3;
  private final boolean[] keysHeld = new boolean[4];
  private final Set<Integer> heldKeys = new HashSet<>();
  private boolean sprintHeld;
  private boolean inputSyncPending;
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
    entity.getEvents().addListener("death", () -> setDead(true));
    entity.getEvents().addListener("revive", () -> setDead(false));
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
   * @return the camera used for aiming, or null if none has been set yet
   */
  public CameraComponent getCameraComponent() {
    return cameraComponent;
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
    if (isShopOpen()) {
      if (keycode == Keys.F) {
        entity.getEvents().trigger("interact");
      }
      return true;
    }
    if (handleMovementKey(keycode, true)) {
      return true;
    }
    if (keycode == Keys.ESCAPE) {
      if (!ServiceLocator.getEntityService().getSettingsOpen()) {
        entity.getEvents().trigger("togglePause");
      }
      unpause();
      return true;
    }
    if (keycode == Keys.M) {
      entity.getEvents().trigger("toggleMap");
      return true;
    }
    if (isPaused()) {
      return isActionKey(keycode);
    }
    if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_9) {
      entity.getEvents().trigger(SELECT_QUICK_SLOT, keycode - Keys.NUM_1);
      return true;
    }
    return handleActionKey(keycode);
  }

  private boolean isActionKey(int keycode) {
    return switch (keycode) {
      case Keys.NUM_1,
          Keys.NUM_2,
          Keys.NUM_3,
          Keys.NUM_4,
          Keys.NUM_5,
          Keys.NUM_6,
          Keys.NUM_7,
          Keys.NUM_8,
          Keys.NUM_9,
          Keys.SPACE,
          Keys.E,
          Keys.F,
          Keys.B,
          Keys.R,
          Keys.FORWARD_DEL,
          Keys.PERIOD,
          Keys.COMMA,
          Keys.TAB,
          Keys.Q ->
          true;
      default -> false;
    };
  }

  private boolean handleActionKey(int keycode) {
    switch (keycode) {
      case Keys.SPACE:
        triggerJumpEvent();
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
      case Keys.TAB:
        entity.getEvents().trigger("openArrowWheel");
        return true;
      case Keys.Q:
        entity.getEvents().trigger("instrumentStart");
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
    if (handleMovementKey(keycode, false)) {
      return true;
    }
    switch (keycode) {
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

  private boolean handleMovementKey(int keycode, boolean pressed) {
    switch (keycode) {
      case Keys.A, Keys.LEFT, Keys.D, Keys.RIGHT, Keys.W, Keys.S, Keys.SHIFT_LEFT, Keys.SHIFT_RIGHT:
        break;
      default:
        return false;
    }
    if (pressed) {
      heldKeys.add(keycode);
    } else {
      heldKeys.remove(keycode);
    }
    keysHeld[LEFT] = heldKeys.contains(Keys.A) || heldKeys.contains(Keys.LEFT);
    keysHeld[RIGHT] = heldKeys.contains(Keys.D) || heldKeys.contains(Keys.RIGHT);
    keysHeld[UP] = heldKeys.contains(Keys.W);
    keysHeld[DOWN] = heldKeys.contains(Keys.S);
    sprintHeld = heldKeys.contains(Keys.SHIFT_LEFT) || heldKeys.contains(Keys.SHIFT_RIGHT);
    if (!isPaused()) {
      triggerMovementKey(keycode, pressed);
    } else {
      inputSyncPending = true;
      if (!pressed) {
        stopGrappleMovement(keycode);
      }
    }
    return true;
  }

  private void triggerMovementKey(int keycode, boolean pressed) {
    if (keycode == Keys.SHIFT_LEFT || keycode == Keys.SHIFT_RIGHT) {
      triggerSprintEvent();
      return;
    }
    if (keycode == Keys.W) {
      entity.getEvents().trigger(pressed ? "grappleClimbStart" : "grappleClimbStop");
    } else if (keycode == Keys.S) {
      entity.getEvents().trigger(pressed ? "grappleDescendStart" : "grappleDescendStop");
      if (pressed) {
        entity.getEvents().trigger("updateLedgeDrop", true);
      }
    }
    triggerWalkEvent();
  }

  private void stopGrappleMovement(int keycode) {
    if (keycode == Keys.W) {
      entity.getEvents().trigger("grappleClimbStop");
    } else if (keycode == Keys.S) {
      entity.getEvents().trigger("grappleDescendStop");
    }
  }

  private boolean isPaused() {
    return ServiceLocator.getEntityService().getPaused();
  }

  private void resetHeldInput() {
    inputSyncPending = false;
    heldKeys.clear();
    Arrays.fill(keysHeld, false);
    sprintHeld = false;
    attackHeld = false;
    rightMouseHeld = false;
  }

  private void setDead(boolean dead) {
    this.dead = dead;
    resetHeldInput();
  }

  public void toggleCheats() {
    cheats = !cheats;
  }

  /**
   * Left click swings the melee weapon in the direction the player is facing. Right click fires the
   * selected arrow toward the clicked world position.
   *
   * @return whether the input was processed
   * @see InputProcessor#touchDown(int, int, int, int)
   */
  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (isShopOpen()) {
      return true;
    }
    if (dead || isArrowWheelOpen() || isPaused()) {
      return false;
    }
    if (button == Buttons.LEFT) {
      if (rightMouseHeld) {
        return false;
      }
      entity.getEvents().trigger("meleeStart");
      return true;
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
      clearHeldShootButton(isShopOpen() || isPaused());
      return true;
    }

    return isShopOpen();
  }

  /**
   * Reports the pointer's offset from the centre of the arrow wheel, or from the centre of the
   * screen if the wheel hasn't been placed.
   */
  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    if (Gdx.graphics != null) {
      float centreX = Gdx.graphics.getWidth() / 2f;
      float centreYFromTop = Gdx.graphics.getHeight() / 2f;
      ArrowWheelComponent wheel = entity.getComponent(ArrowWheelComponent.class);
      Vector2 wheelCentre = wheel == null ? null : wheel.getScreenCentre();
      if (wheelCentre != null) {
        centreX = wheelCentre.x;
        centreYFromTop = wheelCentre.y;
      }
      // Screen y grows downwards, so flip it to match the wheel's y-up directions.
      Vector2 offsetFromCentre = new Vector2(screenX - centreX, centreYFromTop - screenY);
      entity.getEvents().trigger("arrowWheelPointerMoved", offsetFromCentre);
    }

    // Reported, not consumed, so other handlers still see the movement.
    return false;
  }

  private boolean isShopOpen() {
    PlayerInteractionComponent interaction = entity.getComponent(PlayerInteractionComponent.class);
    return interaction != null && interaction.isShopOpen();
  }

  private void releaseHeldGameplayInput() {
    resetHeldInput();
    stopGrappleMovement(Keys.W);
    stopGrappleMovement(Keys.S);
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
    Vector3 screenPosition = screenPosition(screenX, screenY);
    Vector3 worldPosition = camera.unproject(screenPosition);
    return new Vector2(worldPosition.x, worldPosition.y).sub(entity.getCenterPosition());
  }

  /** Converts pixel coordinates to the floating-point vector required by the camera. */
  private static Vector3 screenPosition(double x, double y) {
    return new Vector3((float) x, (float) y, 0f);
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

  @Override
  public void earlyUpdate() {
    if (inputSyncPending && !isPaused()) {
      unpause();
    }
  }

  public void unpause() {
    if (dead) {
      return;
    }
    if (isPaused()) {
      // Screens change the pause state after the input callback. Apply on the next game frame.
      inputSyncPending = true;
      return;
    }
    inputSyncPending = false;
    triggerWalkEvent();
    triggerSprintEvent();
    if (keysHeld[UP]) {
      entity.getEvents().trigger("grappleClimbStart");
    }
    if (keysHeld[DOWN]) {
      entity.getEvents().trigger("grappleDescendStart");
    }
  }
}
