package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tutorial area for the game with platforms, enemies, and a player. */
public class TutorialGameArea extends GameArea {

  private static final Logger logger =
      LoggerFactory.getLogger(TutorialGameArea.class);

  private static final int NUM_TREES = 7;
  private static final int NUM_GHOSTS = 2;

  private static final GridPoint2 PLAYER_SPAWN =
      new GridPoint2(10, 10);

  private static final float WALL_WIDTH = 0.1f;

  /** Textures used by the tutorial game area. */
  private static final String[] forestTextures = {

    // Existing game textures
    "images/black_roof.png",
    "images/transparent.png",
    "images/Tile_2.png",
    "images/platform.png",
    "images/box_boy_leaf.png",
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

    // Parallax background layers
    "images/parallax/sky.png",
    "images/parallax/Clouds.png",
    "images/parallax/Mountains.png",
    "images/parallax/ground.png",
    "images/parallax/Rocks.png"
  };

  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas",
    "images/ghost.atlas",
    "images/ghostKing.atlas"
  };

  private static final String[] forestSounds = {
    "sounds/Impact4.ogg"
  };

  private static final String backgroundMusic =
      "sounds/BGM_03_mp3.mp3";

  private static final String[] forestMusic = {
    backgroundMusic
  };

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  private Entity player;

  /**
   * Initialise this TutorialGameArea using the provided
   * TerrainFactory and CameraComponent.
   *
   * @param terrainFactory TerrainFactory used to create the terrain.
   * @param camera CameraComponent used by the parallax background.
   */
  public TutorialGameArea(
      TerrainFactory terrainFactory,
      CameraComponent camera) {

    super();

    this.terrainFactory = terrainFactory;
    this.camera = camera;
  }

  /**
   * Create the game area, including terrain, background,
   * platforms and player.
   */
  @Override
  public void create() {

    loadAssets();

    displayUI();

    spawnTerrain();

    spawnBackground();

    // spawnTrees();

    spawnPlatforms();

    player = spawnPlayer();

    // spawnGhosts();
    // spawnGhostKing();

    // playMusic();
  }

  private void displayUI() {

    Entity ui = new Entity();

    ui.addComponent(new GameAreaDisplay("Tutorial"));

    spawnEntity(ui);
  }

  /**
   * Creates the multi-layer parallax background.
   *
   * Each layer has its own size, position and parallax factor.
   */
  private void spawnBackground() {

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera);

    // Layer 1 - Sky
    backgroundComponent.addLayer(
        "images/parallax/sky.png",
        0.05f,
        60f,
        7.66f,
        25.00f
    );

    // Layer 2 - Clouds
    backgroundComponent.addLayer(
        "images/parallax/Clouds.png",
        0.15f,
        60f,
        6.36f,
        18.90f
    );

    // Layer 3 - Mountains
    backgroundComponent.addLayer(
        "images/parallax/Mountains.png",
        0.30f,
        60f,
        6.29f,
        13.00f
    );

    // Layer 4 - Ground
    backgroundComponent.addLayer(
        "images/parallax/ground.png",
        0.50f,
        60f,
        8.22f,
        6.00f
    );

    // Layer 5 - Rocks / foreground
    backgroundComponent.addLayer(
        "images/parallax/Rocks.png",
        0.80f,
        60f,
        8.07f,
        -1.50f
    );

    // Create the background entity.
    Entity background =
        new Entity()
            .addComponent(backgroundComponent);

    /*
     * Position the background in the game world.
     *
     * Individual layer heights and vertical positions
     * are controlled by BackgroundRenderComponent.
     */
    background.setPosition(
        -20f,
        -10f
    );

    spawnEntity(background);
  }

  private void spawnTerrain() {

    // Background terrain
    terrain =
        terrainFactory.createTerrain(
            TerrainType.BACKGROUND_DESERT);

    spawnEntity(
        new Entity().addComponent(terrain));

    // Terrain walls
    float tileSize =
        terrain.getTileSize();

    GridPoint2 tileBounds =
        terrain.getMapBounds(0);

    Vector2 worldBounds =
        new Vector2(
            tileBounds.x * tileSize,
            tileBounds.y * tileSize);

    // Left wall
    spawnEntityAt(
        ObstacleFactory.createWall(
            WALL_WIDTH,
            worldBounds.y),
        GridPoint2Utils.ZERO,
        false,
        false);

    // Right wall
    /*
    spawnEntityAt(
        ObstacleFactory.createWall(
            WALL_WIDTH,
            worldBounds.y
        ),
        new GridPoint2(tileBounds.x, 0),
        false,
        false
    );
    */

    // Top wall
    spawnEntityAt(
        ObstacleFactory.createWall(
            worldBounds.x,
            WALL_WIDTH),
        new GridPoint2(0, tileBounds.y),
        false,
        false);

    // Bottom wall
    spawnEntityAt(
        ObstacleFactory.createWall(
            worldBounds.x,
            WALL_WIDTH),
        GridPoint2Utils.ZERO,
        false,
        false);
  }

  private void spawnTrees() {

    GridPoint2 minPos =
        new GridPoint2(0, 0);

    GridPoint2 maxPos =
        terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_TREES; i++) {

      GridPoint2 randomPos =
          RandomUtils.random(minPos, maxPos);

      Entity tree =
          ObstacleFactory.createTree();

      spawnEntityAt(
          tree,
          randomPos,
          true,
          false);
    }
  }

  private void spawnPlatforms() {

    GridPoint2 floorPos =
        new GridPoint2(0, 3);

    Entity floor =
        ObstacleFactory.createFloor();

    floor.setScale(
        6,
        3
    );

    spawnEntityAt(
        floor,
        floorPos,
        false,
        false);

    GridPoint2 platformPos2 =
        new GridPoint2(16, 10);

    Entity platform2 =
        ObstacleFactory.createPlatform(0);

    platform2.setScale(
        3,
        1
    );

    spawnEntityAt(
        platform2,
        platformPos2,
        false,
        false);

    GridPoint2 platformPos3 =
        new GridPoint2(26, 12);

    Entity platform3 =
        ObstacleFactory.createPlatform(0);

    platform3.setScale(
        3,
        1
    );

    spawnEntityAt(
        platform3,
        platformPos3,
        false,
        false);
  }

  private Entity spawnPlayer() {

    Entity newPlayer =
        PlayerFactory.createPlayer();

    spawnEntityAt(
        newPlayer,
        PLAYER_SPAWN,
        true,
        true);

    return newPlayer;
  }

  private void spawnGhosts() {

    GridPoint2 minPos =
        new GridPoint2(0, 0);

    GridPoint2 maxPos =
        terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_GHOSTS; i++) {

      GridPoint2 randomPos =
          RandomUtils.random(minPos, maxPos);

      Entity ghost =
          NPCFactory.createGhost(player);

      spawnEntityAt(
          ghost,
          randomPos,
          true,
          true);
    }
  }

  private void spawnGhostKing() {

    GridPoint2 minPos =
        new GridPoint2(0, 0);

    GridPoint2 maxPos =
        terrain.getMapBounds(0).sub(2, 2);

    GridPoint2 randomPos =
        RandomUtils.random(minPos, maxPos);

    Entity ghostKing =
        NPCFactory.createGhostKing(player);

    spawnEntityAt(
        ghostKing,
        randomPos,
        true,
        true);
  }

  private void playMusic() {

    Music music =
        ServiceLocator.getResourceService()
            .getAsset(
                backgroundMusic,
                Music.class);

    music.setLooping(true);
    music.setVolume(0.3f);
    music.play();
  }

  private void loadAssets() {

    logger.debug("Loading assets");

    ResourceService resourceService =
        ServiceLocator.getResourceService();

    resourceService.loadTextures(forestTextures);

    resourceService.loadTextureAtlases(
        forestTextureAtlases);

    resourceService.loadSounds(
        forestSounds);

    resourceService.loadMusic(
        forestMusic);

    while (!resourceService.loadForMillis(10)) {

      logger.info(
          "Loading... {}%",
          resourceService.getProgress());
    }
  }

  private void unloadAssets() {

    logger.debug("Unloading assets");

    ResourceService resourceService =
        ServiceLocator.getResourceService();

    resourceService.unloadAssets(
        forestTextures);

    resourceService.unloadAssets(
        forestTextureAtlases);

    resourceService.unloadAssets(
        forestSounds);

    resourceService.unloadAssets(
        forestMusic);
  }

  public Entity getPlayer() {
    return player;
  }

  @Override
  public void dispose() {

    super.dispose();

    ServiceLocator.getResourceService()
        .getAsset(
            backgroundMusic,
            Music.class)
        .stop();

    this.unloadAssets();
  }
}