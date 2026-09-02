package com.csse3200.game.areas;

import com.csse3200.game.events.EventHandler;
import com.csse3200.game.ui.GameEndActions;
import com.csse3200.game.ui.GameEndDisplay;
import com.csse3200.game.ui.GameEndState;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.PlatformConfig;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
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
    // first half
    new PlatformConfig(new GridPoint2(4, 2), 3, 1, 0),
    new PlatformConfig(new GridPoint2(8, 3), 3, 1, 0),
    new PlatformConfig(new GridPoint2(14, 6), 3, 1, 0),
    new PlatformConfig(new GridPoint2(19, 6), 3, 1, 0),
    new PlatformConfig(new GridPoint2(27, 2), 3, 1, 0),
    new PlatformConfig(new GridPoint2(32, 3), 2, 2, 2),
    new PlatformConfig(new GridPoint2(30, 6), 3, 1, 0),
    new PlatformConfig(new GridPoint2(27, 8), 3, 1, 10),
    new PlatformConfig(new GridPoint2(23, 10), 3, 1, 2),
    new PlatformConfig(new GridPoint2(14, 11), 3, 1, 0),
    new PlatformConfig(new GridPoint2(9, 13), 3, 1, 0),

    // second half
    new PlatformConfig(new GridPoint2(56, 22), 3, 1, 1),
    new PlatformConfig(new GridPoint2(60, 23), 3, 1, 1),
    new PlatformConfig(new GridPoint2(64, 22), 3, 1, 1),
  };

  private static final PlatformConfig[] floors = {
    // borders
    new PlatformConfig(new GridPoint2(0, 0), 100, 1, 0),
    new PlatformConfig(new GridPoint2(0, 22), 50, 5, 0),
    new PlatformConfig(new GridPoint2(50, 25), 40, 5, 1),
    new PlatformConfig(new GridPoint2(0, 0), 1, 30, 1),
    new PlatformConfig(new GridPoint2(90, 0), 1, 30, 1),

    // first half
    new PlatformConfig(new GridPoint2(0, 0), 3, 3, 0),
    new PlatformConfig(new GridPoint2(13, 0), 12, 5, 2),
    new PlatformConfig(new GridPoint2(40, 0), 10, 15, 0),
    new PlatformConfig(new GridPoint2(40, 15), 10, 2, 8),
    new PlatformConfig(new GridPoint2(18, 15), 6, 2, 9),
    new PlatformConfig(new GridPoint2(0, 13), 8, 3, 2),
    new PlatformConfig(new GridPoint2(0, 16), 1, 5, 0),
    new PlatformConfig(new GridPoint2(0, 21), 17, 1, 0),
    new PlatformConfig(new GridPoint2(28, 19), 4, 1, 0),
    new PlatformConfig(new GridPoint2(35, 21), 15, 1, 0),

    // second half
    new PlatformConfig(new GridPoint2(53, 13), 3, 4, 0),
    new PlatformConfig(new GridPoint2(56, 15), 3, 1, 0),
    new PlatformConfig(new GridPoint2(56, 16), 1, 3, 2),
    new PlatformConfig(new GridPoint2(53, 7), 3, 4, 0),
    new PlatformConfig(new GridPoint2(56, 10), 4, 1, 0),
    new PlatformConfig(new GridPoint2(63, 0), 1, 18, 0),
    new PlatformConfig(new GridPoint2(59, 18), 10, 1, 0),
    new PlatformConfig(new GridPoint2(68, 15), 3, 1, 0),
    new PlatformConfig(new GridPoint2(71, 15), 1, 6, 0),
    new PlatformConfig(new GridPoint2(64, 15), 1, 1, 0),
    new PlatformConfig(new GridPoint2(68, 11), 1, 3, 0),
    new PlatformConfig(new GridPoint2(68, 11), 3, 1, 0),
    new PlatformConfig(new GridPoint2(75, 15), 3, 1, 8),
    new PlatformConfig(new GridPoint2(75, 12), 1, 3, 8),
    new PlatformConfig(new GridPoint2(78, 0), 13, 17, 0),
  };

  private static final GridPoint2[] spikes = {
    new GridPoint2(10, 2), new GridPoint2(20, 2), new GridPoint2(35, 2)
  };

  private static final GridPoint2[] skeletonWarriorSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(45, 17), new GridPoint2(56, 16), new GridPoint2(77, 12),
      };

  private static final GridPoint2[] skeletonArcherSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(60, 1), new GridPoint2(57, 10),
      };

  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 4);

  private static final float WALL_WIDTH = 0.1f;

  /** Textures used by the tutorial game area. */
  private static final String[] forestTextures = {

    // Existing game textures
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

    // Parallax background layers
    "images/parallax/original_background.png",
    "images/parallax/sky.png",
    "images/parallax/Clouds.png",
    "images/parallax/Mountains.png",
    "images/parallax/ground.png",
    "images/parallax/Rocks.png",

    // Enemy textures
    "images/skeleton_warrior.png",
    "images/skeleton_archer.png",
    "images/arrow.png"
  };

  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas",
    "images/ghost.atlas",
    "images/ghostKing.atlas",
    "images/player.atlas"
  };

  private static final String[] forestSounds = {"sounds/Impact4.ogg"};

  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";

  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  /**
   * Initialise this TutorialGameArea using the provided TerrainFactory and CameraComponent.
   *
   * @param terrainFactory TerrainFactory used to create the terrain.
   * @param camera CameraComponent used by the parallax background.
   */
  public TutorialGameArea(TerrainFactory terrainFactory, CameraComponent camera) {
    super(camera);

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
    spawnSpikes();

    player = spawnPlayer();
    spawnWinCondition();
    spawnSkeletonArcher();
    spawnSkeletonWarrior();
    //spawnTestWinCondition(); // Temporary test win condition near player spawn for quick testing

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

    // Complete original background image
    backgroundComponent.addLayer(
        "images/parallax/original_background.png", 0.30f, 60f, 33.515625f, -1.50f);

    // Create the background entity.
    Entity background = new Entity().addComponent(backgroundComponent);

    /*
     * Position the background in the game world.
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

  private void spawnMovingPlatforms() {

    Entity movingPlatform =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(9, 15), new Vector2(14, 15), new Vector2(3, 0));
    movingPlatform.setScale(2, 1);
    spawnEntityAt(movingPlatform, new GridPoint2(9, 15), false, false);

    Entity movingPlatform2 =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(25, 15), new Vector2(36, 15), new Vector2(3, 0));
    movingPlatform2.setScale(2, 1);
    spawnEntityAt(movingPlatform2, new GridPoint2(25, 15), false, false);

    Entity movingPlatform3 =
        ObstacleFactory.createMovingPlatform(
            10, new Vector2(61, 2), new Vector2(61, 10), new Vector2(0, 3));
    spawnEntityAt(movingPlatform3, new GridPoint2(61, 10), false, false);

    Entity movingPlatform4 =
        ObstacleFactory.createMovingPlatform(
            0, new Vector2(60, 12), new Vector2(60, 15), new Vector2(0, 3));
    spawnEntityAt(movingPlatform4, new GridPoint2(60, 12), false, false);

    Entity movingPlatform5 =
        ObstacleFactory.createMovingPlatform(
            10, new Vector2(72, 2), new Vector2(72, 12), new Vector2(0, 3));
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
      Entity platform = ObstacleFactory.createFloor(config.grappleSides);

      platform.setScale(config.width, config.height);

      spawnEntityAt(platform, config.position, false, false);
    }
  }

  private void spawnSpikes() {
    // 1. Upper-Left Block Top Surface (Y = 17)
    for (int x = 53; x <= 55; x++) {
      spawnEntityAt(ObstacleFactory.createSpike(), new GridPoint2(x, 17), true, true);
    }

    // 2. Overhead T-Bar Top Surface (Y = 19)
    for (int x = 59; x <= 68; x++) {
      spawnEntityAt(ObstacleFactory.createSpike(), new GridPoint2(x, 19), true, true);
    }

    // 3. Ground Pit Surface (Y = 1)
    for (int x = 60; x <= 62; x++) {
      spawnEntityAt(ObstacleFactory.createSpike(), new GridPoint2(x, 1), true, true);
    }
  }

  private Entity spawnPlayer() {

    Entity newPlayer = PlayerFactory.createPlayer();
    newPlayer.getEvents().addListener("grappleRequested", this::checkSuccessfulGrapple);

    KeyboardPlayerInputComponent input = newPlayer.getComponent(KeyboardPlayerInputComponent.class);
    if (input != null) {
      input.setCameraComponent(cameraComponent);
    }
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);

    return newPlayer;
  }

  private void spawnWinCondition() {
    Entity winCon = ObstacleFactory.createWinConEntity();
    spawnEntityAt(winCon, new GridPoint2(80, 18), true, true);
  }

  // Temporary test win condition near player spawn for quick testing
  private void spawnTestWinCondition() {
    // Temporary test win condition near player spawn for quick testing
    Entity testWinCon = ObstacleFactory.createWinConEntity();
    spawnEntityAt(testWinCon, new GridPoint2(3, 4), true, true);
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

  private void spawnSkeletonWarrior() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (GridPoint2 spawnLocation : skeletonWarriorSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonWarrior(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnSkeletonArcher() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (GridPoint2 spawnLocation : skeletonArcherSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonArcher(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
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
