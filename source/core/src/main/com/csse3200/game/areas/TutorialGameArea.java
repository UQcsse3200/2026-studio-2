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

  private static final Logger logger = LoggerFactory.getLogger(TutorialGameArea.class);

  private static final int NUM_TREES = 7;
  private static final int NUM_GHOSTS = 2;

  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

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

    // ==========================================================
    // ORIGINAL COMPLETE BACKGROUND
    // ==========================================================
    "images/parallax/original_background.png",

    // ==========================================================
    // ORIGINAL 5 PARALLAX LAYERS
    // Kept here so you do NOT lose them.
    // ==========================================================
    "images/parallax/sky.png",
    "images/parallax/Clouds.png",
    "images/parallax/Mountains.png",
    "images/parallax/ground.png",
    "images/parallax/Rocks.png"
  };

  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas"
  };

  private static final String[] forestSounds = {"sounds/Impact4.ogg"};

  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";

  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  private Entity player;

  /**
   * Initialise this TutorialGameArea using the provided TerrainFactory and CameraComponent.
   *
   * @param terrainFactory TerrainFactory used to create the terrain.
   * @param camera CameraComponent used by the parallax background.
   */
  public TutorialGameArea(TerrainFactory terrainFactory, CameraComponent camera) {
    super();

    this.terrainFactory = terrainFactory;
    this.camera = camera;
  }

  /** Create the game area, including terrain, background, platforms and a player. */
  @Override
  public void create() {

    loadAssets();

    displayUI();

    spawnTerrain();

    // ==========================================================
    // CURRENTLY ACTIVE BACKGROUND
    // ==========================================================
    //
    // This uses the ORIGINAL COMPLETE background image.
    //
    // Parallax = 0.30
    //
    // Your 5-layer system is preserved below but is NOT active.
    //
    spawnBackground();

    // ==========================================================
    // Existing gameplay
    // ==========================================================

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
   * ============================================================ CURRENT ACTIVE BACKGROUND
   * ============================================================
   *
   * <p>Uses the complete original_background.png as ONE layer.
   *
   * <p>The camera and parallax factor are passed to the BackgroundRenderComponent so that the
   * background moves more slowly than the foreground when the camera moves. The image is 1024 x
   * 572, so when its width is 60 world units, the matching height is approximately 33.52.
   *
   * <p>Parallax factor = 0.30
   *
   * <p>This means the background moves at 30% of the camera movement relative to the world, giving
   * the subtle effect you originally wanted.
   */
  private void spawnBackground() {

    BackgroundRenderComponent backgroundComponent = new BackgroundRenderComponent(camera);

    // ----------------------------------------------------------
    // COMPLETE ORIGINAL BACKGROUND
    // ----------------------------------------------------------
    backgroundComponent.addLayer(
        "images/parallax/original_background.png", 0.30f, 60f, 33.515625f, -1.50f);

    // Create background entity
    Entity background = new Entity().addComponent(backgroundComponent);

    /*
     * Keep the same general world position used by your
     * existing parallax system.
     */
    background.setPosition(-20f, -10f);

    spawnEntity(background);
  }

  /**
   * ============================================================ ORIGINAL 5-LAYER PARALLAX SYSTEM
   * ============================================================
   *
   * <p>IMPORTANT:
   *
   * <p>DO NOT DELETE THIS METHOD.
   *
   * <p>It contains your original five layers so you can return to them later when you start
   * separating/sketching the background properly.
   *
   * <p>To activate the five-layer version later, simply change:
   *
   * <p>spawnBackground();
   *
   * <p>to:
   *
   * <p>spawnFiveLayerBackground();
   *
   * <p>in create().
   */
  private void spawnFiveLayerBackground() {

    BackgroundRenderComponent backgroundComponent = new BackgroundRenderComponent(camera);

    // ==========================================================
    // LAYER 1 - SKY
    // ==========================================================
    backgroundComponent.addLayer("images/parallax/sky.png", 0.05f, 60f, 7.66f, 25.00f);

    // ==========================================================
    // LAYER 2 - CLOUDS
    // ==========================================================
    backgroundComponent.addLayer("images/parallax/Clouds.png", 0.15f, 60f, 6.36f, 18.90f);

    // ==========================================================
    // LAYER 3 - MOUNTAINS
    // ==========================================================
    backgroundComponent.addLayer("images/parallax/Mountains.png", 0.30f, 60f, 6.29f, 13.00f);

    // ==========================================================
    // LAYER 4 - GROUND
    // ==========================================================
    backgroundComponent.addLayer("images/parallax/ground.png", 0.50f, 60f, 8.22f, 6.00f);

    // ==========================================================
    // LAYER 5 - ROCKS / FOREGROUND
    // ==========================================================
    backgroundComponent.addLayer("images/parallax/Rocks.png", 0.80f, 60f, 8.07f, -1.50f);

    // Create background entity
    Entity background = new Entity().addComponent(backgroundComponent);

    background.setPosition(-20f, -10f);

    spawnEntity(background);
  }

  /** Creates the terrain and world boundaries. */
  private void spawnTerrain() {

    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.BACKGROUND_DESERT);

    spawnEntity(new Entity().addComponent(terrain));

    // Terrain walls
    float tileSize = terrain.getTileSize();

    GridPoint2 tileBounds = terrain.getMapBounds(0);

    Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

    // ==========================================================
    // LEFT WALL
    // ==========================================================
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false, false);

    // ==========================================================
    // RIGHT WALL
    // ==========================================================
    /*
    spawnEntityAt(
        ObstacleFactory.createWall(
            WALL_WIDTH,
            worldBounds.y),
        new GridPoint2(tileBounds.x, 0),
        false,
        false);
    */

    // ==========================================================
    // TOP WALL
    // ==========================================================
    spawnEntityAt(
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
        new GridPoint2(0, tileBounds.y),
        false,
        false);

    // ==========================================================
    // BOTTOM WALL
    // ==========================================================
    spawnEntityAt(
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
  }

  /** Creates the gameplay platforms and floors. */
  private void spawnPlatforms() {

    GridPoint2[] platforms = {new GridPoint2(4, 2), new GridPoint2(8, 4)};

    GridPoint2[] floors = {new GridPoint2(0, 3), new GridPoint2(7, 5)};

    int[][] floorScales = {
      {6, 3},
      {3, 5}
    };

    // ==========================================================
    // PLATFORMS
    // ==========================================================
    for (int i = 0; i < platforms.length; i++) {

      Entity platform = ObstacleFactory.createPlatform(0);

      platform.setScale(3, 1);

      spawnEntityAt(platform, platforms[i], false, false);
    }

    // ==========================================================
    // FLOORS
    // ==========================================================
    for (int i = 0; i < floors.length; i++) {

      Entity floor = ObstacleFactory.createFloor();

      floor.setScale(floorScales[i][0], floorScales[i][1]);

      spawnEntityAt(floor, floors[i], false, false);
    }
  }

  /** Creates and spawns the player. */
  private Entity spawnPlayer() {

    Entity newPlayer = PlayerFactory.createPlayer();

    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);

    return newPlayer;
  }

  /** Creates trees. */
  private void spawnTrees() {

    GridPoint2 minPos = new GridPoint2(0, 0);

    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_TREES; i++) {

      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);

      Entity tree = ObstacleFactory.createTree();

      spawnEntityAt(tree, randomPos, false, false);
    }
  }

  /** Creates the ghosts. */
  private void spawnGhosts() {

    GridPoint2 minPos = new GridPoint2(0, 0);

    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_GHOSTS; i++) {

      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);

      Entity ghost = NPCFactory.createGhost(player);

      spawnEntityAt(ghost, randomPos, true, true);
    }
  }

  /** Creates the ghost king. */
  private void spawnGhostKing() {

    GridPoint2 minPos = new GridPoint2(0, 0);

    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);

    Entity ghostKing = NPCFactory.createGhostKing(player);

    spawnEntityAt(ghostKing, randomPos, true, true);
  }

  /** Plays the background music. */
  private void playMusic() {

    Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);

    music.setLooping(true);
    music.setVolume(0.3f);
    music.play();
  }

  /** Loads all assets. */
  private void loadAssets() {

    logger.debug("Loading assets");

    ResourceService resourceService = ServiceLocator.getResourceService();

    resourceService.loadTextures(forestTextures);

    resourceService.loadTextureAtlases(forestTextureAtlases);

    resourceService.loadSounds(forestSounds);

    resourceService.loadMusic(forestMusic);

    while (!resourceService.loadForMillis(10)) {

      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  /** Unloads all assets. */
  private void unloadAssets() {

    logger.debug("Unloading assets");

    ResourceService resourceService = ServiceLocator.getResourceService();

    resourceService.unloadAssets(forestTextures);

    resourceService.unloadAssets(forestTextureAtlases);

    resourceService.unloadAssets(forestSounds);

    resourceService.unloadAssets(forestMusic);
  }

  /**
   * Returns the player entity.
   *
   * @return player entity
   */
  public Entity getPlayer() {
    return player;
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {

    super.dispose();

    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();

    this.unloadAssets();
  }
}
