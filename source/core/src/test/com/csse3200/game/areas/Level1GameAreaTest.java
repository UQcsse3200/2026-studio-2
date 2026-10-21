package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Level1GameAreaTest extends AbstractAreaAssetsTest {

  private Level1GameArea gameArea;

  private TerrainFactory terrainFactory;
  private CameraComponent camera;
  private ResourceService resourceService;
  private EntityService entityService;
  private TerrainComponent terrain;

  @BeforeEach
  void setUp() {
    terrainFactory = mock(TerrainFactory.class);
    camera = mock(CameraComponent.class);
    resourceService = mock(ResourceService.class);
    entityService = mock(EntityService.class);
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
    assertSpawn(5, 3, Level1GameArea.SHOPKEEPER_SPAWN);
  }

  @Test
  void shouldHaveCorrectItemSpawns() {
    assertSpawn(2, 3, Level1GameArea.ROPE_ARROW_SPAWN);
    assertSpawn(4, 3, Level1GameArea.STANDARD_ARROW_SPAWN);
    assertSpawn(6, 3, Level1GameArea.FIRE_ARROW_SPAWN);
    assertSpawn(8, 5, Level1GameArea.ICE_ARROW_SPAWN);
    assertSpawn(10, 5, Level1GameArea.POISON_ARROW_SPAWN);
    assertSpawn(12, 5, Level1GameArea.HEALTH_POTION_SPAWN);
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
    assertEquals(3, Level1GameArea.GOLD_SPAWNS.size());
    assertSpawn(9, 5, Level1GameArea.GOLD_SPAWNS.get(0));
    assertSpawn(15, 7, Level1GameArea.GOLD_SPAWNS.get(1));
    assertSpawn(20, 8, Level1GameArea.GOLD_SPAWNS.get(2));
  }

  @Test
  void shouldHaveCorrectWheelTokenSpawn() {
    assertSpawn(32, 3, Level1GameArea.WHEEL_TOKEN_SPAWN);
  }

  private void assertSpawn(int expectedX, int expectedY, GridPoint2 actual) {
    assertEquals(expectedX, actual.x, "spawn x");
    assertEquals(expectedY, actual.y, "spawn y");
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
  void unloadAssetsUnloadsAllLevel1Resources() throws Exception {
    invokePrivate("unloadAssets");

    verify(resourceService, atLeastOnce()).unloadAssets(any(String[].class));
  }

  @Test
  void loadAssetsLoadsLevel1Ambience() throws Exception {
    when(resourceService.loadForMillis(anyInt())).thenReturn(true);

    invokePrivate("loadAssets");

    verify(resourceService)
        .loadMusic(
            argThat(
                (String[] music) -> Arrays.asList(music).contains(Level1GameArea.AMBIENCE_MUSIC)));
  }

  @Test
  void unloadAssetsUnloadsLevel1Ambience() throws Exception {
    invokePrivate("unloadAssets");

    verify(resourceService)
        .unloadAssets(
            argThat(
                (String[] assets) ->
                    Arrays.asList(assets).contains(Level1GameArea.AMBIENCE_MUSIC)));
  }

  @Override
  ResourceService resourceService() {
    return resourceService;
  }

  @Override
  void invokePrivate(String methodName) throws Exception {
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
}
