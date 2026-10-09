package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.level.SlipperyPlatformComponent;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/** Action component for interacting with the player */
public class PlayerActions extends Component {
  private static final float JUMP_FORCE = 41f;
  private static final Vector2 MAX_SPEED = new Vector2(5f, 5f); // Metres per second
  private static final float SPRINT_MULTIPLIER = 1.75f;
  private static final float ROPE_JUMP_MULTIPLIER = 0.7f;
  private static final float AIR_CONTROL = 0.1f; // How much steering you get mid-air
  private static final long JUMP_WINDUP_MS =
      90; // Anticipation delay before a ground jump lifts off
  private static final float DASH_SPEED = 14f;
  private static final float DASH_DURATION = 0.15f;
  private static final float DASH_COOLDOWN = 1f;
  private static final float DASH_RECOVERY = 0.1f;
  private static final float DASH_RECOVERY_CONTROL = 0.2f;
  private static final float SPRINT_RELEASE_GRACE = 0.12f;
  // How fast the player must be descending before it counts as a fall, so a scuff over a tile seam
  // or the moment of hang time at the apex doesn't flicker the animation.
  private static final float FALL_SPEED_THRESHOLD = 1f;
  // If a jump's impulse fires but the player never leaves the ground (jumping under a low ceiling),
  // give up waiting after this long and report a landing so the animation can recover.
  private static final long LIFTOFF_GRACE_MS = 200;

  private float extraSpeedMultiplier = 1f; // The Speed multiplier
  private long speedPotionEndTimeMs = 0; // The time that speed_potion ends

  private PhysicsComponent physicsComponent;
  private GrappleComponent grapple;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean isGrounded = false;
  private boolean isSprinting = false;
  private boolean isDashing = false;
  private float dashTimeRemaining = 0f;
  private float dashCooldownRemaining = 0f;
  private float dashRecoveryRemaining = 0f;
  private boolean airDashUsed = false;
  private int facingDirection = 1;
  private int dashDirection = 1;
  private boolean sprintStopPending = false;
  private float sprintStopGraceRemaining = 0f;
  private float storedGravityScale = 1f;
  public boolean droppingFromLedge = false;
  private boolean dead = false;
  private long jumpImpulseAt = -1; // Timestamp to apply the queued jump impulse, -1 if none queued
  private boolean airborne = false; // Has left the ground since the last landing
  private boolean falling = false; // "fallStart" already reported for this airborne stretch
  private long liftoffDeadline =
      -1; // When to give up waiting for a queued jump to leave the ground
  private float decelerationTraction = 1f;
  private float traction = 1f;

  /**
   * The direction the player is currently facing.
   *
   * @return 1 if facing right, -1 if facing left
   */
  public int getFacingDirection() {
    return facingDirection;
  }

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    grapple = entity.getComponent(GrappleComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::stopSprinting);
    entity.getEvents().addListener("dash", this::dash);
    entity.getEvents().addListener("hurt", this::onHurtInterruptDash);
    entity.getEvents().addListener("updateLedgeDrop", this::setLedgeDropping);
    entity.getEvents().addListener("speedPotionUsed", this::applySpeedPotion);
    entity.getEvents().addListener("death", this::die);
    entity.getEvents().addListener("revive", this::revive);
  }

  @Override
  public void update() {
    if (dead) {
      return;
    }
    boolean wasGrounded = isGrounded;
    isGrounded = checkGrounded();
    checkJumpWindup();
    updateAirState();

    if (isGrounded && !wasGrounded) {
      airDashUsed = false;
      dashCooldownRemaining = 0f;
    }

    if (dashCooldownRemaining > 0f) {
      dashCooldownRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
    }

    updateSprintRelease();
    if (updateDash()) {
      return;
    }

    if (dashRecoveryRemaining > 0f) {
      dashRecoveryRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
      if (!isGrappling()) {
        updateSpeed();
      }
      return;
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

  private void updateSprintRelease() {
    if (sprintStopPending) {
      sprintStopGraceRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
      if (sprintStopGraceRemaining <= 0f) {
        confirmStopSprinting();
      }
    }
  }

  /**
   * @return whether the active dash consumes this frame's movement.
   */
  private boolean updateDash() {
    if (!isDashing) {
      return false;
    }
    dashTimeRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
    if (dashTimeRemaining <= 0f) {
      endDash();
      dashRecoveryRemaining = DASH_RECOVERY;
      return false;
    }
    // Re-assert the burst so collisions cannot consume it; keep vertical drift at zero.
    physicsComponent.getBody().setLinearVelocity(dashDirection * DASH_SPEED, 0f);
    return true;
  }

  private boolean isGrappling() {
    return grapple != null && grapple.isAttached();
  }

  /** Applies the queued ground-jump impulse once its short wind-up has elapsed. */
  private void checkJumpWindup() {
    if (jumpImpulseAt < 0) {
      return;
    }
    if (ServiceLocator.getTimeSource().getTime() >= jumpImpulseAt) {
      jumpImpulseAt = -1;
      Body body = physicsComponent.getBody();
      body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
      liftoffDeadline = ServiceLocator.getTimeSource().getTime() + LIFTOFF_GRACE_MS;
    }
  }

  /**
   * Tracks whether the player is airborne, rising or falling, and announces the transitions the
   * animation controller needs: "fallStart" the moment a descent begins, and "landed" on touchdown.
   * Velocity-driven rather than jump-driven, so walking off a ledge or dropping through a platform
   * reports a fall too.
   */
  private void updateAirState() {
    if (jumpImpulseAt >= 0) {
      // Still crouching through the wind-up with both feet on the ground.
      return;
    }

    if (isGrappling()) {
      // Swinging is its own thing: not falling, and touching down mid-swing isn't a landing.
      // Clearing the flag means letting go mid-air reports a fresh fall.
      falling = false;
      return;
    }

    if (!isGrounded) {
      airborne = true;
      liftoffDeadline = -1;
      if (!falling && !isDashing) {
        float verticalVelocity = physicsComponent.getBody().getLinearVelocity().y;
        if (verticalVelocity < -FALL_SPEED_THRESHOLD) {
          falling = true;
          entity.getEvents().trigger("fallStart");
        }
      }
      return;
    }

    if (airborne) {
      airborne = false;
      falling = false;
      liftoffDeadline = -1;
      entity.getEvents().trigger("landed");
    } else if (liftoffDeadline >= 0
        && ServiceLocator.getTimeSource().getTime() >= liftoffDeadline) {
      // The jump fired but never got off the ground, e.g. straight into a low ceiling.
      liftoffDeadline = -1;
      entity.getEvents().trigger("landed");
    }
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();

    float speedMultiplier = isSprinting ? SPRINT_MULTIPLIER : 1f;
    float desiredVelocityX = walkDirection.x * MAX_SPEED.x * speedMultiplier * extraSpeedMultiplier;

    // Reduced control while recovering from a dash; otherwise full control on the ground and
    // weak in the air so swing momentum isn't wiped on landing.
    float control = isGrounded ? traction : AIR_CONTROL;
    if (dashRecoveryRemaining > 0f) {
      control = DASH_RECOVERY_CONTROL;
    }

    // impulse = (desiredVel - currentVel) * mass
    float impulseX = (desiredVelocityX - velocity.x) * body.getMass() * control;
    body.applyLinearImpulse(new Vector2(impulseX, 0), body.getWorldCenter(), true);
  }

  /**
   * Short ray down from the player's feet to see if we're standing on something. Enemy bodies count
   * as ground, so landing on one plays the landing recovery and lets you jump straight back off
   * instead of looping the fall animation on their head.
   */
  private boolean checkGrounded() {
    Vector2 position = entity.getCenterPosition();
    float halfHeight = entity.getScale().y / 2f;
    Vector2 rayStart = position.cpy().sub(0, halfHeight);
    Vector2 rayEnd = rayStart.cpy().sub(0, 0.15f);
    RaycastHit hit = new RaycastHit();
    boolean grounded =
        ServiceLocator.getPhysicsService()
            .getPhysics()
            .raycast(rayStart, rayEnd, PhysicsLayer.STANDABLE, hit);

    decelerationTraction = 1f;
    // if we're grounded, we need to check if we're on a slippery platform and update the player's
    // traction used in the updateSpeed() method accordingly
    if (grounded && hit.fixture != null) {
      // get raw data and check if it's user data is a proper BodyUserData
      Object userData = hit.fixture.getBody().getUserData();
      if (userData instanceof BodyUserData data && data.entity != null) {
        SlipperyPlatformComponent slipperyPlatform =
            data.entity.getComponent(SlipperyPlatformComponent.class);
        if (slipperyPlatform != null) {
          decelerationTraction = slipperyPlatform.getSlipperiness();
        }
      }
    }

    return grounded;
  }

  /** Stops the player permanently reacting to input once they've died. */
  void die() {
    dead = true;
    resetMovementState();
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();
    body.setLinearVelocity(0f, velocity.y);
    stopWalking();
  }

  /** Revives the player after a checkpoint restart so input and movement work again. */
  void revive() {
    dead = false;
    resetMovementState();
    stopWalking();
  }

  private void resetMovementState() {
    onHurtInterruptDash();
    isSprinting = false;
    sprintStopPending = false;
    sprintStopGraceRemaining = 0f;
    dashCooldownRemaining = 0f;
    airDashUsed = false;
    jumpImpulseAt = -1;
    liftoffDeadline = -1;
    airborne = false;
    falling = false;
    droppingFromLedge = false;
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
    traction = 1f;
    this.walkDirection = direction;
    if (direction.x != 0) {
      facingDirection = direction.x > 0 ? 1 : -1;
    }
    moving = true;
  }

  /** Stops the player from walking. */
  void stopWalking() {
    this.walkDirection = Vector2.Zero.cpy();
    if (!isDashing && !isGrappling()) {
      traction = decelerationTraction;
      updateSpeed();
    }
    moving = false;
  }

  /** Jump off the ground, or let go of the rope with a kick upward. */
  void jump() {
    if (dead) {
      return;
    }
    Body body = physicsComponent.getBody();

    if (isGrappling()) {
      // A ground jump queued just before the rope latched on would otherwise still fire its
      // impulse on top of the rope kick. Drop it; this jump replaces it.
      jumpImpulseAt = -1;
      if (isDashing) {
        endDash();
      }
      grapple.release();
      airDashUsed = false;
      dashCooldownRemaining = 0f;
      body.applyLinearImpulse(
          new Vector2(0, JUMP_FORCE * ROPE_JUMP_MULTIPLIER), body.getWorldCenter(), true);
      return;
    }

    // A ground jump is already winding up. Re-queueing would restart the crouch and push the
    // liftoff further away with every press.
    if (jumpImpulseAt >= 0) {
      return;
    }

    if (isGrounded) {
      airDashUsed = false;
      dashCooldownRemaining = 0f;
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
    // A press inside the grace window cancels the pending stop, so sprint never breaks.
    sprintStopPending = false;
    sprintStopGraceRemaining = 0f;

    this.isSprinting = true;
    dash();
    if (!isDashing && !isGrappling()) {
      updateSpeed();
    }
  }

  void stopSprinting() {
    if (dead) {
      return;
    }
    if (!isSprinting || sprintStopPending) {
      return;
    }
    sprintStopPending = true;
    sprintStopGraceRemaining = SPRINT_RELEASE_GRACE;
  }

  private void confirmStopSprinting() {
    sprintStopPending = false;
    isSprinting = false;
    if (!isDashing && !isGrappling()) {
      updateSpeed();
    }
    entity.getEvents().trigger("sprintEnd");
  }

  void dash() {
    if (dead || isDashing || dashCooldownRemaining > 0f) {
      return;
    }
    if (isGrappling()) {
      return;
    }
    if (!isGrounded && airDashUsed) {
      return;
    }

    int direction;
    if (walkDirection.x > 0) {
      direction = 1;
    } else if (walkDirection.x < 0) {
      direction = -1;
    } else {
      direction = facingDirection;
    }

    boolean wasGrounded = isGrounded;
    isDashing = true;
    dashTimeRemaining = DASH_DURATION;
    dashCooldownRemaining = DASH_COOLDOWN;
    dashRecoveryRemaining = 0f;
    dashDirection = direction;
    if (!isGrounded) {
      airDashUsed = true;
    }

    Body body = physicsComponent.getBody();
    storedGravityScale = body.getGravityScale();
    body.setGravityScale(0f);
    // Zero vertical drift so the dash is a clean horizontal burst rather than
    // freezing whatever fall speed the player happened to have.
    body.setLinearVelocity(direction * DASH_SPEED, 0f);

    entity.getEvents().trigger(wasGrounded ? "dashStart" : "airDashStart");
  }

  /**
   * Ends the dash burst and restores gravity. Does not start the recovery window; callers that
   * represent a natural end set {@code dashRecoveryRemaining}, while interrupts (hurt, grapple)
   * deliberately skip recovery.
   */
  private void endDash() {
    isDashing = false;
    dashTimeRemaining = 0f;
    Body body = physicsComponent.getBody();
    body.setGravityScale(storedGravityScale);
  }

  private void onHurtInterruptDash() {
    if (isDashing) {
      endDash();
    }
    dashRecoveryRemaining = 0f;
  }

  /**
   * Updates the dropping from ledge flag to allow the physics engine to determine whether a player/
   * ledge collision should be disabled
   *
   * @param value the value to set
   */
  private void setLedgeDropping(boolean value) {
    droppingFromLedge = value;

    // we need to force the player to conduct a contact physics event to trigger the preSolve method
    // as S doesn't seem to automatically trigger a contact collision
    if (droppingFromLedge) {
      PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
      Body body = physics.getBody();

      if (body != null) {
        body.setAwake(true); // force awaken the body to respond to the current contact
      }
    }
  }

  public boolean isSpeedPotionActive() {
    GameTime time = ServiceLocator.getTimeSource();

    return extraSpeedMultiplier != 1f && time != null && time.getTime() < speedPotionEndTimeMs;
  }
}
