package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level3Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.level.RisingWaterComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.rendering.*;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Level3GameArea extends GameArea {

  private static final Logger logger = LoggerFactory.getLogger(Level3GameArea.class);

  private static final float WALL_WIDTH = 0.1f;

  private Vector2 worldBounds;

  private Entity water;

  /** Textures used by the level 2 game area. */
  private static final String[] level3Textures = {
    "images/scroll_bg.png",
    // Level 3 background
    "images/Background-2.png",
    "images/Platform_level-3.png",
    "images/Platform-crumbling-level-3.png",
    "images/parallax/level_1_background.png",
    "images/parallax/level_1_furthest.png",
    "images/parallax/level_1_clouds.png",
    "images/parallax/lightning_1.png",
    "images/parallax/lightning_2.png",
    "images/parallax/lightning_3.png",
    "images/parallax/lightning_4.png",
    "images/parallax/rain_small.png",

    // Level 3 ground tile
    "images/tile-level3.png",
    "images/water tile.png",

    // Transparent texture used for the physics-only floor
    "images/transparent.png",

    // Existing game textures
    "images/black_roof.png",
    "images/purple_heart.png",
    "images/DevGridTile.png",
    "images/Tile_2.png",
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
    "images/spiky_ball.png",
    "images/spiky_ball_trap.png",
    "images/checkpoint_lit.png",
    "images/checkpoint_unlit.png",
    // Enemy textures
    "images/skeleton_warrior.png",
    "images/skeleton_archer.png",
    "images/arrow.png",
    "images/rope_arrow.png",
    "images/fire_arrow.png",
    "images/cold_arrow.png"
  };

  private static final String[] level3TexturesAtlas = {
    "images/terrain_iso_grass.atlas", "images/in_level_button.atlas"
  };

  private static final String[] level3Sounds = {"sounds/Impact4.ogg"};

  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";

  private static final String[] level3Music = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  public Level3GameArea(TerrainFactory terrainFactory, CameraComponent camera, Entity player) {
    super(camera);

    config = new Level3Config();

    this.terrainFactory = terrainFactory;
    this.camera = camera;
    this.player = player;
  }

  @Override
  public void create() {
    loadAssets();

    spawnTerrain();
    spawnBackground();

    spawnConfigEntities();
    spawnRisingWater();
    spawnForeground();
    activateWeather();
    player.setPosition(new Vector2(config.getPlayerSpawn().x, config.getPlayerSpawn().y));
  }

  private void activateWeather() {
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      if (entity.getComponent(AnimationRenderComponent.class) != null) {
        entity.getComponent(AnimationRenderComponent.class).toggleWeather();
      } else if (entity.getComponent(BackgroundRenderComponent.class) != null) {
        entity.getComponent(BackgroundRenderComponent.class).toggleWeather();
      } else if (entity.getComponent(DynamicTextureRenderComponent.class) != null) {
        entity.getComponent(DynamicTextureRenderComponent.class).toggleWeather();
      } else if (entity.getComponent(RotatableAnimationRenderComponent.class) != null) {
        entity.getComponent(RotatableAnimationRenderComponent.class).toggleWeather();
      } else if (entity.getComponent(TextureRenderComponent.class) != null) {
        entity.getComponent(TextureRenderComponent.class).toggleWeather();
      } else if (entity.getComponent(TiledRenderComponent.class) != null) {
        entity.getComponent(TiledRenderComponent.class).toggleWeather();
      } else if (entity.getComponent(GrappleSideRenderComponent.class) != null) {
        entity.getComponent(GrappleSideRenderComponent.class).toggleWeather();
      }
    }
  }

  private void spawnForeground() {
    final Vector2 foregroundPos = new Vector2(-10f, -10f);
    ForegroundRenderComponent foregroundComponent =
        new ForegroundRenderComponent(camera, foregroundPos, worldBounds);
    foregroundComponent.addLayer(
        "images/parallax/rain_small.png", // 0.2,0.4 for xxl
        new Vector2(0f, 0f),
        0.1f,
        0.2f,
        new Vector2(0f, 0f),
        new Vector2(2f, -2f),
        RepeatMode.CHAOTIC,
        1f,
        0.4f,
        false,
        0,
        -1);

    // Create the background entity.
    Entity foreground = new Entity().addComponent(foregroundComponent);

    // Position the background in the game world.
    foreground.setPosition(foregroundPos);

    spawnEntity(foreground);
  }

  /** Creates the Level 3 background. */
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
        new Vector2(0f, 0f), // Independent velocity
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);

    // Furthest clouds image
    backgroundComponent.addLayer(
        "images/parallax/level_1_clouds.png",
        new Vector2(0.1f, 0f), // Parallax factor
        30f,
        4f,
        new Vector2(-3f, 9f), // Positional offset
        new Vector2(-0.06f, 0f), // Independent velocity
        RepeatMode.HORIZONTAL,
        1f,
        1f,
        false,
        0,
        -1);

    // Second-furthest clouds image
    backgroundComponent.addLayer(
        "images/parallax/level_1_clouds.png",
        new Vector2(0.1f, 0f), // Parallax factor
        30f,
        15f,
        new Vector2(25f, 7.5f), // Positional offset
        new Vector2(0.12f, 0f), // Independent velocity
        RepeatMode.HORIZONTAL,
        1f,
        1f,
        false,
        0,
        -1);

    // Lightning image 1
    backgroundComponent.addLayer(
        "images/parallax/lightning_1.png",
        new Vector2(0f, 0f),
        2f,
        7f,
        new Vector2(15f, 9.5f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        0f,
        true,
        0,
        0);

    // Lightning image 2
    backgroundComponent.addLayer(
        "images/parallax/lightning_2.png",
        new Vector2(0f, 0f),
        2f,
        7f,
        new Vector2(6f, 9.5f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        0f,
        true,
        0,
        1);

    // Furthest mountains image
    backgroundComponent.addLayer(
        "images/parallax/level_1_furthest.png",
        new Vector2(0.06f, 0f), // Parallax factor 0.12
        30f,
        7f,
        new Vector2(5f, 6.5f), // Positional offset
        new Vector2(0f, 0f), // Independent velocity
        RepeatMode.HORIZONTAL,
        1f,
        0.6f,
        false,
        0,
        -1);

    // Second-furthest mountains image
    backgroundComponent.addLayer(
        "images/parallax/level_1_furthest.png",
        new Vector2(0.11f, 0f), // Parallax factor 0.12
        30f,
        10f,
        new Vector2(0f, 5f), // Positional offset
        new Vector2(0f, 0f), // Independent velocity
        RepeatMode.HORIZONTAL,
        1f,
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

  /** Creates the Level 2 background terrain and walls. */
  private void spawnTerrain() {
    terrain = terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT);
    spawnEntity(new Entity().addComponent(terrain));
    float tileSize = terrain.getTileSize();
    GridPoint2 tileBounds = terrain.getMapBounds(0);
    worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);
  }

  private void spawnRisingWater() {
    if (config instanceof Level3Config c) {
      water = ObstacleFactory.createRisingWaterEntity(c.getWaterSpeed(), 14f);

      RisingWaterComponent risingWater = water.getComponent(RisingWaterComponent.class);

      // offset the spawn by half of the stage width to ensure the spawn location is the center
      float stageWidth = ServiceLocator.getRenderService().getStage().getWidth();
      GridPoint2 offsetSpawn =
          new GridPoint2(
              (int) (c.getRisingWaterSpawn().x - stageWidth / 2), c.getRisingWaterSpawn().y);

      spawnEntityAt(water, offsetSpawn, true, true);
      spawnEntityAt(risingWater.hitbox, c.getRisingWaterSpawn(), true, true);

      risingWater.hitbox.getEvents().addListener("collisionStart", this::waterCollided);
    }
  }

  private void waterCollided(Fixture me, Fixture other) {
    water
        .getEvents()
        .trigger("setHeight", water.getComponent(RisingWaterComponent.class).getStoredHeight());
    player.getEvents().trigger("respawnAtCheckpoint");
  }

  @Override
  protected void onCheckpointActivated(GridPoint2 position) {
    super.onCheckpointActivated(position);
    water.getEvents().trigger("checkpointEncountered", position.y);
  }

  /** Plays the background music. */
  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);

    music.setLooping(true);
    music.setVolume(0.3f);
    music.play();
  }

  /** Loads all Level 2 assets. */
  private void loadAssets() {
    logger.debug("Loading assets");

    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(level3Textures);
    resourceService.loadTextureAtlases(level3TexturesAtlas);
    resourceService.loadSounds(level3Sounds);
    resourceService.loadMusic(level3Music);

    while (!resourceService.loadForMillis(10)) {
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  /** Unloads all Level 2 assets. */
  private void unloadAssets() {
    logger.debug("Unloading assets");

    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(level3Textures);
    resourceService.unloadAssets(level3TexturesAtlas);
    resourceService.unloadAssets(level3Sounds);
    resourceService.unloadAssets(level3Music);
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
    this.unloadAssets();
  }
}
