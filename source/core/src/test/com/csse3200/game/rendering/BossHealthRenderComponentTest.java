package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class BossHealthRenderComponentTest {
  @ParameterizedTest
  @ValueSource(ints = {100, 50, 0})
  void shouldAnchorTheBarToTheCameraAndScaleItsFillWithHealth(int health) {
    ServiceLocator.registerRenderService(mock(RenderService.class));
    OrthographicCamera worldCamera = new OrthographicCamera();
    worldCamera.position.set(20f, 10f, 0f);
    CameraComponent camera = mock(CameraComponent.class);
    when(camera.getCamera()).thenReturn(worldCamera);
    BossHealthRenderComponent bar = new BossHealthRenderComponent(camera);
    Entity boss =
        new Entity().addComponent(new CombatStatsComponent(100, 100, 1)).addComponent(bar);
    boss.create();
    boss.getEvents().trigger("updateHealth", health);
    SpriteBatch batch = mock(SpriteBatch.class);
    Color originalColor = new Color(0.2f, 0.3f, 0.4f, 0.7f);
    when(batch.getColor()).thenReturn(originalColor);

    try {
      bar.draw(batch);

      ArgumentCaptor<Float> x = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> y = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> width = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> height = ArgumentCaptor.forClass(Float.class);
      verify(batch, times(3))
          .draw(any(Texture.class), x.capture(), y.capture(), width.capture(), height.capture());
      assertEquals(13.97f, x.getAllValues().get(0), 0.0001f);
      assertEquals(15.47f, y.getAllValues().get(0), 0.0001f);
      assertEquals(12.06f, width.getAllValues().get(0), 0.0001f);
      assertEquals(0.31f, height.getAllValues().get(0), 0.0001f);
      assertEquals(14f, x.getAllValues().get(1), 0.0001f);
      assertEquals(15.5f, y.getAllValues().get(1), 0.0001f);
      assertEquals(12f, width.getAllValues().get(1), 0.0001f);
      assertEquals(0.25f, height.getAllValues().get(1), 0.0001f);
      assertEquals(14f, x.getAllValues().get(2), 0.0001f);
      assertEquals(15.5f, y.getAllValues().get(2), 0.0001f);
      assertEquals(12f * health / 100, width.getAllValues().get(2), 0.0001f);
      assertEquals(0.25f, height.getAllValues().get(2), 0.0001f);
      verify(batch).setColor(originalColor);
    } finally {
      bar.dispose();
    }
  }
}
