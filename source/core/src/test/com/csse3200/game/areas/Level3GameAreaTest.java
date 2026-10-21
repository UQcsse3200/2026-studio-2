package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.components.BossPhaseComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.level.CheckpointComponent;
import com.csse3200.game.components.level.RespawnComponent;
import com.csse3200.game.components.level.RisingWaterComponent;
import com.csse3200.game.components.level.SpawnerComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.rendering.DynamicTextureRenderComponent;
import com.csse3200.game.rendering.RotatableAnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TiledRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Level3GameAreaTest extends AbstractAreaAssetsTest {

  private TerrainFactory terrainFactory;
  private CameraComponent camera;
  private Entity player;
  private ResourceService resourceService;
  private EntityService entityService;
  private Music music;
  private TerrainComponent terrain;

  private Level3GameArea area;
  private final AtomicBoolean spawned = new AtomicBoolean();

  @BeforeEach
  void setUp() {
    terrainFactory = mock(TerrainFactory.class);
    camera = mock(CameraComponent.class);
    player = new Entity();

    resourceService = mock(ResourceService.class);
    entityService = mock(EntityService.class);
    music = mock(Music.class);
    terrain = mock(TerrainComponent.class);

    ServiceLocator.registerResourceService(resourceService);
    ServiceLocator.registerEntityService(entityService);

    when(resourceService.loadForMillis(anyInt())).thenReturn(true);
    when(resourceService.getAsset("sounds/BGM_03_mp3.mp3", Music.class)).thenReturn(music);

    area = new Level3GameArea(terrainFactory, camera, player);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void constructorCreatesLevel3Area() {
    assertNotNull(area);
  }

  @Test
  void constructorStoresPlayer() throws Exception {
    Field field = GameArea.class.getDeclaredField("player");
    field.setAccessible(true);

    assertSame(player, field.get(area));
  }

  @Test
  void constructorCreatesLevel3Config() throws Exception {
    Field field = GameArea.class.getDeclaredField("config");
    field.setAccessible(true);

    Object config = field.get(area);

    assertNotNull(config);
    assertEquals("Level3Config", config.getClass().getSimpleName());
  }

  @Test
  void unloadAssetsUnloadsAllLevel3Resources() throws Exception {
    invokePrivate("unloadAssets");

    verify(resourceService, times(4)).unloadAssets(any(String[].class));
  }

  @Test
  void spawnTerrainCreatesBackgroundDesertTerrain() throws Exception {
    PhysicsService physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);

    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(90, 30));
    when(terrain.tileToWorldPosition(any(GridPoint2.class))).thenReturn(new Vector2(0, 0));

    invokePrivate("spawnTerrain");

    verify(terrainFactory).createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT);
  }

  @Test
  void spawnTerrainSetsLevel3WorldBounds() throws Exception {
    PhysicsService physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);

    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(90, 30));
    when(terrain.tileToWorldPosition(any(GridPoint2.class))).thenReturn(new Vector2(0, 0));

    invokePrivate("spawnTerrain");

    Field field = Level3GameArea.class.getDeclaredField("worldBounds");
    field.setAccessible(true);

    Vector2 bounds = (Vector2) field.get(area);

    assertEquals(40f, bounds.x);
    assertEquals(45f, bounds.y);
  }

  @Test
  void disposeStopsBackgroundMusic() {
    area.dispose();

    verify(resourceService).getAsset("sounds/BGM_03_mp3.mp3", Music.class);
    verify(music).stop();
  }

  @Test
  void disposeUnloadsLevel3Assets() {
    area.dispose();

    verify(resourceService, times(4)).unloadAssets(any(String[].class));
  }

  @Override
  ResourceService resourceService() {
    return resourceService;
  }

  @Override
  void invokePrivate(String methodName) throws Exception {
    Method method = Level3GameArea.class.getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(area);
  }

  @Test
  void activateWeatherTogglesWeatherOnSupportedRenderComponents() throws Exception {
    AnimationRenderComponent animation = mock(AnimationRenderComponent.class);
    BackgroundRenderComponent background = mock(BackgroundRenderComponent.class);
    DynamicTextureRenderComponent dynamicTexture = mock(DynamicTextureRenderComponent.class);
    RotatableAnimationRenderComponent rotatableAnimation =
        mock(RotatableAnimationRenderComponent.class);
    TextureRenderComponent texture = mock(TextureRenderComponent.class);
    TiledRenderComponent tiled = mock(TiledRenderComponent.class);

    Entity animationEntity = new Entity().addComponent(animation);
    Entity backgroundEntity = new Entity().addComponent(background);
    Entity dynamicEntity = new Entity().addComponent(dynamicTexture);
    Entity rotatableEntity = new Entity().addComponent(rotatableAnimation);
    Entity textureEntity = new Entity().addComponent(texture);
    Entity tiledEntity = new Entity().addComponent(tiled);

    Array<Entity> entities =
        new Array<>(
            new Entity[] {
              animationEntity,
              backgroundEntity,
              dynamicEntity,
              rotatableEntity,
              textureEntity,
              tiledEntity
            });

    when(entityService.getEntities()).thenReturn(entities);

    invokePrivate("activateWeather");

    verify(animation).toggleWeather();
    verify(background).toggleWeather();
    verify(dynamicTexture).toggleWeather();
    verify(rotatableAnimation).toggleWeather();
    verify(texture).toggleWeather();
    verify(tiled).toggleWeather();
  }

  @Test
  void activateWeatherIgnoresEntitiesWithoutRenderComponents() {
    Array<Entity> entities = new Array<>();
    entities.add(new Entity());

    when(entityService.getEntities()).thenReturn(entities);

    assertDoesNotThrow(() -> invokePrivate("activateWeather"));
  }

  @Test
  void waterCollisionIgnoresMissingBodyUserData() {
    Fixture me = mock(Fixture.class);
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(null);

    assertDoesNotThrow(() -> invokeWaterCollided(me, other));
  }

  @Test
  void waterCollisionIgnoresMissingEntity() {
    Fixture me = mock(Fixture.class);
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    BodyUserData data = new BodyUserData();

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    assertDoesNotThrow(() -> invokeWaterCollided(me, other));
  }

  @Test
  void waterCollisionIgnoresNonPlayerEntity() {
    Fixture me = mock(Fixture.class);
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    BodyUserData data = new BodyUserData();
    data.entity = new Entity();

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    assertDoesNotThrow(() -> invokeWaterCollided(me, other));
  }

  @Test
  void waterCollisionResetsWaterAndRespawnsPlayer() throws Exception {
    Fixture me = mock(Fixture.class);
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    PlayerActions playerActions = mock(PlayerActions.class);
    player.addComponent(playerActions);

    BodyUserData data = new BodyUserData();
    data.entity = player;

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    Entity waterEntity = new Entity();
    Entity hitbox = new Entity();
    RisingWaterComponent risingWater = new RisingWaterComponent(1f, 14f, hitbox);
    waterEntity.addComponent(risingWater);

    Field waterField = Level3GameArea.class.getDeclaredField("water");
    waterField.setAccessible(true);
    waterField.set(area, waterEntity);

    final boolean[] heightReset = {false};
    final float[] resetHeight = {0f};
    final boolean[] respawnTriggered = {false};

    waterEntity
        .getEvents()
        .addListener(
            "setHeight",
            (Float height) -> {
              heightReset[0] = true;
              resetHeight[0] = height;
            });

    player.getEvents().addListener("respawnAtCheckpoint", () -> respawnTriggered[0] = true);

    invokeWaterCollided(me, other);

    assertTrue(heightReset[0]);
    assertEquals(14f, resetHeight[0]);
    assertTrue(respawnTriggered[0]);
  }

  private void invokeWaterCollided(Fixture me, Fixture other) throws Exception {
    Method method =
        Level3GameArea.class.getDeclaredMethod("waterCollided", Fixture.class, Fixture.class);
    method.setAccessible(true);
    method.invoke(area, me, other);
  }

  /**
   * Injects a rising water entity (initial height 14f) and returns a capture of setHeight calls.
   */
  private float[] injectWaterCapture() throws Exception {
    Entity waterEntity = new Entity();
    Entity hitbox = new Entity();
    waterEntity.addComponent(new RisingWaterComponent(1f, 14f, hitbox));

    Field waterField = Level3GameArea.class.getDeclaredField("water");
    waterField.setAccessible(true);
    waterField.set(area, waterEntity);

    float[] resetHeight = {-1f};
    waterEntity.getEvents().addListener("setHeight", (Float height) -> resetHeight[0] = height);
    return resetHeight;
  }

  @Test
  void resetHazardStateLowersWaterToStoredHeight() throws Exception {
    float[] resetHeight = injectWaterCapture();

    area.resetHazardState();

    assertEquals(14f, resetHeight[0]);
  }

  @Test
  void deathRestartLowersWaterAndQueuesCheckpointRespawn() throws Exception {
    float[] resetHeight = injectWaterCapture();

    // A config with no active checkpoints, so restart falls back to the level spawn.
    LevelConfig config = mock(LevelConfig.class);
    when(config.getPlayerSpawn()).thenReturn(new GridPoint2(9, 6));
    area.config = config;
    RespawnComponent respawn = new RespawnComponent();
    player.addComponent(respawn);

    area.restart();

    // The water must be down before the player is teleported back, or the respawn point
    // (spawn at y=6) can sit below the risen surface and soft-lock the player underwater.
    assertEquals(14f, resetHeight[0]);
    assertTrue(queued(respawn));
  }

  private boolean queued(RespawnComponent respawn) throws Exception {
    Field field = RespawnComponent.class.getDeclaredField("respawnQueued");
    field.setAccessible(true);
    return (boolean) field.get(respawn);
  }

  @Test
  void worldResetRequiredBeforeCheckpoint2IsCollected() {
    // Fresh-level state: both checkpoints exist but neither has been collected yet, so a death
    // must rebuild the world or the player stays under the one-shot arena trigger.
    LevelConfig config = mock(LevelConfig.class);
    when(config.getCheckpoints())
        .thenReturn(
            List.of(
                new CheckpointComponent(false, new GridPoint2(10, 6)),
                new CheckpointComponent(false, new GridPoint2(16, 37))));
    area.config = config;

    assertTrue(area.requiresWorldResetOnDeath());
  }

  @Test
  void worldResetSkippedOnceCheckpoint2IsActive() {
    LevelConfig config = mock(LevelConfig.class);
    CheckpointComponent arena2 = mock(CheckpointComponent.class);
    when(arena2.isActive()).thenReturn(true);
    when(arena2.getPosition()).thenReturn(new GridPoint2(16, 37));
    when(config.getCheckpoints()).thenReturn(List.of(arena2));
    area.config = config;

    assertFalse(area.requiresWorldResetOnDeath());
  }

  @Test
  void worldResetStillRequiredWithOnlyCheckpoint1Active() {
    LevelConfig config = mock(LevelConfig.class);
    CheckpointComponent checkpoint1 = mock(CheckpointComponent.class);
    when(checkpoint1.isActive()).thenReturn(true);
    when(checkpoint1.getPosition()).thenReturn(new GridPoint2(10, 6));
    when(config.getCheckpoints()).thenReturn(List.of(checkpoint1));
    area.config = config;

    assertTrue(area.requiresWorldResetOnDeath());
  }

  @Test
  void bossSpawnIsReportedOnTheAreasDispatcherOnlyForCalypso() throws Exception {
    Entity spawner = spawnerEntity();
    Entity calypso = new Entity().addComponent(new BossPhaseComponent());

    area.getEvents()
        .addListener(Level3GameArea.BOSS_SPAWNED, (EventListener0) () -> spawned.set(true));
    spawner.getEvents().trigger("spawnEntity", new Entity());
    assertFalse(spawned.get(), "another enemy spawning must not report the boss");

    spawner.getEvents().trigger("spawnEntity", calypso);
    assertTrue(spawned.get());
  }

  @Test
  void calypsoPhaseEventsAreForwardedToTheAreasDispatcher() throws Exception {
    Entity spawner = spawnerEntity();
    Entity calypso = new Entity().addComponent(new BossPhaseComponent());

    List<Integer> phases = new ArrayList<>();
    AtomicInteger changing = new AtomicInteger();
    area.getEvents()
        .addListener(
            Level3GameArea.BOSS_PHASE_CHANGING, (EventListener0) changing::incrementAndGet);
    area.getEvents()
        .addListener(Level3GameArea.BOSS_PHASE_CHANGED, (EventListener1<Integer>) phases::add);

    spawner.getEvents().trigger("spawnEntity", calypso);
    calypso.getEvents().trigger(Level3GameArea.BOSS_PHASE_CHANGING);
    calypso.getEvents().trigger(Level3GameArea.BOSS_PHASE_CHANGED, 2);

    assertEquals(1, changing.get());
    assertEquals(List.of(2), phases);
  }

  @Test
  void bossCompletionIsReportedOnTheAreasDispatcher() throws Exception {
    Entity spawner = spawnerEntity();

    AtomicInteger defeated = new AtomicInteger();
    area.getEvents()
        .addListener(Level3GameArea.BOSS_DEFEATED, (EventListener0) defeated::incrementAndGet);

    spawner.getEvents().trigger("activateByKey", Level3GameArea.BOSS_DEFEATED_TRIGGER_ID);
    spawner.getEvents().trigger("activateByKey", "someOtherId");

    assertEquals(1, defeated.get());
  }

  @Test
  void rebuiltAreaObservesItsOwnBossInsteadOfThePreviousAreasBoss() throws Exception {
    Entity oldBoss = bareSpawner();
    Entity newBoss = bareSpawner();
    Array<Entity> all = spawnersAround(oldBoss);
    Array<Entity> current = spawnersAround(newBoss);
    all.addAll(current);
    when(entityService.getEntities()).thenReturn(all);
    for (Entity entity : current) {
      area.areaEntities.add(entity);
    }
    invokePrivate("listenForBossSpawn");
    AtomicInteger defeats = new AtomicInteger();
    area.getEvents()
        .addListener(Level3GameArea.BOSS_DEFEATED, (EventListener0) defeats::incrementAndGet);
    oldBoss.getEvents().trigger("activateByKey", Level3GameArea.BOSS_DEFEATED_TRIGGER_ID);
    assertEquals(0, defeats.get());
    newBoss.getEvents().trigger("activateByKey", Level3GameArea.BOSS_DEFEATED_TRIGGER_ID);
    assertEquals(1, defeats.get());
  }

  /** A bare spawner entity in the sixth slot, where the level config puts the boss's spawner. */
  private Entity spawnerEntity() throws Exception {
    Entity spawner = bareSpawner();
    Array<Entity> current = spawnersAround(spawner);
    when(entityService.getEntities()).thenReturn(current);
    for (Entity entity : current) {
      area.areaEntities.add(entity);
    }
    invokePrivate("listenForBossSpawn");
    return spawner;
  }

  private static Entity bareSpawner() {
    return new Entity()
        .addComponent(
            new SpawnerComponent(
                List.<Supplier<Entity>>of(),
                1f,
                1,
                false,
                SpawnerComponent.ACTIVATION_MODE.NORMAL));
  }

  /** Five arena spawners, then the boss's, matching the order the level config declares them. */
  private static Array<Entity> spawnersAround(Entity bossSpawner) {
    Array<Entity> spawners = new Array<>();
    for (int i = 0; i < 5; i++) {
      spawners.add(bareSpawner());
    }
    spawners.add(bossSpawner);
    return spawners;
  }
}
