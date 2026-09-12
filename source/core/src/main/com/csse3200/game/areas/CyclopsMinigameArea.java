package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameLogic;
import com.csse3200.game.components.minigames.cyclopsMinigame.TimingBarDisplay;
import com.csse3200.game.components.minigames.cyclopsMinigame.TimingBarLogic;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameArea.class);
  private static final String[] cyclopsMinigameTextures = {
    "images/black_roof.png",
    "images/purple_heart.png",
    "images/transparent.png",
    "images/DevGridTile.png",
    "images/Tile_2.png",
    "images/platform.png",
    "images/box_boy_leaf.png",
    "images/spike.png",
    "images/tree.png",
    "images/ghost_king.png",
    "images/ghost_1.png",
    "images/grass_1.png",
    "images/grass_2.png",
    "images/grass_3.png",
    "images/hex_grass_1.png",
    "images/hex_grass_2.png",
    "images/hex_grass_3.png",
    "images/iso_grass_1.png",
    "images/iso_grass_2.png",
    "images/iso_grass_3.png",
    "images/transparent.png",
    "images/Greek Statues Pack I/Brute.png",
    "images/CyclopsMinigameFloor.png"
  };

  private static final String[] cyclopsMinigameTexturesAtlases = {"images/player.atlas"};

  private static final String[] cyclopsMinigameSounds = {"sounds/walkingSounds/walkingSound.mp3"};

  private final TerrainFactory terrainFactory;

  private Entity player;
  private Entity minigame;

  private static final GridPoint2 MAP_SIZE = new GridPoint2(40, 30);
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

    minigame = new Entity();
    minigame.addComponent(timingBarDisplay);
    minigame.addComponent(cyclopsMinigameLogic);
    spawnEntity(minigame);
  }

  private void startTimingMinigame() {
    cyclopsMinigameLogic.startMinigame();
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainFactory.TerrainType.CYCLOPS_ROOM);
    spawnEntity(new Entity().addComponent(terrain));

    statueYLevel = 5;
    winLocation = new GridPoint2(MAP_SIZE.x + 10, statueYLevel);

    Entity cameraEntityHolder = new Entity();
    spawnEntityAt(cameraEntityHolder, new GridPoint2(MAP_SIZE.x / 2, MAP_SIZE.y / 2), false, false);
    this.cameraComponent.setTarget(cameraEntityHolder);
  }

  private void spawnStatues() {
    this.statueLocations = new ArrayList<>(NUM_STATUES);
    this.statueGapLocations = new ArrayList<>(NUM_STATUES);

    for (int i = 1; i <= NUM_STATUES; i++) {
      /* Formula for equally spacing out statues.
       Idea was to have equal spacing for all statues (mapSize.x / NUM_STATUES).
       This splits the map into (currently thirds), then * i (statue number) to place
       in correct position.

       This is then offset by (mapSize.x / num_statues*2) which effectively gets the middle
       of the gap between two statues / locations.

       -2 is just to better offset it and can be adjusted freely
      */
      int x = ((MAP_SIZE.x / NUM_STATUES) * i) - (MAP_SIZE.x / (NUM_STATUES * 2)) - 2;
      GridPoint2 location = new GridPoint2(x, statueYLevel);
      statueLocations.add(location);

      Entity statue = ObstacleFactory.createStatue();
      statue.setScale(new Vector2(3, 6));
      spawnEntityAt(statue, new GridPoint2(x, statueYLevel), true, false);

      int gapX = (MAP_SIZE.x / NUM_STATUES) * i - 2;
      GridPoint2 gapLocation = new GridPoint2(gapX, statueYLevel);
      statueGapLocations.add(gapLocation);
    }
  }

  /** Creates and displays the floor entity that spans the entire screen */
  private void displayFloor() {
    spawnEntityAt(ObstacleFactory.createWall(MAP_SIZE.x, 0.1f), new GridPoint2(0, 1), true, true);
  }

  /**
   * Spawns the player at the first statue location.
   *
   * @return the created player Entity
   */
  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayerDisplay();
    spawnEntityAt(newPlayer, statueLocations.getFirst(), false, true);
    return newPlayer;
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(cyclopsMinigameTextures);
    resourceService.loadTextureAtlases(cyclopsMinigameTexturesAtlases);
    resourceService.loadSounds(cyclopsMinigameSounds);

    while (!resourceService.loadForMillis(10)) {
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(cyclopsMinigameTextures);
    resourceService.unloadAssets(cyclopsMinigameTexturesAtlases);
    resourceService.unloadAssets(cyclopsMinigameSounds);
  }

  @Override
  public void dispose() {
    super.dispose();
    this.unloadAssets();
  }
}
