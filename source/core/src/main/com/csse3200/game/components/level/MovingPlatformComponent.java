package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;

public class MovingPlatformComponent extends PlatformGrappleComponent {

  private final Vector2 leftTarget;
  private final Vector2 rightTarget;
  private final Vector2 maxSpeed;
  private PhysicsMovementComponent movementComponent;
  private PhysicsComponent physicsComponent;

  // private static final Logger logger =
  // LoggerFactory.getLogger(com.csse3200.game.physics.components.PhysicsMovementComponent.class);
  // private static final Vector2 maxSpeed = Vector2Utils.ONE;

  /**
   * Constructor for a new MovingPlatformComponent
   *
   * @param grappleSides the base 10 integer representing which sides can be grappled to
   * @param leftTarget the minimum X coordinate the platform can reach before reversing direction
   * @param rightTarget the maximum X coordinate the platform can reach before reversing direction
   * @param maxSpeed the maximum speed of the moving platform
   */
  public MovingPlatformComponent(
      int grappleSides, Vector2 leftTarget, Vector2 rightTarget, Vector2 maxSpeed) {
    super(grappleSides);
    this.leftTarget = leftTarget;
    this.rightTarget = rightTarget;
    this.maxSpeed = maxSpeed;
  }

  @Override
  public void create() {
    movementComponent = entity.getComponent(PhysicsMovementComponent.class);
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    movementComponent.setMoving(true);
    movementComponent.setTarget(leftTarget);
  }

  @Override
  public void update() {
    if (movementComponent.getMoving() && movementComponent.getTarget() != null) {
      Vector2 currentPosition = entity.getPosition();
      Body body = physicsComponent.getBody();

      if (maxSpeed.y == 0) {
        if (currentPosition.x <= leftTarget.x) {
          movementComponent.setTarget(rightTarget);
        } else if (currentPosition.x >= rightTarget.x) {
          movementComponent.setTarget(leftTarget);
        }

        Vector2 velocity = getDirection().scl(maxSpeed.x);
        setToVelocity(body, velocity);
      }
      if (maxSpeed.x == 0) {
        if (currentPosition.y <= leftTarget.y) {
          movementComponent.setTarget(rightTarget);
        } else if (currentPosition.y >= rightTarget.y) {
          movementComponent.setTarget(leftTarget);
        }

        Vector2 velocity = getDirection().scl(maxSpeed.y);
        setToVelocity(body, velocity);
      }

    }
  }

  private void setToVelocity(Body body, Vector2 desiredVelocity) {
    // impulse force = (desired velocity - current velocity) * mass
    Vector2 velocity = body.getLinearVelocity();
    Vector2 impulse = desiredVelocity.cpy().sub(velocity).scl(body.getMass());
    body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
  }

  private Vector2 getDirection() {
    // Move towards targetPosition based on our current position
    return movementComponent.getTarget().cpy().sub(entity.getPosition()).nor();
  }
}
