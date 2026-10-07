package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.BossArenaConfig;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelBossGameArea extends GameArea {

  private static final Logger logger = LoggerFactory.getLogger(Level3GameArea.class);

  private static final float WALL_WIDTH = 0.1f;

  private static final GridPoint2 CALYPSO_SPAWN = new GridPoint2(15, 1);

  private static final List<Vector2> CALYPSO_TP_POSITIONS =
      List.of(
          new Vector2(16f, 1.2f),
          new Vector2(4f, 1.2f),
          new Vector2(-8f, 4.8f),
          new Vector2(0f, 4.8f),
          new Vector2(-8f, 9f),
          new Vector2(12f, 9f),
          new Vector2(-4f, 12.7f),
          new Vector2(8f, 12.7f));

  private Vector2 worldBounds;

  private Entity water;

  /** Textures used by the level boss game area. */
  private static final String[] level3Textures = {
    "images/backgrounds/Background-2.png",
    "images/terrain/Level_3/Platform_level-3.png",
    "images/terrain/Level_3/tile-level3.png",
    "images/ui/transparent.png",
    "images/projectiles/necromancer_projectile.png"
  };

  private static final String[] level3TexturesAtlas = {
    "images/in_level_button.atlas", "images/enemies/calypso.atlas"
  };

  private static final String[] level3Sounds = {"sounds/Impact4.ogg"};

  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";

  private static final String[] level3Music = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  public LevelBossGameArea(TerrainFactory terrainFactory, CameraComponent camera, Entity player) {
    super(camera);

    config = new BossArenaConfig();

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

    player.setPosition(new Vector2(config.getPlayerSpawn().x, config.getPlayerSpawn().y));

    spawnCalypso();
  }

  private void spawnCalypso() {
    Entity calypso = EnemyFactory.createCalypso(player, CALYPSO_TP_POSITIONS);

    spawnEntityAt(calypso, CALYPSO_SPAWN, true, true);
  }

  /** Creates the Level 2 background. */
  private void spawnBackground() {
    final Vector2 backgroundPos = new Vector2(-15f, -10f);

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);
    backgroundComponent.addLayer(
        "images/backgrounds/Background-2.png",
        new Vector2(0.15f, 0f),
        36f,
        20.2f,
        new Vector2(0f, 5f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        0.7f,
        1f,
        false,
        0,
        -1);

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
