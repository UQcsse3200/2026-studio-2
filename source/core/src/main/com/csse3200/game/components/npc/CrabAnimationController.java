package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * Listens to events relevant to a crab enemy's state and plays the matching animation: scuttling
 * ("walk") while wandering or chasing, and a claw-snap-and-wave ("attack") each time it lands a
 * hit.
 */
public class CrabAnimationController extends Component {
  AnimationRenderComponent animator;
  private boolean attacking = false;

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("wanderStart", this::animateWalk);
    entity.getEvents().addListener("chaseStart", this::animateWalk);
    entity.getEvents().addListener("attackStart", this::animateAttack);
  }

  @Override
  public void update() {
    // The attack anim plays once (PlayMode.NORMAL) and holds its last frame; without this the crab
    // would freeze there forever instead of resuming its scuttle between swings.
    if (attacking && animator.isFinished()) {
      attacking = false;
      animateWalk();
    }
  }

  void animateWalk() {
    if (!attacking) {
      animator.startAnimation("walk");
    }
  }

  void animateAttack() {
    attacking = true;
    animator.startAnimation("attack");
  }
}
