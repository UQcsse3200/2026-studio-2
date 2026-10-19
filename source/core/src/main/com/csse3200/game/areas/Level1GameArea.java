package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level1Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.level.DesertHazardRecoveryComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Level 1 area for the game with platforms, enemies, and a player. */
public class Level1GameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(Level1GameArea.class);

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
  private static final GridPoint2[] skeletonArcherSpawnLocations = {
    new GridPoint2(23, 17), new GridPoint2(58, 11)
  };

  /** First floating platform is at (4, 2) and is 3 tiles wide; stand on its centre. */
  public static final GridPoint2 SHOPKEEPER_SPAWN = new GridPoint2(5, 3);

  public static final GridPoint2 ROPE_ARROW_SPAWN = new GridPoint2(2, 3);
  public static final GridPoint2 STANDARD_ARROW_SPAWN = new GridPoint2(4, 3);
  public static final GridPoint2 FIRE_ARROW_SPAWN = new GridPoint2(6, 3);
  public static final GridPoint2 ICE_ARROW_SPAWN = new GridPoint2(8, 5);
  public static final GridPoint2 POISON_ARROW_SPAWN = new GridPoint2(10, 5);
  public static final GridPoint2 HEALTH_POTION_SPAWN = new GridPoint2(12, 5);

  /** Sit on top of the 1-tile-tall floating platforms (skip the first, which has the shop). */
  public static final List<GridPoint2> GOLD_SPAWNS =
      List.of(new GridPoint2(9, 5), new GridPoint2(15, 7), new GridPoint2(20, 8));

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
    "images/backgrounds/coin_bg.png",
    "images/backgrounds/shop_bg.png",
    "images/backgrounds/Level_end.png",
    "images/backgrounds/level_1_bg.png",
    "images/backgrounds/level_1_idea.png",
    "images/ui/menu_box.png",
    "images/terrain/Level_1/Level_1_tile.png",
    "images/terrain/Level_1/Level_1_platform.png",
    "images/terrain/Level_1/Level_1_Spike.png",
    "images/terrain/Level_3/water tile.png",

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
    "images/parallax/white_box.png",
    "images/parallax/lightning_1.png",
    "images/parallax/lightning_2.png",
    "images/parallax/lightning_3.png",
    "images/parallax/lightning_4.png",
    "images/parallax/rain_small.png",
    "images/parallax/rain_medium.png",
    "images/parallax/rain_large.png",
    "images/parallax/rain_xl.png",
    "images/parallax/rain_xxl.png",

    // Enemy textures
    "images/enemies/skeleton_warrior.png",
    "images/enemies/skeleton_archer.png",
    NPCFactory.SHOPKEEPER_TEXTURE,
    "images/projectiles/arrow.png",
    "images/projectiles/rope_arrow.png",
    "images/projectiles/fire_arrow.png",
    "images/projectiles/fireArr_animation.png",
    "images/projectiles/coldArr_animation.png",
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
    "images/terrain/Level_1/Level_1_checkpoint.atlas",
    "images/enemies/skeleton_archer.atlas",
    "images/enemies/skeleton_warrior.atlas",
    "images/enemies/necromancer.atlas",
    "images/enemies/vulture.atlas",
    "images/enemies/calypso.atlas",
  };

  private static final String[] forestSounds = {"sounds/Impact4.ogg"};

  private static final String BACKGROUND_MUSIC = "sounds/BGM_03_mp3.mp3";

  private static final String[] forestMusic = {BACKGROUND_MUSIC};

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
    spawnTorches();
    player = spawnPlayer();
    spawnShopkeeper();
    spawnGold();
    spawnSkeletonArcher();
    spawnSkeletonWarrior();
    spawnWhenApproaching(
        new GridPoint2(74, 9),
        () -> spawnEntityAt(EnemyFactory.createVulture(player), new GridPoint2(74, 9), true, true));
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Level 1"));
    spawnEntity(ui);
  }

  /**
   * Level 1 background.
   *
   * <p>One copy of level_1_idea.png (1672 x 940) that drifts slowly with the camera like a distant
   * backdrop. More layers (such as drifting clouds) can be added with addLayer and move
   * independently of this one.
   *
   * <p>A layer's world position is x = backgroundPos.x + offset.x + cameraX * (1 - parallax.x) and
   * y = backgroundPos.y + offset.y + cameraY * distance. With the base layer's values (parallax x
   * 0.15, distance 0.7) the image covers the view for camera positions of roughly x 5..95 and y
   * 3..25, i.e. the whole 90 x 27 level.
   */
  private void spawnBackground() {
    final Vector2 backgroundPos = new Vector2(-10f, -10f);
    backgroundComponent = new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    // Base layer: the whole scene.
    backgroundComponent.addLayer(
        "images/backgrounds/level_1_idea.png",
        new Vector2(0.15f, 0f),
        36f,
        23.2f,
        new Vector2(0f, 1f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        0.7f,
        1f,
        false,
        0,
        -1);

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
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    newPlayer.addComponent(new DesertHazardRecoveryComponent());
    newPlayer.getEvents().addListener("grappleRequested", this::checkSuccessfulGrapple);

    input = newPlayer.getComponent(KeyboardPlayerInputComponent.class);
    if (input != null) {
      logger.debug("input is not null");
      input.setCameraComponent(cameraComponent);
    }
    spawnEntityAt(newPlayer, config.getPlayerSpawn(), true, true);
    PlayerFactory.giveStartingLoadout(newPlayer);

    logger.debug("player spawned");
    logger.debug("Player input: {}", input);

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

  private void spawnTorches() {
    GridPoint2[] torchSpawns = {
      new GridPoint2(23, 5),
      new GridPoint2(7, 16),
      new GridPoint2(19, 17),
      new GridPoint2(43, 17),
      new GridPoint2(57, 11),
      new GridPoint2(58, 1),
      new GridPoint2(69, 1),
      new GridPoint2(83, 17),
    };

    for (GridPoint2 pos : torchSpawns) {
      spawnEntityAt(ObstacleFactory.createTorch(), pos, true, false);
    }
  }

  private void spawnSkeletonWarrior() {
    for (GridPoint2 spawnLocation : skeletonWarriorSpawnLocations) {
      spawnWhenApproaching(
          spawnLocation,
          () ->
              spawnEntityAt(EnemyFactory.createSkeletonWarrior(player), spawnLocation, true, true));
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

  /** Loads all assets. */
  private void loadAssets() {
    loadAreaAssets(forestTextures, forestTextureAtlases, forestSounds, forestMusic);
  }

  /** Unloads all assets. */
  private void unloadAssets() {
    unloadAreaAssets(forestTextures, forestTextureAtlases, forestSounds, forestMusic);
  }

  @Override
  public void toggleLevelMap() {
    toggleMap(worldBounds, camera, backgroundComponent, "level1");
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    // MAY CAUSE ISSUE
    player.getComponent(DesertHazardRecoveryComponent.class).setEnabled(false);
    player.getEvents().trigger("grappleRelease");
    super.dispose();
    ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class).stop();
    this.unloadAssets();
  }
}
