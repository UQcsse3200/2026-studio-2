package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level2Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Level2GameArea extends GameArea {

  private static final Logger logger = LoggerFactory.getLogger(Level2GameArea.class);

  private static final float WALL_WIDTH = 0.1f;

  private Vector2 worldBounds;

  private static final GridPoint2[] skeletonWarriorSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(6, 23), new GridPoint2(11, 23), new GridPoint2(17, 23),
      };

  /** Textures used by the level 2 game area. */
  private static final String[] level2Textures = {
    "images/ui/scroll_bg.png",
    "images/scroll_bg.png",

    // Level 2 background
    "images/backgrounds/Background-2.png",
    "images/backgrounds/Platform_level-2.png",
    "images/Background-2.png",
    "images/parallax/Clouds-birds.png",
    "images/parallax/Mountains-layer.png",
    "images/Platform_level-2.png",
    "images/parallax/level_2_clouds.png",

    // Assets referenced by Level2Config (the old tile-level2/grass atlas files no longer exist).
    "images/terrain/Level_2/level_2_tile.png",
    "images/terrain/Level_2/level_2_platform.png",
    "images/terrain/Level_2/level_2_spikes.png",

    // Transparent texture used for the physics-only floor
    "images/ui/transparent.png",

    // Existing game textures
    "images/backgrounds/black_roof.png",
    "images/health/purple_heart.png",
    "images/traps/spiky_ball.png",
    "images/traps/spiky_ball_trap.png",
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
    ItemFactory.WHEEL_TOKEN_TEXTURE,

    // Enemy textures
    "images/enemies/skeleton_warrior.png",
    "images/enemies/skeleton_archer.png",
    "images/projectiles/arrow.png",
    "images/projectiles/fireArr_animation.png",
    "images/projectiles/coldArr_animation.png",
    "images/projectiles/rope_arrow.png",
    "images/projectiles/fire_arrow.png",
    "images/projectiles/ice_arrow.png",
    "images/projectiles/poison_arrow.png",
    "images/skeleton_warrior.png",
    "images/skeleton_archer.png",
    "images/arrow.png",
    "images/rope_arrow.png",
    "images/fire_arrow.png",
    "images/cold_arrow.png",
    "images/necromancer_projectile.png",
  };

  private static final String[] level2TexturesAtlas = {
    "images/terrain_iso_grass.atlas",
    "images/in_level_button.atlas",
    "images/skeleton_archer.atlas",
    "images/skeleton_warrior.atlas",
    "images/necromancer.atlas",
    "images/vulture.atlas",
    "images/terrain/Level_1/Level_1_checkpoint.atlas",
    "images/ui/in_level_button.atlas"
  };

  private static final String[] level2Sounds = {"sounds/Impact4.ogg"};

  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";

  private static final String[] level2Music = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  public Level2GameArea(TerrainFactory terrainFactory, CameraComponent camera, Entity player) {
    super(camera);

    config = new Level2Config(player);

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
    spawnSkeletonWarrior();

    player.setPosition(new Vector2(config.getPlayerSpawn().x, config.getPlayerSpawn().y));
  }

  /** Creates the Level 2 parallax background. */
  private void spawnBackground() {
    final Vector2 backgroundPos = new Vector2(-15f, -10f);

    backgroundComponent = new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    // Main background
    backgroundComponent.addLayer(
        "images/backgrounds/Background-2.png",
        new Vector2(0.10f, 0f),
        30f,
        15f,
        new Vector2(0f, 3.5f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
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
    // worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);
    worldBounds = new Vector2(50f, 45f);
  }

  private void spawnSkeletonWarrior() {
    for (GridPoint2 spawnLocation : skeletonWarriorSpawnLocations) {
      Entity enemy = EnemyFactory.createSkeletonWarrior(player);
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

  @Override
  public void toggleLevelMap() {
    toggleMap(worldBounds, camera, backgroundComponent, "level2");
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
    this.unloadAssets();
  }
}
