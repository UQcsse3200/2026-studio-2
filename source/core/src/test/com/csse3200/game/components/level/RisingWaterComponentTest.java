package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;

public class RisingWaterComponentTest {
  @Test
  void shouldGetSpeed() {
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    assertEquals(1f, water.getSpeed());
  }

  @Test
  void shouldSetSpeed() {
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    water.setSpeed(2f);
    assertEquals(2f, water.speed);
  }

  @Test
  void shouldSetCurrentHeight() {
    Entity hb = new Entity();
    Entity e = new Entity().addComponent(new RisingWaterComponent(1f, 10f, hb));
    RisingWaterComponent water = e.getComponent(RisingWaterComponent.class);

    water.setCurrentHeight(11f);
    assertEquals(11f, water.currentHeight);
    assertEquals(11f, e.getScale().y);
    assertEquals(10f, water.targetHitboxPosition.y);
    assertTrue(water.updatedHeight);
  }

  @Test
  void shouldGetCurrentHeight() {
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    assertEquals(10f, water.getCurrentHeight());
  }

  @Test
  void shouldGetStoredHeight() {
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    assertEquals(10f, water.getStoredHeight());
  }

  @Test
  void shouldCreateComponent() {
    RenderService render = mock(RenderService.class);
    ServiceLocator.registerRenderService(render);

    Stage stage = mock(Stage.class);
    when(stage.getWidth()).thenReturn(1f);
    when(render.getStage()).thenReturn(stage);

    Entity e = new Entity();
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    water.setEntity(e);
    water.create();
    assertEquals(1f, e.getScale().x);
  }

  @Test
  void shouldUpdateHeight() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0f);
    ServiceLocator.registerTimeSource(time);

    Entity hb = spy(new Entity());
    Entity e = new Entity().addComponent(new RisingWaterComponent(1f, 10f, hb));
    RisingWaterComponent water = e.getComponent(RisingWaterComponent.class);

    water.setCurrentHeight(11f);
    water.update();
    assertEquals(10f, hb.getPosition().y);
    assertFalse(water.updatedHeight);
  }

  @Test
  void updateShouldIncrementHeight() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.1f);
    ServiceLocator.registerTimeSource(time);

    Entity hb = spy(new Entity());
    Entity e = new Entity().addComponent(new RisingWaterComponent(1f, 10f, hb));
    RisingWaterComponent water = e.getComponent(RisingWaterComponent.class);

    water.update();

    assertEquals(10.1f, water.currentHeight);
  }

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

  @Test
  void shouldStopWaterMovement() {
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    water.toggleActivation(false);
    assertEquals(0f, water.speed);
  }

  @Test
  void shouldStartWaterMovement() {
    RisingWaterComponent water = new RisingWaterComponent(1f, 10f, mock(Entity.class));
    water.toggleActivation(false);
    assertEquals(0f, water.speed);
    water.toggleActivation(true);
    assertEquals(1f, water.speed);
  }
}
