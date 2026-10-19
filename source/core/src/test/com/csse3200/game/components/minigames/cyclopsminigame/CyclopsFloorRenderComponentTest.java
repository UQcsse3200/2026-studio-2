package com.csse3200.game.components.minigames.cyclopsminigame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

class CyclopsFloorRenderComponentTest {
  @Test
  void repeatsTheStripAcrossTheRoomAndFillsBelowIt() {
    TextureRegion strip = mock(TextureRegion.class);
    TextureRegion fill = mock(TextureRegion.class);
    SpriteBatch batch = mock(SpriteBatch.class);
    CyclopsFloorRenderComponent floor =
        new CyclopsFloorRenderComponent(strip, fill, 4f, 2f, 12f, 8f);
    Entity entity = new Entity().addComponent(floor);
    entity.setPosition(3f, 10f);

    floor.render(batch);

    verify(batch).draw(strip, 3f, 8f, 4f, 2f);
    verify(batch).draw(strip, 7f, 8f, 4f, 2f);
    verify(batch).draw(strip, 11f, 8f, 4f, 2f);
    verify(batch).draw(fill, 3f, 2f, 12f, 6f);
    verifyNoMoreInteractions(batch);
    assertEquals(0, floor.getLayer());
    assertEquals(0f, floor.getZIndex());
  }

  @Test
  void finalTileCoversARoomWidthThatIsNotAnExactTileMultiple() {
    TextureRegion strip = mock(TextureRegion.class);
    TextureRegion fill = mock(TextureRegion.class);
    SpriteBatch batch = mock(SpriteBatch.class);
    CyclopsFloorRenderComponent floor =
        new CyclopsFloorRenderComponent(strip, fill, 4f, 2f, 6f, 5f);
    Entity entity = new Entity().addComponent(floor);
    entity.setPosition(-3f, 4f);

    floor.render(batch);

    verify(batch).draw(strip, -3f, 2f, 4f, 2f);
    verify(batch).draw(strip, 1f, 2f, 4f, 2f);
    verify(batch).draw(fill, -3f, -1f, 6f, 3f);
    verifyNoMoreInteractions(batch);
  }
}
