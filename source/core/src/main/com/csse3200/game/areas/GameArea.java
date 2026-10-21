package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.SpawnData;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.inventory.InventoryBarDisplay;
import com.csse3200.game.components.level.*;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
  private static final Logger assetLogger = LoggerFactory.getLogger(GameArea.class);

  private static final String ACTIVATE_BY_KEY = "activateByKey";

  /** Camera used by this game area. */
  protected final CameraComponent cameraComponent;

  protected TerrainComponent terrain;
  protected List<Entity> areaEntities;
  protected List<Entity> platforms = new ArrayList<>();
  protected HashMap<String, ArrayList<Entity>> triggerableEntities = new HashMap<>();
  protected Entity player;
  protected KeyboardPlayerInputComponent input;
  protected Entity levelChanger;
  protected LevelConfig config;
  // Notified (with the checkpoint's position) whenever one of this area's checkpoints activates;
  // used by the game screen to start checkpoint-linked tutorials. Null when nothing listens.
  private Consumer<GridPoint2> checkpointListener;
  // The area's own event dispatcher, for lifecycle events the game screen reacts to (e.g. the boss
  // spawning and changing phase). Separate from every entity's dispatcher.
  protected final EventHandler events = new EventHandler();
  protected boolean mapToggled = false;
  protected float viewportWidth;
  protected float viewportHeight;
  protected Vector2 resizeScale;
  protected BackgroundRenderComponent backgroundComponent;
  protected String torchTexture = "images/terrain/Level_1/Level_1_torch.png";

  /**
   * Creates a game area using the provided camera component.
   *
   * @param cameraComponent active camera component
   */
  protected GameArea(CameraComponent cameraComponent) {
    this.cameraComponent = cameraComponent;
    areaEntities = new ArrayList<>();
  }

  /** Create the game area in the world. */
  public abstract void create();

  /** Dispose of all internal entities in the area */
  public void dispose() {
    // Only the player carries state between areas. World pickups belong to this area.
    areaEntities.remove(player);

    // Skip collected pickups, as they're already disposed and disposing twice crashes physics
    Array<Entity> registered = ServiceLocator.getEntityService().getEntities();
    for (Entity entity : areaEntities) {
      if (registered.contains(entity, true)) {
        entity.dispose();
      }
    }

    // clear all references
    areaEntities.clear();
    platforms.clear();
    triggerableEntities.clear();
  }

  public Entity getPlayer() {
    return player;
  }

  /**
   * Gets the level changer from this level specified by the level config
   *
   * @return the level changer entity or null if this level shouldn't change
   */
  public Entity getLevelChanger() {
    return levelChanger;
  }

  /**
   * Spawn entity at its current position
   *
   * @param entity Entity (not yet registered)
   */
  protected void spawnEntity(Entity entity) {
    areaEntities.add(entity);

    // listen for child entities that may wish to spawn their own entities, like traps and
    // enemy spawners
    entity.getEvents().addListener("spawnEntity", this::spawnEntity);

    // keep track of all grappleable platforms
    PlatformGrappleComponent platform = entity.getComponent(PlatformGrappleComponent.class);
    if (platform != null && platform.getGrappleSides() != 0) {
      platforms.add(entity);
    }

    // keep track of all triggerable objects
    ActivatableComponent activate = entity.getComponent(ActivatableComponent.class);
    if (activate != null) {
      registerActivatable(entity, activate);
    }

    // listen for trigger entity activation events
    TriggerComponent trigger = entity.getComponent(TriggerComponent.class);
    if (trigger != null) {
      entity.getEvents().addListener(ACTIVATE_BY_KEY, this::onButtonActivated);
    }

    // listen for any spawner completion activation events
    SpawnerComponent spawner = entity.getComponent(SpawnerComponent.class);
    if (spawner != null) {
      entity.getEvents().addListener(ACTIVATE_BY_KEY, this::onButtonActivated);
    }

    // listen for checkpoint activations
    CheckpointComponent checkpoint = entity.getComponent(CheckpointComponent.class);
    if (checkpoint != null) {
      entity.getEvents().addListener("checkpointActivated", this::onCheckpointActivated);
    }

    SlipperyPlatformComponent slipperyPlatform =
        entity.getComponent(SlipperyPlatformComponent.class);
    if (slipperyPlatform != null) {
      entity
          .getEvents()
          .addListener("grappleTimeExceeded", () -> player.getEvents().trigger("grappleRelease"));
      player.getEvents().addListener("grappleRelease", () -> slipperyPlatform.setGrappled(false));
    }

    LevelTriggerComponent levelTrigger = entity.getComponent(LevelTriggerComponent.class);
    if (levelTrigger != null) {
      levelChanger = entity;
    }

    EnemyDeathComponent enemy = entity.getComponent(EnemyDeathComponent.class);
    if (enemy != null) {
      entity.getEvents().trigger("updatedId", entity.getId());
    }

    ServiceLocator.getEntityService().register(entity);
  }

  /**
   * Tracks a triggerable entity under each of its ids, and listens for its activation if it is a
   * trigger button.
   *
   * @param entity the entity being spawned
   * @param activate the entity's activatable component
   */
  private void registerActivatable(Entity entity, ActivatableComponent activate) {
    String[] ids = activate.getIds();

    for (String id : ids) {
      // add the entity to the id's list, creating the list if the id is not present
      triggerableEntities.computeIfAbsent(id, key -> new ArrayList<>()).add(entity);
    }

    // listen for trigger buttons' activation event call
    TriggerButtonComponent trigger = entity.getComponent(TriggerButtonComponent.class);
    if (trigger != null) {
      entity.getEvents().addListener(ACTIVATE_BY_KEY, this::onButtonActivated);
    }
  }

  /**
   * Spawn entity on a given tile. Requires the terrain to be set first.
   *
   * @param entity Entity (not yet registered)
   * @param tilePos tile position to spawn at
   * @param centerX true to center entity X on the tile, false to align the bottom left corner
   * @param centerY true to center entity Y on the tile, false to align the bottom left corner
   */
  protected void spawnEntityAt(
      Entity entity, GridPoint2 tilePos, boolean centerX, boolean centerY) {
    Vector2 worldPos = terrain.tileToWorldPosition(tilePos);
    float tileSize = terrain.getTileSize();

    if (centerX) {
      worldPos.x += (tileSize / 2) - entity.getCenterPosition().x;
    }
    if (centerY) {
      worldPos.y += (tileSize / 2) - entity.getCenterPosition().y;
    }

    entity.setPosition(worldPos);
    spawnEntity(entity);
  }

  /**
   * Requests the game area's config file to create all level entities and then spawns them at their
   * specified position
   */
  protected void spawnConfigEntities() {
    List<SpawnData> entities = config.createEntities();

    for (SpawnData data : entities) {
      spawnEntityAt(data.entity, data.pos, false, false);
    }

    if (config.getWheelSpinSpawns() != null) {
      for (GridPoint2 spawn : config.getWheelSpinSpawns()) {
        spawnWheelToken(spawn);
      }
    }
  }

  private void spawnWheelToken(GridPoint2 spawn) {
    Entity token = ItemFactory.createWheelToken();
    spawnEntityAt(token, spawn, true, false);
    token.setPosition(token.getPosition().add(0f, 0.3f));
  }

  /**
   * A protected method that loops through all stored platforms
   *
   * @param raycastEnd the Vector2 object created by the physics engine's raycast that corresponds
   *     to the final point in the world the grapple hit
   * @return An entity that is guaranteed to be a platform entity (the platform check is performed
   *     before adding to the platforms list) that is the closest to the point at the end of the
   *     raycast.<br>
   *     Note: this does not guarantee the platform was successfully hit, it just returns the most
   *     likely entity to do the check on
   */
  protected Entity findTargetedPlatform(Vector2 raycastEnd) {
    Entity platform = null;
    float lowestDistance = Float.MAX_VALUE; // ensures platforms[0] will be best candidate initially

    for (Entity entity : platforms) {
      // find distance between this and raycast end and compare to best known candidate
      Vector2 pos = entity.getCenterPosition();
      float currentDistance = pos.dst(raycastEnd);

      // update best candidate
      if (currentDistance < lowestDistance) {
        platform = entity;
        lowestDistance = currentDistance;
      }
    }
    return platform;
  }

  public TerrainComponent getTerrain() {
    return terrain;
  }

  /**
   * Public method for grapples to check the end of the raycast position hits a valid side of a
   * platform to confirm a successful grapple location was hit
   *
   * @param raycastEnd the Vector2 object created by the physics engine's raycast that corresponds
   *     to the final point in the world the grapple hit
   */
  public void checkSuccessfulGrapple(Vector2 raycastEnd) {
    Entity p = findTargetedPlatform(raycastEnd);
    if (p == null) {
      return;
    }

    PlatformGrappleComponent grappleComponent = p.getComponent(PlatformGrappleComponent.class);
    int hit = grappleComponent.checkSideHit(p, raycastEnd);
    boolean result = grappleComponent.successfulGrapple(hit);
    player.getEvents().trigger("grappleResponse", result);
  }

  /**
   * Triggers the activated component on all stored entities that require an update for a specific
   * activation id
   *
   * @param id a String of an activation id to search the HashMap for a list of entities that are
   *     activated by this id
   */
  private void onButtonActivated(String id) {
    ArrayList<Entity> entities = triggerableEntities.get(id);
    if (entities == null) {
      return;
    }

    for (Entity entity : entities) {
      ActivatableComponent activate = entity.getComponent(ActivatableComponent.class);
      boolean newActive = !activate.isActive();
      activate.setActive(newActive);
    }
  }

  /** The level config this area was created from, e.g. for its checkpoint-linked tutorials. */
  public LevelConfig getConfig() {
    return config;
  }

  /**
   * This area's event dispatcher, for the lifecycle events the game screen reacts to, such as the
   * boss spawning and changing phase. Entity-level events are unaffected; this is the area's own.
   */
  public EventHandler getEvents() {
    return events;
  }

  /**
   * Sets a callback fired with a checkpoint's position whenever it activates, after the autosave it
   * triggers. Used by the game screen to start checkpoint-linked tutorials.
   *
   * @param checkpointListener callback to run, or null to stop listening
   */
  public void setCheckpointListener(Consumer<GridPoint2> checkpointListener) {
    this.checkpointListener = checkpointListener;
  }

  /** Represents functionality that all game areas should use when a checkpoint is encountered */
  protected void onCheckpointActivated(GridPoint2 position) {
    if (ServiceLocator.getGameSaveEventHandler() != null) {
      ServiceLocator.getGameSaveEventHandler().trigger("autosave");
    }
    if (checkpointListener != null) {
      checkpointListener.accept(position);
    }
  }

  /**
   * Positions of every checkpoint currently activated in this area, captured when saving.
   *
   * @return activated checkpoint positions in level configuration order
   */
  public List<GridPoint2> getActiveCheckpointPositions() {
    return config.getCheckpoints().stream()
        .filter(CheckpointComponent::isActive)
        .map(CheckpointComponent::getPosition)
        .toList();
  }

  /**
   * Re-activates the checkpoints recorded in a save, so respawn resolution, lit torches and level
   * specific checkpoint behaviour (e.g. level 3's rising water) match the saved progress. Called
   * after the area has been created.
   *
   * @param positions checkpoint positions to activate, or null/empty for none
   */
  public void restoreCheckpoints(List<GridPoint2> positions) {
    if (positions == null || positions.isEmpty()) {
      return;
    }
    for (CheckpointComponent checkpoint : config.getCheckpoints()) {
      for (GridPoint2 position : positions) {
        if (checkpoint.getPosition().equals(position)) {
          checkpoint.activate();
          break;
        }
      }
    }
  }

  /**
   * Whether the checkpoint at the given position has been activated in this area.
   *
   * @param position checkpoint position to look for
   * @return true when a checkpoint exists at that position and has been collected
   */
  public boolean isCheckpointActive(GridPoint2 position) {
    return config.getCheckpoints().stream()
        .anyMatch(checkpoint -> checkpoint.isActive() && checkpoint.getPosition().equals(position));
  }

  /** Layer repeat behaviour for background layers */
  public enum RepeatMode {
    NONE,
    HORIZONTAL,
    CHAOTIC
  }

  /** Public method to respawn the player at the last collected checkpoint upon an event trigger. */
  public void respawn() {
    GridPoint2 respawnPoint = getRespawnPoint();
    resetPlayerVelocity();
    RespawnComponent respawn = player.getComponent(RespawnComponent.class);
    respawn.queueRespawn(new Vector2(respawnPoint.x, respawnPoint.y));
  }

  /**
   * Level-specific hazard reset run before the player is moved back to a checkpoint on a death
   * restart (e.g. level 3's rising water). Default: nothing to reset.
   */
  protected void resetHazardState() {}

  /**
   * Whether a death restart in this area should rebuild the level's world (fresh triggers, spawners
   * and platforms) in addition to respawning the player. Default: only respawn, keeping all world
   * state as it was.
   */
  public boolean requiresWorldResetOnDeath() {
    return false;
  }

  /**
   * Restart after death/game-over: teleport to the last checkpoint and revive with a health
   * penalty. Unlike a full level reload, checkpoint progress is kept.
   */
  public void restart() {
    // Hazards are lowered first so the player is never dropped back in under one (level 3's
    // water has risen well above the checkpoint by the time a mid-level death happens).
    resetHazardState();
    GridPoint2 respawnPoint = getRespawnPoint();
    resetPlayerVelocity();
    player.setEnabled(true);
    RespawnComponent.reviveWithPenalty(player);
    RespawnComponent respawn = player.getComponent(RespawnComponent.class);
    respawn.queueRespawn(new Vector2(respawnPoint.x, respawnPoint.y));
    player.getEvents().trigger("revive");
  }

  /** Last collected checkpoint position, or the level spawn if none collected. */
  public GridPoint2 getRespawnPoint() {
    List<CheckpointComponent> checkpoints = config.getCheckpoints();
    // If no checkpoints collected use playerSpawn as respawnPoint
    GridPoint2 respawnPoint = config.getPlayerSpawn();

    // Get last collected checkpoint
    // Note: will choose last active checkpoint in same order they are instantiated
    // in {level}Config
    for (CheckpointComponent checkpoint : checkpoints) {
      if (checkpoint.isActive()) {
        respawnPoint = checkpoint.getPosition();
      }
    }
    return respawnPoint;
  }

  private void resetPlayerVelocity() {
    if (player == null) {
      return;
    }
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    if (physics != null && physics.getBody() != null) {
      physics.getBody().setLinearVelocity(0f, 0f);
    }
  }

  public void toggleBossCamera() {}

  public void toggleLevelMap() {}

  public void updateLevelMap() {}

  public void updateMap(Vector2 worldBounds, CameraComponent cameraComponent) {
    if (mapToggled) {
      Entity camera = new Entity();
      float playerPosY = getPlayer().getPosition().y;
      camera.setPosition(worldBounds.x / 2 - 5, worldBounds.y / 2 + playerPosY / 1.3f);
      cameraComponent.setTarget(camera);
    }
  }

  public void toggleMap(
      Vector2 worldBounds,
      CameraComponent cameraComponent,
      BackgroundRenderComponent backgroundComponent,
      String level) {
    if (cameraComponent.getZoomingCamera() || backgroundComponent.getScalingBackground()) {
      return;
    }

    float currentRatio =
        cameraComponent.getCamera().viewportHeight / cameraComponent.getCamera().viewportWidth;

    if (!mapToggled) {
      int worldBoundX = (int) worldBounds.x;
      int worldBoundY = (int) worldBounds.y;
      viewportWidth = cameraComponent.getCamera().viewportWidth;
      viewportHeight = cameraComponent.getCamera().viewportHeight;

      Entity camera = new Entity();
      float downwardShift = 4.5f;
      if (level.equals("level3")) {
        float cameraPosY = cameraComponent.getCamera().position.y;
        cameraComponent.targetGameWidth = (float) worldBoundX + 5;
        cameraComponent.targetGameHeight = (float) worldBoundY;
        cameraComponent.targetPosition =
            new Vector2(
                worldBounds.x / 2 - 5, (worldBounds.y / 2 + cameraPosY / 1.3f) - downwardShift);
        cameraComponent.setTarget(camera);
      } else {
        cameraComponent.targetGameWidth = (float) worldBoundX + 10;
        cameraComponent.targetGameHeight = cameraComponent.targetGameWidth * currentRatio;
        cameraComponent.targetPosition =
            new Vector2(worldBounds.x / 2, (worldBounds.y / 2) - downwardShift);
        cameraComponent.setTarget(null);
      }

      float scaleX = cameraComponent.targetGameWidth / viewportWidth;
      float scaleY = cameraComponent.targetGameHeight / viewportHeight;
      resizeScale = new Vector2(scaleX, scaleY);

      backgroundComponent.scaleEntity(resizeScale, worldBounds, true, level);
      getPlayer().getComponent(InventoryBarDisplay.class).hideBar();
      getPlayer().getComponent(PlayerStatsDisplay.class).hide();
    } else {
      cameraComponent.targetGameWidth = viewportWidth;
      cameraComponent.targetGameHeight = viewportHeight;
      cameraComponent.targetPosition.set(getPlayer().getPosition());

      cameraComponent.setTarget(getPlayer());
      backgroundComponent.scaleEntity(resizeScale, worldBounds, false, level);
      getPlayer().getComponent(InventoryBarDisplay.class).showBar();
      getPlayer().getComponent(PlayerStatsDisplay.class).show();
    }
    mapToggled = !mapToggled;
    cameraComponent.requestZoom();
  }

  public boolean isMapToggled() {
    return mapToggled;
  }

  public KeyboardPlayerInputComponent getInput() {
    return input;
  }

  /** Loads the assets an area uses and waits until they are all ready. */
  protected void loadAreaAssets(
      String[] textures, String[] atlases, String[] sounds, String[] music) {
    assetLogger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(textures);
    resourceService.loadTextureAtlases(atlases);
    resourceService.loadSounds(sounds);
    resourceService.loadMusic(music);

    while (!resourceService.loadForMillis(10)) {
      assetLogger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  /** Unloads the assets that {@link #loadAreaAssets} loaded. */
  protected void unloadAreaAssets(
      String[] textures, String[] atlases, String[] sounds, String[] music) {
    assetLogger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(textures);
    resourceService.unloadAssets(atlases);
    resourceService.unloadAssets(sounds);
    resourceService.unloadAssets(music);
  }

  /** Plays the (already loaded) background music on a loop. */
  protected void playLoopingMusic(String musicPath, float volume) {
    Music music = ServiceLocator.getResourceService().getAsset(musicPath, Music.class);
    music.setLooping(true);
    GameVolume.setMusicVolume(music, volume);
    music.play();
  }
}
