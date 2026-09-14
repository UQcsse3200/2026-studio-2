package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Covers the player's death-state animation handling: entering it, locking it in, and signalling
 * completion.
 */
@ExtendWith(GameExtension.class)
class PlayerAnimationControllerTest {

  @BeforeEach
  void beforeEach() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(gameTime);

    EntityService entityService = mock(EntityService.class);
    when(entityService.getPaused()).thenReturn(false);
    ServiceLocator.registerEntityService(entityService);
  }

  private static TextureAtlas mockAtlasWithRegions(String... names) {
    TextureAtlas atlas = mock(TextureAtlas.class);
    for (String name : names) {
      Array<AtlasRegion> regions = new Array<>(1);
      regions.add(mock(AtlasRegion.class));
      when(atlas.findRegions(name)).thenReturn(regions);
    }
    return atlas;
  }

  /**
   * Builds a player-like entity with single-frame "death"/"bow_draw"/"bow_shoot" animations (so one
   * draw() call completes them) and a looping "bow_hold", wired up without needing a registered
   * RenderService.
   */
  private PlayerAnimationController createController(
      Entity entity, AnimationRenderComponent animator) {
    animator.addAnimation("idle", 1f, PlayMode.LOOP);
    animator.addAnimation("death", 1f, PlayMode.NORMAL);
    animator.addAnimation("bow_draw", 1f, PlayMode.NORMAL);
    animator.addAnimation("bow_hold", 1f, PlayMode.LOOP);
    animator.addAnimation("bow_shoot", 1f, PlayMode.NORMAL);
    // Registered so priority tests fail loudly if a competing animation is allowed through, rather
    // than silently no-opping because the animation was never added.
    animator.addAnimation("jump", 1f, PlayMode.NORMAL);
    animator.addAnimation("air_dash", 1f, PlayMode.NORMAL);
    animator.addAnimation("hurt", 1f, PlayMode.NORMAL);
    animator.addAnimation("melee", 1f, PlayMode.NORMAL);
    entity.addComponent(animator);
    PlayerAnimationController controller = new PlayerAnimationController();
    entity.addComponent(controller);
    controller.create();
    return controller;
  }

  @Test
  void shouldPlayDeathAnimationOnDeathEvent() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("death");

    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldIgnoreMovementAndHurtEventsAfterDeath() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("death");
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    entity.getEvents().trigger("sprint");
    entity.getEvents().trigger("jumpStart");
    entity.getEvents().trigger("hurt");

    // None of the above should have replaced the death animation.
    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotFireDeathAnimationFinishedBeforeAnimationCompletes() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);
    AtomicInteger finishedEvents = new AtomicInteger();
    entity.getEvents().addListener("deathAnimationFinished", finishedEvents::incrementAndGet);

    entity.getEvents().trigger("death");
    controller.update(); // No frame has been drawn yet, so the animation isn't finished.

    assertEquals(0, finishedEvents.get());
  }

  @Test
  void shouldFireDeathAnimationFinishedExactlyOnceWhenAnimationCompletes() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);
    AtomicInteger finishedEvents = new AtomicInteger();
    entity.getEvents().addListener("deathAnimationFinished", finishedEvents::incrementAndGet);

    entity.getEvents().trigger("death");
    animator.render(mock(SpriteBatch.class)); // Advances past the single-frame death animation.

    controller.update();
    controller.update(); // Should not fire a second time.

    assertEquals(1, finishedEvents.get());
  }

  @Test
  void shouldPlayBowDrawOnChargeStartAndBlockWalkFromOverridingIt() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    assertEquals("bow_draw", animator.getCurrentAnimation());

    // Walking shouldn't interrupt the draw-back, same as it can't interrupt melee/dash.
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    assertEquals("bow_draw", animator.getCurrentAnimation());
  }

  @Test
  void shouldTransitionFromDrawToHoldOnceDrawFinishes() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    assertEquals("bow_draw", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class)); // Advances past the single-frame draw animation.
    controller.update();

    assertEquals("bow_hold", animator.getCurrentAnimation());
  }

  @Test
  void shouldKeepPlayingBowHoldPastOneLoopCycleWhileStillCharging() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    animator.render(mock(SpriteBatch.class)); // Finishes bow_draw, settles into bow_hold.
    controller.update();
    assertEquals("bow_hold", animator.getCurrentAnimation());

    // Regression: bow_hold is a 1s LOOP here; render()/update() several times to push playtime
    // well past that single cycle, simulating a held charge. The hold must not snap back to idle
    // just because the animator's own clip length has elapsed once.
    for (int i = 0; i < 5; i++) {
      animator.render(mock(SpriteBatch.class));
      controller.update();
    }

    assertEquals("bow_hold", animator.getCurrentAnimation());
  }

  @Test
  void shouldPlayBowShootOnceAndClearAttackingWhenItFinishes() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    animator.render(mock(SpriteBatch.class)); // Finishes bow_draw, settles into bow_hold.
    controller.update();

    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));
    assertEquals("bow_shoot", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class)); // Advances past the single-frame shoot animation.
    controller.update();

    // attacking clears once bow_shoot finishes, same as melee, reverting to idle/walk.
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldCutDrawShortAndShootImmediatelyOnQuickRelease() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    // Releasing before the draw-back animation itself has finished should still fire immediately
    // (no disengage mechanism - a tap-release always shoots).
    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));

    assertEquals("bow_shoot", animator.getCurrentAnimation());
  }

  @Test
  void shouldPrioritiseEveryBowStageOverOtherAnimations() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions(
                "idle",
                "death",
                "bow_draw",
                "bow_hold",
                "bow_shoot",
                "jump",
                "air_dash",
                "hurt",
                "melee"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);

    // Stage 1: drawing.
    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    triggerCompetingAnimations(entity);
    assertEquals("bow_draw", animator.getCurrentAnimation());

    // Stage 2: holding.
    animator.render(mock(SpriteBatch.class));
    controller.update();
    assertEquals("bow_hold", animator.getCurrentAnimation());
    triggerCompetingAnimations(entity);
    assertEquals("bow_hold", animator.getCurrentAnimation());

    // Stage 3: shooting.
    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));
    triggerCompetingAnimations(entity);
    assertEquals("bow_shoot", animator.getCurrentAnimation());
  }

  @Test
  void shouldAllowOtherAnimationsAgainOnceShootFinishes() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot", "jump"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));
    animator.render(mock(SpriteBatch.class)); // Finishes bow_shoot.
    controller.update();

    entity.getEvents().trigger("jumpStart");

    assertEquals("jump", animator.getCurrentAnimation());
  }

  @Test
  void shouldStillLetDeathOverrideTheBowSequence() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    entity.getEvents().trigger("death");

    assertEquals("death", animator.getCurrentAnimation());
  }

  private static void triggerCompetingAnimations(Entity entity) {
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    entity.getEvents().trigger("sprint");
    entity.getEvents().trigger("jumpStart");
    entity.getEvents().trigger("dashStart");
    entity.getEvents().trigger("airDashStart");
    entity.getEvents().trigger("hurt");
    entity.getEvents().trigger("melee", new Vector2(1f, 0f));
  }

  @Test
  void shouldIgnoreChargeReleaseWithoutPriorChargeStart() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));

    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldIgnoreChargeStartAndReleaseWhileDead() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("death");
    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));

    assertEquals("death", animator.getCurrentAnimation());
  }
}
