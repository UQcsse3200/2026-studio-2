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
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.NPCFactory;
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

  /*
  private static final PlatformConfig[] floors = {
    // borders
    new PlatformConfig(new GridPoint2(0, 0), 100, 1, 0),
    new PlatformConfig(new GridPoint2(0, 22), 50, 5, 0),
    new PlatformConfig(new GridPoint2(50, 25), 40, 5, 1),
    new PlatformConfig(new GridPoint2(0, 0), 1, 30, 1),
    new PlatformConfig(new GridPoint2(90, 0), 1, 30, 1),
  };
  */

  private static final GridPoint2[] spikes = {
    new GridPoint2(10, 2), new GridPoint2(20, 2), new GridPoint2(35, 2)
  };

  private static final GridPoint2[] skeletonWarriorSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(45, 17),
        new GridPoint2(56, 16),
        new GridPoint2(77, 12),
        new GridPoint2(30, 5),
      };

  private static final GridPoint2[] VultureSpawnLocations = new GridPoint2[] {};

  private static final GridPoint2[] NecromancerSpawnLocations = new GridPoint2[] {};

  private static final GridPoint2[] skeletonArcherSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(60, 1), new GridPoint2(57, 10),
      };

  // ============ TESTING SPAWN LOCATIONS ================

  private static final GridPoint2[] skeletonArcherTestSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(60, 1), new GridPoint2(4, 4),
      };

  private static final GridPoint2[] VultureTestSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(6, 10),
      };

  private static final GridPoint2[] testSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(6, 4),
      };

  // ======== ^^^^^^^^^^ ============================

  public static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 4);
  public static final GridPoint2 SHOPKEEPER_SPAWN = new GridPoint2(3, 1);
  public static final GridPoint2 ROPE_ARROW_SPAWN = new GridPoint2(2, 3);
  public static final GridPoint2 STANDARD_ARROW_SPAWN = new GridPoint2(4, 3);
  public static final GridPoint2 FIRE_ARROW_SPAWN = new GridPoint2(6, 3);
  public static final GridPoint2 COLD_ARROW_SPAWN = new GridPoint2(8, 5);
  public static final GridPoint2 HEALTH_POTION_SPAWN = new GridPoint2(10, 5);
  public static final GridPoint2[] GOLD_SPAWNS = {
    new GridPoint2(10, 1), new GridPoint2(9, 4), new GridPoint2(7, 1)
  };

  public static final int STANDARD_ARROW_QUANTITY = 5;
  public static final int FIRE_ARROW_QUANTITY = 5;
  public static final int COLD_ARROW_QUANTITY = 5;
  public static final int HEALTH_POTION_QUANTITY = 3;

  private static final float WALL_WIDTH = 0.1f;
  private Vector2 worldBounds;

  /** Textures used by the tutorial game area. */
  private static final String[] forestTextures = {

    // Existing game textures
    "images/black_roof.png",
    "images/purple_heart.png",
    "images/red_heart.png",
    "images/PixelArt_HeartBack.png",
    "images/transparent.png",
    "images/DevGridTile.png",
    "images/Tile_2.png",
    "images/platform.png",
    "images/hook_platform.png",
    "images/tall_platform.png",
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
    "images/checkpoint_unlit.png",
    "images/checkpoint_lit.png",

    // Parallax background layers
    "images/parallax/original_background.png",
    "images/parallax/sky.png",
    "images/parallax/Clouds.png",
    "images/parallax/Mountains.png",
    "images/parallax/ground.png",
    "images/parallax/Rocks.png",
    "images/parallax/level_1_background.png",
    "images/parallax/level_1_clouds.png",
    "images/parallax/level_1_furthest.png",

    // Enemy textures
    "images/skeleton_warrior.png",
    "images/skeleton_archer.png",
    NPCFactory.SHOPKEEPER_TEXTURE,
    "images/arrow.png",
    "images/rope_arrow.png",
    "images/fire_arrow.png",
    "images/fireArr_animation.png",
    "images/coldArr_animation.png",
    "images/heart.png",
    "images/sword.png",
    "images/spear.png",
    "images/health_potion.png",
    "images/speed_potion.png",
    "images/poison_potion.png",
    ItemFactory.GOLD_TEXTURE,
    "images/cold_arrow.png",
    "images/necromancer_projectile.png",
  };

  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas",
    "images/ghost.atlas",
    "images/ghostKing.atlas",
    "images/player.atlas",
    "images/skeleton_archer.atlas",
    "images/skeleton_warrior.atlas",
    "images/necromancer.atlas",
    "images/vulture.atlas",
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
    spawnShopkeeper();
    spawnGold();
    spawnSkeletonArcher();
    spawnSkeletonWarrior();

    // Test enemy functionalitys
    // spawnTestSkeletonWarrior();
    // spawnTestSkeletonArcher();
    // spawnTestVulture();
    // spawnTestNecromancer();

    // spawnVulture();
    // spawnNecromancer();

    // spawnTestWinCondition(); // Temporary test win condition near player spawn for quick testing

    //// spawnTestEnemyNearPlayer(); // Temporary enemy near player spawn for quick HUD/flicker
    // testing
    // testing
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
    final Vector2 backgroundPos = new Vector2(-10f, -10f);
    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    // Complete original background image
    backgroundComponent.addLayer(
        "images/parallax/level_1_background.png",
        new Vector2(0.1f, 0f), // Parallax factor
        30f,
        12f,
        new Vector2(0f, 4.25f), // Positional offset
        BackgroundType.DEPENDENT,
        new Vector2(0f, 0f), // Independent velocity
        false,
        1f,
        1f);

    // Complete clouds image
    backgroundComponent.addLayer(
        "images/parallax/level_1_clouds.png",
        new Vector2(0.1f, 0f), // Parallax factor
        30f,
        4f,
        new Vector2(0f, 10f), // Positional offset
        BackgroundType.DEPENDENT,
        new Vector2(0.1f, 0f), // Independent velocity
        true,
        1f,
        1f);

    // Complete mountains image
    backgroundComponent.addLayer(
        "images/parallax/level_1_clouds.png",
        new Vector2(0.1f, 0f), // Parallax factor
        30f,
        15f,
        new Vector2(25f, 7.5f), // Positional offset
        BackgroundType.DEPENDENT,
        new Vector2(0.2f, 0f), // Independent velocity
        true,
        1f,
        1f);

    // Complete furthest mountains image
    backgroundComponent.addLayer(
        "images/parallax/level_1_furthest.png",
        new Vector2(0.06f, 0f), // Parallax factor 0.12
        30f,
        7f,
        new Vector2(5f, 6.5f), // Positional offset
        BackgroundType.DEPENDENT,
        new Vector2(0f, 0f), // Independent velocity
        true,
        1f,
        0.5f);

    // Complete second-furthest mountains image
    backgroundComponent.addLayer(
        "images/parallax/level_1_furthest.png",
        new Vector2(0.11f, 0f), // Parallax factor 0.12
        30f,
        10f,
        new Vector2(0f, 5f), // Positional offset
        BackgroundType.DEPENDENT,
        new Vector2(0f, 0f), // Independent velocity
        true,
        1f,
        1f);

    // Create the background entity.
    Entity background = new Entity().addComponent(backgroundComponent);

    // Position the background in the game world.
    background.setPosition(backgroundPos);

    spawnEntity(background);
  }

  private void spawnTerrain() {

    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.BACKGROUND_DESERT);
    spawnEntity(new Entity().addComponent(terrain));

    // Terrain walls
    float tileSize = terrain.getTileSize();
    GridPoint2 tileBounds = terrain.getMapBounds(0);
    worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

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
    // spawnEntityAt(
    //    ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false,
    // false);
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

  private void spawnShopkeeper() {
    Entity shopkeeper = NPCFactory.createShopkeeper();
    spawnEntityAt(shopkeeper, SHOPKEEPER_SPAWN, true, false);
  }

  private void spawnGold() {
    for (GridPoint2 goldSpawn : GOLD_SPAWNS) {
      spawnEntityAt(ItemFactory.createGold(), goldSpawn, true, false);
    }
  }

  /*
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

  // Temporary enemy near player spawn for quick HUD/flicker testing
  private void spawnTestEnemyNearPlayer() {
    Entity testEnemy = EnemyFactory.createSkeletonWarrior(player);
    spawnEntityAt(testEnemy, new GridPoint2(12, 4), true, true);
  }
  */

  private void spawnSkeletonWarrior() {
    for (GridPoint2 spawnLocation : skeletonWarriorSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonWarrior(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnNecromancer() {
    for (GridPoint2 spawnLocation : NecromancerSpawnLocations) {
      Entity enemy = EnemyFactory.createNecromancer(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnSkeletonArcher() {
    for (GridPoint2 spawnLocation : skeletonArcherSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonArcher(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnVulture() {
    for (GridPoint2 spawnLocation : VultureSpawnLocations) {
      Entity enemy = EnemyFactory.createVulture(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  // ======== TEST ENEMY SPAWN FUNCTIONS. ============
  private void spawnTestSkeletonWarrior() {
    for (GridPoint2 spawnLocation : testSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonWarrior(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnTestNecromancer() {
    for (GridPoint2 spawnLocation : testSpawnLocations) {
      Entity enemy = EnemyFactory.createNecromancer(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnTestSkeletonArcher() {
    for (GridPoint2 spawnLocation : skeletonArcherTestSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonArcher(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  private void spawnTestVulture() {
    for (GridPoint2 spawnLocation : VultureTestSpawnLocations) {
      Entity enemy = EnemyFactory.createVulture(player);
      spawnEntityAt(enemy, spawnLocation, true, true);
    }
  }

  // ======== ^^^^^ ============

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

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
    this.unloadAssets();
  }

  /** generate items */
  /*
  private void spawnItems() {
    List.of(
            Map.entry(ItemFactory.createRopeArrow(), ROPE_ARROW_SPAWN),
            Map.entry(
                ItemFactory.createStandardArrow(STANDARD_ARROW_QUANTITY), STANDARD_ARROW_SPAWN),
            Map.entry(ItemFactory.createHealthPotion(HEALTH_POTION_QUANTITY), HEALTH_POTION_SPAWN),
            Map.entry(ItemFactory.createFireArrow(FIRE_ARROW_QUANTITY), FIRE_ARROW_SPAWN),
            Map.entry(ItemFactory.createColdArrow(COLD_ARROW_QUANTITY), COLD_ARROW_SPAWN))
        .forEach(entry -> spawnEntityAt(entry.getKey(), entry.getValue(), true, false));
  }
   */
}
