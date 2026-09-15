package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;

public class MovingPlatformComponent extends PlatformGrappleComponent {
  private final Vector2 firstTarget;
  private final Vector2 secondTarget;
  private final Vector2 maxSpeed;
  private Vector2 currentSpeed;
  private PhysicsMovementComponent movementComponent;
  private PhysicsComponent physicsComponent;
  private ActivatableComponent activatableComponent;
  private boolean initialised = false;

  /**
   * Constructor for a new MovingPlatformComponent
   *
   * @param grappleSides the base 10 integer representing which sides can be grappled to
   * @param firstTarget the first coordinate the platform can reach before reversing direction
   * @param secondTarget the second coordinate the platform can reach before reversing direction
   * @param maxSpeed the maximum speed of the moving platform
   */
  public MovingPlatformComponent(
      int grappleSides, Vector2 firstTarget, Vector2 secondTarget, Vector2 maxSpeed) {
    super(grappleSides);
    this.firstTarget = firstTarget;
    this.secondTarget = secondTarget;
    this.maxSpeed = maxSpeed;
  }

  @Override
  /** Create the moving platform and assign it an initial target. */
  public void create() {
    movementComponent = entity.getComponent(PhysicsMovementComponent.class);
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    activatableComponent = entity.getComponent(ActivatableComponent.class);

    boolean active = activatableComponent.isActive();
    this.currentSpeed = active ? maxSpeed : new Vector2();

    entity.getEvents().addListener("activatedMapComponent", this::activate);
  }

  @Override
  /**
   * Updates the target and speed of the moving platform once it reaches one of its targets. Upon
   * reaching its first target, its speed is reversed and its target is set to the second target.
   */
  public void update() {
    if (!initialised) {
      boolean active = activatableComponent.isActive();
      movementComponent.setMoving(active);
      movementComponent.setTarget(firstTarget);
      initialised = true;
    }

    if (movementComponent.getMoving() && movementComponent.getTarget() != null) {
      Body body = physicsComponent.getBody();
      Vector2 currentPosition = body.getPosition();

      // if moving horizontally
      if (currentSpeed.y == 0) {
        if (currentPosition.x <= firstTarget.x) {
          movementComponent.setTarget(secondTarget);
        } else if (currentPosition.x >= secondTarget.x) {
          movementComponent.setTarget(firstTarget);
        }
        // reverse direction
        Vector2 velocity = getDirection().scl(maxSpeed.x);
        body.setLinearVelocity(velocity);
      }
      // if moving vertically
      if (currentSpeed.x == 0) {
        if (currentPosition.y <= firstTarget.y) {
          movementComponent.setTarget(secondTarget);
        } else if (currentPosition.y >= secondTarget.y) {
          movementComponent.setTarget(firstTarget);
        }
        // reverse direction
        Vector2 velocity = getDirection().scl(maxSpeed.y);
        body.setLinearVelocity(velocity);
      }
    }
  }

  /**
   * Gets the normalised direction of the moving platform. e.g. (-1, 0) for left and (1, 0) for
   * right
   *
   * @return normalised direction of movement.
   */
  private Vector2 getDirection() {
    // Move towards targetPosition based on our current position
    return movementComponent.getTarget().cpy().sub(entity.getPosition()).nor();
  }

  /**
   * Updates the current speed of the platform based on that activation state received through a
   * trigger
   *
   * @param active whether the moving platform is now active
   */
  private void activate(boolean active) {
    movementComponent.setMoving(active); // update internal moving reference
    currentSpeed = active ? maxSpeed : new Vector2();

    if (!active) {
      Body body = physicsComponent.getBody();
      body.setLinearVelocity(0f, 0f); // override force the body's velocity to exactly 0
    }
  }
}
