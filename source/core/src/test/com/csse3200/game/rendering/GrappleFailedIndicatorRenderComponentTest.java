package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.item.GrappleFailedIndicatorRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;

/** Verifies the red cross shown for a failed grapple, without creating an OpenGL context. */
@ExtendWith(GameExtension.class)
class GrappleFailedIndicatorRenderComponentTest {
  private static final float DURATION = 0.8f;
  private static final float HALF_SIZE = 0.25f;

  private GameTime time;
  private SpriteBatch batch;
  private Entity player;
  private GrappleFailedIndicatorRenderComponent indicator;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerRenderService(mock(RenderService.class));
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    batch = mock(SpriteBatch.class);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());

    indicator = new GrappleFailedIndicatorRenderComponent();
    player = new Entity().addComponent(indicator);
    player.create();
  }

  private void failAt(float x, float y) {
    player.getEvents().trigger("grappleFailed", new Vector2(x, y));
  }

  private void pass(float seconds) {
    when(time.getDeltaTime()).thenReturn(seconds);
    indicator.update();
  }

  @Test
  void shouldDrawNothingUntilAGrappleFails() {
    indicator.render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldDrawARedCrossCentredOnWhereTheArrowLanded() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      failAt(4f, 1.5f);

      indicator.render(batch);

      ShapeRenderer shape = shapes.constructed().get(0);
      // Two lines through the landing point, one each way, so they form an X.
      verify(shape)
          .rectLine(
              eq(4f - HALF_SIZE),
              eq(1.5f - HALF_SIZE),
              eq(4f + HALF_SIZE),
              eq(1.5f + HALF_SIZE),
              anyFloat());
      verify(shape)
          .rectLine(
              eq(4f - HALF_SIZE),
              eq(1.5f + HALF_SIZE),
              eq(4f + HALF_SIZE),
              eq(1.5f - HALF_SIZE),
              anyFloat());
      ArgumentCaptor<Float> red = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> green = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> alpha = ArgumentCaptor.forClass(Float.class);
      verify(shape).setColor(red.capture(), green.capture(), eq(0.1f), alpha.capture());
      assertTrue(red.getValue() > 0.7f, "it should be red");
      assertTrue(green.getValue() < 0.3f, "it should be red");
      assertEquals(1f, alpha.getValue(), 1e-5f, "it starts fully visible");
    }
  }

  @Test
  void shouldHandTheSpriteBatchBackAfterDrawing() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      failAt(1f, 1f);

      indicator.render(batch);

      InOrder order = inOrder(batch, shapes.constructed().get(0));
      order.verify(batch).end();
      order.verify(shapes.constructed().get(0)).begin(ShapeRenderer.ShapeType.Filled);
      order.verify(shapes.constructed().get(0)).end();
      order.verify(batch).begin();
    }
  }

  @Test
  void shouldFadeOutOverItsDuration() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      failAt(2f, 2f);

      pass(DURATION / 2f);
      indicator.render(batch);

      ArgumentCaptor<Float> alpha = ArgumentCaptor.forClass(Float.class);
      verify(shapes.constructed().get(0))
          .setColor(anyFloat(), anyFloat(), anyFloat(), alpha.capture());
      assertEquals(0.5f, alpha.getValue(), 1e-4f);
    }
  }

  @Test
  void shouldDisappearOnceItsDurationIsUp() {
    failAt(2f, 2f);

    pass(DURATION);
    indicator.render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldNotCountDownBelowNothingOrComeBackAfterwards() {
    failAt(2f, 2f);

    pass(DURATION * 3f);
    pass(DURATION * 3f);
    indicator.render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldRestartAtTheNewSpotWhenAnotherGrappleFails() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      failAt(2f, 2f);
      pass(DURATION * 0.9f);

      failAt(9f, 0f);
      indicator.render(batch);

      ShapeRenderer shape = shapes.constructed().get(0);
      verify(shape)
          .rectLine(eq(9f - HALF_SIZE), eq(0f - HALF_SIZE), anyFloat(), anyFloat(), anyFloat());
      verify(shape, never())
          .rectLine(eq(2f - HALF_SIZE), anyFloat(), anyFloat(), anyFloat(), anyFloat());
      ArgumentCaptor<Float> alpha = ArgumentCaptor.forClass(Float.class);
      verify(shape).setColor(anyFloat(), anyFloat(), anyFloat(), alpha.capture());
      assertEquals(1f, alpha.getValue(), 1e-5f, "a new failure starts fully visible again");
    }
  }

  @Test
  void shouldIgnoreAFailureWithNoPoint() {
    player.getEvents().trigger("grappleFailed", (Vector2) null);

    indicator.render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldNotBeAffectedByLaterChangesToThePointItWasGiven() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      Vector2 landed = new Vector2(3f, 3f);
      player.getEvents().trigger("grappleFailed", landed);
      landed.set(50f, 50f);

      indicator.render(batch);

      verify(shapes.constructed().get(0))
          .rectLine(eq(3f - HALF_SIZE), eq(3f - HALF_SIZE), anyFloat(), anyFloat(), anyFloat());
    }
  }
}
