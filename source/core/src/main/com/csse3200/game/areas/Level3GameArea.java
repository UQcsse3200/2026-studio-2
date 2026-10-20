package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level3Config;
import com.csse3200.game.components.BossPhaseComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.level.RisingWaterComponent;
import com.csse3200.game.components.level.SpawnerComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.rendering.*;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

public class Level3GameArea extends GameArea {
  private static final String CLOUDS_TEXTURE = "images/parallax/level_1_clouds.png";

  private static final String FURTHEST_BACKGROUND_TEXTURE = "images/parallax/level_1_furthest.png";

  private static final String LIGHTNING_1 = "images/parallax/lightning_1.png";

  private static final float WALL_WIDTH = 0.1f;

  private Vector2 worldBounds;

  private Entity water;
  private boolean bossCamToggled = false;

  /** Textures used by the level 2 game area. */
  private static final String[] level3Textures = {
    "images/ui/scroll_bg.png",
    "images/backgrounds/coin_bg.png",
    "images/backgrounds/shop_bg.png",
    // Level 3 background
    "images/terrain/Level_3/Platform_level-3.png",
    "images/terrain/Level_3/Platform-crumbling-level-3.png",
    "images/parallax/level_1_background.png",
    FURTHEST_BACKGROUND_TEXTURE,
    CLOUDS_TEXTURE,
    LIGHTNING_1,
    "images/parallax/lightning_2.png",
    "images/parallax/lightning_3.png",
    "images/parallax/lightning_4.png",
    "images/parallax/rain_small.png",
    "images/parallax/rain_small_grey.png",
    "images/parallax/rain_small_white.png",

    // Level 3 ground tile
    "images/terrain/Level_3/tile-level3.png",
    "images/terrain/Level_3/water tile.png",

    // Torches
    "images/terrain/Level_3/water tile.png",

    // Transparent texture used for the physics-only floor
    "images/ui/transparent.png",

    // Existing game textures
    "images/terrain/Level_1/Level_1_Spike.png",
    "images/traps/spiky_ball.png",
    "images/traps/spiky_ball_trap.png",
    ItemFactory.WHEEL_TOKEN_TEXTURE,

    // Enemy textures
    "images/enemies/skeleton_warrior.png",
    "images/enemies/skeleton_archer.png",
    "images/projectiles/arrow.png",
    "images/projectiles/rope_arrow.png",
    "images/projectiles/fire_arrow.png",
    "images/projectiles/ice_arrow.png",
    "images/projectiles/necromancer_projectile.png",
    "images/projectiles/calypso_laser_beam.png",
    "images/items/poison_potion.png",
  };

  private static final String[] level3TexturesAtlas = {
    "images/enemies/skeleton_archer.atlas",
    "images/enemies/skeleton_warrior.atlas",
    "images/enemies/necromancer.atlas",
    "images/enemies/vulture.atlas",
    "images/terrain/Level_1/Level_1_checkpoint.atlas",
    "images/ui/in_level_button.atlas",
    "images/enemies/calypso.atlas"
  };

  private static final String[] level3Sounds = {"sounds/Impact4.ogg"};

  private static final String BACKGROUND_MUSIC = "sounds/BGM_03_mp3.mp3";

  private static final String[] level3Music = {BACKGROUND_MUSIC};

  /**
   * Checkpoint 2, at the top of the arena-1 climb: a death before it would leave the player below a
   * one-shot arena trigger whose floor (TP1) has already turned solid, so the world is rebuilt.
   */
  private static final GridPoint2 ARENA_2_CHECKPOINT = new GridPoint2(16, 37);

  /** Calypso spawns from the level 3 boss arena's spawner, which is the last spawner config. */
  private static final int BOSS_SPAWNER_INDEX = 5;

  /** The game screen hears this on the area's event dispatcher once Calypso is on the field. */
  public static final String BOSS_SPAWNED = "bossSpawned";

  /** Forwarded from Calypso when her health drops to half: she starts changing phase. */
  public static final String BOSS_PHASE_CHANGING = "beginBossPhaseChange";

  /** Forwarded from Calypso when the phase transition ends and phase 2 is live. */
  public static final String BOSS_PHASE_CHANGED = "bossPhaseChanged";

  /** Fired on this area's dispatcher once every enemy Calypso's arena spawner produces is dead. */
  public static final String BOSS_DEFEATED = "calypsoDefeated";

  /** The level config's id for the boss arena's spawner completion. */
  public static final String BOSS_DEFEATED_TRIGGER_ID = "bossArena";

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;

  public Level3GameArea(TerrainFactory terrainFactory, CameraComponent camera, Entity player) {
    super(camera);

    config = new Level3Config(player, camera);

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
    listenForBossSpawn();
    spawnTorches();
    spawnRisingWater();
    spawnForeground();
    activateWeather();
    player.setPosition(new Vector2(config.getPlayerSpawn().x, config.getPlayerSpawn().y));
  }

  private void activateWeather() {
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      toggleEntityWeather(entity);
    }
  }

  /**
   * Reports the boss lifecycle on this area's event dispatcher, where the game screen can hear it:
   * {@link #BOSS_SPAWNED} once Calypso's arena spawner produces her, then {@link
   * #BOSS_PHASE_CHANGING} and {@link #BOSS_PHASE_CHANGED} forwarded off her own events.
   *
   * <p>The spawner entity is the last spawner the level config declares. Spawners broadcast on
   * {@code spawnEntity} once per child and {@code activateByKey} when every child is defeated, so
   * the spawn event can be observed without any extra component hook.
   */
  private void listenForBossSpawn() {
    List<Entity> spawners = new ArrayList<>();
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      if (entity.getComponent(SpawnerComponent.class) != null) {
        spawners.add(entity);
      }
    }
    if (spawners.size() <= BOSS_SPAWNER_INDEX) {
      return;
    }

    Entity bossSpawner = spawners.get(BOSS_SPAWNER_INDEX);
    bossSpawner.getEvents().addListener("spawnEntity", this::onBossSpawned);
    // The level config leaves the boss spawner without completion ids; report its completion on
    // the area's dispatcher anyway, so the end of the fight is observable where the screen hears.
    bossSpawner
        .getEvents()
        .addListener(
            "activateByKey",
            (String id) -> {
              if (BOSS_DEFEATED_TRIGGER_ID.equals(id)) {
                getEvents().trigger(BOSS_DEFEATED);
              }
            });
  }

  /**
   * Forwards Calypso's phase-change events to this area's dispatcher. Wired as she spawns, so a
   * later respawn that rebuilds the level picks up her fresh events.
   *
   * @param calypso the spawned entity, only adopted when it is the boss
   */
  private void onBossSpawned(Entity calypso) {
    if (calypso == null || calypso.getComponent(BossPhaseComponent.class) == null) {
      return;
    }
    calypso
        .getEvents()
        .addListener(
            BOSS_PHASE_CHANGING, (EventListener0) () -> getEvents().trigger(BOSS_PHASE_CHANGING));
    calypso
        .getEvents()
        .addListener(
            BOSS_PHASE_CHANGED,
            (EventListener1<Integer>) phase -> getEvents().trigger(BOSS_PHASE_CHANGED, phase));
    getEvents().trigger(BOSS_SPAWNED);
  }

  /** Toggles weather on every weather-aware render component attached to the entity. */
  private static void toggleEntityWeather(Entity entity) {
    if (entity.getComponent(AnimationRenderComponent.class) != null) {
      entity.getComponent(AnimationRenderComponent.class).toggleWeather();
    }
    if (entity.getComponent(BackgroundRenderComponent.class) != null) {
      entity.getComponent(BackgroundRenderComponent.class).toggleWeather();
    }
    if (entity.getComponent(DynamicTextureRenderComponent.class) != null) {
      entity.getComponent(DynamicTextureRenderComponent.class).toggleWeather();
    }
    if (entity.getComponent(RotatableAnimationRenderComponent.class) != null) {
      entity.getComponent(RotatableAnimationRenderComponent.class).toggleWeather();
    }
    if (entity.getComponent(TextureRenderComponent.class) != null) {
      entity.getComponent(TextureRenderComponent.class).toggleWeather();
    }
    if (entity.getComponent(TiledRenderComponent.class) != null) {
      entity.getComponent(TiledRenderComponent.class).toggleWeather();
    }
    if (entity.getComponent(GrappleSideRenderComponent.class) != null) {
      entity.getComponent(GrappleSideRenderComponent.class).toggleWeather();
    }
    if (entity.getComponent(ForegroundRenderComponent.class) != null) {
      entity.getComponent(ForegroundRenderComponent.class).toggleWeather();
    }
  }

  private void spawnForeground() {
    final Vector2 foregroundPos = new Vector2(-10f, -10f);
    ForegroundRenderComponent foregroundComponent =
        new ForegroundRenderComponent(camera, foregroundPos, worldBounds);
    foregroundComponent.addLayer(
        "images/parallax/rain_small_white.png", // 0.2,0.4 for xxl
        new Vector2(0f, 0f),
        0.1f,
        0.2f,
        new Vector2(0f, 0f),
        new Vector2(1.5f, -2f),
        RepeatMode.CHAOTIC,
        1f,
        0.4f,
        false,
        15,
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
    backgroundComponent = new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    // Complete original background image
    backgroundComponent.addLayer(
        "images/parallax/level_1_background.png",
        new Vector2(0.1f, 0f), // Parallax factor
        30f,
        13f,
        new Vector2(-1.5f, 4.25f), // Positional offset
        new Vector2(0f, 0f), // Independent velocity
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);

    // Furthest clouds image
    backgroundComponent.addLayer(
        CLOUDS_TEXTURE,
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
        CLOUDS_TEXTURE,
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
        LIGHTNING_1,
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
        LIGHTNING_1,
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
        FURTHEST_BACKGROUND_TEXTURE,
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
        FURTHEST_BACKGROUND_TEXTURE,
        new Vector2(0.11f, 0f), // Parallax factor 0.12
        30f,
        10f,
        new Vector2(-5f, 5f), // Positional offset
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
    worldBounds = new Vector2(40f, 35f);

    // Left
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, 130), new GridPoint2(0, -10), false, false);
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, 130),
        new GridPoint2(27, -10), // CHANGE THIS X VALUE FOR ELLA'S NEW LEVEL 3
        false,
        false);
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

  private void spawnTorches() {
    GridPoint2[] guideTorches = {
      new GridPoint2(19, 11),
      new GridPoint2(18, 16),
      new GridPoint2(5, 22),
      new GridPoint2(7, 28),
      new GridPoint2(11, 37),
      new GridPoint2(18, 46),
      new GridPoint2(14, 52),
      new GridPoint2(17, 68),
      new GridPoint2(14, 81),
      new GridPoint2(26, 85),
      new GridPoint2(11, 95),
      new GridPoint2(3, 107),
      new GridPoint2(23, 107),
      new GridPoint2(3, 101),
      new GridPoint2(23, 101),
    };

    for (GridPoint2 pos : guideTorches) {
      spawnEntityAt(ObstacleFactory.createTorch(), pos, true, false);
    }
  }

  private void waterCollided(Fixture me, Fixture other) {
    // Only emit signals if the other colliding body is the player
    BodyUserData data = (BodyUserData) other.getBody().getUserData();
    if (data == null
        || data.entity == null
        || data.entity.getComponent(PlayerActions.class) == null) {
      return;
    }

    CombatStatsComponent stats = data.entity.getComponent(CombatStatsComponent.class);
    if (stats != null && stats.isGodMode()) {
      return;
    }

    lowerWaterToCheckpoint();
    player.getEvents().trigger("respawnAtCheckpoint");
  }

  /**
   * Drops the rising water back to the height stored when the last checkpoint was encountered, so a
   * respawn point is never left below the surface.
   */
  private void lowerWaterToCheckpoint() {
    if (water == null) {
      return;
    }
    water
        .getEvents()
        .trigger("setHeight", water.getComponent(RisingWaterComponent.class).getStoredHeight());
  }

  @Override
  protected void resetHazardState() {
    lowerWaterToCheckpoint();
  }

  @Override
  public boolean requiresWorldResetOnDeath() {
    // Before CP2 a plain respawn drops the player below the arena: its entry trigger has already
    // fired once (so it can never re-arm) and the arena floor TP1 is solid, blocking re-entry.
    // Rebuilding the level gives back the exact state the level was proven traversable in.
    return !isCheckpointActive(ARENA_2_CHECKPOINT);
  }

  @Override
  protected void onCheckpointActivated(GridPoint2 position) {
    super.onCheckpointActivated(position);
    water.getEvents().trigger("checkpointEncountered", position.y);
  }

  /** Loads all Level 2 assets. */
  private void loadAssets() {
    loadAreaAssets(level3Textures, level3TexturesAtlas, level3Sounds, level3Music);
  }

  /** Unloads all Level 2 assets. */
  private void unloadAssets() {
    unloadAreaAssets(level3Textures, level3TexturesAtlas, level3Sounds, level3Music);
  }

  @Override
  public void toggleBossCamera() {
    if (!bossCamToggled) {
      if (mapToggled) {
        toggleLevelMap();
      }
      Vector2 bossBounds = new Vector2(25f, 12f);
      Vector2 bossResizeScale = new Vector2(0f, 0f);
      int bossBoundX = (int) bossBounds.x;
      int bossBoundY = (int) bossBounds.y;

      Entity bossCamera = new Entity();
      cameraComponent.resize(bossBoundX, bossBoundY, (float) bossBoundX + 4);
      bossCamera.setPosition(bossBounds.x / 2 + 1, bossBounds.y / 2 + 97);
      cameraComponent.setTarget(bossCamera);

      backgroundComponent.scaleEntity(bossResizeScale, bossBounds, true, "bossArena");
      bossCamToggled = true;
    }
  }

  @Override
  public void toggleLevelMap() {
    if (!bossCamToggled) {
      toggleMap(worldBounds, camera, backgroundComponent, "level3");
    }
  }

  @Override
  public void updateLevelMap() {
    updateMap(worldBounds, camera);
  }

  /** Dispose of the game area. */
  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class).stop();
    this.unloadAssets();
  }
}
