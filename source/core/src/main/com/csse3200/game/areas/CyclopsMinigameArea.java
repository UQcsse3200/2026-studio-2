package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.minigames.CyclopsTimingBar.CyclopsMinigameLogic;
import com.csse3200.game.components.minigames.CyclopsTimingBar.TimingBarDisplay;
import com.csse3200.game.components.minigames.CyclopsTimingBar.TimingBarLogic;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameArea.class);
  private static final String[] cyclopsMinigameTextures = {
    "images/box_boy_leaf.png",
    "images/transparent.png",
    "images/Greek Statues Pack I/Brute.png",
    "images/CyclopsMinigameFloor.png"
  };

  private final TerrainFactory terrainFactory;

    private Entity player;

  private static final int NUM_STATUES = 3;
  private int statueYLevel;
  private GridPoint2 winLocation;
  private ArrayList<GridPoint2> statueLocations;
  private ArrayList<GridPoint2> statueGapLocations;
  private CyclopsMinigameLogic cyclopsMinigameLogic;

  public CyclopsMinigameArea(CameraComponent camera, TerrainFactory terrainFactory) {
    super(camera);
    this.terrainFactory = terrainFactory;
  }

  /** Create the game area in the world. */
  @Override
  public void create() {
    loadAssets();

    displayUI();
    spawnTerrain();
    displayFloor();
    spawnStatues();

    player = spawnPlayer();

    setupTimingMinigame();
    startTimingMinigame();
  }

  /**
   * Initialises all timing minigame components. Creates the TimingBarLogic component,
   * TimingBarDisplay component, and the CyclopsMinigameLogic.
   *
   * <p>Sets the win, safe and loss locations that the player moves to as the statue locations and
   * statue gap locations.
   */
  private void setupTimingMinigame() {
      /* Timing Minigame Components */
      TimingBarLogic timingBarLogic = new TimingBarLogic(20f);
      TimingBarDisplay timingBarDisplay = new TimingBarDisplay(timingBarLogic);
    cyclopsMinigameLogic =
        new CyclopsMinigameLogic(timingBarLogic, timingBarDisplay, terrain, player);
    cyclopsMinigameLogic.setWinLocation(winLocation);
    cyclopsMinigameLogic.setSafeLocations(statueLocations);
    cyclopsMinigameLogic.setLossLocations(statueGapLocations);

      Entity timingMinigameEntity = new Entity();
    timingMinigameEntity.addComponent(timingBarDisplay);
    timingMinigameEntity.addComponent(cyclopsMinigameLogic);
    spawnEntity(timingMinigameEntity);
  }

  private void startTimingMinigame() {
    cyclopsMinigameLogic.startMinigame();
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Cyclops Minigame Room"));
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainFactory.TerrainType.CYCLOPS_ROOM);
    spawnEntity(new Entity().addComponent(terrain));

    statueYLevel = (int) (terrain.getMapBounds(terrain.getLayer()).y * 0.1);
    winLocation = new GridPoint2(terrain.getMapBounds(terrain.getLayer()).x + 10, statueYLevel);
  }

  private void spawnStatues() {
    this.statueLocations = new ArrayList<>(NUM_STATUES);
    this.statueGapLocations = new ArrayList<>(NUM_STATUES);

    GridPoint2 mapSize = terrain.getMapBounds(terrain.getLayer());

    for (int i = 1; i <= NUM_STATUES; i++) {
      /* Formula for equally spacing out statues.
       Idea was to have equal spacing for all statues (mapSize.x / NUM_STATUES).
       This splits the map into (currently thirds), then * i (statue number) to place
       in correct position.

       This is then offset by (mapSize.x / num_statues*2) which effectively gets the middle
       of the gap between two statues / locations.

       -2 is just to better offset it and can be adjusted freely
      */
      int x = ((mapSize.x / NUM_STATUES) * i) - (mapSize.x / (NUM_STATUES * 2)) - 2;
      GridPoint2 location = new GridPoint2(x - 1, statueYLevel);
      statueLocations.add(location);

      Entity statue = ObstacleFactory.createStatue();
      statue.setScale(new Vector2(3, 6));
      spawnEntityAt(statue, new GridPoint2(x, statueYLevel), true, false);

      int gapX = (mapSize.x / NUM_STATUES) * i - 2;
      GridPoint2 gapLocation = new GridPoint2(gapX, statueYLevel);
      statueGapLocations.add(gapLocation);
    }
  }

  /** Creates and displays the floor entity that spans the entire screen */
  private void displayFloor() {
    Entity floor =
        new Entity()
            .addComponent(new TextureRenderComponent("images/CyclopsMinigameFloor.png"))
            .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE));
    floor.getComponent(TextureRenderComponent.class).scaleEntity();
    floor.setScale(20, 5);
    spawnEntityAt(floor, new GridPoint2(-5, 2), false, false);
  }

  /**
   * Spawns the player at the first statue location.
   *
   * @return the created player Entity
   */
  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayerDisplay();
    spawnEntityAt(newPlayer, statueLocations.getFirst(), true, false);
    return newPlayer;
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(cyclopsMinigameTextures);

    while (!resourceService.loadForMillis(10)) {
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(cyclopsMinigameTextures);
  }

  @Override
  public void dispose() {
    super.dispose();
    this.unloadAssets();
  }
}
