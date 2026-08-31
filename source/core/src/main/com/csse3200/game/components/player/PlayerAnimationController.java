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

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::walkStop);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::sprintStop);
    entity.getEvents().addListener("jumpStart", this::jumpStart);

    animator.startAnimation("idle");
  }

  @Override
  public void update() {
    if (jumping && animator.isFinished()) {
      jumping = false;
      updateAnimation();
    }
  }

  void walk(Vector2 direction) {
    moving = true;
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
    }
    if (!jumping) {
      updateAnimation();
    }
  }

  void walkStop() {
    moving = false;
    if (!jumping) {
      updateAnimation();
    }
  }

  void sprint() {
    sprinting = true;
    if (!jumping) {
      updateAnimation();
    }
  }

  void sprintStop() {
    sprinting = false;
    if (!jumping) {
      updateAnimation();
    }
  }

  void jumpStart() {
    jumping = true;
    animator.startAnimation("jump");
  }

  private void updateAnimation() {
    String desired = moving ? (sprinting ? "sprint" : "walk") : "idle";
    if (!desired.equals(animator.getCurrentAnimation())) {
      animator.startAnimation(desired);
    }
  }
}
