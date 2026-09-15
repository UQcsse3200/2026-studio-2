package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * This class listens to events relevant to a skeleton entity's state and plays the animation when
 * one of the events is triggered.
 */
public class SkeletonAnimationController extends Component {
  private static final float MOVING_SPEED_THRESHOLD = 0.1f;
  private static final float FACING_DEADZONE = 0.05f;
  private final Entity target;
  private AnimationRenderComponent animator;
  private PhysicsComponent physics;

  public SkeletonAnimationController(Entity target) {
    this.target = target;
  }

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    physics = this.entity.getComponent(PhysicsComponent.class);
    animateIdle();
  }

  @Override
  public void update() {
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
    if (!"walk".equals(animator.getCurrentAnimation())) {
      animator.startAnimation("walk");
    }
  }

  void animateIdle() {
    if (!"idle".equals(animator.getCurrentAnimation())) {
      animator.startAnimation("idle");
    }
  }
}
