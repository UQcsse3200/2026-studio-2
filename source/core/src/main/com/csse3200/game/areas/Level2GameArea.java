package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level2Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class Level2GameArea extends GameArea {

  private Vector2 worldBounds;

  private static final GridPoint2[] skeletonWarriorSpawnLocations =
      new GridPoint2[] {
        new GridPoint2(6, 23), new GridPoint2(11, 23), new GridPoint2(17, 23),
      };

  /** Textures used by the level 2 game area. */
  private static final String[] level2Textures = {
    "images/ui/scroll_bg.png",

    // Level 2 background
    "images/backgrounds/coin_bg.png",
    "images/backgrounds/shop_bg.png",
    "images/backgrounds/Level_end.png",
    "images/backgrounds/Background-2.png",
    "images/parallax/Clouds-birds.png",
    "images/parallax/Mountains-layer.png",
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
    "images/projectiles/necromancer_projectile.png",
  };

  private static final String[] level2TexturesAtlas = {
    "images/ui/in_level_button.atlas",
    "images/enemies/skeleton_archer.atlas",
    "images/enemies/skeleton_warrior.atlas",
    "images/enemies/necromancer.atlas",
    "images/enemies/vulture.atlas",
    "images/terrain/Level_2/level_2_checkpoint.atlas"
  };

  private static final String[] level2Sounds = {"sounds/Impact4.ogg"};

  private static final String BACKGROUND_MUSIC = "sounds/BGM_03_mp3.mp3";

  private static final String[] level2Music = {BACKGROUND_MUSIC};

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
    displayUI();

    spawnTerrain();
    spawnBackground();
    spawnConfigEntities();
    spawnSkeletonWarrior();

    player.setPosition(
        new Vector2((float) config.getPlayerSpawn().x, (float) config.getPlayerSpawn().y));
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Level 2"));
    spawnEntity(ui);
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
    playLoopingMusic(BACKGROUND_MUSIC, 0.3f);
  }

  /** Loads all Level 2 assets. */
  private void loadAssets() {
    loadAreaAssets(level2Textures, level2TexturesAtlas, level2Sounds, level2Music);
  }

  /** Unloads all Level 2 assets. */
  private void unloadAssets() {
    unloadAreaAssets(level2Textures, level2TexturesAtlas, level2Sounds, level2Music);
  }

  @Override
  public void toggleLevelMap() {
    toggleMap(worldBounds, camera, backgroundComponent, "level2");
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class).stop();
    this.unloadAssets();
  }
}
