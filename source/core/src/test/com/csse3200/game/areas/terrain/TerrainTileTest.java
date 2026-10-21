package com.csse3200.game.areas.terrain;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile.BlendMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerrainTileTest {
  private TerrainTile tile;
  private TextureRegion textureRegion;

  @BeforeEach
  void setUp() {
    textureRegion = new TextureRegion();
    tile = new TerrainTile(textureRegion);
  }

  @Test
  void constructorStoresTextureRegion() {
    assertSame(textureRegion, tile.getTextureRegion());
  }

  @Test
  void defaultIdIsZero() {
    assertEquals(0, tile.getId());
  }

  @Test
  void setIdUpdatesId() {
    tile.setId(42);
    assertEquals(42, tile.getId());

    tile.setId(7);
    assertEquals(7, tile.getId());
  }

  @Test
  void defaultBlendModeIsAlpha() {
    assertEquals(BlendMode.ALPHA, tile.getBlendMode());
  }

  @Test
  void setBlendModeUpdatesBlendMode() {
    tile.setBlendMode(BlendMode.NONE);
    assertEquals(BlendMode.NONE, tile.getBlendMode());

    tile.setBlendMode(BlendMode.ALPHA);
    assertEquals(BlendMode.ALPHA, tile.getBlendMode());
  }

  @Test
  void setTextureRegionUpdatesRegion() {
    TextureRegion newRegion = new TextureRegion();

    tile.setTextureRegion(newRegion);

    assertSame(newRegion, tile.getTextureRegion());
  }

  @Test
  void defaultOffsetsAreZero() {
    assertEquals(0f, tile.getOffsetX());
    assertEquals(0f, tile.getOffsetY());
  }

  @Test
  void setOffsetXUpdatesOffset() {
    tile.setOffsetX(3.5f);
    assertEquals(3.5f, tile.getOffsetX());

    tile.setOffsetX(-2f);
    assertEquals(-2f, tile.getOffsetX());
  }

  @Test
  void setOffsetYUpdatesOffset() {
    tile.setOffsetY(4.5f);
    assertEquals(4.5f, tile.getOffsetY());

    tile.setOffsetY(-1.5f);
    assertEquals(-1.5f, tile.getOffsetY());
  }

  @Test
  void propertiesAreNotImplemented() {
    assertNull(tile.getProperties());
  }

  @Test
  void objectsAreNotImplemented() {
    assertNull(tile.getObjects());
  }
}
