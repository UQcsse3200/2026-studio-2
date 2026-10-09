package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.weapons.melee.MeleeComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

/** Verifies the slash geometry, fading, graphics state, and resource lifecycle. */
@ExtendWith(GameExtension.class)
class MeleeRenderDrawingTest {
  private GL20 previousGl;
  private MeleeComponent melee;
  private MeleeRenderComponent renderer;
  private SpriteBatch batch;
  private final Matrix4 projection = new Matrix4();

  @BeforeEach
  void setUp() {
    previousGl = Gdx.gl;
    Gdx.gl = mock(GL20.class);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    melee = mock(MeleeComponent.class);
    when(melee.isSwinging()).thenReturn(true);
    when(melee.getFacing()).thenReturn(1);
    renderer = new MeleeRenderComponent();
    new Entity().addComponent(melee).addComponent(renderer);
    renderer.create();
    batch = mock(SpriteBatch.class);
    when(batch.getProjectionMatrix()).thenReturn(projection);
  }

  @AfterEach
  void restoreGraphics() {
    Gdx.gl = previousGl;
  }

  @Test
  void shouldDrawAContinuousArcAndRestoreGraphicsState() {
    when(melee.getSwingProgress()).thenReturn(0.5f);
    try (var shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();
      var order = inOrder(batch, shape, Gdx.gl);
      order.verify(batch).end();
      order.verify(Gdx.gl).glEnable(GL20.GL_BLEND);
      order.verify(Gdx.gl).glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      order.verify(shape).setProjectionMatrix(projection);
      order.verify(shape).begin(ShapeRenderer.ShapeType.Filled);
      order.verify(shape).setColor(1f, 1f, 1f, 0.45f);
      ArgumentCaptor<Float> x1 = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> y1 = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> x2 = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> y2 = ArgumentCaptor.forClass(Float.class);
      order
          .verify(shape, times(24))
          .rectLine(x1.capture(), y1.capture(), x2.capture(), y2.capture(), eq(0.08f));
      order.verify(shape).end();
      order.verify(Gdx.gl).glDisable(GL20.GL_BLEND);
      order.verify(batch).begin();
      for (int i = 0; i < 24; i++) {
        Vector2 end = new Vector2(x2.getAllValues().get(i), y2.getAllValues().get(i));
        assertEquals(MeleeComponent.RANGE, end.dst(0.5f, 0.5f), 0.001f);
        assertTrue(end.x > 0.5f, "right-facing slash must stay in front of the player");
        if (i > 0) {
          assertEquals(x2.getAllValues().get(i - 1), x1.getAllValues().get(i));
          assertEquals(y2.getAllValues().get(i - 1), y1.getAllValues().get(i));
        }
      }
      renderer.render(batch);
      assertEquals(1, shapes.constructed().size(), "reuse the shape renderer across frames");
      renderer.dispose();
      verify(shape).dispose();
    }
  }

  @Test
  void shouldMirrorTheSweepingArcWhenFacingLeft() {
    when(melee.getFacing()).thenReturn(-1);
    when(melee.getSwingProgress()).thenReturn(0.15f);
    try (var shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();
      ArgumentCaptor<Float> ends = ArgumentCaptor.forClass(Float.class);
      verify(shape, times(24))
          .rectLine(anyFloat(), anyFloat(), ends.capture(), anyFloat(), eq(0.08f));
      assertTrue(ends.getAllValues().stream().allMatch(x -> x < 0.5f));
      assertEquals(0.5f - MeleeComponent.RANGE, ends.getValue(), 0.0001f);
    }
  }

  @Test
  void shouldSkipDrawingAtTheStartAndEndOfTheSweep() {
    try (var shapes = mockConstruction(ShapeRenderer.class)) {
      when(melee.getSwingProgress()).thenReturn(0f);
      renderer.render(batch);
      when(melee.getSwingProgress()).thenReturn(1f);
      renderer.render(batch);
      verifyNoInteractions(batch);
      assertTrue(shapes.constructed().isEmpty());
    }
  }
}
