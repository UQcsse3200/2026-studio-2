package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/** Action component for interacting with the player */
public class PlayerActions extends Component {
  private static final float JUMP_FORCE = 27f;
  private static final Vector2 MAX_SPEED = new Vector2(5f, 5f); // Metres per second
  private static final float SPRINT_MULTIPLIER = 1.75f;
  private static final float ROPE_JUMP_MULTIPLIER = 0.7f;
  private static final float AIR_CONTROL = 0.1f; // How much steering you get mid-air
  private static final long JUMP_WINDUP_MS =
      90; // Anticipation delay before a ground jump lifts off

  private float extraSpeedMultiplier = 1f; // The Speed multiplier
  private long speedPotionEndTimeMs = 0; // The time that speed_potion ends

  private PhysicsComponent physicsComponent;
  private GrappleComponent grapple;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean isGrounded = false;
  private boolean isSprinting = false;
  private boolean paused = false;
  private boolean dead = false;
  private long jumpImpulseAt = -1; // Timestamp to apply the queued jump impulse, -1 if none queued

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    grapple = entity.getComponent(GrappleComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::stopSprinting);
    entity.getEvents().addListener("togglePaused", this::togglePause);
    entity.getEvents().addListener("speedPotionUsed", this::applySpeedPotion);
    entity.getEvents().addListener("death", this::die);
  }

  @Override
  public void update() {
    isGrounded = checkGrounded();
    checkJumpWindup();

    // The grapple is a hold action: let go of right click and the rope drops
    if (isGrappling() && !isRightMouseHeld()) {
      grapple.release();
    }

    if (!moving) {
      return;
    }
    if (isGrappling()) {
      // Walking is off while swinging, movement keys just add speed to the arc
      entity.getEvents().trigger("grappleSwing", walkDirection.x);
    } else {
      updateSpeed();
    }

    GameTime time = ServiceLocator.getTimeSource();
    if (extraSpeedMultiplier != 1f && time != null && time.getTime() >= speedPotionEndTimeMs) {
      extraSpeedMultiplier = 1f;
    }
  }

  private boolean isGrappling() {
    return grapple != null && grapple.isAttached();
  }

  /** Applies the queued ground-jump impulse once its short wind-up has elapsed. */
  private void checkJumpWindup() {
    if (jumpImpulseAt < 0) {
      return;
    }
    if (dead) {
      jumpImpulseAt = -1;
      return;
    }
    if (ServiceLocator.getTimeSource().getTime() >= jumpImpulseAt) {
      jumpImpulseAt = -1;
      Body body = physicsComponent.getBody();
      body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
    }
  }

  private boolean isRightMouseHeld() {
    KeyboardPlayerInputComponent input = entity.getComponent(KeyboardPlayerInputComponent.class);
    return input != null && input.isRightMouseHeld();
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();

    float speedMultiplier = isSprinting ? SPRINT_MULTIPLIER : 1f;
    float desiredVelocityX = walkDirection.x * MAX_SPEED.x * speedMultiplier * extraSpeedMultiplier;

    // Full control on the ground, weak in the air so swing momentum isn't wiped on landing
    float control = isGrounded ? 1f : AIR_CONTROL;

    // impulse = (desiredVel - currentVel) * mass
    float impulseX = (desiredVelocityX - velocity.x) * body.getMass() * control;
    body.applyLinearImpulse(new Vector2(impulseX, 0), body.getWorldCenter(), true);
  }

  /** Short ray down from the player's feet to see if we're standing on something. */
  private boolean checkGrounded() {
    Vector2 position = entity.getCenterPosition();
    float halfHeight = entity.getScale().y / 2f;
    Vector2 rayStart = position.cpy().sub(0, halfHeight);
    Vector2 rayEnd = rayStart.cpy().sub(0, 0.15f);
    RaycastHit hit = new RaycastHit();
    return ServiceLocator.getPhysicsService()
        .getPhysics()
        .raycast(rayStart, rayEnd, PhysicsLayer.SOLID, hit);
  }

  void togglePause() {
    paused = !paused;
  }

  /** Stops the player permanently reacting to input once they've died. */
  void die() {
    dead = true;
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();
    body.setLinearVelocity(0f, velocity.y);
    stopWalking();
  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    if (dead) {
      return;
    }
    if (paused) {
      stopWalking();
    } else {
      this.walkDirection = direction;
      moving = true;
    }
  }

  /** Stops the player from walking. */
  void stopWalking() {
    this.walkDirection = Vector2.Zero.cpy();
    if (!isGrappling()) {
      updateSpeed();
    }
    moving = false;
  }

  /** Jump off the ground, or let go of the rope with a kick upward. */
  void jump() {
    if (dead || jumpImpulseAt >= 0) {
      return;
    }
    Body body = physicsComponent.getBody();

    if (isGrappling()) {
      grapple.release();
      body.applyLinearImpulse(
          new Vector2(0, JUMP_FORCE * ROPE_JUMP_MULTIPLIER), body.getWorldCenter(), true);
      return;
    }

    if (isGrounded) {
      isGrounded = false;
      jumpImpulseAt = ServiceLocator.getTimeSource().getTime() + JUMP_WINDUP_MS;
      entity.getEvents().trigger("jumpStart");
    }
  }

  // one para is the extraMutiplier, another one is the time the potion last
  private void applySpeedPotion(float boost, float duration) {
    extraSpeedMultiplier = 1f + boost;

    long durationMs = (long) (duration * 1000f);
    speedPotionEndTimeMs = ServiceLocator.getTimeSource().getTime() + durationMs;

    updateSpeed();
  }

  void sprint() {
    if (dead) {
      return;
    }
    this.isSprinting = true;
    if (!isGrappling()) {
      updateSpeed();
    }
  }

  void stopSprinting() {
    if (dead) {
      return;
    }
    this.isSprinting = false;
    if (!isGrappling()) {
      updateSpeed();
    }
  }

  public boolean isSpeedPotionActive() {
    GameTime time = ServiceLocator.getTimeSource();

    return extraSpeedMultiplier != 1f && time != null && time.getTime() < speedPotionEndTimeMs;
  }
}
