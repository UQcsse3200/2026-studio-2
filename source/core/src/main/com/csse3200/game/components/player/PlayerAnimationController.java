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
  private boolean attacking = false;
  private boolean dead = false;
  private boolean deathAnimationFinishedFired = false;
  private boolean sleep = false;

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
    entity.getEvents().addListener("melee", this::meleeStart);
    entity.getEvents().addListener("sprintEnd", this::sprintStop);
    entity.getEvents().addListener("death", this::death);
    entity.getEvents().addListener("sleep", this::sleep);

    animator.startAnimation("idle");
  }

  @Override
  public void update() {
    if (dead) {
      if (!deathAnimationFinishedFired && animator.isFinished()) {
        deathAnimationFinishedFired = true;
        entity.getEvents().trigger("deathAnimationFinished");
      }
      return;
    }
    if (hurt && animator.isFinished()) {
      hurt = false;
      updateAnimation();
    } else if (dashing && animator.isFinished()) {
      dashing = false;
      updateAnimation();
    } else if (attacking && animator.isFinished()) {
      attacking = false;
      updateAnimation();
    } else if (jumping && animator.isFinished()) {
      jumping = false;
      updateAnimation();
    }
  }

  void walk(Vector2 direction) {
    if (dead) {
      return;
    }
    moving = true;
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
    }
    if (!jumping && !dashing && !attacking) {
      updateAnimation();
    }
  }

  void walkStop() {
    if (dead) {
      return;
    }
    moving = false;
    if (!jumping && !dashing && !attacking) {
      updateAnimation();
    }
  }

  void sprint() {
    if (dead) {
      return;
    }
    sprinting = true;
    if (!jumping && !dashing && !attacking) {
      updateAnimation();
    }
  }

  void sprintStop() {
    if (dead) {
      return;
    }
    sprinting = false;
    if (!jumping && !dashing && !attacking) {
      updateAnimation();
    }
  }

  void jumpStart() {
    if (dead) {
      return;
    }
    if (dashing) {
      return;
    }
    jumping = true;
    animator.startAnimation("jump");
  }

  void dashStart() {
    jumping = false;
    attacking = false;      // dash cancels the attack
    dashing = true;
    animator.startAnimation("air_dash");
  }

  void airDashStart() {
    jumping = false;
    attacking = false;
    dashing = true;
    animator.startAnimation("air_dash");
  }

  void hurt() {
    if (dead) {
      return;
    }
    jumping = false;
    dashing = false;
    attacking = false;
    hurt = true;
    animator.startAnimation("hurt");
  }

  void death() {
    dead = true;
    animator.startAnimation("death");
  }

  void sleep() {
    sleep = true;
    animator.startAnimation("sleep");
  }

  void meleeStart(Vector2 aim) {
    attacking = true;
    if (aim.x != 0) {
      animator.setFlipX(aim.x < 0);
    }
    animator.startAnimation("melee");
  }

  private void updateAnimation() {
    String desired = moving ? (sprinting ? "sprint" : "walk") : "idle";
    if (!desired.equals(animator.getCurrentAnimation())) {
      animator.startAnimation(desired);
    }
  }
}
