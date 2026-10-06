package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.item.GrappleRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;

/** Verifies rope geometry sent to the graphics boundary without creating an OpenGL context. */
@ExtendWith(GameExtension.class)
class GrappleRenderComponentTest {
  private RenderService service;
  private SpriteBatch batch;

  @BeforeEach
  void setUp() {
    service = mock(RenderService.class);
    batch = mock(SpriteBatch.class);
    ServiceLocator.registerRenderService(service);
  }

  private GrappleRenderComponent renderer(GrappleComponent grapple) {
    Entity entity = new Entity();
    if (grapple != null) {
      entity.addComponent(grapple);
    }
    GrappleRenderComponent renderer = new GrappleRenderComponent();
    entity.addComponent(renderer);
    renderer.create();
    return renderer;
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenGrappleIsMissing() {
    GrappleRenderComponent renderer = renderer(null);
    renderer.render(batch);
    renderer.dispose();
    verifyNoInteractions(batch);
    verify(service).unregister(renderer);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenDetached() {
    GrappleRenderComponent renderer = renderer(new GrappleComponent());
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldSkipPathsWithoutAnyCompleteSegment() {
    GrappleComponent grapple = mock(GrappleComponent.class);
    when(grapple.isAttached()).thenReturn(true);
    GrappleRenderComponent renderer = renderer(grapple);
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      when(grapple.getRopePath()).thenReturn(List.of());
      renderer.render(batch);
      when(grapple.getRopePath()).thenReturn(List.of(new Vector2(1f, 2f)));
      renderer.render(batch);
      assertEquals(0, shapes.constructed().size());
      verifyNoInteractions(batch);
    }
  }

  @Test
  void shouldDrawTheRopeWhileArrowIsFlyingBeforeTerrainAttachment() {
    GrappleComponent grapple = mock(GrappleComponent.class);
    when(grapple.isAttached()).thenReturn(false);
    when(grapple.getRopePath())
        .thenReturn(List.of(new Vector2(0f, 0f), new Vector2(2f, 3f), new Vector2(5f, 3f)));
    GrappleRenderComponent renderer = renderer(grapple);
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      assertEquals(1, shapes.constructed().size());
      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape).rectLine(0f, 0f, 2f, 3f, 0.05f);
      verify(shape).rectLine(2f, 3f, 5f, 3f, 0.05f);
      verify(batch).end();
      verify(batch).begin();
      renderer.dispose();
    }
  }

  @Test
  void shouldDrawEveryBendInOrderAndRestoreSpriteBatch() {
    GrappleComponent grapple = mock(GrappleComponent.class);
    when(grapple.isAttached()).thenReturn(true);
    when(grapple.getRopePath())
        .thenReturn(List.of(new Vector2(0f, 0f), new Vector2(2f, 3f), new Vector2(5f, 3f)));
    Matrix4 projection = new Matrix4();
    when(batch.getProjectionMatrix()).thenReturn(projection);
    GrappleRenderComponent renderer = renderer(grapple);
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      assertEquals(1, shapes.constructed().size());
      ShapeRenderer shape = shapes.constructed().getFirst();
      InOrder order = inOrder(batch, shape);
      order.verify(batch).end();
      order.verify(batch).getProjectionMatrix();
      order.verify(shape).setProjectionMatrix(projection);
      order.verify(shape).begin(ShapeRenderer.ShapeType.Filled);
      order.verify(shape).setColor(Color.BROWN);
      order.verify(shape).rectLine(0f, 0f, 2f, 3f, 0.05f);
      order.verify(shape).rectLine(2f, 3f, 5f, 3f, 0.05f);
      order.verify(shape).end();
      order.verify(batch).begin();
      order.verifyNoMoreInteractions();
      renderer.dispose();
      verify(shape).dispose();
      verify(service).unregister(renderer);
    }
  }

  @Test
  void shouldReuseRendererAndUseUpdatedPathOnNextFrame() {
    GrappleComponent grapple = mock(GrappleComponent.class);
    when(grapple.isAttached()).thenReturn(true);
    when(grapple.getRopePath()).thenReturn(List.of(new Vector2(0f, 0f), new Vector2(2f, 3f)));
    GrappleRenderComponent renderer = renderer(grapple);
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      when(grapple.getRopePath()).thenReturn(List.of(new Vector2(1f, 1f), new Vector2(4f, 6f)));
      renderer.render(batch);
      assertEquals(1, shapes.constructed().size());
      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape).rectLine(0f, 0f, 2f, 3f, 0.05f);
      verify(shape).rectLine(1f, 1f, 4f, 6f, 0.05f);
      verify(batch, times(2)).begin();
      renderer.dispose();
    }
  }
}
