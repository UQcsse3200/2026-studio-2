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
  private boolean rolling = false;

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::walkStop);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::sprintStop);
    entity.getEvents().addListener("jumpStart", this::jumpStart);
    entity.getEvents().addListener("rollStart", this::rollStart);
    entity.getEvents().addListener("rollEnd", this::rollEnd);
    entity.getEvents().addListener("hurt", this::hurt);

    animator.startAnimation("idle");
  }

  @Override
  public void update() {
    if (hurt && animator.isFinished()) {
      hurt = false;
      updateAnimation();
    } else if (jumping && animator.isFinished()) {
      jumping = false;
      updateAnimation();
    }
  }

  private boolean locked() {
    return jumping || rolling;
  }

  void walk(Vector2 direction) {
    moving = true;
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
    }
    if (!locked()) {
      updateAnimation();
    }
  }

  void walkStop() {
    moving = false;
    if (!locked()) {
      updateAnimation();
    }
  }

  void sprint() {
    sprinting = true;
    if (!locked()) {
      updateAnimation();
    }
  }

  void sprintStop() {
    sprinting = false;
    if (!locked()) {
      updateAnimation();
    }
  }

  void jumpStart() {
    jumping = true;
    animator.startAnimation("jump");
  }

  void rollStart() {
    rolling = true;
    // The roll animation is optional until its art is added; without it the current animation
    // simply keeps playing through the dash.
    if (animator.hasAnimation("roll")) {
      animator.startAnimation("roll");
    }
  }

  void rollEnd() {
    rolling = false;
    updateAnimation();
  }

  void hurt() {
    jumping = false;
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
