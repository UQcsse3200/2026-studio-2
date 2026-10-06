package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class PlayerAnimationController extends Component {
  private AnimationRenderComponent animator;
  private boolean moving = false;
  private boolean sprinting = false;
  private boolean jumping = false;
  private boolean falling = false;
  private boolean landing = false;
  private boolean dashing = false;
  private boolean hurt = false;
  private boolean attacking = false;
  private boolean dead = false;
  private boolean deathAnimationFinishedFired = false;
  private boolean charging = false;
  private boolean drawingIn = false;
  // True for the whole bow sequence (draw -> hold -> shoot). While set, every other animation is
  // suppressed so a shot can't be visually interrupted part way through. Death is the exception.
  private boolean bowActive = false;

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::walkStop);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::sprintStop);
    entity.getEvents().addListener("jumpStart", this::jumpStart);
    entity.getEvents().addListener("fallStart", this::fallStart);
    entity.getEvents().addListener("landed", this::landed);
    entity.getEvents().addListener("grappleAttached", this::grappleAttached);
    entity.getEvents().addListener("dashStart", this::dashStart);
    entity.getEvents().addListener("airDashStart", this::airDashStart);
    entity.getEvents().addListener("hurt", this::hurt);
    entity.getEvents().addListener("chargeStart", this::drawStart);
    entity.getEvents().addListener("chargeRelease", this::drawRelease);
    entity.getEvents().addListener("chargeCancel", this::drawCancel);
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
    } else if (drawingIn && animator.isFinished()) {
      // The one-shot draw-back has finished pulling the string - settle into the looping hold.
      drawingIn = false;
      animator.startAnimation("bow_hold");
    } else if (attacking && animator.isFinished()) {
      // Also where bow_shoot lands, which is the end of the bow sequence.
      attacking = false;
      bowActive = false;
      updateAnimation();
    } else if (landing && animator.isFinished()) {
      landing = false;
      updateAnimation();
    }
    // Note: the takeoff clip deliberately has no "finished" branch. It is NORMAL mode, so it holds
    // its final tucked frame through the rest of the ascent until the fall or landing takes over.
  }

  void walk(Vector2 direction) {
    if (dead) {
      return;
    }
    moving = true;
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
    }
    if (!jumping && !dashing && !attacking && !landing) {
      updateAnimation();
    }
  }

  void walkStop() {
    if (dead) {
      return;
    }
    moving = false;
    if (!jumping && !dashing && !attacking && !landing) {
      updateAnimation();
    }
  }

  void sprint() {
    if (dead) {
      return;
    }
    sprinting = true;
    if (!jumping && !dashing && !attacking && !landing) {
      updateAnimation();
    }
  }

  void sprintStop() {
    if (dead) {
      return;
    }
    sprinting = false;
    if (!jumping && !dashing && !attacking && !landing) {
      updateAnimation();
    }
  }

  void jumpStart() {
    if (dead || bowActive) {
      return;
    }
    if (dashing) {
      return;
    }
    landing = false;
    jumping = true;
    animator.startAnimation("jump_takeoff");
  }

  /**
   * The player has started descending - whether from a jump, walking off a ledge or dropping
   * through a platform. The looping fall carries on until they touch down.
   */
  void fallStart() {
    if (dead) {
      return;
    }
    jumping = false;
    landing = false;
    falling = true;
    if (isBusyWithHigherPriorityAnimation()) {
      // Recorded only. updateAnimation() picks the fall up once the current clip finishes.
      return;
    }
    animator.startAnimation("jump_fall");
  }

  /** Touchdown. Plays a short recovery, but only if the player was visibly in the air. */
  void landed() {
    if (dead) {
      return;
    }
    boolean wasAirborne = jumping || falling;
    jumping = false;
    falling = false;
    if (!wasAirborne) {
      // A one-frame blip in the ground raycast, e.g. crossing a seam between tiles. Ignore it
      // rather than punching a landing crouch into the middle of a run.
      return;
    }
    if (isBusyWithHigherPriorityAnimation()) {
      return;
    }
    landing = true;
    animator.startAnimation("jump_land");
  }

  /**
   * Latching onto a rope ends the air sequence without a landing - the player is swinging now, so
   * the fall loop shouldn't keep playing underneath them.
   */
  void grappleAttached() {
    if (dead || (!jumping && !falling)) {
      return;
    }
    jumping = false;
    falling = false;
    if (isBusyWithHigherPriorityAnimation()) {
      return;
    }
    updateAnimation();
  }

  /**
   * @return whether a clip that outranks the jump stages is currently playing.
   */
  private boolean isBusyWithHigherPriorityAnimation() {
    return bowActive || dashing || hurt || attacking;
  }

  void dashStart() {
    if (dead || bowActive) {
      return;
    }
    jumping = false;
    attacking = false; // dash cancels the attack
    dashing = true;
    animator.startAnimation("air_dash");
  }

  void airDashStart() {
    dashStart();
  }

  void hurt() {
    if (dead || bowActive) {
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
    // Death outranks everything, including the bow sequence and the jump stages.
    charging = false;
    drawingIn = false;
    bowActive = false;
    jumping = false;
    falling = false;
    landing = false;
    animator.startAnimation("death");
  }

  void sleep() {
    animator.startAnimation("sleep");
  }

  void drawStart(Vector2 aim) {
    if (dead) {
      return;
    }
    charging = true;
    drawingIn = true;
    attacking = true;
    bowActive = true;
    if (aim != null && aim.x != 0) {
      animator.setFlipX(aim.x < 0);
    }
    animator.startAnimation("bow_draw");
  }

  void drawRelease(Vector2 aim) {
    if (!charging) {
      return;
    }
    charging = false;
    drawingIn = false;
    if (dead) {
      bowActive = false;
      return;
    }
    animator.startAnimation("bow_shoot");
  }

  void drawCancel() {
    if (dead || !bowActive) {
      return;
    }
    charging = false;
    drawingIn = false;
    attacking = false;
    bowActive = false;
    updateAnimation();
  }

  private void updateAnimation() {
    String desired = "idle";
    if (falling) {
      // Still in the air: anything finishing mid-fall resumes the fall rather than idling.
      desired = "jump_fall";
    } else if (moving) {
      desired = sprinting ? "sprint" : "walk";
    }
    if (!desired.equals(animator.getCurrentAnimation())) {
      animator.startAnimation(desired);
    }
  }

  public void playAnimation(String animationName) {
    animator.startAnimation(animationName);
  }

  public AnimationRenderComponent getAnimator() {
    return this.animator;
  }
}
