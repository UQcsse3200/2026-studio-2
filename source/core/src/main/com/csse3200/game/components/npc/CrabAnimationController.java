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

  @Override
  public void create() {
    super.create();
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("wanderStart", this::animateWalk);
    entity.getEvents().addListener("chaseStart", this::animateWalk);
    entity.getEvents().addListener("attackStart", this::animateAttack);
  }

  void animateWalk() {
    animator.startAnimation("walk");
  }

  void animateAttack() {
    animator.startAnimation("attack");
  }
}
