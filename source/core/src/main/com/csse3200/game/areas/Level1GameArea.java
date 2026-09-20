package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level1Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Level 1 area for the game with platforms, enemies, and a player. */
public class Level1GameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(Level1GameArea.class);
  private KeyboardPlayerInputComponent input;

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

  // private static final GridPoint2[] skeletonWarriorSpawnLocations =
  //     new GridPoint2[] {
  //       new GridPoint2(45, 17),
  //       new GridPoint2(56, 16),
  //       new GridPoint2(77, 12),
  //       new GridPoint2(30, 5),
  //       new GridPoint2(18, 5),
  //       new GridPoint2(4, 16),
  //       new GridPoint2(10, 23),
  //       new GridPoint2(42, 22),
  //     };

  // private static final GridPoint2[] VultureSpawnLocations =
  //     new GridPoint2[] {
  //       new GridPoint2(30, 24), new GridPoint2(65, 20),
  //     };

  // private static final GridPoint2[] NecromancerSpawnLocations = new GridPoint2[] {};

  // private static final GridPoint2[] skeletonArcherSpawnLocations =
  //     new GridPoint2[] {
  //       new GridPoint2(60, 1),
  //       new GridPoint2(57, 10),
  //       new GridPoint2(20, 8),
  //       new GridPoint2(46, 16),
  //     };

  // // ============ TESTING SPAWN LOCATIONS ================

  // private static final GridPoint2[] skeletonArcherTestSpawnLocations =
  //     new GridPoint2[] {
  //       new GridPoint2(60, 1), new GridPoint2(4, 4),
  //     };

  // private static final GridPoint2[] VultureTestSpawnLocations =
  //     new GridPoint2[] {
  //       new GridPoint2(6, 10),
  //     };

  // private static final GridPoint2[] testSpawnLocations =
  //     new GridPoint2[] {
  //       new GridPoint2(6, 4),
  //     };

  // ======== ^^^^^^^^^^ ============================

  public static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 4);
  public static final GridPoint2 ROPE_ARROW_SPAWN = new GridPoint2(2, 3);
  public static final GridPoint2 STANDARD_ARROW_SPAWN = new GridPoint2(4, 3);
  public static final GridPoint2 FIRE_ARROW_SPAWN = new GridPoint2(6, 3);
  public static final GridPoint2 COLD_ARROW_SPAWN = new GridPoint2(8, 5);
  public static final GridPoint2 HEALTH_POTION_SPAWN = new GridPoint2(10, 5);

  public static final int STANDARD_ARROW_QUANTITY = 5;
  public static final int FIRE_ARROW_QUANTITY = 5;
  public static final int COLD_ARROW_QUANTITY = 5;
  public static final int HEALTH_POTION_QUANTITY = 3;

  private Vector2 worldBounds;

  /** Textures used by the level 1 game area. */
  private static final String[] forestTextures = {

    // Existing game textures
    "images/health/red_heart.png",
    "images/health/PixelArt_HeartBack.png",
    "images/ui/transparent.png",
    "images/backgrounds/level_1_bg.png",
    "images/terrain/cave_platform.png",
    "images/terrain/cave_tile.png",
    "images/terrain/cave_touch.png",
    "images/terrain/closed_door.png",
    "images/terrain/open_door.png",
    "images/terrain/sheeps_cave.png",
    "images/terrain/treasure_room.png",
    "images/terrain/cave_mossy.png",
    "images/terrain/npc_room.png",
    "images/terrain/normal_cave.png",
    "images/terrain/checkpoint_unlit.png",
    "images/terrain/checkpoint_lit.png",
    "images/terrain/cave_checkpoint.png",


    // Enemy textures
    "images/enemies/skeleton_warrior.png",
    "images/enemies/skeleton_archer.png",
    "images/projectiles/arrow.png",
    "images/projectiles/rope_arrow.png",
    "images/projectiles/fire_arrow.png",
    "images/projectiles/cold_arrow.png",
    "images/projectiles/necromancer_projectile.png",
  };

  private static final String[] forestTextureAtlases = {
    "images/player/player.atlas",
    "images/enemies/skeleton_archer.atlas",
    "images/enemies/skeleton_warrior.atlas",
    "images/enemies/necromancer.atlas",
    "images/enemies/vulture.atlas",
  };

  private static final String[] forestSounds = {"sounds/Impact4.ogg"};

  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";

  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  /**
   * Initialise this Level1GameArea using the provided TerrainFactory and CameraComponent.
   *
   * @param terrainFactory TerrainFactory used to create the terrain.
   * @param camera CameraComponent used by the parallax background.
   */
  public Level1GameArea(TerrainFactory terrainFactory, CameraComponent camera) {
    super(camera);

    config = new Level1Config();
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
    //// spawnItems(); // test items
    //// spawnWinCondition();
    //spawnSkeletonArcher();
    //spawnSkeletonWarrior();
    //spawnVulture();

    // Test enemy functionalitys
    // spawnTestSkeletonWarrior();
    // spawnTestSkeletonArcher();
    // spawnTestVulture();
    // spawnTestNecromancer();

    // spawnNecromancer();

    // spawnTestWinCondition(); // Temporary test win condition near player spawn for quick testing

    //// spawnTestEnemyNearPlayer(); // Temporary enemy near player spawn for quick HUD/flicker
    // testing
    // testing
    // spawnTestWinCondition(); // Temporary test win condition near player spawn for quick testing

    // playMusic();

  }

  public KeyboardPlayerInputComponent getInput() {
    return input;
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Level 1"));
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

    // Starting area background only — fixed in world space (parallaxFactor.x = 1, distance = 0
    // cancel out the camera-following terms) so it doesn't scroll with the camera like the old
    // level-wide backdrop did. Sized bigger than the default camera viewport (20 x 11.25) plus
    // margin, so it fully covers the screen with no black bars while the player is in this area.
    // Other areas will get their own layer positioned at their own world location as they're
    // built, e.g. treasure_room.png, sheeps_cave.png, etc.
    backgroundComponent.addLayer(
        "images/terrain/normal_cave.png",
        new Vector2(1f, 0f),
        29f,
        21.4f,
        new Vector2(3f, 3f),
        BackgroundType.DEPENDENT,
        new Vector2(0f, 0f),
        false,
        0f,
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

    float tileSize = terrain.getTileSize();
    GridPoint2 tileBounds = terrain.getMapBounds(0);
    worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);
    camera.setRoomBounds(0f, 0f, worldBounds.x, worldBounds.y);
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    newPlayer.getEvents().addListener("grappleRequested", this::checkSuccessfulGrapple);

    input = newPlayer.getComponent(KeyboardPlayerInputComponent.class);
    if (input != null) {
      System.out.println("input is not null");
      input.setCameraComponent(cameraComponent);
    }
    spawnEntityAt(newPlayer, config.getPlayerSpawn(), true, true);

    System.out.println("player spawned");
    System.out.println(input);

    return newPlayer;
  }

  // private void spawnSkeletonWarrior() {
  //   for (GridPoint2 spawnLocation : skeletonWarriorSpawnLocations) {
  //     Entity enemy = EnemyFactory.createSkeletonWarrior(player);
  //     spawnEntityAt(enemy, spawnLocation, true, true);
  //   }
  // }

  // // private void spawnNecromancer() {
  // //   for (GridPoint2 spawnLocation : NecromancerSpawnLocations) {
  // //     Entity enemy = EnemyFactory.createNecromancer(player);
  // //     spawnEntityAt(enemy, spawnLocation, true, true);
  // //   }
  // // }

  // private void spawnSkeletonArcher() {
  //   for (GridPoint2 spawnLocation : skeletonArcherSpawnLocations) {
  //     Entity enemy = EnemyFactory.createSkeletonArcher(player);
  //     spawnEntityAt(enemy, spawnLocation, true, true);
  //   }
  // }

  // private void spawnVulture() {
  //   for (GridPoint2 spawnLocation : VultureSpawnLocations) {
  //     Entity enemy = EnemyFactory.createVulture(player);
  //     spawnEntityAt(enemy, spawnLocation, true, true);
  //   }
  // }

  // // ======== TEST ENEMY SPAWN FUNCTIONS. ============
  // private void spawnTestSkeletonWarrior() {
  //   for (GridPoint2 spawnLocation : testSpawnLocations) {
  //     Entity enemy = EnemyFactory.createSkeletonWarrior(player);
  //     spawnEntityAt(enemy, spawnLocation, true, true);
  //   }
  // }

  // private void spawnTestNecromancer() {
  //   for (GridPoint2 spawnLocation : testSpawnLocations) {
  //     Entity enemy = EnemyFactory.createNecromancer(player);
  //     spawnEntityAt(enemy, spawnLocation, true, true);
  //   }
  // }

  // private void spawnTestSkeletonArcher() {
  //   for (GridPoint2 spawnLocation : skeletonArcherTestSpawnLocations) {
  //     Entity enemy = EnemyFactory.createSkeletonArcher(player);
  //     spawnEntityAt(enemy, spawnLocation, true, true);
  //   }
  // }

  // private void spawnTestVulture() {
  //   for (GridPoint2 spawnLocation : VultureTestSpawnLocations) {
  //     Entity enemy = EnemyFactory.createVulture(player);
  //     spawnEntityAt(enemy, spawnLocation, true, true);
  //   }
  // }

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
