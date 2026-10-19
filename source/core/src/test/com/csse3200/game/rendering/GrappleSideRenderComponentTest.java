package com.csse3200.game.rendering;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.level.PlatformGrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;

/** Verifies the gold outline on grappleable platform sides without creating a GL context. */
@ExtendWith(GameExtension.class)
class GrappleSideRenderComponentTest {
  private static final float LINE_WIDTH = 0.12f;

  // The platform used throughout: left=2, right=6, bottom=3, top=4.
  private static final Vector2 BOTTOM_LEFT = new Vector2(2f, 3f);
  private static final Vector2 TOP_LEFT = new Vector2(2f, 4f);
  private static final Vector2 BOTTOM_RIGHT = new Vector2(6f, 3f);
  private static final Vector2 TOP_RIGHT = new Vector2(6f, 4f);

  private RenderService renderService;
  private SpriteBatch batch;

  @BeforeEach
  void setUp() {
    renderService = mock(RenderService.class);
    ServiceLocator.registerRenderService(renderService);
    batch = mock(SpriteBatch.class);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());
  }

  /**
   * The component builds its ShapeRenderer as soon as it's constructed, so this must be called
   * inside a {@code mockConstruction(ShapeRenderer.class)} block.
   */
  private GrappleSideRenderComponent renderer(PlatformGrappleComponent platform) {
    Entity entity = new Entity();
    if (platform != null) {
      entity.addComponent(platform);
    }
    entity.setPosition(2f, 3f);
    entity.setScale(4f, 1f);
    GrappleSideRenderComponent renderer = new GrappleSideRenderComponent();
    entity.addComponent(renderer);
    renderer.create();
    return renderer;
  }

  @Test
  void shouldOutlineOnlyTheGrappleableSides() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      GrappleSideRenderComponent renderer =
          renderer(
              new PlatformGrappleComponent(
                  PlatformGrappleComponent.LEFT_SIDE | PlatformGrappleComponent.BOTTOM_SIDE));

      renderer.render(batch);

      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape).setColor(Color.GOLD);
      verify(shape).rectLine(eq(BOTTOM_LEFT), eq(TOP_LEFT), eq(LINE_WIDTH));
      verify(shape).rectLine(eq(BOTTOM_LEFT), eq(BOTTOM_RIGHT), eq(LINE_WIDTH));
      verify(shape, never()).rectLine(eq(TOP_LEFT), eq(TOP_RIGHT), eq(LINE_WIDTH));
      verify(shape, never()).rectLine(eq(BOTTOM_RIGHT), eq(TOP_RIGHT), eq(LINE_WIDTH));
    }
  }

  @Test
  void shouldOutlineEverySideWhenAllAreGrappleable() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      GrappleSideRenderComponent renderer = renderer(new PlatformGrappleComponent(15));

      renderer.render(batch);

      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape).rectLine(eq(BOTTOM_LEFT), eq(TOP_LEFT), eq(LINE_WIDTH));
      verify(shape).rectLine(eq(TOP_LEFT), eq(TOP_RIGHT), eq(LINE_WIDTH));
      verify(shape).rectLine(eq(BOTTOM_RIGHT), eq(TOP_RIGHT), eq(LINE_WIDTH));
      verify(shape).rectLine(eq(BOTTOM_LEFT), eq(BOTTOM_RIGHT), eq(LINE_WIDTH));
    }
  }

  @Test
  void shouldDrawNoLinesButStillRestoreTheBatchWhenNoSideIsGrappleable() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      GrappleSideRenderComponent renderer = renderer(new PlatformGrappleComponent(0));

      renderer.render(batch);

      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape, never()).rectLine(any(Vector2.class), any(Vector2.class), eq(LINE_WIDTH));
      // The sprite batch is paused for the shape pass and must always be handed back.
      InOrder order = inOrder(batch, shape);
      order.verify(batch).end();
      order.verify(shape).begin(ShapeType.Filled);
      order.verify(shape).end();
      order.verify(batch).begin();
    }
  }

  @Test
  void shouldLeaveSpriteBatchAloneWithoutAPlatformGrappleComponent() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      GrappleSideRenderComponent renderer = renderer(null);

      renderer.render(batch);

      verifyNoInteractions(batch);
      verifyNoInteractions(shapes.constructed().getFirst());
    }
  }

  @Test
  void shouldReflectSidesBeingSwitchedOnAndOffDuringPlay() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      PlatformGrappleComponent platform =
          new PlatformGrappleComponent(PlatformGrappleComponent.TOP_SIDE);
      GrappleSideRenderComponent renderer = renderer(platform);
      ShapeRenderer shape = shapes.constructed().getFirst();

      renderer.render(batch);
      verify(shape, times(1)).rectLine(eq(TOP_LEFT), eq(TOP_RIGHT), eq(LINE_WIDTH));

      // e.g. a button press opens up the right-hand side and closes the top
      platform.updateGrappleSides(PlatformGrappleComponent.RIGHT_SIDE);
      renderer.render(batch);

      verify(shape, times(1)).rectLine(eq(TOP_LEFT), eq(TOP_RIGHT), eq(LINE_WIDTH));
      verify(shape, times(1)).rectLine(eq(BOTTOM_RIGHT), eq(TOP_RIGHT), eq(LINE_WIDTH));
    }
  }

  @Test
  void shouldDisposeShapeRendererAndUnregister() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      GrappleSideRenderComponent renderer = renderer(new PlatformGrappleComponent(15));

      renderer.dispose();

      verify(shapes.constructed().getFirst()).dispose();
      verify(renderService).unregister(renderer);
    }
  }
}
