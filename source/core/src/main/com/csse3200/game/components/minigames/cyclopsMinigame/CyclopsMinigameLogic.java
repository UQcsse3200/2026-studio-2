package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlankTransitionScreenCover;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameLogic extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameLogic.class);

  static final int BUTTON = Input.Buttons.LEFT;

  /* State Machine */
  enum State {
    STOP,
    SHOW_DELAY,
    PLAY,
    HIDE_DELAY,
    PRE_MOVE,
    MOVING,
    LOSS,
    RETREAT,
    DEATH,
    WIN
  }

  State state = State.STOP;
  float timeInState = 0f;
  private boolean hasWon = false;

  static final float SHOW_HIDE_DELAY = 0.3f;
  static final float TRANSITION_DELAY_GAP = 0.2f;
  static final float TRANSITION_DELAY = 0.8f;
  static final float HURT_PAUSE_DELAY = 0.6f;
  static final float DEATH_DISPLAY_DELAY = 1.5f;
  static final int MISS_DAMAGE = 2;
  public static final String CYCLOPS_WAKE_EVENT = "cyclopsWake";
  public static final String CYCLOPS_SLEEP_EVENT = "cyclopsSleep";
  static final float START_SCORING_PERCENT = 20f;
  static final float END_SCORING_PERCENT = 10f;

  /* Components */
  private final TimingBarLogic timingBarLogic;
  private final TimingBarDisplay timingBarDisplay;
  private final TerrainComponent terrainComponent;

  /* Player Entity */
  private final Entity playerEntity;
  private static final String playerLossAnimation = "hurt";

  BlankTransitionScreenCover transitionScreenCover;

  private Vector2 runStart = new Vector2();
  private Vector2 runTarget = new Vector2();
  private boolean runSuccess = false;
  private Vector2 playerOffset = new Vector2();

  /* Important Grid Locations */
  private List<GridPoint2> safeLocations;
  private List<GridPoint2> lossLocations;
  private GridPoint2 winLocation;
  private int currentSafeLocation = 0;

  /* Audio/Sound Effects */
  Sound walkingSound;
  private long walkingSoundID;
  private static final String walkingSoundPath = "sounds/walkingSounds/walkingSound.mp3";
  private static final float walkingSoundVolume = 0.4f;

  Sound hitSound;
  private long hitSoundID;
  private static final String hitSoundPath = "sounds/minigames/cyclops/marker-hit.ogg";
  private static final float hitSoundVolume = 0.4f;

  Sound missSound;
  private long missSoundID;
  private static final String missSoundPath = "sounds/minigames/cyclops/marker-miss.ogg";
  private static final float missSoundVolume = 0.4f;

  /* Constructors */
  public CyclopsMinigameLogic(
      TimingBarLogic logic,
      TimingBarDisplay display,
      TerrainComponent terrainComponent,
      Entity player) {
    this.timingBarLogic = logic;
    this.timingBarDisplay = display;
    this.terrainComponent = terrainComponent;
    this.playerEntity = player;
  }

  /* Getters & Setters */
  public void setPlayerOffset(Vector2 playerOffset) {
    this.playerOffset = playerOffset.cpy();
  }

  public void setWinLocation(GridPoint2 winLocation) {
    this.winLocation = winLocation;
  }

  public void setLossLocations(List<GridPoint2> lossLocations) {
    this.lossLocations = lossLocations;
  }

  public void setSafeLocations(List<GridPoint2> safeLocations) {
    this.safeLocations = safeLocations;
  }

  public boolean areNextSafeLocations() {
    return this.currentSafeLocation < this.safeLocations.size();
  }

  /* Package Private (Used for Testing) */
  GridPoint2 getWinLocation() {
    return this.winLocation;
  }

  List<GridPoint2> getLossLocations() {
    return this.lossLocations;
  }

  List<GridPoint2> getSafeLocations() {
    return this.safeLocations;
  }

  /* Player Handling Functions */
  private void movePlayer(GridPoint2 location) {
    Vector2 worldPos = this.terrainComponent.tileToWorldPosition(location).add(playerOffset);
    logger.debug("Converting GridPoint2 Location ({}) to World Position ({})", location, worldPos);
    logger.info(
        "Moving Player (Entity {}) from orig:{} to dest:{}",
        this.playerEntity.getId(),
        this.playerEntity.getPosition(),
        worldPos);
    playerEntity.setPosition(worldPos);
  }

  GridPoint2 advanceToNextLocation(boolean success) {
    if (!success) {
      return lossLocations.get(Math.min(this.currentSafeLocation, lossLocations.size() - 1));
    }
    this.currentSafeLocation = Math.min(this.currentSafeLocation + 1, safeLocations.size());
    if (!areNextSafeLocations()) {
      hasWon = true;
      return this.winLocation;
    }
    timingBarLogic.changeScoringAreaWidth(
        scoringPercentForRock(currentSafeLocation, safeLocations.size()));
    return safeLocations.get(this.currentSafeLocation);
  }

  static float scoringPercentForRock(int rockIndex, int rockCount) {
    int last = Math.max(rockCount - 1, 1);
    int clampedIndex = Math.min(rockIndex, last);
    return START_SCORING_PERCENT
        - (START_SCORING_PERCENT - END_SCORING_PERCENT) * clampedIndex / last;
  }

  private boolean stopPressed() {
    return Gdx.input.isButtonJustPressed(BUTTON);
  }

  /* Audio */

  private void loadSounds() {
    walkingSound = ServiceLocator.getResourceService().getAsset(walkingSoundPath, Sound.class);
    hitSound = ServiceLocator.getResourceService().getAsset(hitSoundPath, Sound.class);
    missSound = ServiceLocator.getResourceService().getAsset(missSoundPath, Sound.class);
  }

  private void playWalkingSound() {
    this.walkingSoundID = walkingSound.play();
    walkingSound.setLooping(this.walkingSoundID, true);
    walkingSound.setVolume(this.walkingSoundID, GameVolume.scale(walkingSoundVolume));
  }

  private void stopWalkingSound() {
    if (walkingSound == null) {
      logger.warn("Attempting to stop walking sound when walkingSound variable is null");
      return;
    }
    walkingSound.stop(walkingSoundID);
    logger.debug("Stopped walking sound");
  }

  private void playMarkerHitSound() {
    this.hitSoundID = hitSound.play();
    hitSound.setVolume(this.hitSoundID, GameVolume.scale(hitSoundVolume));
    logger.debug("Starting hit sound (ID: {}) with volume {}", this.hitSoundID, hitSoundVolume);
  }

  private void stopMarkerHitSound() {
    if (hitSound == null) {
      logger.warn("Attempting to stop hit sound when hitSound variable is null");
      return;
    }
    hitSound.stop(hitSoundID);
    logger.debug("Stopped hit sound");
  }

  private void playMarkerMissSound() {
    this.missSoundID = missSound.play();
    missSound.setVolume(this.missSoundID, GameVolume.scale(missSoundVolume));
    logger.debug("Starting miss sound (ID: {}) with volume {}", this.missSoundID, missSoundVolume);
  }

  private void stopMarkerMissSound() {
    if (missSound == null) {
      logger.warn("Attempting to stop miss sound when missSound variable is null");
      return;
    }
    missSound.stop(missSoundID);
    logger.debug("Stopped miss sound");
  }

  void playCorrectMarkerSound() {
    if (this.timingBarLogic.checkHit()) {
      playMarkerHitSound();
    } else {
      playMarkerMissSound();
    }
  }

  /* Game Logic */
  void changeState(State next) {
    state = next;
    timeInState = 0f;
  }

  boolean elapsed(float seconds) {
    return timeInState >= seconds;
  }

  private void beginRun(boolean success) {
    runSuccess = success;
    runStart = playerEntity.getPosition().cpy();
    runTarget =
        terrainComponent.tileToWorldPosition(advanceToNextLocation(runSuccess)).add(playerOffset);
    playWalkingSound();
    playerEntity.getEvents().trigger("walk", new Vector2(1, 0));
    playerEntity.getEvents().trigger("sprint");
  }

  private void handleTimingOutcome() {
    if (runSuccess) {
      if (hasWon) changeState(State.WIN);
      else startMinigame();
    } else {
      CombatStatsComponent combatStats = playerEntity.getComponent(CombatStatsComponent.class);
      if (combatStats != null) {
        combatStats.addHealth(-MISS_DAMAGE);
      }
      playerEntity.getEvents().trigger(playerLossAnimation);
      ServiceLocator.getCyclopsMinigameEventHandler().trigger(CYCLOPS_WAKE_EVENT);
      // A miss costs a heart and retries the current statue; losing all hearts restarts the
      // minigame.
      changeState(playerIsDead() ? State.DEATH : State.LOSS);
    }
  }

  private boolean playerIsDead() {
    CombatStatsComponent combatStats = playerEntity.getComponent(CombatStatsComponent.class);
    return combatStats != null && combatStats.isDead();
  }

  private void handlePlaying(float delta) {
    timingBarLogic.update(delta);
    if (stopPressed()) {
      timingBarLogic.stopMarker();
      playCorrectMarkerSound();
      changeState(State.HIDE_DELAY);
    }
  }

  private boolean canForceOutcome(String outcome) {
    if (state == State.PLAY) {
      return true;
    }
    logger.info("Ignoring forced {} outcome while in state {}", outcome, state);
    return false;
  }

  void timingSuccess() {
    if (!canForceOutcome("success")) return;
    timingBarLogic.stopMarker();
    timingBarDisplay.setVisible(false);
    beginRun(true);
    changeState(State.MOVING);
  }

  void timingFailure() {
    if (!canForceOutcome("failure")) return;
    timingBarLogic.stopMarker();
    timingBarDisplay.setVisible(false);
    beginRun(false);
    changeState(State.MOVING);
  }

  public void startMinigame() {
    changeState(State.SHOW_DELAY);
  }

  public void stopMinigame() {
    changeState(State.STOP);
    timingBarLogic.stopMarker();

    stopMarkerMissSound();
    stopMarkerHitSound();
    stopWalkingSound();
  }

  public void restartMinigame() {
    currentSafeLocation = 0;
    timingBarLogic.changeScoringAreaWidth(START_SCORING_PERCENT);
    movePlayer(safeLocations.getFirst());
    ServiceLocator.getCyclopsMinigameEventHandler().trigger(CYCLOPS_SLEEP_EVENT);
    startMinigame();
  }

  private void beginRetreat() {
    runStart = playerEntity.getPosition().cpy();
    runTarget =
        terrainComponent
            .tileToWorldPosition(safeLocations.get(currentSafeLocation))
            .add(playerOffset);
    playWalkingSound();
    playerEntity.getEvents().trigger("walk", new Vector2(-1, 0));
    playerEntity.getEvents().trigger("sprint");
    changeState(State.RETREAT);
  }

  private void finishRetreat() {
    playerEntity.setPosition(runTarget);
    playerEntity.getEvents().trigger("sprintStop");
    playerEntity.getEvents().trigger("walkStop");
    stopWalkingSound();
    playerEntity.getEvents().trigger("walk", new Vector2(1, 0));
    playerEntity.getEvents().trigger("walkStop");
    ServiceLocator.getCyclopsMinigameEventHandler().trigger(CYCLOPS_SLEEP_EVENT);
    startMinigame();
  }

  /* Component Overrides */
  @Override
  public void create() {
    loadSounds();

    ServiceLocator.getCyclopsMinigameEventHandler().addListener("start", this::startMinigame);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("stop", this::stopMinigame);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("restart", this::restartMinigame);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("success", this::timingSuccess);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("failure", this::timingFailure);

    this.transitionScreenCover = new BlankTransitionScreenCover();
    ServiceLocator.getEntityService()
        .register(new Entity().addComponent(this.transitionScreenCover));
    this.state = State.STOP;
  }

  @Override
  public void update() {
    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    timeInState += delta;

    switch (state) {
      case STOP -> {
        /* Do Nothing / Wait for External Input */
      }
      case WIN -> {
        /* TODO: Connect to next level / cutscene */
        logger.info("Player has WON the cyclops minigame");
        changeState(State.STOP);
        Gdx.app.postRunnable(() -> ServiceLocator.getCyclopsMinigameEventHandler().trigger("win"));
      }
      case LOSS -> {
        if (elapsed(HURT_PAUSE_DELAY)) {
          beginRetreat();
        }
      }
      case RETREAT -> {
        if (elapsed(TRANSITION_DELAY)) {
          finishRetreat();
        } else {
          float progress = timeInState / TRANSITION_DELAY;
          playerEntity.setPosition(runStart.cpy().lerp(runTarget, progress));
        }
      }
      case DEATH -> {
        if (elapsed(DEATH_DISPLAY_DELAY)) {
          logger.info("Player has DIED in the cyclops minigame");
          transitionScreenCover.setVisible(true);
          ServiceLocator.getCyclopsMinigameEventHandler().trigger("died");
          changeState(State.STOP);
        }
      }
      case SHOW_DELAY -> {
        if (elapsed(SHOW_HIDE_DELAY)) {
          timingBarDisplay.setVisible(true);
          timingBarLogic.resetMarker();
          timingBarLogic.startMarker();
          changeState(State.PLAY);
        }
      }
      case PLAY -> handlePlaying(delta);
      case HIDE_DELAY -> {
        if (elapsed(SHOW_HIDE_DELAY)) {
          timingBarDisplay.setVisible(false);
          changeState(State.PRE_MOVE);
        }
      }
      case PRE_MOVE -> {
        if (elapsed(TRANSITION_DELAY_GAP)) {
          beginRun(timingBarLogic.checkHit());
          changeState(State.MOVING);
        }
      }
      case MOVING -> {
        if (elapsed(TRANSITION_DELAY)) {
          playerEntity.setPosition(runTarget);
          playerEntity.getEvents().trigger("sprintStop");
          playerEntity.getEvents().trigger("walkStop");
          stopWalkingSound();
          handleTimingOutcome(); /* Changes state itself */
        } else {
          float progress = timeInState / TRANSITION_DELAY;
          playerEntity.setPosition(runStart.cpy().lerp(runTarget, progress));
        }
      }
    }
  }

  @Override
  public void dispose() {
    stopMarkerHitSound();
    stopMarkerMissSound();
    stopWalkingSound();

    super.dispose();
  }
}
