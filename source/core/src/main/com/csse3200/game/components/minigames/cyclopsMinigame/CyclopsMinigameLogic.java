package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.Component;
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
    WIN
  }

  State state = State.STOP;
  float timeInState = 0f;
  private boolean hasWon = false;

  static final float SHOW_HIDE_DELAY = 0.3f;
  static final float TRANSITION_DELAY_GAP = 0.2f;
  static final float TRANSITION_DELAY = 0.8f;

  /* Components */
  private final TimingBarLogic timingBarLogic;
  private final TimingBarDisplay timingBarDisplay;
  private final TerrainComponent terrainComponent;

  /* Player Entity */
  private final Entity playerEntity;
  private static final String playerLossAnimation = "hurt";

  private BlankTransitionScreenCover transitionScreenCover;

  /* Important Grid Locations */
  private List<GridPoint2> safeLocations;
  private List<GridPoint2> lossLocations;
  private GridPoint2 winLocation;
  private int currentSafeLocation = 0;

  /* Audio/Sound Effects */
  Sound walkingSound;
  long walkingSoundID;
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
    Vector2 worldPos = this.terrainComponent.tileToWorldPosition(location);
    logger.debug("Converting GridPoint2 Location ({}) to World Position ({})", location, worldPos);
    logger.info(
        "Moving Player (Entity {}) from orig:{} to dest:{}",
        this.playerEntity.getId(),
        this.playerEntity.getPosition(),
        worldPos);
    playerEntity.setPosition(worldPos);
  }

  void moveToNextLocation(boolean success) {
    if (success) {
      this.currentSafeLocation++;

      if (!areNextSafeLocations()) { // Then player will move to win location
        logger.info("Moving Player (Entity {}) to win location", this.playerEntity.getId());
        movePlayer(this.winLocation);
        hasWon = true;
        return;
      }

      logger.info(
          "Moving Player (Entity {}) to next safe location #{}",
          this.playerEntity.getId(),
          this.currentSafeLocation);
      movePlayer(safeLocations.get(this.currentSafeLocation));
    } else {
      logger.info(
          "Moving Player (Entity {}) to loss location #{}",
          this.playerEntity.getId(),
          this.currentSafeLocation);
      movePlayer(lossLocations.get(this.currentSafeLocation));
    }
  }

  void timingSuccess() {
    moveToNextLocation(true);
  }

  void timingFailure() {
    moveToNextLocation(false);
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
    walkingSound.setVolume(this.walkingSoundID, walkingSoundVolume);
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
    hitSound.setVolume(this.hitSoundID, hitSoundVolume);
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
    missSound.setVolume(this.missSoundID, missSoundVolume);
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

  private void handleTimingOutcome() {
    if (this.timingBarLogic.checkHit()) {
      timingSuccess();
      if (hasWon) changeState(State.WIN);
      else startMinigame();
    } else {
      timingFailure();
      changeState(State.LOSS);
    }
  }

  private void handlePlaying(float delta) {
    timingBarLogic.update(delta);
    if (stopPressed()) {
      timingBarLogic.stopMarker();
      playCorrectMarkerSound();
      changeState(State.HIDE_DELAY);
    }
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
    movePlayer(safeLocations.getFirst());
    startMinigame();
  }

  /* Component Overrides */
  @Override
  public void create() {
    loadSounds();

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
      }
      case LOSS -> {
        /* Want to play a scream from cyclops, screen go black and after delay
         * show player back at start of level
         */
        logger.info("Player has LOST the cyclops minigame");
        changeState(State.STOP);
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
          transitionScreenCover.setVisible(true);
          playWalkingSound();
          changeState(State.MOVING);
        }
      }
      case MOVING -> {
        if (elapsed(TRANSITION_DELAY)) {
          transitionScreenCover.setVisible(false);
          stopWalkingSound();
          handleTimingOutcome(); /* Changes state itself */
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
