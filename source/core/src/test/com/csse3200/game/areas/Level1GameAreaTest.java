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
class Level1GameAreaTest {

  private Level1GameArea gameArea;

  private TerrainFactory terrainFactory;
  private CameraComponent camera;
  private ResourceService resourceService;
  private EntityService entityService;
  private Music music;
  private TerrainComponent terrain;

  @BeforeEach
  void setUp() {
    terrainFactory = mock(TerrainFactory.class);
    camera = mock(CameraComponent.class);
    resourceService = mock(ResourceService.class);
    entityService = mock(EntityService.class);
    music = mock(Music.class);
    terrain = mock(TerrainComponent.class);

    ServiceLocator.registerResourceService(resourceService);
    ServiceLocator.registerEntityService(entityService);

    when(resourceService.loadForMillis(anyInt())).thenReturn(true);

    gameArea = new Level1GameArea(terrainFactory, camera);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void shouldCreateLevel1GameArea() {
    assertNotNull(gameArea);
  }

  @Test
  void shouldInitiallyHaveNoInput() {
    assertNull(gameArea.getInput());
  }

  @Test
  void shouldHaveCorrectShopkeeperSpawn() {
    assertEquals(new GridPoint2(5, 3), Level1GameArea.SHOPKEEPER_SPAWN);
  }

  @Test
  void shouldHaveCorrectItemSpawns() {
    assertEquals(new GridPoint2(2, 3), Level1GameArea.ROPE_ARROW_SPAWN);
    assertEquals(new GridPoint2(4, 3), Level1GameArea.STANDARD_ARROW_SPAWN);
    assertEquals(new GridPoint2(6, 3), Level1GameArea.FIRE_ARROW_SPAWN);
    assertEquals(new GridPoint2(8, 5), Level1GameArea.ICE_ARROW_SPAWN);
    assertEquals(new GridPoint2(10, 5), Level1GameArea.POISON_ARROW_SPAWN);
    assertEquals(new GridPoint2(12, 5), Level1GameArea.HEALTH_POTION_SPAWN);
  }

  @Test
  void shouldHaveCorrectItemQuantities() {
    assertEquals(5, Level1GameArea.STANDARD_ARROW_QUANTITY);
    assertEquals(5, Level1GameArea.FIRE_ARROW_QUANTITY);
    assertEquals(5, Level1GameArea.ICE_ARROW_QUANTITY);
    assertEquals(5, Level1GameArea.POISON_ARROW_QUANTITY);
    assertEquals(3, Level1GameArea.HEALTH_POTION_QUANTITY);
  }

  @Test
  void shouldHaveCorrectGoldSpawns() {
    assertArrayEquals(
        new GridPoint2[] {new GridPoint2(9, 5), new GridPoint2(15, 7), new GridPoint2(20, 8)},
        Level1GameArea.GOLD_SPAWNS);
  }

  @Test
  void shouldHaveCorrectWheelTokenSpawn() {
    assertEquals(new GridPoint2(32, 3), Level1GameArea.WHEEL_TOKEN_SPAWN);
  }

  @Test
  void constructorStoresTerrainFactory() throws Exception {
    Field field = Level1GameArea.class.getDeclaredField("terrainFactory");
    field.setAccessible(true);

    assertSame(terrainFactory, field.get(gameArea));
  }

  @Test
  void constructorStoresCamera() throws Exception {
    Field field = Level1GameArea.class.getDeclaredField("camera");
    field.setAccessible(true);

    assertSame(camera, field.get(gameArea));
  }

  @Test
  void loadAssetsLoadsLevel1Resources() throws Exception {
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
  void unloadAssetsUnloadsAllLevel1Resources() throws Exception {
    invokePrivate("unloadAssets");

    verify(resourceService, atLeastOnce()).unloadAssets(any(String[].class));
  }

  private void invokePrivate(String methodName) throws Exception {
    Method method = Level1GameArea.class.getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(gameArea);
  }

  @Test
  void spawnTerrainCreatesBackgroundDesertTerrain() throws Exception {
    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(100, 50));

    invokePrivate("spawnTerrain");

    verify(terrainFactory).createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT);
  }

  @Test
  void spawnTerrainSetsWorldBounds() throws Exception {
    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    when(terrain.getTileSize()).thenReturn(2f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(100, 50));

    invokePrivate("spawnTerrain");

    Field field = Level1GameArea.class.getDeclaredField("worldBounds");
    field.setAccessible(true);

    Vector2 bounds = (Vector2) field.get(gameArea);

    assertNotNull(bounds);
    assertTrue(bounds.x > 0);
    assertTrue(bounds.y > 0);
  }

  @Test
  void playMusicConfiguresAndStartsBackgroundMusic() throws Exception {
    when(resourceService.getAsset(anyString(), eq(Music.class))).thenReturn(music);

    invokePrivate("playMusic");

    verify(resourceService).getAsset(anyString(), eq(Music.class));
    verify(music).setLooping(true);
    verify(music).setVolume(anyFloat());
    verify(music).play();
  }
}
