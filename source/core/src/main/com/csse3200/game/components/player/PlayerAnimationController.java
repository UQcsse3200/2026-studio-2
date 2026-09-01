package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * This class listens to events relevant to the player's movement state and plays the corresponding
 * animation when one of the events is triggered.
 */
public class PlayerAnimationController extends Component {
  /**
   * How much larger the player renders while the bow is drawn or being released.
   */
  private static final float BOW_SCALE = 1.15f;

  private AnimationRenderComponent animator;
  private Vector2 baseScale;
  private boolean moving = false;
  private boolean sprinting = false;
  private boolean jumping = false;
  private boolean hurt = false;
  private boolean attacking = false;
  private boolean drawingBow = false;
  private boolean holdingBow = false;
  private boolean releasingBow = false;

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    baseScale = entity.getScale().cpy();
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::walkStop);
    entity.getEvents().addListener("sprint", this::sprint);
    entity.getEvents().addListener("sprintStop", this::sprintStop);
    entity.getEvents().addListener("jumpStart", this::jumpStart);
    entity.getEvents().addListener("hurt", this::hurt);
    entity.getEvents().addListener("melee", this::meleeAttack);
    entity.getEvents().addListener("attackChargeStart", this::bowDrawStart);
    entity.getEvents().addListener("attackAnimation", this::bowRelease);
    entity.getEvents().addListener("attackCancelled", this::bowCancel);

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
    } else if (releasingBow && animator.isFinished()) {
      releasingBow = false;
      restoreScale();
      updateAnimation();
    }
  }

  void walk(Vector2 direction) {
    moving = true;
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
    }
    if (!jumping && !drawingBow && !releasingBow) {
      updateAnimation();
    }
  }

  void walkStop() {
    moving = false;
    if (!jumping && !drawingBow && !releasingBow) {
      updateAnimation();
    }
  }

  void sprint() {
    sprinting = true;
    if (!jumping && !drawingBow && !releasingBow) {
      updateAnimation();
    }
  }

  void sprintStop() {
    sprinting = false;
    if (!jumping && !drawingBow && !releasingBow) {
      updateAnimation();
    }
  }

  void jumpStart() {
    jumping = true;
    drawingBow = false;
    releasingBow = false;
    restoreScale();
    animator.startAnimation("jump");
  }

  void hurt() {
    jumping = false;
    hurt = true;
    drawingBow = false;
    releasingBow = false;
    restoreScale();
    animator.startAnimation("hurt");
  }

  void meleeAttack() {
    jumping = false;
    hurt = false;
    attacking = true;
    drawingBow = false;
    releasingBow = false;
    restoreScale();
    animator.startAnimation("melee");
  }

  /**
   * Bow is being drawn/held - freezes on the last "bowDraw" frame until release or cancel.
   */
  void bowDrawStart() {
    jumping = false;
    hurt = false;
    drawingBow = true;
    releasingBow = false;
    applyBowScale();
    animator.startAnimation("bowDraw");
  }

  /**
   * Arrow has been fired - plays the short release animation, then falls back to idle/walk.
   */
  void bowRelease(Vector2 direction) {
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
    }
    drawingBow = false;
    releasingBow = true;
    applyBowScale();
    animator.startAnimation("bowRelease");
  }

  /**
   * Draw was cancelled (cooldown or no valid direction) with no shot fired - snap back to idle.
   */
  void bowCancel() {
    drawingBow = false;
    releasingBow = false;
    restoreScale();
    updateAnimation();
  }

  private void applyBowScale() {
    entity.setScale(baseScale.x * BOW_SCALE, baseScale.y * BOW_SCALE);
  }

  private void restoreScale() {
    entity.setScale(baseScale.x, baseScale.y);
  }

  private void updateAnimation() {
    String desired = moving ? (sprinting ? "sprint" : "walk") : "idle";
    if (!desired.equals(animator.getCurrentAnimation())) {
      animator.startAnimation(desired);
    } else if (drawingBow && !holdingBow && animator.isFinished()) {
      holdingBow = true;
      animator.startAnimation("bowHold");
    }
  }
}