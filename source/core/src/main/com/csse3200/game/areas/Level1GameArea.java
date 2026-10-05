package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level1Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.level.DesertHazardRecoveryComponent;
import com.csse3200.game.components.level.RoomDoorComponent;
import com.csse3200.game.components.level.RoomDoorDisplay;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.rendering.CaveEntranceRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
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

  // Encounters are activated nearby so sentries do not wander away before the player arrives.
  private static final GridPoint2[] skeletonWarriorSpawnLocations = {
    new GridPoint2(21, 5),
    new GridPoint2(3, 16),
    new GridPoint2(30, 20),
    new GridPoint2(37, 22),
    new GridPoint2(40, 22),
    new GridPoint2(79, 17),
    new GridPoint2(82, 17)
  };
  private static final GridPoint2[] NecromancerSpawnLocations = {};
  private static final GridPoint2[] skeletonArcherSpawnLocations = {
    new GridPoint2(23, 17), new GridPoint2(58, 11)
  };

  // ======== ^^^^^^^^^^ ============================

  public static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 4);

  /** First floating platform is at (4, 2) and is 3 tiles wide; stand on its centre. */
  public static final GridPoint2 SHOPKEEPER_SPAWN = new GridPoint2(5, 3);

  public static final GridPoint2 ROPE_ARROW_SPAWN = new GridPoint2(2, 3);
  public static final GridPoint2 STANDARD_ARROW_SPAWN = new GridPoint2(4, 3);
  public static final GridPoint2 FIRE_ARROW_SPAWN = new GridPoint2(6, 3);
  public static final GridPoint2 ICE_ARROW_SPAWN = new GridPoint2(8, 5);
  public static final GridPoint2 POISON_ARROW_SPAWN = new GridPoint2(10, 5);
  public static final GridPoint2 HEALTH_POTION_SPAWN = new GridPoint2(12, 5);

  /** Sit on top of the 1-tile-tall floating platforms (skip the first, which has the shop). */
  public static final GridPoint2[] GOLD_SPAWNS = {
    new GridPoint2(9, 5), new GridPoint2(15, 7), new GridPoint2(20, 8)
  };

  /** Centre of the stepping-stone platform at (31, 2) */
  public static final GridPoint2 WHEEL_TOKEN_SPAWN = new GridPoint2(32, 3);

  public static final int STANDARD_ARROW_QUANTITY = 5;
  public static final int FIRE_ARROW_QUANTITY = 5;
  public static final int ICE_ARROW_QUANTITY = 5;
  public static final int POISON_ARROW_QUANTITY = 5;
  public static final int HEALTH_POTION_QUANTITY = 3;

  private Vector2 worldBounds;

  /** Textures used by the level 1 game area. */
  private static final String[] forestTextures = {

    // Existing game textures
    "images/health/red_heart.png",
    "images/health/PixelArt_HeartBack.png",
    "images/ui/transparent.png",
    "images/backgrounds/level_1_bg.png",
    "images/backgrounds/level_1_idea.png",
    "images/terrain/Others/closed_door.png",
    "images/terrain/Others/open_door.png",
    "images/terrain/Level_1/sheeps_cave.png",
    "images/terrain/Others/treasure_room.png",
    "images/terrain/Others/npc_room.png",
    "images/ui/menu_box.png",
    "images/terrain/Level_1/Level_1_door.png",
    "images/terrain/Others/normal_cave.png",
    "images/terrain/Level_1/Level_1_tile.png",
    "images/terrain/Level_1/Level_1_platform.png",
    "images/terrain/Level_1/Level_1_Spike.png",

    // Enemy textures
    "images/enemies/skeleton_warrior.png",
    "images/enemies/skeleton_archer.png",
    NPCFactory.SHOPKEEPER_TEXTURE,
    "images/projectiles/arrow.png",
    "images/projectiles/rope_arrow.png",
    "images/projectiles/fire_arrow.png",
    "images/projectiles/fireArr_animation.png",
    "images/projectiles/coldArr_animation.png",
    "images/items/sword.png",
    "images/items/spear.png",
    "images/health/heart_potion.png",
    "images/items/speed_potion.png",
    "images/items/poison_potion.png",
    ItemFactory.GOLD_TEXTURE,
    ItemFactory.WHEEL_TOKEN_TEXTURE,
    "images/projectiles/ice_arrow.png",
    "images/projectiles/poison_arrow.png",
    "images/projectiles/necromancer_projectile.png",
  };

  private static final String[] forestTextureAtlases = {
    "images/player/player.atlas",
    "images/terrain/Level_1/sheep.atlas",
    "images/terrain/Level_1/Level_1_checkpoint.atlas",
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
    spawnShopkeeper();
    spawnGold();
    spawnWheelToken();
    spawnSkeletonArcher();
    spawnSkeletonWarrior();
    spawnWhenApproaching(
        new GridPoint2(74, 9),
        () -> spawnEntityAt(EnemyFactory.createVulture(player), new GridPoint2(74, 9), true, true));
    spawnSideRooms();
    Entity exit = new Entity().addComponent(new CaveEntranceRenderComponent());
    exit.setScale(3f, 4f);
    spawnEntityAt(exit, new GridPoint2(87, 17), false, false);

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

    // Level-wide background: a single copy of level_1_idea.png (1672 x 940, ~1.78 aspect ratio)
    // that drifts slowly with the camera like a distant backdrop, so one image always fills the
    // screen and nothing has to be repeated. The layer's world position is
    //   x = backgroundPos.x + offset.x + cameraX * (1 - parallaxFactor.x)
    //   y = backgroundPos.y + offset.y + cameraY * distance
    // With the values below (parallax x 0.15, distance 0.7) the image covers the view for camera
    // positions of roughly x 5..95 and y 3..25, i.e. the whole 90 x 27 level. Making the image
    // smaller looks further away, but needs a smaller parallax x / larger distance to keep
    // covering the screen; making it larger is always safe.
    backgroundComponent.addLayer(
        "images/backgrounds/level_1_idea.png",
        new Vector2(0.15f, 0f),
        36f,
        20.2f,
        new Vector2(0f, 5f),
        BackgroundType.DEPENDENT,
        new Vector2(0f, 0f),
        false,
        0.7f,
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
    newPlayer.addComponent(new DesertHazardRecoveryComponent());
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

  private void spawnShopkeeper() {
    Entity shopkeeper = NPCFactory.createShopkeeper();
    spawnEntityAt(shopkeeper, SHOPKEEPER_SPAWN, true, false);
  }

  private void spawnGold() {
    for (GridPoint2 goldSpawn : GOLD_SPAWNS) {
      spawnEntityAt(ItemFactory.createGold(), goldSpawn, true, false);
    }
  }

  private void spawnWheelToken() {
    Entity token = ItemFactory.createWheelToken();
    spawnEntityAt(token, WHEEL_TOKEN_SPAWN, true, false);
    token.setPosition(token.getPosition().add(0f, 0.3f));
  }

  private void spawnSkeletonWarrior() {
    for (GridPoint2 spawnLocation : skeletonWarriorSpawnLocations) {
      spawnWhenApproaching(
          spawnLocation,
          () ->
              spawnEntityAt(EnemyFactory.createSkeletonWarrior(player), spawnLocation, true, true));
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
      spawnWhenApproaching(
          spawnLocation,
          () ->
              spawnEntityAt(EnemyFactory.createSkeletonArcher(player), spawnLocation, true, true));
    }
  }

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

  private void spawnWhenApproaching(GridPoint2 location, Runnable spawn) {
    spawnEntity(
        new Entity()
            .addComponent(
                new Component() {
                  private boolean spawned;

                  @Override
                  public void update() {
                    if (!spawned
                        && Math.abs(player.getPosition().x - location.x) < 9
                        && Math.abs(player.getPosition().y - location.y) < 3.5f) {
                      spawned = true;
                      spawn.run();
                    }
                  }
                }));
  }

  /** Optional rooms leave every platform, pickup and checkpoint on the original route intact. */
  private void spawnSideRooms() {
    spawnRoom(110, "images/terrain/Others/npc_room.png");
    spawnRoom(140, "images/terrain/Others/normal_cave.png");
    Rectangle mainBounds = new Rectangle(0, 0, worldBounds.x, worldBounds.y);
    // Only one entrance on the main map: a reverse climb onto the original roof.
    spawnDoor(
        3,
        23,
        "Enter the hidden refuge",
        new Vector2(115, 2.1f),
        new Rectangle(110, 0, 20, 11.25f));
    spawnDoor(112, 2, "Return to the desert", new Vector2(6, 23.1f), mainBounds);
    // The Easter egg is a second discovery inside the refuge, not another main-route door.
    Entity alcove =
        ObstacleFactory.createPlatform(
            new PlatformConfig(
                new GridPoint2(125, 4), 4, 1, 0, "images/terrain/Level_1/Level_1_platform.png"));
    alcove.setScale(4, 1);
    alcove.setPosition(125, 4);
    spawnEntity(alcove);
    spawnDoor(
        127,
        5,
        "Explore the quiet cave",
        new Vector2(145, 2.1f),
        new Rectangle(140, 0, 20, 11.25f));
    spawnDoor(
        142, 2, "Return to the refuge", new Vector2(125, 5.1f), new Rectangle(110, 0, 20, 11.25f));

    for (int i = 0; i < 3; i++) {
      AnimationRenderComponent sheepAnimator =
          new AnimationRenderComponent(
              ServiceLocator.getResourceService()
                  .getAsset("images/terrain/Level_1/sheep.atlas", TextureAtlas.class));
      sheepAnimator.addAnimation("idle", 0.5f + i * 0.12f, Animation.PlayMode.LOOP);
      Entity sheep = new Entity().addComponent(sheepAnimator);
      sheepAnimator.scaleEntity();
      sheep.scaleWidth(1.7f);
      sheepAnimator.startAnimation("idle");
      sheep.setPosition(150 + i * 2.5f, 2);
      spawnEntity(sheep);
    }
  }

  private void spawnDoor(float x, float y, String label, Vector2 destination, Rectangle bounds) {
    Entity door =
        new Entity()
            .addComponent(new CaveEntranceRenderComponent())
            .addComponent(new RoomDoorComponent(player, camera, destination, bounds, label))
            .addComponent(new RoomDoorDisplay());
    door.setScale(2f, 2.67f);
    door.setPosition(x, y);
    spawnEntity(door);
  }

  private void spawnRoom(float x, String texture) {
    Entity room =
        new Entity()
            .addComponent(
                new TextureRenderComponent(texture) {
                  @Override
                  public float getZIndex() {
                    return -200f;
                  }
                });
    room.setScale(20, 11.25f);
    room.setPosition(x, 0.2f);
    spawnEntity(room);
    Entity floor = ObstacleFactory.createWall(20, 2);
    floor.setPosition(x, 0);
    spawnEntity(floor);
    for (float wallX : new float[] {x, x + 19.5f}) {
      Entity wall = ObstacleFactory.createWall(0.5f, 11.25f);
      wall.setPosition(wallX, 0);
      spawnEntity(wall);
    }
    Entity ceiling = ObstacleFactory.createWall(20, 1);
    ceiling.setPosition(x, 11.25f);
    spawnEntity(ceiling);
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

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    player.getComponent(DesertHazardRecoveryComponent.class).setEnabled(false);
    player.getEvents().trigger("grappleRelease");
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
