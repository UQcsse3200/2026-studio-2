package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * This class listens to events relevant to a skeleton entity's state and plays the animation when
 * one of the events is triggered.
 */
public class EnemyAnimationController extends Component {
  private static final String LASER_ANIMATION = "laser";

  private static final float MOVING_SPEED_THRESHOLD = 0.1f;
  private static final float FACING_DEADZONE = 0.05f;
  private final Entity target;
  private AnimationRenderComponent animator;
  private PhysicsComponent physics;
  private boolean isAttacking = false;

  public EnemyAnimationController(Entity target) {
    this.target = target;
  }

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    physics = this.entity.getComponent(PhysicsComponent.class);
    animateIdle();

    entity.getEvents().addListener("laserStart", this::animateLaser);
    entity.getEvents().addListener("attackStart", this::animateSweep);
    entity.getEvents().addListener("hurt", this::animateHurt);
  }

  @Override
  public void update() {
    if (isAttacking) {
      if (animator.isFinished()) {
        isAttacking = false;
        animateIdle();
      }
      return;
    }
    faceTarget();

    if (isMoving()) {
      animateWalk();
    } else {
      animateIdle();
    }
  }

  private void faceTarget() {
    if (target == null) {
      return;
    }

    float dx = target.getCenterPosition().x - this.entity.getCenterPosition().x;

    if (Math.abs(dx) > FACING_DEADZONE) {
      animator.setFlipX(dx < 0f);
    }
  }

  private boolean isMoving() {
    if (physics == null || physics.getBody() == null) {
      return false;
    }
    return Math.abs(physics.getBody().getLinearVelocity().x) > MOVING_SPEED_THRESHOLD;
  }

  void animateWalk() {
    if (!animator.hasAnimation("walk")) {
      animateIdle();
      return;
    }
    if (!"walk".equals(animator.getCurrentAnimation())) {
      animator.startAnimation("walk");
    }
  }

  void animateIdle() {
    if (!"idle".equals(animator.getCurrentAnimation())) {
      animator.startAnimation("idle");
    }
  }

  void animateSweep() {
    if ("hurt".equals(animator.getCurrentAnimation()) && !animator.isFinished()) {
      return;
    }
    String name = animator.hasAnimation("sweep") ? "sweep" : "attack";
    if (!animator.hasAnimation(name)) {
      return;
    }
    isAttacking = true;
    animator.startAnimation(name);
  }

  void animateHurt() {
    if (!animator.hasAnimation("hurt")) {
      return;
    }

    if (LASER_ANIMATION.equals(animator.getCurrentAnimation()) && !animator.isFinished()) {
      return;
    }

    if ("hurt".equals(animator.getCurrentAnimation()) && !animator.isFinished()) {
      return;
    }
    isAttacking = true;
    animator.startAnimation("hurt");
  }

  void animateLaser() {
    if (!animator.hasAnimation(LASER_ANIMATION)) {
      return;
    }
    isAttacking = true;
    animator.startAnimation(LASER_ANIMATION);
  }
}
