package com.csse3200.game.components.minigames.cyclopsminigame;

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
  private static final String WALK_STOP = "walkStop";

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
    CAUGHT,
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
  public static final String CYCLOPS_HIT_EVENT = "cyclopsHit";
  static final float CAUGHT_TIMEOUT = 5f;
  static final float START_SCORING_PERCENT = 20f;
  static final float END_SCORING_PERCENT = 10f;

  /* Components */
  private final TimingBarLogic timingBarLogic;
  private final TimingBarDisplay timingBarDisplay;
  private final TerrainComponent terrainComponent;

  /* Player Entity */
  private final Entity playerEntity;
  private static final String PLAYER_LOSS_ANIMATION = "hurt";

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
  private static final String WALKING_SOUND_PATH = "sounds/walkingSounds/walkingSound.mp3";
  private static final float WALKING_SOUND_VOLUME = 0.4f;

  Sound hitSound;
  private long hitSoundID;
  private static final String HIT_SOUND_PATH = "sounds/minigames/cyclops/marker-hit.ogg";
  private static final float HIT_SOUND_VOLUME = 0.4f;

  Sound missSound;
  private long missSoundID;
  private static final String MISS_SOUND_PATH = "sounds/minigames/cyclops/marker-miss.ogg";
  private static final float MISS_SOUND_VOLUME = 0.4f;

  /**
   * CyclopsMinigameLogic Component Handles all game components of the minigame
   *
   * @param logic - TimingBarLogic component that handles moving the sweeping marker
   * @param display - TimingBarDisplay component that handles showing the sweeping marker and
   *     display
   * @param terrainComponent - TerrainComponent used to position game objects and the player within
   *     the game
   * @param player - Entity for the player that is used in the game area (Must have an animation
   *     component)
   */
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

  /**
   * set the offset of the player (the current vector location of the player)
   *
   * @param playerOffset - Vector2 of the current location of the player
   */
  public void setPlayerOffset(Vector2 playerOffset) {
    this.playerOffset = playerOffset.cpy();
  }

  /**
   * Set the win position of the game area that the player moves to on winning the minigame
   *
   * @param winLocation - GridPoint2 location for the final place to move the player
   */
  public void setWinLocation(GridPoint2 winLocation) {
    this.winLocation = winLocation;
  }

  /**
   * Sets the loss locations of the minigame. The positions the player moves to on unsuccessful
   * timing minigame outcomes
   *
   * @param lossLocations - List of GridPoint2 locations
   */
  public void setLossLocations(List<GridPoint2> lossLocations) {
    this.lossLocations = lossLocations;
  }

  /**
   * Sets the safe locations of the minigame. The positions the player moves to on successful timing
   * minigame outcomes
   *
   * @param safeLocations - List of GridPoint2 locations
   */
  public void setSafeLocations(List<GridPoint2> safeLocations) {
    this.safeLocations = safeLocations;
  }

  /**
   * Checks if there are more safe locations that can be moved to.
   *
   * @return True if more safe locations are accessible, otherwise False
   */
  public boolean areNextSafeLocations() {
    return this.currentSafeLocation < this.safeLocations.size();
  }

  /**
   * (USED FOR TESTING ONLY)
   *
   * <p>Get the set win location of the minigame
   *
   * @return GridPoint2
   */
  GridPoint2 getWinLocation() {
    return this.winLocation;
  }

  /**
   * (USED FOR TESTING ONLY)
   *
   * <p>Get the set loss locations of the minigame
   *
   * @return List of Gridpoint2
   */
  List<GridPoint2> getLossLocations() {
    return this.lossLocations;
  }

  /**
   * (USED FOR TESTING ONLY)
   *
   * <p>Get the set safe locations of the minigame
   *
   * @return List of Gridpoint2
   */
  List<GridPoint2> getSafeLocations() {
    return this.safeLocations;
  }

  /**
   * Convert the given GridPoint2 location to a Vector2 position
   *
   * @param location - The GridPoint2 location to convert
   * @return a Vector2 position
   */
  private Vector2 worldPosition(GridPoint2 location) {
    Vector2 position = terrainComponent.tileToWorldPosition(location);
    if (position == null) {
      throw new IllegalStateException(
          "Cyclops terrain has no world coordinates for tile " + location);
    }
    return position.add(playerOffset);
  }

  /**
   * Moves the player entity to the given GridPoint 2 location
   *
   * @param location - The location to move the player to
   */
  private void movePlayer(GridPoint2 location) {
    Vector2 worldPos = worldPosition(location);
    logger.debug("Converting GridPoint2 Location ({}) to World Position ({})", location, worldPos);
    logger.info(
        "Moving Player (Entity {}) from orig:{} to dest:{}",
        this.playerEntity.getId(),
        this.playerEntity.getPosition(),
        worldPos);
    playerEntity.setPosition(worldPos);
  }

  /**
   * Get the next move location based on the given success boolean.
   *
   * <p>Locations based on outcome: - True: The next safe location - True and no more safe
   * locations: The win location - False: The next loss location
   *
   * @param success - Whether the player is successful at the timing minigame
   * @return the GridPoint2 location of the next move
   */
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

  /**
   * Calculates and returns the next (smaller) size for the scoring area of the TimingBar
   *
   * <p>(Rocks are an object placed at every safe location)
   *
   * @param rockIndex - the index of the current rock position in the minigame
   * @param rockCount - the total number of rocks in the minigame
   * @return
   */
  static float scoringPercentForRock(int rockIndex, int rockCount) {
    int last = Math.max(rockCount - 1, 1);
    int clampedIndex = Math.min(rockIndex, last);
    return START_SCORING_PERCENT
        - (START_SCORING_PERCENT - END_SCORING_PERCENT) * clampedIndex / last;
  }

  /**
   * Checks whether the user has pressed a specific predefined BUTTON
   *
   * @return - True whether the BUTTON was pressed, otherwise False
   */
  private boolean stopPressed() {
    return Gdx.input.isButtonJustPressed(BUTTON);
  }

  /** Load the sound effects used */
  private void loadSounds() {
    walkingSound = ServiceLocator.getResourceService().getAsset(WALKING_SOUND_PATH, Sound.class);
    hitSound = ServiceLocator.getResourceService().getAsset(HIT_SOUND_PATH, Sound.class);
    missSound = ServiceLocator.getResourceService().getAsset(MISS_SOUND_PATH, Sound.class);
  }

  /** Play a walking sound effect on loop */
  private void playWalkingSound() {
    this.walkingSoundID = walkingSound.play();
    walkingSound.setLooping(this.walkingSoundID, true);
    walkingSound.setVolume(this.walkingSoundID, GameVolume.scale(WALKING_SOUND_VOLUME));
  }

  /** Stop the playing walking sound effect */
  private void stopWalkingSound() {
    if (walkingSound == null) {
      logger.warn("Attempting to stop walking sound when walkingSound variable is null");
      return;
    }
    walkingSound.stop(walkingSoundID);
    logger.debug("Stopped walking sound");
  }

  /** Plays a (good) hit sound effect */
  private void playMarkerHitSound() {
    this.hitSoundID = hitSound.play();
    hitSound.setVolume(this.hitSoundID, GameVolume.scale(HIT_SOUND_VOLUME));
    logger.debug("Starting hit sound (ID: {}) with volume {}", this.hitSoundID, HIT_SOUND_VOLUME);
  }

  /** Stops the playing hit sound effect */
  private void stopMarkerHitSound() {
    if (hitSound == null) {
      logger.warn("Attempting to stop hit sound when hitSound variable is null");
      return;
    }
    hitSound.stop(hitSoundID);
    logger.debug("Stopped hit sound");
  }

  /** Plays a (bad) miss sound effect */
  private void playMarkerMissSound() {
    this.missSoundID = missSound.play();
    missSound.setVolume(this.missSoundID, GameVolume.scale(MISS_SOUND_VOLUME));
    logger.debug(
        "Starting miss sound (ID: {}) with volume {}", this.missSoundID, MISS_SOUND_VOLUME);
  }

  /** Stops the playing miss sound effect */
  private void stopMarkerMissSound() {
    if (missSound == null) {
      logger.warn("Attempting to stop miss sound when missSound variable is null");
      return;
    }
    missSound.stop(missSoundID);
    logger.debug("Stopped miss sound");
  }

  /**
   * Plays the correct marker sound effect (hit or miss) based on whether the sweeping timing marker
   * is in the scoring zone
   */
  void playCorrectMarkerSound() {
    if (this.timingBarLogic.checkHit()) {
      playMarkerHitSound();
    } else {
      playMarkerMissSound();
    }
  }

  /**
   * Change the state of the minigame. Sets the new State and time counter
   *
   * @param next
   */
  void changeState(State next) {
    state = next;
    timeInState = 0f;
  }

  /**
   * Checks whether the time spent in the current game state is greater than the given time (in
   * seconds)
   *
   * @param seconds - the time passed since changing states (in seconds)
   * @return True if the time spent in the current game state is greater than the given time.
   */
  boolean elapsed(float seconds) {
    return timeInState >= seconds;
  }

  /**
   * Starts the running animation of the player between the current player position and the next
   * position based on the success boolean
   *
   * @param success - Whether the player was successful at the timing minigame
   */
  private void beginRun(boolean success) {
    runSuccess = success;
    runStart = playerEntity.getPosition().cpy();
    runTarget = worldPosition(advanceToNextLocation(runSuccess));
    playWalkingSound();
    playerEntity.getEvents().trigger("walk", new Vector2(1, 0));
    playerEntity.getEvents().trigger("sprint");
  }

  /**
   * Handles the output of the timing minigame
   *
   * <p>Outcomes: - Success : Starts the timing minigame again - Success and Has Won: Sets the game
   * state to WIN - Failure: Triggers the cyclops wake up event and moves to the CAUGHT game state
   */
  private void handleTimingOutcome() {
    if (runSuccess) {
      if (hasWon) changeState(State.WIN);
      else startMinigame();
    } else {
      ServiceLocator.getCyclopsMinigameEventHandler().trigger(CYCLOPS_WAKE_EVENT);
      changeState(State.CAUGHT);
    }
  }

  /**
   * Update the player's health with the damage done by the cyclops enemy.
   *
   * <p>If this killed the player, changes the game state to DEATH state
   */
  void takeCyclopsHit() {
    if (state != State.CAUGHT) {
      return;
    }
    CombatStatsComponent combatStats = playerEntity.getComponent(CombatStatsComponent.class);
    if (combatStats != null) {
      combatStats.addHealth(-MISS_DAMAGE);
    }
    playerEntity.getEvents().trigger(PLAYER_LOSS_ANIMATION);
    // A miss costs a heart and retries the current statue; losing all hearts restarts the
    // minigame.
    changeState(playerIsDead() ? State.DEATH : State.LOSS);
  }

  /** Wait for the cyclops hit animation to finish playing */
  private void waitForCyclopsHit() {
    if (elapsed(CAUGHT_TIMEOUT)) {
      takeCyclopsHit();
    }
  }

  /**
   * Check if the player has died (run out of health)
   *
   * @return True if the player is dead, otherwise False
   */
  private boolean playerIsDead() {
    CombatStatsComponent combatStats = playerEntity.getComponent(CombatStatsComponent.class);
    return combatStats != null && combatStats.isDead();
  }

  /**
   * Update the PLAYING state components. Moves the sweeping marker of the timing bar until the
   * player hits a specific button
   *
   * @param delta - the time passed since the last frame
   */
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

  /** Handle the actions that occur on a successful run of the timing minigame */
  void timingSuccess() {
    if (!canForceOutcome("success")) return;
    timingBarLogic.stopMarker();
    timingBarDisplay.setVisible(false);
    beginRun(true);
    changeState(State.MOVING);
  }

  /** Handle the actions that occur on an unsuccessful run of the timing minigame */
  void timingFailure() {
    if (!canForceOutcome("failure")) return;
    timingBarLogic.stopMarker();
    timingBarDisplay.setVisible(false);
    beginRun(false);
    changeState(State.MOVING);
  }

  /**
   * Starts the timing minigame. Shows the timing bar display after a short delay and begins moving
   * the timing bar
   */
  public void startMinigame() {
    changeState(State.SHOW_DELAY);
  }

  /** Stops the cyclops minigame stopping all sound effects and moving components */
  public void stopMinigame() {
    changeState(State.STOP);
    timingBarLogic.stopMarker();

    stopMarkerMissSound();
    stopMarkerHitSound();
    stopWalkingSound();
  }

  /**
   * Restarts the cyclops minigame to the initial setup. - Moves player to the starting location -
   * Resets the timing bar scoring area width to the starting value
   */
  public void restartMinigame() {
    hasWon = false;
    currentSafeLocation = 0;
    timingBarLogic.changeScoringAreaWidth(START_SCORING_PERCENT);
    movePlayer(safeLocations.getFirst());
    ServiceLocator.getCyclopsMinigameEventHandler().trigger(CYCLOPS_SLEEP_EVENT);
    startMinigame();
  }

  /**
   * Starts the player animation and moves the player from a failing location to the last safe
   * location and starts the walking sound effect
   */
  private void beginRetreat() {
    runStart = playerEntity.getPosition().cpy();
    runTarget = worldPosition(safeLocations.get(currentSafeLocation));
    playWalkingSound();
    playerEntity.getEvents().trigger("walk", new Vector2(-1, 0));
    playerEntity.getEvents().trigger("sprint");
    changeState(State.RETREAT);
  }

  /**
   * Stops the player walking animation and sound effect. Resetting the cyclops enemy and returns
   * the player to idle animation
   */
  private void finishRetreat() {
    playerEntity.setPosition(runTarget);
    playerEntity.getEvents().trigger("sprintStop");
    playerEntity.getEvents().trigger(WALK_STOP);
    stopWalkingSound();
    playerEntity.getEvents().trigger("walk", new Vector2(1, 0));
    playerEntity.getEvents().trigger(WALK_STOP);
    ServiceLocator.getCyclopsMinigameEventHandler().trigger(CYCLOPS_SLEEP_EVENT);
    startMinigame();
  }

  /**
   * Creates the CyclopsMinigameLogic and sets up the necessary event listeners for the dev commands
   */
  @Override
  public void create() {
    loadSounds();

    ServiceLocator.getCyclopsMinigameEventHandler().addListener("start", this::startMinigame);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("stop", this::stopMinigame);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("restart", this::restartMinigame);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("success", this::timingSuccess);
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("failure", this::timingFailure);
    ServiceLocator.getCyclopsMinigameEventHandler()
        .addListener(CYCLOPS_HIT_EVENT, this::takeCyclopsHit);

    this.transitionScreenCover = new BlankTransitionScreenCover();
    ServiceLocator.getEntityService()
        .register(new Entity().addComponent(this.transitionScreenCover));
    this.state = State.STOP;
  }

  /** Handles updating the minigame states depending on the current game state */
  @Override
  public void update() {
    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    timeInState += delta;

    switch (state) {
      case CAUGHT -> waitForCyclopsHit();
      case LOSS -> waitBeforeRetreat();
      case RETREAT -> updateRetreat();
      case DEATH -> updateDeath();
      case SHOW_DELAY -> showTimingBarWhenReady();
      case PLAY -> handlePlaying(delta);
      case HIDE_DELAY -> hideTimingBarWhenReady();
      case PRE_MOVE -> beginRunWhenReady();
      case MOVING -> updateRun();
      default -> {
        // STOP has no per-frame work: wait for the intro or an external start event.
      }
    }
  }

  private void finishMinigame() {
    logger.info("Player has WON the cyclops minigame");
    changeState(State.STOP);
    // The room screen handles this event and starts the next cutscene.
    Gdx.app.postRunnable(() -> ServiceLocator.getCyclopsMinigameEventHandler().trigger("win"));
  }

  private void waitBeforeRetreat() {
    if (elapsed(HURT_PAUSE_DELAY)) {
      beginRetreat();
    }
  }

  private void updateRetreat() {
    if (elapsed(TRANSITION_DELAY)) {
      finishRetreat();
    } else {
      float progress = timeInState / TRANSITION_DELAY;
      playerEntity.setPosition(runStart.cpy().lerp(runTarget, progress));
    }
  }

  private void updateDeath() {
    if (elapsed(DEATH_DISPLAY_DELAY)) {
      logger.info("Player has DIED in the cyclops minigame");
      transitionScreenCover.setVisible(true);
      ServiceLocator.getCyclopsMinigameEventHandler().trigger("died");
      changeState(State.STOP);
    }
  }

  private void showTimingBarWhenReady() {
    if (elapsed(SHOW_HIDE_DELAY)) {
      timingBarDisplay.setVisible(true);
      timingBarLogic.resetMarker();
      timingBarLogic.startMarker();
      changeState(State.PLAY);
    }
  }

  private void hideTimingBarWhenReady() {
    if (elapsed(SHOW_HIDE_DELAY)) {
      timingBarDisplay.setVisible(false);
      changeState(State.PRE_MOVE);
    }
  }

  private void beginRunWhenReady() {
    if (elapsed(TRANSITION_DELAY_GAP)) {
      beginRun(timingBarLogic.checkHit());
      changeState(State.MOVING);
    }
  }

  private void updateRun() {
    if (elapsed(TRANSITION_DELAY)) {
      playerEntity.setPosition(runTarget);
      playerEntity.getEvents().trigger("sprintStop");
      playerEntity.getEvents().trigger(WALK_STOP);
      stopWalkingSound();
      handleTimingOutcome();
    } else {
      float progress = timeInState / TRANSITION_DELAY;
      playerEntity.setPosition(runStart.cpy().lerp(runTarget, progress));
    }
  }

  /** Dispose all disposable components and stops any playing sound effects */
  @Override
  public void dispose() {
    stopMarkerHitSound();
    stopMarkerMissSound();
    stopWalkingSound();

    super.dispose();
  }
}
