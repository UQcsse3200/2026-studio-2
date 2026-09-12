package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level2Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Level2GameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(Level2GameArea.class);
  private static final float WALL_WIDTH = 0.1f;

  /** Textures used by the level 2 game area. */
  private static final String[] level2Textures = {
    // Level 2 background
    "images/Background-2.png",
    "images/Platform_level-2.png",
    "images/Ground_level-2.png",

    // Existing game textures
    "images/black_roof.png",
    "images/purple_heart.png",
    "images/transparent.png",
    "images/DevGridTile.png",
    "images/Tile_2.png",
    "images/Platform_level-2.png",
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

    // Enemy textures
    "images/skeleton_warrior.png",
    "images/skeleton_archer.png",
    "images/arrow.png",
    "images/rope_arrow.png",
    "images/fire_arrow.png",
    "images/cold_arrow.png"
  };

  private static final String[] level2TexturesAtlas = {
    "images/terrain_iso_grass.atlas", "images/player.atlas"
  };

  private static final String[] level2Sounds = {"sounds/Impact4.ogg"};

  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";

  private static final String[] level2Music = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  public Level2GameArea(TerrainFactory terrainFactory, CameraComponent camera) {
    super(camera);

    config = new Level2Config();
    this.terrainFactory = terrainFactory;
    this.camera = camera;
  }

  @Override
  public void create() {
    loadAssets();

    // Spawn the Level 2 background before the terrain.
    spawnBackground();

    spawnTerrain();
    spawnConfigEntities();
    player = spawnPlayer();
  }

  /**
   * Creates the Level 2 background.
   *
   * <p>The background uses a single image, Background-2.png, with a parallax factor of 0.30. This
   * means the background moves at 30% of the camera movement, creating the desired parallax effect.
   */
  private void spawnBackground() {
    BackgroundRenderComponent backgroundComponent = new BackgroundRenderComponent(camera);

    // Level 2 background with 30% parallax.
    backgroundComponent.addLayer("images/Background-2.png", 0.30f, 60f, 33.515625f, -1.50f);

    // Create the background entity.
    Entity background = new Entity().addComponent(backgroundComponent);

    // Position the background in the game world.
    background.setPosition(-20f, -10f);

    // Add the background to the game area.
    spawnEntity(background);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT);

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

    resourceService.loadTextures(level2Textures);
    resourceService.loadTextureAtlases(level2TexturesAtlas);
    resourceService.loadSounds(level2Sounds);
    resourceService.loadMusic(level2Music);

    while (!resourceService.loadForMillis(10)) {
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  /** Unloads all assets. */
  private void unloadAssets() {
    logger.debug("Unloading assets");

    ResourceService resourceService = ServiceLocator.getResourceService();

    resourceService.unloadAssets(level2Textures);
    resourceService.unloadAssets(level2TexturesAtlas);
    resourceService.unloadAssets(level2Sounds);
    resourceService.unloadAssets(level2Music);
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    super.dispose();

    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();

    this.unloadAssets();
  }
}
