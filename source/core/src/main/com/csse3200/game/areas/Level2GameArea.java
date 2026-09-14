package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level2Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Level2GameArea extends GameArea {

  private static final Logger logger = LoggerFactory.getLogger(Level2GameArea.class);

  private static final float WALL_WIDTH = 0.1f;

  private Vector2 worldBounds;

  /** Textures used by the level 2 game area. */
  private static final String[] level2Textures = {

    // Level 2 background
    "images/Background-2.png",
    "images/Platform_level-2.png",

    // Level 2 ground tile
    "images/tile-level2.png",

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

    // Enemy textures
    "images/skeleton_warrior.png",
    "images/skeleton_archer.png",
    "images/arrow.png",
    "images/rope_arrow.png",
    "images/fire_arrow.png",
    "images/cold_arrow.png"
  };

  private static final String[] level2TexturesAtlas = {
    "images/terrain_iso_grass.atlas", "images/player.atlas", "images/in_level_button.atlas"
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
        BackgroundType.DEPENDENT,
        new Vector2(0f, 0f),
        false);

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

  /** Creates the Level 2 player. */
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

  /** Loads all Level 2 assets. */
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

  /** Unloads all Level 2 assets. */
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
