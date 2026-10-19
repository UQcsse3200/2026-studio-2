package com.csse3200.game.areas.terrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class TerrainFactoryTest {
  @ParameterizedTest
  @CsvSource({"BACKGROUND_DESERT, 2.0, 4.0", "CYCLOPS_ROOM, 1.0, 2.0"})
  void filledTerrainRetainsItsWorldScaleAndTransparentTiles(
      TerrainFactory.TerrainType type, float worldX, float worldY) {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(16);
    when(texture.getHeight()).thenReturn(16);
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset("images/ui/transparent.png", Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resources);
    ServiceLocator.registerRenderService(new RenderService());
    // The tile map is real; only its GPU renderer is isolated from the headless test.
    try (MockedConstruction<OrthogonalTiledMapRenderer> renderers =
        mockConstruction(OrthogonalTiledMapRenderer.class)) {
      TerrainComponent terrain = new TerrainFactory(new CameraComponent()).createTerrain(type);
      try {
        assertEquals(new GridPoint2(90, 30), terrain.getMapBounds(0));
        assertEquals(new Vector2(worldX, worldY), terrain.tileToWorldPosition(2, 4));
        assertEquals(1, terrain.getMap().getLayers().getCount());
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        for (int x = 0; x < 90; x++) {
          for (int y = 0; y < 30; y++) {
            assertSame(texture, layer.getCell(x, y).getTile().getTextureRegion().getTexture());
          }
        }
      } finally {
        terrain.dispose();
      }
    }
  }
}
