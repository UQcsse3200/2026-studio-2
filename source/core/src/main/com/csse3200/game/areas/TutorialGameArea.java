package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.PlatformConfig;
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

  private static final PlatformConfig[] platforms = {
    //first half
    new PlatformConfig(new GridPoint2(4, 2), 3, 1, 0),
    new PlatformConfig(new GridPoint2(8, 3), 3, 1, 0),
    new PlatformConfig(new GridPoint2(14, 6), 3, 1, 0),
    new PlatformConfig(new GridPoint2(19, 8), 3, 1, 0),
    new PlatformConfig(new GridPoint2(27, 3), 3, 1, 0),
    new PlatformConfig(new GridPoint2(32, 4), 2, 2, 1),
    new PlatformConfig(new GridPoint2(30, 7), 3, 1, 0),
    new PlatformConfig(new GridPoint2(27, 9), 3, 1, 0),
    new PlatformConfig(new GridPoint2(23, 11), 3, 1, 0),
    new PlatformConfig(new GridPoint2(14, 11), 3, 1, 0),
    new PlatformConfig(new GridPoint2(9, 13), 3, 1, 0),

    //second half
    new PlatformConfig(new GridPoint2(56, 22), 3, 1, 0),
    new PlatformConfig(new GridPoint2(60, 23), 3, 1, 0),
    new PlatformConfig(new GridPoint2(64, 22), 3, 1, 0),

  };

  private static final PlatformConfig[] floors = {
    //borders 
    new PlatformConfig(new GridPoint2(0, 0), 100, 1, 0),
    new PlatformConfig(new GridPoint2(0, 22), 50, 5, 0),
    new PlatformConfig(new GridPoint2(50, 25), 40, 5, 0),
    new PlatformConfig(new GridPoint2(0, 0), 1, 30, 0),
    new PlatformConfig(new GridPoint2(90, 0), 1, 30, 0),

    // first half
    new PlatformConfig(new GridPoint2(0, 0), 3, 3, 0),
    new PlatformConfig(new GridPoint2(13, 0), 12, 5, 0),
    new PlatformConfig(new GridPoint2(40, 0), 10, 17, 0),
    new PlatformConfig(new GridPoint2(18, 15), 6, 2, 0),
    new PlatformConfig(new GridPoint2(0, 13), 8, 3, 0),
    new PlatformConfig(new GridPoint2(0, 16), 1, 5, 0),
    new PlatformConfig(new GridPoint2(0, 21), 17, 1, 0),
    new PlatformConfig(new GridPoint2(28, 19), 4, 1, 0),
    new PlatformConfig(new GridPoint2(35, 21), 15, 1, 0),

    // second half
    new PlatformConfig(new GridPoint2(53, 13), 3, 4, 0),
    new PlatformConfig(new GridPoint2(56, 15), 3, 1, 0),

    new PlatformConfig(new GridPoint2(56, 16), 1, 3, 0),


    new PlatformConfig(new GridPoint2(53, 7), 3, 4, 0),
    new PlatformConfig(new GridPoint2(56, 10), 4, 1, 0),


    new PlatformConfig(new GridPoint2(63, 0), 1, 18, 0),
    new PlatformConfig(new GridPoint2(59, 18), 10, 1, 0),

    new PlatformConfig(new GridPoint2(68, 15), 3, 1, 0),
    new PlatformConfig(new GridPoint2(71, 15), 1, 6, 0),

    new PlatformConfig(new GridPoint2(64, 15), 1, 1, 0),

    new PlatformConfig(new GridPoint2(68, 11), 1, 3, 0),
    new PlatformConfig(new GridPoint2(68, 11), 3, 1, 0),

    new PlatformConfig(new GridPoint2(75, 15), 3, 1, 0),
    new PlatformConfig(new GridPoint2(75, 12), 1, 3, 0),

    new PlatformConfig(new GridPoint2(78, 0), 13, 17, 0),
  };

  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 4);

  private static final float WALL_WIDTH = 0.1f;

  /** Textures used by the tutorial game area. */
  private static final String[] forestTextures = {

    // Existing game textures
    "images/black_roof.png",
    "images/transparent.png",
    "images/DevGridTile.png",
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

    spawnBackground();

    // spawnTrees();

    spawnPlatforms();
    spawnFloors();

    player = spawnPlayer();
    spawnWinCondition();

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

    // Layer 1 - Sky
    backgroundComponent.addLayer("images/parallax/sky.png", 0.05f, 60f, 7.66f, 25.00f);

    // Layer 2 - Clouds
    backgroundComponent.addLayer("images/parallax/Clouds.png", 0.15f, 60f, 6.36f, 18.90f);

    // Layer 3 - Mountains
    backgroundComponent.addLayer("images/parallax/Mountains.png", 0.30f, 60f, 6.29f, 13.00f);

    // Layer 4 - Ground
    backgroundComponent.addLayer("images/parallax/ground.png", 0.50f, 60f, 8.22f, 6.00f);

    // Layer 5 - Rocks / foreground
    backgroundComponent.addLayer("images/parallax/Rocks.png", 0.80f, 60f, 8.07f, -1.50f);

    // Create the background entity.
    Entity background = new Entity().addComponent(backgroundComponent);

    /*
     * Position the background in the game world.
     *
     * Individual layer heights and vertical positions
     * are controlled by BackgroundRenderComponent.
     */
    background.setPosition(-20f, -10f);

    spawnEntity(background);
  }

  private void spawnTerrain() {

    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.BACKGROUND_DESERT);

    spawnEntity(new Entity().addComponent(terrain));

    // Terrain walls
    float tileSize = terrain.getTileSize();

    GridPoint2 tileBounds = terrain.getMapBounds(0);

    Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

    spawnMovingPlatforms();

    // Left wall
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false, false);

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
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
        new GridPoint2(0, tileBounds.y),
        false,
        false);

    // Bottom wall
    spawnEntityAt(
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
  }

  private void spawnMovingPlatforms(){

    Entity movingPlatform =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(9, 15), new Vector2(14, 15), new Vector2(3, 0));
            spawnEntityAt(movingPlatform, new GridPoint2(9, 15), false, false);
    movingPlatform.setScale(2, 1);

    Entity movingPlatform2 =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(25, 15), new Vector2(36, 15), new Vector2(3, 0));
            spawnEntityAt(movingPlatform2, new GridPoint2(25, 15), false, false);
    movingPlatform2.setScale(2, 1);

    Entity movingPlatform3 =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(61, 2), new Vector2(61, 10), new Vector2(0, 3));
            spawnEntityAt(movingPlatform3, new GridPoint2(61, 10), false, false);

    Entity movingPlatform4 =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(60, 12), new Vector2(60, 15), new Vector2(0, 3));
            spawnEntityAt(movingPlatform4, new GridPoint2(60, 12), false, false);

    Entity movingPlatform5 =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(72, 2), new Vector2(72, 12), new Vector2(0, 3));
            spawnEntityAt(movingPlatform5, new GridPoint2(75, 2), false, false);
  }

  private void spawnPlatforms() {

    for (PlatformConfig config : platforms) {
      Entity platform = ObstacleFactory.createPlatform(config.grappleSides);

      platform.setScale(config.width, config.height);

      spawnEntityAt(platform, config.position, false, false);
    }
  }

  private void spawnFloors() {

    for (PlatformConfig config : floors) {
      Entity platform = ObstacleFactory.createFloor();

      platform.setScale(config.width, config.height);

      spawnEntityAt(platform, config.position, false, false);
    }
  }

  private Entity spawnPlayer() {

    Entity newPlayer = PlayerFactory.createPlayer();

    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);

    return newPlayer;
  }

  private void spawnWinCondition() {
    Entity winCon = ObstacleFactory.createWinConEntity();
    spawnEntityAt(winCon, new GridPoint2(80, 18), true, true);
  }

  private void spawnGhosts() {

    GridPoint2 minPos = new GridPoint2(0, 0);

    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_GHOSTS; i++) {

      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);

      Entity ghost = NPCFactory.createGhost(player);

      spawnEntityAt(ghost, randomPos, true, true);
    }
  }

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
