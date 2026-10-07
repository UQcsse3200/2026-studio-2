package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class Level2GameAreaTest {

  private TerrainFactory terrainFactory;
  private CameraComponent camera;
  private Entity player;
  private ResourceService resourceService;
  private Music music;
  private TerrainComponent terrain;
  private EntityService entityService;
  private Level2GameArea area;

  @BeforeEach
  void setUp() {
    terrainFactory = mock(TerrainFactory.class);
    camera = mock(CameraComponent.class);
    player = new Entity();

    resourceService = mock(ResourceService.class);
    music = mock(Music.class);

    entityService = mock(EntityService.class);

    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerResourceService(resourceService);

    when(resourceService.loadForMillis(anyInt())).thenReturn(true);
    when(resourceService.getAsset("sounds/BGM_03_mp3.mp3", Music.class)).thenReturn(music);

    terrain = mock(TerrainComponent.class);

    area = new Level2GameArea(terrainFactory, camera, player);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void constructorCreatesLevel2Area() {
    assertNotNull(area);
  }

  @Test
  void constructorStoresPlayer() throws Exception {
    Field playerField = GameArea.class.getDeclaredField("player");
    playerField.setAccessible(true);

    assertSame(player, playerField.get(area));
  }

  @Test
  void constructorCreatesLevel2Config() throws Exception {
    Field configField = GameArea.class.getDeclaredField("config");
    configField.setAccessible(true);

    Object config = configField.get(area);

    assertNotNull(config);
    assertEquals("Level2Config", config.getClass().getSimpleName());
  }

  @Test
  void loadAssetsLoadsLevel2Resources() throws Exception {
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
  void unloadAssetsUnloadsAllLevel2Resources() throws Exception {
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
  void skeletonSpawnLocationsAreCorrect() throws Exception {
    Field field = Level2GameArea.class.getDeclaredField("skeletonWarriorSpawnLocations");
    field.setAccessible(true);

    GridPoint2[] locations = (GridPoint2[]) field.get(null);

    assertEquals(3, locations.length);
    assertEquals(new GridPoint2(6, 23), locations[0]);
    assertEquals(new GridPoint2(11, 23), locations[1]);
    assertEquals(new GridPoint2(17, 23), locations[2]);
  }

  @Test
  void worldBoundsCanBeStoredForMapUse() throws Exception {
    Field field = Level2GameArea.class.getDeclaredField("worldBounds");
    field.setAccessible(true);

    Vector2 bounds = new Vector2(50f, 45f);
    field.set(area, bounds);

    assertEquals(bounds, field.get(area));
  }

  private void invokePrivate(String methodName) throws Exception {
    Method method = Level2GameArea.class.getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(area);
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
  void spawnTerrainSetsWorldBounds() throws Exception {
    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(90, 30));

    invokePrivate("spawnTerrain");

    Field field = Level2GameArea.class.getDeclaredField("worldBounds");
    field.setAccessible(true);

    Vector2 bounds = (Vector2) field.get(area);

    assertEquals(50f, bounds.x);
    assertEquals(45f, bounds.y);
  }

  @Test
  void disposeStopsBackgroundMusic() {
    area.dispose();

    verify(resourceService).getAsset("sounds/BGM_03_mp3.mp3", Music.class);
    verify(music).stop();
  }

  @Test
  void disposeUnloadsLevel2Assets() {
    area.dispose();

    verify(resourceService, times(4)).unloadAssets(any(String[].class));
  }
}
