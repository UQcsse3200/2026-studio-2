package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Action component for interacting with the player */
public class PlayerActions extends Component {
  private static final float JUMP_FORCE = 22f;
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

  private PhysicsComponent physicsComponent;
  private GrappleComponent grapple;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean isGrounded = false;
  private boolean isSprinting = false;
  private boolean paused = false;
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
    entity.getEvents().addListener("dash", this::dash);
    entity.getEvents().addListener("hurt", this::onHurtInterruptDash);
    entity.getEvents().addListener("togglePaused", this::togglePause);
    entity.getEvents().addListener("death", this::die);
  }

  @Override
  public void update() {
    boolean wasGrounded = isGrounded;
    isGrounded = checkGrounded();
    checkJumpWindup();

    // The grapple is a hold action: let go of right click and the rope drops
    if (isGrappling() && !isRightMouseHeld()) {
      grapple.release();
    }

    if (isGrounded && !wasGrounded) {
      airDashUsed = false;
      dashCooldownRemaining = 0f;
    }

    if (dashCooldownRemaining > 0f) {
      dashCooldownRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
    }

    if (sprintStopPending) {
      sprintStopGraceRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
      if (sprintStopGraceRemaining <= 0f) {
        confirmStopSprinting();
      }
    }

    if (isDashing) {
      dashTimeRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
      if (dashTimeRemaining <= 0f) {
        endDash();
        dashRecoveryRemaining = DASH_RECOVERY;
      } else {
        // Re-assert the burst every frame so collisions and stray impulses can't eat it.
        // Vertical velocity is held at zero to match the zero-gravity dash.
        Body body = physicsComponent.getBody();
        body.setLinearVelocity(dashDirection * DASH_SPEED, 0f);
        return;
      }
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
    float desiredVelocityX = walkDirection.x * MAX_SPEED.x * speedMultiplier;

    // Reduced control while recovering from a dash; otherwise full control on the ground and
    // weak in the air so swing momentum isn't wiped on landing.
    float control =
        dashRecoveryRemaining > 0f ? DASH_RECOVERY_CONTROL : (isGrounded ? 1f : AIR_CONTROL);

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
      if (direction.x != 0) {
        facingDirection = direction.x > 0 ? 1 : -1;
      }
      moving = true;
    }
  }

  /** Stops the player from walking. */
  void stopWalking() {
    this.walkDirection = Vector2.Zero.cpy();
    if (!isDashing && !isGrappling()) {
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

    if (isGrounded) {
      airDashUsed = false;
      dashCooldownRemaining = 0f;
      body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
      isGrounded = false;
      jumpImpulseAt = ServiceLocator.getTimeSource().getTime() + JUMP_WINDUP_MS;
      entity.getEvents().trigger("jumpStart");
    }
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
    if (isDashing || dashCooldownRemaining > 0f || paused) {
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
}
