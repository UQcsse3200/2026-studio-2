package com.csse3200.game.components.npc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** A ghost floats about calmly, and floats angrily when it is chasing. */
@ExtendWith(GameExtension.class)
class GhostAnimationControllerTest {
  private AnimationRenderComponent animator;
  private Entity ghost;

  @BeforeEach
  void setUp() {
    animator = mock(AnimationRenderComponent.class);
    ghost = new Entity().addComponent(animator).addComponent(new GhostAnimationController());
    ghost.create();
  }

  @Test
  void shouldNotAnimateAnythingOnItsOwn() {
    verify(animator, never()).startAnimation(anyString());
  }

  @Test
  void shouldFloatCalmlyWhenItStartsWandering() {
    ghost.getEvents().trigger("wanderStart");

    verify(animator).startAnimation("float");
    verify(animator, never()).startAnimation("angry_float");
  }

  @Test
  void shouldFloatAngrilyWhenItStartsChasing() {
    ghost.getEvents().trigger("chaseStart");

    verify(animator).startAnimation("angry_float");
    verify(animator, never()).startAnimation("float");
  }

  @Test
  void shouldSwitchBetweenTheTwoAsItsBehaviourChanges() {
    ghost.getEvents().trigger("wanderStart");
    ghost.getEvents().trigger("chaseStart");
    ghost.getEvents().trigger("wanderStart");

    verify(animator, times(2)).startAnimation("float");
    verify(animator, times(1)).startAnimation("angry_float");
  }
}
