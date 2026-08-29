package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
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
  private static final float JUMP_FORCE = 5f;
  private static final Vector2 MAX_SPEED = new Vector2(5f, 5f); // Metres per second
  private static final float SPRINT_MULTIPLIER = 1.75f;
  private static final float ROPE_JUMP_MULTIPLIER = 0.7f;
  private static final float AIR_CONTROL = 0.1f; // How much steering you get mid-air

  private PhysicsComponent physicsComponent;
  private GrappleComponent grapple;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean isGrounded = false;
  private boolean isSprinting = false;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    grapple = entity.getComponent(GrappleComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::stopSprinting);
  }

  @Override
  public void update() {
    isGrounded = checkGrounded();
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
        .raycast(rayStart, rayEnd, PhysicsLayer.GROUND, hit);
  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    this.walkDirection = direction;
    moving = true;
  }

  /** Stops the player from walking. */
  void stopWalking() {
    this.walkDirection = Vector2.Zero.cpy();
    if (!isGrappling()) {
      updateSpeed();
    }
    moving = false;
  }

  /** Makes the player attack. */
  void attack() {
    Sound attackSound =
        ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();
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
