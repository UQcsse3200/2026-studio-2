package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameLogicComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameLogicComponent.class);

  /* Minigame Components */
  private final TimingBarLogic logic;
  private final TimingBarDisplay timingBarDisplay;
  private boolean running = false;

  /* Timing Components */
  private static final float timingBarDelay = 0.5f; // 0.5 of a second

  private boolean timingBarGameActive = false;
  private boolean pillarTransitioning = false;

  /* Player */
  private Entity player;
  private GridPoint2 startingLocation;

  /* Map Info */
  private TerrainComponent terrain;
  private final GridPoint2 MAPSIZE;
  private ArrayList<GridPoint2> pillarLocations;
  private int numOfPillars = 3;
  private int currentPillar = 0;
  private static final int Y = 3;

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
    this.logic = logic;
    this.timingBarDisplay = display;
    this.terrain = terrain;
    this.player = player;

    this.MAPSIZE = terrain.getMapBounds(terrain.getLayer());
    setupPillarLocations();
  }

  private void setupPillarLocations() {
    logger.info("Setting up pillar locations");
    this.pillarLocations = new ArrayList<>();

    logger.info("Pillars used = {}", numOfPillars);

    for (int i = 1; i <= numOfPillars; i++) {
      int x = ((MAPSIZE.x / (numOfPillars)) * i) - (MAPSIZE.x / (numOfPillars * 2)) - 1;
      GridPoint2 pillar = new GridPoint2(x, Y);
      pillarLocations.add(pillar);
      logger.info("Pillar {} X world-location set to {}", i, pillar);
    }

    logger.info("Pillar locations setup");
  }

  private boolean moveToNextPillar() {
    currentPillar += 1;

    if (currentPillar >= numOfPillars) return false;

    player.setPosition(terrain.tileToWorldPosition(pillarLocations.get(currentPillar)));
    return true;
  }

  private void toggleTimingBarGame() {
    Timer.schedule(
        new Task() {
          @Override
          public void run() {
            logic.startMarker();
            timingBarDisplay.setVisible(!timingBarDisplay.isVisible());
            timingBarGameActive = !timingBarGameActive;
          }
        },
        timingBarDelay);
  }

  @Override
  public void update() {
    if (running) {

      if (timingBarGameActive) {
        if (logic != null && !logic.isStopped) {
          logic.update(Gdx.graphics.getDeltaTime());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
          logger.info("sliding marker stopped");
          logic.stopMarker();
          toggleTimingBarGame();
        }
      }

      // check if player has reached end of game
      checkDevInputs();
    }
  }

  private void checkDevInputs() {
    if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) { // DEV TOOL Sort of
      logger.info("DEV: activated timing bar");
      logic.startMarker();
      timingBarDisplay.setVisible(true);
      timingBarGameActive = true;
      running = true;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.COMMA)) { // DEV TOOL
      logger.info("DEV: move player forward");
      moveToNextPillar();
    }
  }

  public void startMinigame() {
    logger.info("starting timing bar minigame");

    startingLocation = pillarLocations.getFirst();
    player.setPosition(terrain.tileToWorldPosition(startingLocation));

    this.running = true;
    toggleTimingBarGame();
  }
}
