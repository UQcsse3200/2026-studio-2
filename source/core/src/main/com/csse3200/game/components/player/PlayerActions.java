package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
  private static final float JUMP_FORCE = 5.5f;
  private static final Vector2 MAX_SPEED = new Vector2(5f, 5f); // Metres per second
  private static final float SPRINT_MULTIPLIER = 1.75f;
  private static final float ROPE_JUMP_MULTIPLIER = 0.7f;
  private static final float AIR_CONTROL = 0.1f; // How much steering you get mid-air
  private static final float ROLL_SPEED = 10f; // Metres per second at the start of a roll
  private static final float ROLL_DURATION = 0.5f; // Seconds the roll (and its invulnerability) lasts

  private PhysicsComponent physicsComponent;
  private GrappleComponent grapple;
  private CombatStatsComponent combatStats;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private float facing = 1f;
  private boolean moving = false;
  private boolean isGrounded = false;
  private boolean isSprinting = false;
  private boolean paused = false;
  private float rollTimeLeft = 0f;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    grapple = entity.getComponent(GrappleComponent.class);
    combatStats = entity.getComponent(CombatStatsComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::stopSprinting);
    entity.getEvents().addListener("roll", this::roll);
  }

  @Override
  public void update() {
    isGrounded = checkGrounded();
    if (isRolling()) {
      // Momentum carries the roll; steering is ignored until it ends.
      rollTimeLeft -= ServiceLocator.getTimeSource().getDeltaTime();
      if (!isRolling()) {
        entity.getEvents().trigger("rollEnd");
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

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();
    float speedMultiplier = isSprinting ? SPRINT_MULTIPLIER : 1f;
    float desiredVelocityX = walkDirection.x * MAX_SPEED.x * speedMultiplier;

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

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    if (paused) {
      stopWalking();
    } else {
      this.walkDirection = direction;
      if (direction.x != 0) {
        facing = Math.signum(direction.x);
      }
      moving = true;
    }
  }

  /**
   * Dashes forward along the ground, briefly invulnerable — used to get past hazards like spikes.
   */
  void roll() {
    if (isRolling() || !isGrounded || isGrappling()) {
      return;
    }
    rollTimeLeft = ROLL_DURATION;
    Body body = physicsComponent.getBody();
    body.setLinearVelocity(facing * ROLL_SPEED, body.getLinearVelocity().y);
    if (combatStats != null) {
      combatStats.grantInvulnerability((long) (ROLL_DURATION * 1000f));
    }
    entity.getEvents().trigger("rollStart");
  }

  boolean isRolling() {
    return rollTimeLeft > 0f;
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
    Body body = physicsComponent.getBody();

    if (isGrappling()) {
      grapple.release();
      body.applyLinearImpulse(
          new Vector2(0, JUMP_FORCE * ROPE_JUMP_MULTIPLIER), body.getWorldCenter(), true);
      return;
    }

    if (isGrounded) {
      body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
      isGrounded = false;
      entity.getEvents().trigger("jumpStart");
    }
  }

  void sprint() {
    this.isSprinting = true;
    if (!isGrappling()) {
      updateSpeed();
    }
  }

  void stopSprinting() {
    this.isSprinting = false;
    if (!isGrappling()) {
      updateSpeed();
    }
  }
}
