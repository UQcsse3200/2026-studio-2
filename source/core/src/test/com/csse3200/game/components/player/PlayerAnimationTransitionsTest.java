package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
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
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
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
  private Audio originalAudio;
  private Files originalFiles;
  private Music instrument;
  private Music gameplay;
  private ResourceService resources;

  @BeforeEach
  void setUp() {
    originalAudio = Gdx.audio;
    originalFiles = Gdx.files;
    time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(time);
    ServiceLocator.registerEntityService(new EntityService());
    TextureAtlas atlas = mock(TextureAtlas.class);
    animator = new AnimationRenderComponent(atlas);
    for (String name :
        new String[] {
          "instrument_draw",
          "instrument_hold",
          "sleep",
          "idle",
          "walk",
          "sprint",
          "jump_takeoff",
          "jump_fall",
          "jump_land",
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
              || name.equals("bow_hold")
              || name.equals("jump_fall")
              || name.equals("instrument_hold");
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
  void shouldHoldTakeoffThroughTheAscentAndUseLatestMovementStateAfterLanding() {
    player.getEvents().trigger("jumpStart");
    controller.update();
    assertEquals("jump_takeoff", animator.getCurrentAnimation());
    player.getEvents().trigger("walk", Vector2.X);
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("walkStop");
    player.getEvents().trigger("sprintStop");
    assertEquals("jump_takeoff", animator.getCurrentAnimation());

    // The takeoff now holds its final frame for the rest of the ascent instead of dropping back
    // to a movement animation mid-air.
    finishClip();
    assertEquals("jump_takeoff", animator.getCurrentAnimation());

    player.getEvents().trigger("fallStart");
    assertEquals("jump_fall", animator.getCurrentAnimation());

    player.getEvents().trigger("landed");
    assertEquals("jump_land", animator.getCurrentAnimation());
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
    assertEquals("jump_takeoff", animator.getCurrentAnimation());
    player.getEvents().trigger("chargeCancel");
    assertEquals("jump_takeoff", animator.getCurrentAnimation());
  }

  @Test
  void shouldIgnoreRemainingMovementAndAttackEventsAfterDeath() {
    player.getEvents().trigger("death");
    player.getEvents().trigger("walkStop");
    player.getEvents().trigger("sprintStop");
    player.getEvents().trigger("dashStart");
    player.getEvents().trigger("airDashStart");
    player.getEvents().trigger("meleeSwing", 1);
    player.getEvents().trigger("chargeCancel");
    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotRestartMeleeOnRepeatedIdleUpdateAfterCompletion() {
    player.getEvents().trigger("meleeSwing", 1);
    finishClip();
    assertEquals("idle", animator.getCurrentAnimation());
    controller.update();
    controller.update();
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldKeepHurtClipWhenMovementChanges() {
    player.getEvents().trigger("hurt");
    player.getEvents().trigger("walk", Vector2.X);
    player.getEvents().trigger("sprint");
    assertEquals("hurt", animator.getCurrentAnimation());
    finishClip();
    assertEquals("sprint", animator.getCurrentAnimation());
  }

  @Test
  void shouldResumeMovementAfterBowInterruptsLanding() {
    player.getEvents().trigger("fallStart");
    player.getEvents().trigger("landed");
    player.getEvents().trigger("chargeStart", Vector2.X);
    player.getEvents().trigger("chargeRelease", Vector2.X);
    finishClip();
    player.getEvents().trigger("walk", Vector2.X);
    assertEquals("walk", animator.getCurrentAnimation());
    player.getEvents().trigger("walkStop");
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldResumeMovementAfterMeleeInterruptsLanding() {
    player.getEvents().trigger("fallStart");
    player.getEvents().trigger("landed");
    player.getEvents().trigger("meleeSwing", -1);
    assertEquals("melee", animator.getCurrentAnimation());
    assertTrue(animator.isFlipX());
    finishClip();
    player.getEvents().trigger("walk", Vector2.X);
    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldClearSprintAnimationAfterRevival() {
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("death");
    player.getEvents().trigger("revive");
    player.getEvents().trigger("walk", Vector2.X);
    assertEquals("walk", animator.getCurrentAnimation());
  }

  @AfterEach
  void restoreAudio() {
    Gdx.audio = originalAudio;
    Gdx.files = originalFiles;
  }

  private void configureAudio(boolean backgroundPlaying) {
    Gdx.audio = mock(Audio.class);
    Gdx.files = mock(Files.class);
    FileHandle handle = new FileHandle("sounds/instrument_loop.wav");
    when(Gdx.files.internal("sounds/instrument_loop.wav")).thenReturn(handle);
    instrument = mock(Music.class);
    when(Gdx.audio.newMusic(handle)).thenReturn(instrument);
    gameplay = mock(Music.class);
    when(gameplay.isPlaying()).thenReturn(backgroundPlaying);
    resources = mock(ResourceService.class);
    when(resources.getAsset("sounds/gameplay_bg.ogg", Music.class)).thenReturn(gameplay);
    ServiceLocator.registerResourceService(resources);
  }

  @Test
  void shouldDelayInstrumentAudioUntilOutFrameAndReuseItAfterAnInterruption() {
    configureAudio(true);
    controller.instrumentStart();
    when(time.getDeltaTime()).thenReturn(0.5f);
    controller.update();
    verify(instrument, never()).play();
    controller.update();
    verify(instrument, never()).play();
    when(time.getDeltaTime()).thenReturn(0.1f);
    controller.update();
    verify(instrument).setLooping(true);
    verify(instrument).setVolume(0.3f);
    verify(instrument).play();
    verify(gameplay).pause();
    when(time.getDeltaTime()).thenReturn(1f);
    finishClip();
    assertEquals("instrument_hold", animator.getCurrentAnimation());
    verify(instrument, times(1)).play();
    controller.walk(Vector2.X);
    verify(instrument).stop();
    verify(gameplay).play();
    controller.instrumentStart();
    finishClip();
    verify(Gdx.audio, times(1)).newMusic(any());
    verify(instrument, times(2)).play();
    controller.dispose();
    verify(instrument).dispose();
    verify(gameplay, times(2)).play();
    controller.dispose();
    verify(instrument, times(1)).dispose();
  }

  @Test
  void shouldNotAdvancePendingInstrumentAudioWhileGameIsPaused() {
    configureAudio(true);
    EntityService entities = mock(EntityService.class);
    when(entities.getPaused()).thenReturn(true);
    ServiceLocator.registerEntityService(entities);
    controller.instrumentStart();
    when(time.getDeltaTime()).thenReturn(2f);
    controller.update();
    verify(instrument, never()).play();
    when(entities.getPaused()).thenReturn(false);
    controller.update();
    verify(instrument).play();
  }

  @Test
  void shouldLeaveAlreadyPausedBackgroundMusicPausedWhenInstrumentStops() {
    configureAudio(false);
    controller.instrumentStart();
    finishClip();
    controller.walk(Vector2.X);
    verify(instrument).stop();
    verify(gameplay, never()).pause();
    verify(gameplay, never()).play();
  }

  @Test
  void shouldNotResumeBackgroundWhenPauseMenuCancelsInstrument() {
    configureAudio(true);
    player.getEvents().trigger("togglePause");
    assertEquals("idle", animator.getCurrentAnimation());
    controller.instrumentStart();
    finishClip();
    player.getEvents().trigger("togglePause");
    assertEquals("idle", animator.getCurrentAnimation());
    verify(instrument).stop();
    verify(gameplay, never()).play();
  }

  @Test
  void shouldStopInstrumentSafelyWhenBackgroundTrackHasBeenUnloaded() {
    configureAudio(true);
    controller.instrumentStart();
    finishClip();
    verify(gameplay).pause();
    when(resources.getAsset("sounds/gameplay_bg.ogg", Music.class))
        .thenThrow(new IllegalStateException("Level unloaded"));
    assertDoesNotThrow(() -> controller.walk(Vector2.X));
    verify(instrument).stop();
    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldStillPlayInstrumentWhenLevelHasNoBackgroundTrack() {
    configureAudio(false);
    when(resources.getAsset("sounds/gameplay_bg.ogg", Music.class))
        .thenThrow(new IllegalStateException("No track loaded"));
    controller.instrumentStart();
    finishClip();
    verify(instrument).play();
    assertEquals("instrument_hold", animator.getCurrentAnimation());
    controller.dispose();
    verify(instrument).stop();
    verify(instrument).dispose();
  }

  @Test
  void shouldCompleteInstrumentAnimationWithoutAudioOrFiles() {
    Gdx.audio = null;
    controller.instrumentStart();
    finishClip();
    assertEquals("instrument_hold", animator.getCurrentAnimation());
    controller.walk(Vector2.X);
    Gdx.audio = mock(Audio.class);
    Gdx.files = null;
    controller.instrumentStart();
    finishClip();
    assertEquals("instrument_hold", animator.getCurrentAnimation());
    verifyNoInteractions(Gdx.audio);
  }

  @Test
  void shouldPreserveFacingForNullOrVerticalBowAimAndZeroOrNullMeleeFacing() {
    controller.walk(new Vector2(-1f, 0f));
    controller.drawStart(null);
    assertTrue(animator.isFlipX());
    assertEquals("bow_draw", animator.getCurrentAnimation());
    controller.drawCancel();
    controller.drawStart(Vector2.Y);
    assertTrue(animator.isFlipX());
    controller.drawCancel();
    controller.meleeStart(null);
    assertTrue(animator.isFlipX());
    assertEquals("melee", animator.getCurrentAnimation());
    finishClip();
    controller.meleeStart(0);
    assertTrue(animator.isFlipX());
    assertEquals("melee", animator.getCurrentAnimation());
  }

  @Test
  void shouldRefuseInstrumentDuringEveryHigherPriorityActionOrExistingDraw() {
    controller.drawStart(Vector2.X);
    controller.instrumentStart();
    assertEquals("bow_draw", animator.getCurrentAnimation());
    controller.drawCancel();
    controller.dashStart();
    controller.instrumentStart();
    assertEquals("air_dash", animator.getCurrentAnimation());
    finishClip();
    controller.hurt();
    controller.instrumentStart();
    assertEquals("hurt", animator.getCurrentAnimation());
    finishClip();
    controller.instrumentStart();
    when(time.getDeltaTime()).thenReturn(0.5f);
    animator.render(mock(SpriteBatch.class));
    controller.instrumentStart();
    animator.render(mock(SpriteBatch.class));
    controller.update();
    assertEquals(
        "instrument_hold", animator.getCurrentAnimation(), "Repeated input must not restart draw");
    controller.death();
    controller.instrumentStart();
    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldKeepBowSequenceWhenGrappleAttachesDuringRecordedFall() {
    controller.drawStart(Vector2.X);
    controller.fallStart();
    controller.grappleAttached();
    assertEquals("bow_draw", animator.getCurrentAnimation());
    controller.drawRelease(Vector2.X);
    finishClip();
    assertEquals("idle", animator.getCurrentAnimation(), "Grapple clears the recorded fall");
    controller.grappleAttached();
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldBlockRopePoseForEachActionStageAndAllowItAgainAfterLanding() {
    assertFalse(controller.isPlayingActionAnimation());
    controller.instrumentStart();
    assertTrue(controller.isPlayingActionAnimation());
    controller.jumpStart();
    assertTrue(controller.isPlayingActionAnimation());
    controller.landed();
    assertTrue(controller.isPlayingActionAnimation());
    finishClip();
    assertFalse(controller.isPlayingActionAnimation());
    controller.death();
    assertTrue(controller.isPlayingActionAnimation());
  }

  @Test
  void shouldExposeActiveAnimatorAndAllowScriptedSleepAndCustomClips() {
    player.getEvents().trigger("sleep");
    assertEquals("sleep", controller.getAnimator().getCurrentAnimation());
    controller.playAnimation("walk");
    assertEquals("walk", animator.getCurrentAnimation());
    controller.refreshAnimation();
    assertEquals("idle", animator.getCurrentAnimation());
  }
}
