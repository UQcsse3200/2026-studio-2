package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class PlayerAnimationController extends Component {
  private static final float INSTRUMENT_VOLUME = 0.3f; // 0.0 is silent, 1.0 is full volume
  // Must match the frame duration given to "instrument_draw" in PlayerFactory.
  private static final float INSTRUMENT_FRAME_DURATION = 0.11f;
  // First frame of instrument_draw where the instrument is clearly out of its hiding place.
  private static final int INSTRUMENT_OUT_FRAME = 10;
  // The level's background track, the same one PauseMenuDisplay pauses and resumes.
  private static final String GAMEPLAY_MUSIC = "sounds/gameplay_bg.ogg";

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
  // True from the moment the instrument is picked up until another action interrupts it. While
  // set, updateAnimation() leaves the animation alone so the hold keeps looping.
  private boolean instrumentActive = false;
  private boolean instrumentDrawing = false;
  // The music waits until the instrument is actually out, so it is queued and started by timer.
  private boolean instrumentMusicPending = false;
  private float instrumentDrawTime = 0f;
  private Music instrumentMusic;
  // True while the instrument track is playing, so the "stopped" event only fires after a start.
  private boolean instrumentMusicPlaying = false;
  // True while the level's background track is paused because the instrument is playing.
  private boolean gameplayMusicPausedForInstrument = false;

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
    // The grapple broadcasts its own charge events and shares the bow animation clips.
    entity.getEvents().addListener("grappleChargeStart", this::drawStart);
    entity.getEvents().addListener("grappleChargeFire", this::drawRelease);
    entity.getEvents().addListener("instrumentStart", this::instrumentStart);
    entity.getEvents().addListener("meleeSwing", this::meleeStart);
    entity.getEvents().addListener("togglePause", this::cancelInstrumentForPause);
    entity.getEvents().addListener("sprintEnd", this::sprintStop);
    entity.getEvents().addListener("death", this::death);
    entity.getEvents().addListener("revive", this::revive);
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
    updateInstrumentMusic();
    if (!animator.isFinished()) {
      return;
    }
    if (hurt) {
      hurt = false;
      updateAnimation();
    } else if (dashing) {
      dashing = false;
      updateAnimation();
    } else if (drawingIn) {
      // The one-shot draw-back has finished pulling the string - settle into the looping hold.
      drawingIn = false;
      animator.startAnimation("bow_hold");
    } else if (instrumentDrawing) {
      // The one-shot pick-up has finished - settle into the looping hold until interrupted.
      instrumentDrawing = false;
      animator.startAnimation("instrument_hold");
      if (instrumentMusicPending) {
        instrumentMusicPending = false;
        playInstrumentMusic();
      }
    } else if (attacking) {
      // Also where bow_shoot lands, which is the end of the bow sequence.
      attacking = false;
      bowActive = false;
      updateAnimation();
    } else if (landing) {
      landing = false;
      updateAnimation();
    }
    // Note: the takeoff clip deliberately has no "finished" branch. It is NORMAL mode, so it holds
    // its final tucked frame through the rest of the ascent until the fall or landing takes over.
  }

  private void updateInstrumentMusic() {
    if (instrumentMusicPending && !ServiceLocator.getEntityService().getPaused()) {
      instrumentDrawTime += ServiceLocator.getTimeSource().getDeltaTime();
      if (instrumentDrawTime >= INSTRUMENT_FRAME_DURATION * INSTRUMENT_OUT_FRAME) {
        instrumentMusicPending = false;
        playInstrumentMusic();
      }
    }
  }

  void walk(Vector2 direction) {
    if (dead) {
      return;
    }
    moving = true;
    if (direction.x != 0) {
      animator.setFlipX(direction.x < 0);
      cancelInstrument();
    }
    updateAnimation();
  }

  void walkStop() {
    if (dead) {
      return;
    }
    moving = false;
    updateAnimation();
  }

  void sprint() {
    if (dead) {
      return;
    }
    cancelInstrument();
    sprinting = true;
    updateAnimation();
  }

  void sprintStop() {
    if (dead) {
      return;
    }
    sprinting = false;
    updateAnimation();
  }

  void jumpStart() {
    if (dead || bowActive) {
      return;
    }
    if (dashing) {
      return;
    }
    landing = false;
    cancelInstrument();
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
    hurt = false;
    landing = false;
    cancelInstrument();
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
    landing = false;
    cancelInstrument();
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
    cancelInstrument();
    animator.startAnimation("death");
  }

  void revive() {
    dead = false;
    deathAnimationFinishedFired = false;
    hurt = false;
    dashing = false;
    attacking = false;
    jumping = false;
    moving = false;
    sprinting = false;
    updateAnimation();
  }

  void sleep() {
    animator.startAnimation("sleep");
  }

  void drawStart(Vector2 aim) {
    if (dead) {
      return;
    }
    // The draw replaces the previous animation. Its completion must not be handled as the
    // end of a hurt, dash or jump that was still active when the shoot button was pressed.
    hurt = false;
    dashing = false;
    jumping = false;
    landing = false;
    cancelInstrument();
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

  /**
   * Plays the melee swing facing the way the swing goes. Skipped during the bow sequence, a dash or
   * a hurt reaction, which shouldn't be visually interrupted.
   */
  void meleeStart(Integer facing) {
    if (dead || bowActive || dashing || hurt) {
      return;
    }
    landing = false;
    cancelInstrument();
    if (facing != null && facing != 0) {
      animator.setFlipX(facing < 0);
    }
    attacking = true;
    animator.startAnimation("melee");
  }

  /** Picks up the instrument and holds it, with music, until another action interrupts it. */
  void instrumentStart() {
    if (dead || bowActive || jumping || dashing || hurt || attacking || instrumentActive) {
      return;
    }
    landing = false;
    instrumentActive = true;
    instrumentDrawing = true;
    instrumentDrawTime = 0f;
    instrumentMusicPending = true;
    animator.startAnimation("instrument_draw");
  }

  /** Marks the instrument as put away. Callers then pick the next animation themselves. */
  private void cancelInstrument() {
    instrumentActive = false;
    instrumentDrawing = false;
    instrumentMusicPending = false;
    stopInstrumentMusic();
  }

  private void playInstrumentMusic() {
    if (Gdx.audio == null || Gdx.files == null) {
      return; // no audio device, e.g. in unit tests
    }
    if (instrumentMusic == null) {
      instrumentMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/instrument_loop.wav"));
      instrumentMusic.setLooping(true);
      instrumentMusic.setVolume(INSTRUMENT_VOLUME);
    }
    instrumentMusic.play();
    instrumentMusicPlaying = true;
    pauseGameplayMusic();
  }

  private void stopInstrumentMusic() {
    if (instrumentMusic != null) {
      instrumentMusic.stop();
    }
    if (instrumentMusicPlaying) {
      instrumentMusicPlaying = false;
      resumeGameplayMusic();
    }
  }

  /** Pauses the level's background track so the instrument music is heard on its own. */
  private void pauseGameplayMusic() {
    try {
      Music gameplay = ServiceLocator.getResourceService().getAsset(GAMEPLAY_MUSIC, Music.class);
      if (gameplay.isPlaying()) {
        gameplay.pause();
        gameplayMusicPausedForInstrument = true;
      }
    } catch (Exception e) {
      // The track isn't loaded in this level, so there is nothing to pause.
    }
  }

  /** Resumes the level's background track, but only if the instrument was what paused it. */
  private void resumeGameplayMusic() {
    if (!gameplayMusicPausedForInstrument) {
      return;
    }
    gameplayMusicPausedForInstrument = false;
    try {
      ServiceLocator.getResourceService().getAsset(GAMEPLAY_MUSIC, Music.class).play();
    } catch (Exception e) {
      // The track is no longer loaded, so there is nothing to resume.
    }
  }

  /**
   * Puts the instrument away when the pause menu opens. The pause menu takes over the background
   * track itself (it pauses it, then plays it again on resume), so this must not resume it.
   */
  private void cancelInstrumentForPause() {
    if (!instrumentActive) {
      return;
    }
    gameplayMusicPausedForInstrument = false;
    cancelInstrument();
    updateAnimation();
  }

  private void updateAnimation() {
    if (isPlayingActionAnimation()) {
      return;
    }
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

  /** Whether an action clip must keep playing instead of being hidden by the rope pose. */
  public boolean isPlayingActionAnimation() {
    return dead || instrumentActive || jumping || landing || isBusyWithHigherPriorityAnimation();
  }

  /** Re-picks the idle, walk or sprint animation, e.g. after the rope pose stops drawing. */
  public void refreshAnimation() {
    updateAnimation();
  }

  public void playAnimation(String animationName) {
    animator.startAnimation(animationName);
  }

  public AnimationRenderComponent getAnimator() {
    return this.animator;
  }

  @Override
  public void dispose() {
    // Don't leave the level music silent if the instrument was playing when the player is removed.
    stopInstrumentMusic();
    if (instrumentMusic != null) {
      instrumentMusic.dispose();
      instrumentMusic = null;
    }
    super.dispose();
  }
}
