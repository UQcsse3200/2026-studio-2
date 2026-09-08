package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * This class listens to events relevant to the player's movement state and plays the corresponding
 * animation when one of the events is triggered.
 */
public class PlayerAnimationController extends Component {
  private AnimationRenderComponent animator;
  private boolean moving = false;
  private boolean sprinting = false;
  private boolean jumping = false;
  private boolean hurt = false;
  private boolean dead = false;
  private boolean deathAnimationFinishedFired = false;

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::walkStop);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::sprintStop);
    entity.getEvents().addListener("jumpStart", this::jumpStart);
    entity.getEvents().addListener("hurt", this::hurt);
    entity.getEvents().addListener("death", this::death);

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
    if (!jumping) {
      updateAnimation();
    }
  }

  void walkStop() {
    if (dead) {
      return;
    }
    moving = false;
    if (!jumping) {
      updateAnimation();
    }
  }

  void sprint() {
    if (dead) {
      return;
    }
    sprinting = true;
    if (!jumping) {
      updateAnimation();
    }
  }

  void sprintStop() {
    if (dead) {
      return;
    }
    sprinting = false;
    if (!jumping) {
      updateAnimation();
    }
  }

  void jumpStart() {
    if (dead) {
      return;
    }
    jumping = true;
    animator.startAnimation("jump");
  }

  void hurt() {
    if (dead) {
      return;
    }
    jumping = false;
    hurt = true;
    animator.startAnimation("hurt");
  }

  void death() {
    dead = true;
    animator.startAnimation("death");
  }

  private void updateAnimation() {
    String desired = moving ? (sprinting ? "sprint" : "walk") : "idle";
    if (!desired.equals(animator.getCurrentAnimation())) {
      animator.startAnimation(desired);
    }
  }
}
