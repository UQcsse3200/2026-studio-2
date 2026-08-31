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
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameLogicComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameLogicComponent.class);

  /* State Machine */
  protected enum State {
    STOPPED,
    PLAYING,
  }

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
  private GridPoint2 mapSize;
  private static final int Y_LEVEL = 3;
  private GridPoint2 winLocation;
  private ArrayList<GridPoint2> pillarLocations;
  private ArrayList<GridPoint2> lossLocations;
  private int numOfPillars = 3;
  private int currentPillar = 0;

  /**
   * Creates the game logic for the Cyclops minigame.
   *
   * @param logic - TimingBar Logic component
   * @param display - TimingBar display component
   * @param terrain
   * @param player - A display only player entity
   */
  public CyclopsMinigameLogicComponent(
      TimingBarLogic logic, TimingBarDisplay display, TerrainComponent terrain, Entity player) {
    this.timingBarLogic = logic;
    this.timingBarDisplay = display;
    this.terrain = terrain;
    this.player = player;

    this.state = State.STOPPED;

    initialiseComponents();
  }

  protected void initialiseComponents() {
    /* Screens - Transition, win, lose screen */
    this.transitionScreen = new BlankTransitionScreen();
    ServiceLocator.getEntityService().register(new Entity().addComponent(this.transitionScreen));

    /* Map / Terrain dependent components */
    this.mapSize = terrain.getMapBounds(terrain.getLayer());
    winLocation = new GridPoint2(mapSize.x - 1, Y_LEVEL);
    setupPillarLocations();
  }

  private void setupPillarLocations() {
    logger.info("Setting up pillar locations");
    this.pillarLocations = new ArrayList<>();
    this.lossLocations = new ArrayList<>();

    logger.info("Pillars used = {}", numOfPillars);

    for (int i = 1; i <= numOfPillars; i++) {
      int x = ((mapSize.x / (numOfPillars)) * i) - (mapSize.x / (numOfPillars * 2)) - 1;
      GridPoint2 pillar = new GridPoint2(x, Y_LEVEL);
      pillarLocations.add(pillar);
      logger.info("Pillar {} X world-location set to {}", i, pillar);

      int lossX = ((mapSize.x / (numOfPillars)) * i);
      GridPoint2 loss = new GridPoint2(lossX, Y_LEVEL);
      lossLocations.add(loss);
    }

    logger.info("Pillar locations setup");
  }

  private boolean moveToNextLocation(boolean success) {
    if (success) {
      currentPillar += 1;

      if (currentPillar >= numOfPillars) {
        player.setPosition(terrain.tileToWorldPosition(winLocation));
        return false;
      }

      player.setPosition(terrain.tileToWorldPosition(pillarLocations.get(currentPillar)));

    } else {
      player.setPosition(terrain.tileToWorldPosition(lossLocations.get(currentPillar)));
    }

    return true;
  }

  private void updatePlaying() {
    timingBarLogic.update(Gdx.graphics.getDeltaTime());
    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      stopTimingBar();
    }
  }

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

  private void stopTransition(boolean restart) {
    Timer.schedule(
        new Task() {
          @Override
          public void run() {
            transitionScreen.setVisible(false);
            if (restart) {
              startTimingBar();
            }
          }
        },
        TRANSITION_DELAY);
  }

  private void transition() {
    boolean success = timingBarLogic.checkHit();

    boolean moved = moveToNextLocation(success);

    if (moved && success) {
      // keep playing
      stopTransition(true);
    } else if (moved) {
      // lost
      stopTransition(false);
    } else {
      // Show win screen
      stopTransition(false);
    }
  }

  @Override
  public void update() {
    switch (state) {
      case State.PLAYING:
        updatePlaying();
        break;
      default:
        // Waiting for another state to finish
        break;
    }
  }

  public void startMinigame() {
    logger.info("starting timing bar minigame");
    player.setPosition(terrain.tileToWorldPosition(pillarLocations.getFirst()));
    startTimingBar();
  }
}
