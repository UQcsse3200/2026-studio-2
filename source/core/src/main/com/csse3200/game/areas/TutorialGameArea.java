package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.areas.terrain.configs.levelconfigs.LevelTutorialConfig;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tutorial area for the game with platforms, enemies, and a player. */
public class TutorialGameArea extends GameArea {

  private static final Logger logger = LoggerFactory.getLogger(TutorialGameArea.class);

  private static final GridPoint2[] skeletonWarriorSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(45, 17), new GridPoint2(56, 16), new GridPoint2(77, 12),
      };

  private static final GridPoint2[] skeletonArcherSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(60, 1), new GridPoint2(57, 10),
      };

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
    "images/arrow.png",
    "images/rope_arrow.png",
    "images/fire_arrow.png",
    "images/cold_arrow.png"
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

    config = new LevelTutorialConfig();
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
    spawnConfigEntities();
    player = spawnPlayer();
    spawnSkeletonArcher();
    spawnSkeletonWarrior();
    // spawnTestWinCondition(); // Temporary test win condition near player spawn for quick testing

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
        "images/parallax/original_background.png",
        new Vector2(0.10f, 0f),
        30f,
        15f,
        3.5f,
        BackgroundType.DEPENDENT,
        new Vector2(0f, 0f));

    backgroundComponent.addLayer(
        "images/parallax/Clouds.png",
        new Vector2(0.1f, 0f),
        30f,
        15f,
        3.5f,
        BackgroundType.DEPENDENT,
        new Vector2(0.1f, 0f));
    /*
    backgroundComponent.addLayer(
            "images/parallax/Mountains.png",
            new Vector2(0f, 0f),
            30f,
            15f,
            2f,
            BackgroundType.INDEPENDENT,
            new Vector2(0f, 0f));
    */

    // Create the background entity.
    Entity background = new Entity().addComponent(backgroundComponent);

    // Position the background in the game world.
    background.setPosition(-10f, -10f);

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

    // Left wall
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false, false);

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

  private Entity spawnPlayer() {

    Entity newPlayer = PlayerFactory.createPlayer();
    newPlayer.getEvents().addListener("grappleRequested", this::checkSuccessfulGrapple);

    KeyboardPlayerInputComponent input = newPlayer.getComponent(KeyboardPlayerInputComponent.class);
    if (input != null) {
      input.setCameraComponent(cameraComponent);
    }
    spawnEntityAt(newPlayer, config.getPlayerSpawn(), true, true);

    return newPlayer;
  }

  // Temporary test win condition near player spawn for quick testing
  private void spawnTestWinCondition() {
    // Temporary test win condition near player spawn for quick testing
    Entity testWinCon = ObstacleFactory.createWinConEntity();
    spawnEntityAt(testWinCon, new GridPoint2(3, 4), true, true);
  }

  private void spawnSkeletonWarrior() {
    for (GridPoint2 spawnLocation : skeletonWarriorSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonWarrior(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnSkeletonArcher() {
    for (GridPoint2 spawnLocation : skeletonArcherSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonArcher(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
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

  public enum BackgroundType {
    INDEPENDENT,
    DEPENDENT
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
    this.unloadAssets();
  }
}
