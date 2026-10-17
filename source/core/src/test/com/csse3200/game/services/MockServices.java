package com.csse3200.game.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;

/** Mock services that tests of the entity factories need but do not care about. */
public final class MockServices {
  private MockServices() {}

  /** Registers a lighting service whose engine has a ray handler, so lights can be created. */
  public static void registerLighting() {
    LightingEngine lightingEngine = mock(LightingEngine.class);
    when(lightingEngine.getRayHandler()).thenReturn(mock(RayHandler.class));
    LightingService lightingService = mock(LightingService.class);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerLightingService(lightingService);
  }

  /**
   * Registers a resource service that hands out a one by one texture for any path, so entities can
   * be built headlessly without loading real assets.
   *
   * @return the mock resource service
   */
  public static ResourceService registerTextures() {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);
    return resourceService;
  }

  /**
   * Makes a mock terrain factory hand out a 50 by 30 terrain of one-unit tiles, so an area can be
   * built without loading a map.
   */
  public static void stubFlatTerrain(TerrainFactory factory) {
    TerrainComponent terrain = mock(TerrainComponent.class);
    when(factory.createTerrain(any())).thenReturn(terrain);
    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(50, 30));
    when(terrain.tileToWorldPosition(any()))
        .thenAnswer(
            call -> {
              GridPoint2 tile = call.getArgument(0);
              return new Vector2(tile.x, tile.y);
            });
  }
}
