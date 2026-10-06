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
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.level.RisingWaterComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class Level3GameAreaTest {

  private TerrainFactory terrainFactory;
  private CameraComponent camera;
  private Entity player;
  private ResourceService resourceService;
  private EntityService entityService;
  private Music music;
  private TerrainComponent terrain;

  private Level3GameArea area;

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
  void loadAssetsLoadsLevel3Resources() throws Exception {
    invokePrivate("loadAssets");

    verify(resourceService).loadTextures(any(String[].class));
    verify(resourceService).loadTextureAtlases(any(String[].class));
    verify(resourceService).loadSounds(any(String[].class));
    verify(resourceService).loadMusic(any(String[].class));
    verify(resourceService).loadForMillis(10);
  }

  @Test
  void loadAssetsWaitsUntilLoadingCompletes() throws Exception {
    when(resourceService.loadForMillis(10)).thenReturn(false, false, true);
    when(resourceService.getProgress()).thenReturn(25, 75);

    invokePrivate("loadAssets");

    verify(resourceService, times(3)).loadForMillis(10);
    verify(resourceService, times(2)).getProgress();
  }

  @Test
  void unloadAssetsUnloadsAllLevel3Resources() throws Exception {
    invokePrivate("unloadAssets");

    verify(resourceService, times(4)).unloadAssets(any(String[].class));
  }

  @Test
  void playMusicConfiguresAndStartsBackgroundMusic() throws Exception {
    invokePrivate("playMusic");

    verify(resourceService).getAsset("sounds/BGM_03_mp3.mp3", Music.class);
    verify(music).setLooping(true);
    verify(music).setVolume(0.3f);
    verify(music).play();
  }

  @Test
  void spawnTerrainCreatesBackgroundDesertTerrain() throws Exception {
    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(90, 30));

    invokePrivate("spawnTerrain");

    verify(terrainFactory).createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT);
  }

  @Test
  void spawnTerrainSetsLevel3WorldBounds() throws Exception {
    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(90, 30));

    invokePrivate("spawnTerrain");

    Field field = Level3GameArea.class.getDeclaredField("worldBounds");
    field.setAccessible(true);

    Vector2 bounds = (Vector2) field.get(area);

    assertEquals(30f, bounds.x);
    assertEquals(100f, bounds.y);
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

  private void invokePrivate(String methodName) throws Exception {
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
  void activateWeatherIgnoresEntitiesWithoutRenderComponents() throws Exception {
    Array<Entity> entities = new Array<>();
    entities.add(new Entity());

    when(entityService.getEntities()).thenReturn(entities);

    assertDoesNotThrow(() -> invokePrivate("activateWeather"));
  }

  @Test
  void waterCollisionIgnoresMissingBodyUserData() throws Exception {
    Fixture me = mock(Fixture.class);
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(null);

    assertDoesNotThrow(() -> invokeWaterCollided(me, other));
  }

  @Test
  void waterCollisionIgnoresMissingEntity() throws Exception {
    Fixture me = mock(Fixture.class);
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    BodyUserData data = new BodyUserData();

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    assertDoesNotThrow(() -> invokeWaterCollided(me, other));
  }

  @Test
  void waterCollisionIgnoresNonPlayerEntity() throws Exception {
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
}
