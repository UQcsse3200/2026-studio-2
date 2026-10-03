package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

public class RisingWaterComponentTest {
  @Test
  void shouldCaptureCurrentHeight() {
    Entity e = mock(Entity.class);
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    water.setEntity(e);
    when(e.getPosition()).thenReturn(new Vector2(0f, -10f));

    water.captureValues(20);
    assertEquals(10, water.storedHeight);
  }

  @Test
  void shouldCaptureLenientHeight() {
    Entity e = mock(Entity.class);
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    water.currentHeight = 25f;
    water.setEntity(e);
    when(e.getPosition()).thenReturn(new Vector2(0f, -10f));

    water.captureValues(20);
    // must equal the checkpoint y - the lenience + 10 from the position being -10
    assertEquals(20 - water.CHECKPOINT_LENIENCE + 10, water.storedHeight);
  }

  @Test
  void shouldCaptureInitialHeight() {
    Entity e = mock(Entity.class);
    RisingWaterComponent water = new RisingWaterComponent(1f, 2f, mock(Entity.class));
    water.setEntity(e);
    when(e.getPosition()).thenReturn(new Vector2(0f, -10f));

    water.captureValues(5);
    assertEquals(water.initialHeight, water.storedHeight);
  }
}
