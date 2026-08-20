package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

public class PlayerActions extends Component {
  private static final float JUMP_FORCE = 5f;

  private PhysicsComponent physicsComponent;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean isGrounded = false;

  private static final Vector2 MAX_SPEED = new Vector2(3f, 3f);
  private static final float SPRINT_MULTIPLIER = 1.75f; // adjust to taste
  private boolean isSprinting = false;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
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
    if (moving) {
      updateSpeed();
    }
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();
    float speedMultiplier = isSprinting ? SPRINT_MULTIPLIER : 1f;
    float desiredVelocityX = walkDirection.x * MAX_SPEED.x * speedMultiplier;
    float impulseX = (desiredVelocityX - velocity.x) * body.getMass();
    body.applyLinearImpulse(new Vector2(impulseX, 0), body.getWorldCenter(), true);
  }

  private boolean checkGrounded() {
    Vector2 position = entity.getCenterPosition();
    Vector2 rayEnd = position.cpy().sub(0, 0.1f);
    RaycastHit hit = new RaycastHit();
    return ServiceLocator.getPhysicsService().getPhysics()
            .raycast(position, rayEnd, PhysicsLayer.GROUND, hit);
  }

  void walk(Vector2 direction) {
    this.walkDirection = direction;
    moving = true;
  }

  void stopWalking() {
    this.walkDirection = Vector2.Zero.cpy();
    updateSpeed();
    moving = false;
  }

  void jump() {
    if (isGrounded) {
      Body body = physicsComponent.getBody();
      body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
      isGrounded = false;
    }
  }

  void sprint() {
    this.isSprinting = true;
    updateSpeed();
  }

  void stopSprinting() {
    this.isSprinting = false;
    updateSpeed();
  }

  void attack() {
    Sound attackSound =
            ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();
  }
}
