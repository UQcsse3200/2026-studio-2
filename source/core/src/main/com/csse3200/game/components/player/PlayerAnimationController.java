package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class PlayerAnimationController extends Component {
  private AnimationRenderComponent animator;
  private boolean moving = false;
  private boolean sprinting = false;
  private boolean jumping = false;
  private boolean dashing = false;
  private boolean hurt = false;

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::walkStop);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::sprintStop);
    entity.getEvents().addListener("jumpStart", this::jumpStart);
    entity.getEvents().addListener("dashStart", this::dashStart);
    entity.getEvents().addListener("airDashStart", this::airDashStart);
    entity.getEvents().addListener("hurt", this::hurt);
    entity.getEvents().addListener("sprintEnd", this::sprintStop);

    animator.startAnimation("idle");
  }

  @Override
  public void update() {
    if (hurt && animator.isFinished()) {
      hurt = false;
      updateAnimation();
    } else if (dashing && animator.isFinished()) {
      dashing = false;
      updateAnimation();
    } else if (jumping && animator.isFinished()) {
      jumping = false;
      updateAnimation();
    }
  }

  void walk(Vector2 direction) {
    moving = true;
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
    }
    if (!jumping && !dashing) {
      updateAnimation();
    }
  }

  void walkStop() {
    moving = false;
    if (!jumping && !dashing) {
      updateAnimation();
    }
  }

  void sprint() {
    sprinting = true;
    if (!jumping && !dashing) {
      updateAnimation();
    }
  }

  void sprintStop() {
    sprinting = false;
    if (!jumping && !dashing) {
      updateAnimation();
    }
  }

  void jumpStart() {
    if (dashing) {
      return;
    }
    jumping = true;
    animator.startAnimation("jump");
  }

  void dashStart() {
    jumping = false;
    dashing = true;
    animator.startAnimation("dash");
  }

  void airDashStart() {
    jumping = false;
    dashing = true;
    animator.startAnimation("air_dash");
  }

  void hurt() {
    jumping = false;
    dashing = false;
    hurt = true;
    animator.startAnimation("hurt");
  }

  private void updateAnimation() {
    String desired = moving ? (sprinting ? "sprint" : "walk") : "idle";
    if (!desired.equals(animator.getCurrentAnimation())) {
      animator.startAnimation(desired);
    }
  }
}
