package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
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
  private GameTime timer = ServiceLocator.getTimeSource();
  private long savedTime;
  private boolean timingBarDelayActive = false;
  private long timingBarDelay = 500; // 0.5 of a second

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

  @Override
  public void update() {
    if (running) {

      // Checking any timers
      if (timingBarDelayActive) {
        if (timer.getTimeSince(savedTime) > timingBarDelay) {
          timingBarDisplay.setVisible(!timingBarDisplay.isVisible()); // Toggle visibility
          logic.startMarker();
          timingBarDelayActive = false;
          timingBarGameActive = !timingBarGameActive;
        }
      }

      if (timingBarGameActive) {
        if (logic != null && !logic.isStopped) {
          logic.update(Gdx.graphics.getDeltaTime());
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
          logger.info("sliding marker stopped");
          logic.stopMarker();
          // Set delay then hide, do a half second delay
          savedTime = timer.getTime();
          timingBarDelayActive = true;
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
    this.savedTime = timer.getTime();
    timingBarDelayActive = true;
    this.running = true;
  }
}
