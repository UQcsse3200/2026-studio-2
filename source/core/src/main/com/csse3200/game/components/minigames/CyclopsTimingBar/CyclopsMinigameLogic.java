package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
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
  };

  private GameEndState outcome = GameEndState.WIN;

  private State state;

  /* Minigame Components */
  private final TimingBarLogic timingBarLogic;
  private final TimingBarDisplay timingBarDisplay;

  /* Screen Components */
  private BlankTransitionScreen transitionScreen;

  /* Timing Components */
  private static final float TIMING_BAR_DELAY = 0.3f; // 0.3 of a second
  private static final float TRANSITION_CHANGE_DELAY = 0.3f; // 0.3 of a second
  private static final float TRANSITION_DELAY = 0.5f; // 0.3 of a second

  /* Player */
  private Entity player;

  /* Map Info */
  private TerrainComponent terrain;
  private GridPoint2 winLocation;
  private List<GridPoint2> safeLocations;
  private List<GridPoint2> lossLocations;
  private int numOfSafeLocs;
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

  public void setWinLocation(GridPoint2 winLocation) {
    this.winLocation = winLocation;
  }

  public void setLossLocations(List<GridPoint2> lossLocations) {
    this.lossLocations = lossLocations;
  }

  public void setSafeLocations(List<GridPoint2> safeLocations) {
    this.safeLocations = safeLocations;
    this.numOfSafeLocs = safeLocations.size();
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
  private boolean moveToNextLocation(boolean success) {
    if (success) {
      currentSafeLoc += 1;

      if (currentSafeLoc >= numOfSafeLocs) {
        player.setPosition(terrain.tileToWorldPosition(winLocation));
        return false;
      }

      player.setPosition(terrain.tileToWorldPosition(safeLocations.get(currentSafeLoc)));

    } else {
      player.setPosition(terrain.tileToWorldPosition(lossLocations.get(currentSafeLoc)));
    }

    return true;
  }

  /**
   * Update the timing bar logic and checks whether the 'space bar' has been hit to stop the timing
   * bar
   */
  private void updatePlaying() {
    timingBarLogic.update(Gdx.graphics.getDeltaTime());
    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      stopTimingBar();
    }
  }

  /** Starts the timing bar logic and show the timing bar display after a delay */
  private void startTimingBar() {
    Timer.schedule(
        new Task() {
          @Override
          public void run() {
            timingBarDisplay.setVisible(true);
            timingBarLogic.startMarker();
            state = State.PLAYING;
          }
        },
        TIMING_BAR_DELAY);
  }

  /** Stops the timingBarLogic marker. And after a delay hide the timingBarDisplay */
  private void stopTimingBar() {
    timingBarLogic.stopMarker();

    Timer.schedule(
        new Task() {
          @Override
          public void run() {
            timingBarDisplay.setVisible(false);
            startTransition();
          }
        },
        TIMING_BAR_DELAY);
  }

  /** Schedules the transition screen to appear after a delay */
  private void startTransition() {
    Timer.schedule(
        new Task() {
          @Override
          public void run() {
            transitionScreen.setVisible(true);
            transition();
          }
        },
        TRANSITION_CHANGE_DELAY);
  }

  /**
   * Schedules the transition screen to hide and either restarts the timing minigame, or moves the
   * state of the game to GAME_OVER, after a certain time has passed;
   *
   * @param restart - True and the timing bar minigame starts over
   */
  private void stopTransition(boolean restart) {
    Timer.schedule(
        new Task() {
          @Override
          public void run() {
            transitionScreen.setVisible(false);
            if (restart) startTimingBar();
            else state = State.GAME_OVER;
          }
        },
        TRANSITION_DELAY);
  }

  /** */
  private void transition() {
    boolean success = timingBarLogic.checkHit();

    boolean moved = moveToNextLocation(success);

    if (moved && success) {
      // keep playing
      stopTransition(true);
      return;
    }

    state = State.GAME_OVER;
    if (moved) {
      // Moved to loss position
      outcome = GameEndState.LOSE;
    } else {
      // Didn't move, so game was won
      outcome = GameEndState.WIN;
    }
    stopTransition(false);
  }

  private void showEndState() {
    Timer.schedule(
        new Task() {
          @Override
          public void run() {
            ServiceLocator.getGameEndEventHandler().trigger("gameEnd", outcome);
          }
        },
        TRANSITION_DELAY);
    state = State.STOPPED;
  }

  @Override
  public void update() {
    switch (state) {
      case State.PLAYING:
        updatePlaying();
        break;
      case State.GAME_OVER:
        showEndState();
        break;
      default:
        // Waiting for another state to finish
        break;
    }
  }

  public void startMinigame() {
    logger.info("starting timing bar minigame");
    startTimingBar();
  }
}
