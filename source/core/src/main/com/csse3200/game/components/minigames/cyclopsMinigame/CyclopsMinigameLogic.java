package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndState;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameLogic extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameLogic.class);

  /* State Machine */
  private enum State {
    STOPPED,
    PLAYING,
    GAME_OVER
  }

  private GameEndState outcome = GameEndState.LOSE;

  private State state;

  /* Minigame Components */
  private final TimingBarLogic timingBarLogic;
  private final TimingBarDisplay timingBarDisplay;

  /* Screen Components */
  private BlankTransitionScreen transitionScreen;

  /* Music / Sound effect components*/
  private Sound sound;
  private long soundId;

  /* Timing Components */
  private static final float TIMING_BAR_DELAY = 0.3f; // 0.3 of a second
  private static final float TRANSITION_CHANGE_DELAY = 0.3f; // 0.3 of a second
  private static final float TRANSITION_DELAY = 0.5f; // 0.3 of a second

  /* Player */
  private final Entity player;

  /* Map Info */
  private final TerrainComponent terrain;
  private GridPoint2 winLocation;
  private List<GridPoint2> safeLocations;
  private List<GridPoint2> lossLocations;
  private int currentSafeLoc = 0;

  /**
   * Creates the game logic for the Cyclops minigame.
   *
   * @param logic - TimingBar Logic component
   * @param display - TimingBar display component
   * @param terrain
   * @param player - A display only player entity
   */
  public CyclopsMinigameLogic(
      TimingBarLogic logic, TimingBarDisplay display, TerrainComponent terrain, Entity player) {
    this.timingBarLogic = logic;
    this.timingBarDisplay = display;
    this.terrain = terrain;
    this.player = player;

    this.state = State.STOPPED;

    this.transitionScreen = new BlankTransitionScreen();
    ServiceLocator.getEntityService().register(new Entity().addComponent(this.transitionScreen));
  }

  @Override
  public void create() {
    EventHandler eventHandler = ServiceLocator.getCyclopsMinigameEventHandler();
    eventHandler.addListener("startMinigame", this::startMinigame);
    eventHandler.addListener("restartMinigame", this::restartMinigame);
    eventHandler.addListener("timingSuccess", this::onTimingSuccess);
    eventHandler.addListener("timingFailure", this::onTimingFailure);
    eventHandler.addListener("showTimingBar", this::showTimingBar);
    eventHandler.addListener("hideTimingBar", this::hideTimingBar);

    sound =
        ServiceLocator.getResourceService()
            .getAsset("sounds/walkingSounds/walkingSound.mp3", Sound.class);
  }

  @Override
  public void update() {
    switch (state) {
      case PLAYING -> updatePlaying();
      case GAME_OVER -> scheduleGameOver();
    }
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

  private void movePlayer(GridPoint2 location) {
    player.setPosition(terrain.tileToWorldPosition(location));
  }

  /**
   * Moves the player to the next locations.
   *
   * <p>If 'success' player moves to next safe location. Returns False is player has moved to the
   * win position otherwise True if player moves to next safe location. If not 'success' then player
   * is moved to the next loss location and returns True
   *
   * @param success - boolean on whether to move player to next win or loss location
   * @return True if player moved location, otherwise false if moved to win location
   */
  private void moveToNextLocation(boolean success) {
    if (success) {
      currentSafeLoc += 1;
      if (currentSafeLoc >= safeLocations.size()) {
        player.setPosition(terrain.tileToWorldPosition(winLocation));
        state = State.GAME_OVER;
        outcome = GameEndState.WIN;
        return;
      }
      movePlayer(safeLocations.get(currentSafeLoc));
    } else {
      movePlayer(lossLocations.get(currentSafeLoc));
    }
  }

  private void updatePlaying() {
    timingBarLogic.update(Gdx.graphics.getDeltaTime());
    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      timingBarLogic.stopMarker();
      scheduleTimingMinigameHide();
    }
  }

  private void showTimingBar() {
    timingBarDisplay.setVisible(true);
  }

  private void hideTimingBar() {
    timingBarDisplay.setVisible(false);
  }

  private void onTimingSuccess() {
    moveToNextLocation(true);
  }

  private void onTimingFailure() {
    moveToNextLocation(false);
  }

  private void playWalkingSound() {
    soundId = sound.play();
    sound.setLooping(soundId, true);
    sound.setVolume(soundId, 0.1f);
  }

  private void transition() {
    if (timingBarLogic.checkHit()) {
      onTimingSuccess();
    } else {
      onTimingFailure();
    }
  }

  private void stopWalkingSound() {
    sound.stop(soundId);
  }

  private void onTransitionStart() {
    transitionScreen.setVisible(true);
    playWalkingSound();
    transition();
  }

  private void onTransitionEnd() {
    stopWalkingSound();
    transitionScreen.setVisible(false);
  }

  public void restartMinigame() {
    logger.info("restarting minigame");
  }

  public void startMinigame() {
    logger.info("starting minigame");
    scheduleTimingMinigameShow();
  }

  public void gameOver() {
    ServiceLocator.getGameEndEventHandler().trigger("gameEnd", outcome);
    state = State.STOPPED;
  }

  private void scheduleTimingMinigameShow() {
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            showTimingBar();
            timingBarLogic.resetMarker();
            timingBarLogic.startMarker();
            state = State.PLAYING;
          }
        },
        0.3f);
  }

  private void scheduleTimingMinigameHide() {
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            hideTimingBar();
            scheduleTransitionStart();
          }
        },
        0.3f);
  }

  private void scheduleTransitionStart() {
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            onTransitionStart();
            scheduleTransitionEnd();
          }
        },
        0.2f);
  }

  private void scheduleTransitionEnd() {
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            onTransitionEnd();

            if (state != State.GAME_OVER) {
              scheduleTimingMinigameShow();
            }
          }
        },
        0.8f);
  }

  private void scheduleGameOver() {
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            gameOver();
          }
        },
        1f);
  }
}
