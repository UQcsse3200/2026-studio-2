package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    animator.addAnimation("jump_takeoff", 1f, PlayMode.NORMAL);
    animator.addAnimation("jump_fall", 1f, PlayMode.LOOP);
    animator.addAnimation("jump_land", 1f, PlayMode.NORMAL);
    animator.addAnimation("air_dash", 1f, PlayMode.NORMAL);
    animator.addAnimation("hurt", 1f, PlayMode.NORMAL);
    animator.addAnimation("melee", 1f, PlayMode.NORMAL);
    animator.addAnimation("instrument_draw", 1f, PlayMode.NORMAL);
    animator.addAnimation("instrument_hold", 1f, PlayMode.LOOP);
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
                "jump_takeoff",
                "jump_fall",
                "jump_land",
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
            mockAtlasWithRegions(
                "idle",
                "death",
                "bow_draw",
                "bow_hold",
                "bow_shoot",
                "jump_takeoff",
                "jump_fall",
                "jump_land"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));
    animator.render(mock(SpriteBatch.class)); // Finishes bow_shoot.
    controller.update();

    entity.getEvents().trigger("jumpStart");

    assertEquals("jump_takeoff", animator.getCurrentAnimation());
  }

  /** Builds an entity with every jump stage registered, ready to drive through the sequence. */
  private PlayerAnimationController createJumpController(Entity entity) {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions(
                "idle",
                "walk",
                "death",
                "bow_draw",
                "bow_hold",
                "bow_shoot",
                "jump_takeoff",
                "jump_fall",
                "jump_land",
                "air_dash",
                "hurt",
                "melee",
                "instrument_draw",
                "instrument_hold"));
    animator.addAnimation("walk", 1f, PlayMode.LOOP);
    return createController(entity, animator);
  }

  /** Plays the current clip to completion and lets the controller react to it finishing. */
  private void finishClip(Entity entity, PlayerAnimationController controller) {
    entity.getComponent(AnimationRenderComponent.class).render(mock(SpriteBatch.class));
    controller.update();
  }

  @Test
  void shouldPlayTheMeleeSwingAndFlipItForALeftwardSwing() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("meleeSwing", -1);

    assertEquals("melee", animator.getCurrentAnimation());
    assertTrue(animator.isFlipX(), "a leftward swing should face left");
  }

  @Test
  void shouldFaceRightForARightwardSwingRegardlessOfTheLastWalkDirection() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().trigger("walk", new Vector2(-1f, 0f));

    entity.getEvents().trigger("meleeSwing", 1);

    assertEquals("melee", animator.getCurrentAnimation());
    assertFalse(animator.isFlipX());
  }

  @Test
  void shouldNotStartAMeleeSwingDuringABowShotOrADashOrAHurt() {
    Entity bowEntity = new Entity();
    createJumpController(bowEntity);
    bowEntity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    bowEntity.getEvents().trigger("meleeSwing", 1);
    assertEquals(
        "bow_draw",
        bowEntity.getComponent(AnimationRenderComponent.class).getCurrentAnimation(),
        "the bow sequence outranks a swing");

    Entity dashEntity = new Entity();
    createJumpController(dashEntity);
    dashEntity.getEvents().trigger("dashStart");
    dashEntity.getEvents().trigger("meleeSwing", 1);
    assertEquals(
        "air_dash", dashEntity.getComponent(AnimationRenderComponent.class).getCurrentAnimation());

    Entity hurtEntity = new Entity();
    createJumpController(hurtEntity);
    hurtEntity.getEvents().trigger("hurt");
    hurtEntity.getEvents().trigger("meleeSwing", 1);
    assertEquals(
        "hurt", hurtEntity.getComponent(AnimationRenderComponent.class).getCurrentAnimation());
  }

  @Test
  void shouldResumeFallingAfterAMeleeSwingEndsMidAir() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("meleeSwing", 1);
    assertEquals("melee", animator.getCurrentAnimation(), "a swing outranks the fall loop");

    finishClip(entity, controller);

    // The fall was only recorded while the swing played, so it has to come back afterwards rather
    // than dropping the player into idle in mid-air.
    assertEquals("jump_fall", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotLetMovementCutAMeleeSwingShort() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("meleeSwing", 1);
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    entity.getEvents().trigger("sprint");

    assertEquals("melee", animator.getCurrentAnimation());
  }

  @Test
  void shouldSuppressTheLandingRecoveryWhileASwingIsPlaying() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("meleeSwing", 1);
    entity.getEvents().trigger("landed");

    assertEquals("melee", animator.getCurrentAnimation());
  }

  @Test
  void shouldSettleIntoTheInstrumentHoldOnceTheDrawFinishes() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("instrumentStart");
    assertEquals("instrument_draw", animator.getCurrentAnimation());

    finishClip(entity, controller);

    assertEquals("instrument_hold", animator.getCurrentAnimation());
  }

  @Test
  void shouldKeepHoldingTheInstrumentUntilSomethingInterruptsIt() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("instrumentStart");
    finishClip(entity, controller);

    // The hold loops, so repeated updates must not drop it back to idle.
    for (int i = 0; i < 5; i++) {
      finishClip(entity, controller);
    }

    assertEquals("instrument_hold", animator.getCurrentAnimation());
  }

  @Test
  void shouldPutTheInstrumentAwayWhenThePlayerWalks() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("instrumentStart");
    finishClip(entity, controller);

    entity.getEvents().trigger("walk", new Vector2(1f, 0f));

    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldRefuseTheInstrumentWhileBusyWithAnotherAction() {
    Entity jumpEntity = new Entity();
    createJumpController(jumpEntity);
    jumpEntity.getEvents().trigger("jumpStart");
    jumpEntity.getEvents().trigger("instrumentStart");
    assertEquals(
        "jump_takeoff",
        jumpEntity.getComponent(AnimationRenderComponent.class).getCurrentAnimation());

    Entity swingEntity = new Entity();
    createJumpController(swingEntity);
    swingEntity.getEvents().trigger("meleeSwing", 1);
    swingEntity.getEvents().trigger("instrumentStart");
    assertEquals(
        "melee", swingEntity.getComponent(AnimationRenderComponent.class).getCurrentAnimation());
  }

  @Test
  void shouldPutTheInstrumentAwayOnDeath() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().trigger("instrumentStart");
    finishClip(entity, controller);

    entity.getEvents().trigger("death");

    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldPutTheInstrumentAwayWhenThePauseMenuOpens() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().trigger("instrumentStart");
    finishClip(entity, controller);

    entity.getEvents().trigger("togglePause");

    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldRestoreTheMovementAnimationWhenRefreshed() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));

    // The rope pose stops the animator while it draws the player itself; refreshAnimation() is how
    // the normal animation comes back once it stops.
    animator.stopAnimation();
    controller.refreshAnimation();

    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldHoldTakeoffPastItsClipLengthInsteadOfRevertingToIdle() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("jumpStart");

    // Regression: the takeoff used to clear itself when the clip finished, dropping the player
    // into idle while still airborne. It must hold its last tucked frame until the fall begins.
    for (int i = 0; i < 5; i++) {
      animator.render(mock(SpriteBatch.class));
      controller.update();
    }

    assertEquals("jump_takeoff", animator.getCurrentAnimation());
  }

  @Test
  void shouldLoopTheFallUntilLanding() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("jumpStart");
    entity.getEvents().trigger("fallStart");
    assertEquals("jump_fall", animator.getCurrentAnimation());

    for (int i = 0; i < 5; i++) {
      animator.render(mock(SpriteBatch.class));
      controller.update();
    }

    assertEquals("jump_fall", animator.getCurrentAnimation());
  }

  @Test
  void shouldPlayLandingRecoveryOnceThenReturnToIdle() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("jumpStart");
    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("landed");
    assertEquals("jump_land", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class)); // Finishes the single-frame recovery.
    controller.update();

    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotLetMovementCutTheLandingRecoveryShort() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("landed");
    assertEquals("jump_land", animator.getCurrentAnimation());

    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    assertEquals("jump_land", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class));
    controller.update();

    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldResumeFallingAfterADashInterruptsIt() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("airDashStart");
    assertEquals("air_dash", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class)); // dash clip finishes, still airborne
    controller.update();

    assertEquals("jump_fall", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotPlayTheLandingRecoveryOverADash() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("airDashStart");
    entity.getEvents().trigger("landed"); // dashed straight into the floor
    assertEquals("air_dash", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class));
    controller.update();

    // Grounded again, so it resolves to idle rather than resuming the fall.
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldLetDeathOverrideTheAirSequenceAndIgnoreLaterAirEvents() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("jumpStart");
    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("death");
    assertEquals("death", animator.getCurrentAnimation());

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("landed");
    entity.getEvents().trigger("grappleAttached");

    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotStartTheTakeoffDuringABowShot() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    entity.getEvents().trigger("jumpStart");

    assertEquals("bow_draw", animator.getCurrentAnimation());
  }

  @Test
  void shouldStopTheFallLoopWhenTheGrappleAttaches() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    assertEquals("jump_fall", animator.getCurrentAnimation());

    entity.getEvents().trigger("grappleAttached");

    // Swinging, not falling - the loop must not keep playing underneath the player.
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldStopTheTakeoffPoseWhenTheGrappleAttaches() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    entity.getEvents().trigger("jumpStart");
    entity.getEvents().trigger("grappleAttached");

    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldFallAgainAfterReleasingTheGrapple() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("grappleAttached");
    assertEquals("idle", animator.getCurrentAnimation());

    // PlayerActions clears its own falling flag on attach, so letting go mid-air reports a
    // fresh fall and the loop comes back.
    entity.getEvents().trigger("fallStart");

    assertEquals("jump_fall", animator.getCurrentAnimation());
  }

  @Test
  void shouldIgnoreLandingWhenNeverAirborne() {
    Entity entity = new Entity();
    createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    // A one-frame blip in the ground raycast while running must not punch in a landing crouch.
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    entity.getEvents().trigger("landed");

    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldResumeFallAfterAHurtEndsMidAir() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("fallStart");
    entity.getEvents().trigger("hurt");
    assertEquals("hurt", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class)); // Finishes the hurt reaction.
    controller.update();

    // Still in the air, so it must go back to falling rather than idling mid-flight.
    assertEquals("jump_fall", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotInterruptABowShotWithAFall() {
    Entity entity = new Entity();
    PlayerAnimationController controller = createJumpController(entity);
    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    entity.getEvents().trigger("chargeRelease", new Vector2(1f, 0f));
    entity.getEvents().trigger("fallStart");
    assertEquals("bow_shoot", animator.getCurrentAnimation());

    animator.render(mock(SpriteBatch.class)); // Finishes bow_shoot.
    controller.update();

    assertEquals("jump_fall", animator.getCurrentAnimation());
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
  void shouldKeepBowChargeWhenStartedDuringHurtAnimation() {
    assertChargeSurvivesPriorAnimation("hurt", "chargeStart", "chargeRelease");
  }

  @Test
  void shouldKeepBowChargeWhenStartedDuringDashAnimation() {
    assertChargeSurvivesPriorAnimation("dashStart", "chargeStart", "chargeRelease");
  }

  @Test
  void shouldKeepGrappleChargeWhenStartedDuringHurtAnimation() {
    assertChargeSurvivesPriorAnimation("hurt", "grappleChargeStart", "grappleChargeFire");
  }

  @Test
  void shouldKeepGrappleChargeWhenStartedDuringDashAnimation() {
    assertChargeSurvivesPriorAnimation("airDashStart", "grappleChargeStart", "grappleChargeFire");
  }

  @Test
  void shouldResumeWalkingAfterChargingDuringJumpAnimation() {
    assertChargeSurvivesPriorAnimation("jumpStart", "chargeStart", "chargeRelease");
  }

  private void assertChargeSurvivesPriorAnimation(
      String priorEvent, String chargeStartEvent, String chargeReleaseEvent) {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions(
                "idle",
                "death",
                "bow_draw",
                "bow_hold",
                "bow_shoot",
                "hurt",
                "air_dash",
                "jump_takeoff",
                "walk"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);
    animator.addAnimation("walk", 1f, PlayMode.LOOP);
    SpriteBatch batch = mock(SpriteBatch.class);

    // Start the draw before the previous one-shot animation has completed.
    entity.getEvents().trigger(priorEvent);
    String priorAnimation =
        switch (priorEvent) {
          case "jumpStart" -> "jump_takeoff";
          case "dashStart", "airDashStart" -> "air_dash";
          default -> "hurt";
        };
    assertEquals(priorAnimation, animator.getCurrentAnimation());
    entity.getEvents().trigger(chargeStartEvent, new Vector2(1f, 0f));
    assertEquals("bow_draw", animator.getCurrentAnimation());
    animator.render(batch);
    controller.update();
    assertEquals(
        "bow_hold",
        animator.getCurrentAnimation(),
        "the previous animation must not cut off the draw when it finishes");
    for (int i = 0; i < 4; i++) {
      animator.render(batch);
      controller.update();
      assertEquals("bow_hold", animator.getCurrentAnimation(), "hold until the button is released");
    }

    entity.getEvents().trigger(chargeReleaseEvent, new Vector2(1f, 0f));
    assertEquals("bow_shoot", animator.getCurrentAnimation());
    animator.render(batch);
    controller.update();
    assertEquals("idle", animator.getCurrentAnimation());
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    assertEquals("walk", animator.getCurrentAnimation(), "movement should resume after the shot");
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
  void shouldReturnToIdleWhenChargeIsCancelled() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            mockAtlasWithRegions("idle", "death", "bow_draw", "bow_hold", "bow_shoot"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("chargeStart", new Vector2(1f, 0f));
    assertEquals("bow_draw", animator.getCurrentAnimation());

    entity.getEvents().trigger("chargeCancel");

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
