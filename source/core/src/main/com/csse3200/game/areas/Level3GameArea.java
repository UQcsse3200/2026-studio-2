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
import com.csse3200.game.rendering.BackgroundRenderComponent;
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

    spawnBackground();
    spawnTerrain();
    spawnConfigEntities();
    spawnRisingWater();
    player.setPosition(new Vector2(config.getPlayerSpawn().x, config.getPlayerSpawn().y));
  }

  /** Creates the Level 2 background. */
  private void spawnBackground() {
    final Vector2 backgroundPos = new Vector2(-15f, -10f);

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);
    backgroundComponent.addLayer(
        "images/Background-2.png",
        new Vector2(0.10f, 0f),
        30f,
        15f,
        new Vector2(0f, 3.5f),
        new Vector2(0f, 0f),
        false,
        1f,
        1f);

    Entity background = new Entity().addComponent(backgroundComponent);
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
