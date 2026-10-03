package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Checks event-driven transitions with the real animation clock and controller. */
@ExtendWith(GameExtension.class)
class PlayerAnimationTransitionsTest {
  private Entity player;
  private AnimationRenderComponent animator;
  private PlayerAnimationController controller;
  private GameTime time;

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(time);
    ServiceLocator.registerEntityService(new EntityService());
    TextureAtlas atlas = mock(TextureAtlas.class);
    animator = new AnimationRenderComponent(atlas);
    for (String name :
        new String[] {
          "idle",
          "walk",
          "sprint",
          "jump",
          "air_dash",
          "hurt",
          "melee",
          "death",
          "bow_draw",
          "bow_hold",
          "bow_shoot"
        }) {
      Array<AtlasRegion> frames = new Array<>();
      frames.add(mock(AtlasRegion.class));
      when(atlas.findRegions(name)).thenReturn(frames);
      boolean looping =
          name.equals("idle")
              || name.equals("walk")
              || name.equals("sprint")
              || name.equals("bow_hold");
      animator.addAnimation(name, 1f, looping ? PlayMode.LOOP : PlayMode.NORMAL);
    }
    controller = new PlayerAnimationController();
    player = new Entity().addComponent(animator).addComponent(controller);
    controller.create();
  }

  private void finishClip() {
    animator.render(mock(SpriteBatch.class));
    controller.update();
  }

  @Test
  void shouldTransitionBetweenIdleWalkAndSprintAndKeepFacingOnVerticalInput() {
    player.getEvents().trigger("sprint");
    assertEquals("idle", animator.getCurrentAnimation());
    player.getEvents().trigger("walk", new Vector2(-1f, 0f));
    assertEquals("sprint", animator.getCurrentAnimation());
    assertTrue(animator.isFlipX());
    player.getEvents().trigger("walk", Vector2.Y);
    assertTrue(animator.isFlipX());
    player.getEvents().trigger("sprintStop");
    assertEquals("walk", animator.getCurrentAnimation());
    player.getEvents().trigger("walk", Vector2.X);
    assertFalse(animator.isFlipX());
    player.getEvents().trigger("walkStop");
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldKeepJumpUntilCompletionAndThenUseLatestMovementState() {
    player.getEvents().trigger("jumpStart");
    controller.update();
    assertEquals("jump", animator.getCurrentAnimation());
    player.getEvents().trigger("walk", Vector2.X);
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("walkStop");
    player.getEvents().trigger("sprintStop");
    assertEquals("jump", animator.getCurrentAnimation());
    finishClip();
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldKeepDashOverJumpAndResumeSprintAfterCompletion() {
    player.getEvents().trigger("walk", Vector2.X);
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("dashStart");
    player.getEvents().trigger("jumpStart");
    controller.update();
    assertEquals("air_dash", animator.getCurrentAnimation());
    finishClip();
    assertEquals("sprint", animator.getCurrentAnimation());
    player.getEvents().trigger("sprintEnd");
    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldRecoverFromAirDashToIdleAfterMovementStops() {
    player.getEvents().trigger("walk", Vector2.X);
    player.getEvents().trigger("airDashStart");
    player.getEvents().trigger("walkStop");
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("sprintStop");
    assertEquals("air_dash", animator.getCurrentAnimation());
    finishClip();
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldRecoverFromHurtToPreviousMovement() {
    player.getEvents().trigger("walk", Vector2.X);
    player.getEvents().trigger("hurt");
    controller.update();
    assertEquals("hurt", animator.getCurrentAnimation());
    finishClip();
    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldFaceMeleeAimAndResumeLocomotionOnlyAfterCompletion() {
    player.getEvents().trigger("melee", new Vector2(-1f, 0f));
    assertTrue(animator.isFlipX());
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("walk", new Vector2(-1f, 0f));
    assertEquals("melee", animator.getCurrentAnimation());
    finishClip();
    assertEquals("sprint", animator.getCurrentAnimation());
    player.getEvents().trigger("melee", Vector2.X);
    assertFalse(animator.isFlipX());
    player.getEvents().trigger("melee", Vector2.Y);
    assertFalse(animator.isFlipX());
  }

  @Test
  void shouldCancelBowBackToSprintAndAllowNextJump() {
    player.getEvents().trigger("walk", Vector2.X);
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("chargeStart", new Vector2(-1f, 0f));
    assertTrue(animator.isFlipX());
    player.getEvents().trigger("chargeCancel");
    assertEquals("sprint", animator.getCurrentAnimation());
    player.getEvents().trigger("chargeRelease", Vector2.X);
    assertEquals("sprint", animator.getCurrentAnimation());
    player.getEvents().trigger("jumpStart");
    assertEquals("jump", animator.getCurrentAnimation());
    player.getEvents().trigger("chargeCancel");
    assertEquals("jump", animator.getCurrentAnimation());
  }

  @Test
  void shouldIgnoreRemainingMovementAndAttackEventsAfterDeath() {
    player.getEvents().trigger("death");
    player.getEvents().trigger("walkStop");
    player.getEvents().trigger("sprintStop");
    player.getEvents().trigger("dashStart");
    player.getEvents().trigger("airDashStart");
    player.getEvents().trigger("melee", Vector2.X);
    player.getEvents().trigger("chargeCancel");
    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotRestartMeleeOnRepeatedIdleUpdateAfterCompletion() {
    player.getEvents().trigger("melee", Vector2.X);
    finishClip();
    assertEquals("idle", animator.getCurrentAnimation());
    controller.update();
    controller.update();
    assertEquals("idle", animator.getCurrentAnimation());
  }
}
