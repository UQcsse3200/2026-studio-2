package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.item.MeleeRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.invocation.Invocation;

@ExtendWith(GameExtension.class)
class MeleeRenderComponentTest {
  private static final float LINE_WIDTH = 0.08f;

  /** Keep in sync with ARC_DEGREES in MeleeRenderComponent. */
  private static final float ARC_DEGREES = 120f;

  private RenderService service;
  private GameTime time;
  private SpriteBatch batch;
  private Entity player;
  private MeleeRenderComponent renderer;

  @BeforeEach
  void setUp() {
    Gdx.gl = mock(GL20.class); // the arc is drawn with alpha blending
    service = mock(RenderService.class);
    time = mock(GameTime.class);
    batch = mock(SpriteBatch.class);
    ServiceLocator.registerRenderService(service);
    ServiceLocator.registerTimeSource(time);
    renderer = new MeleeRenderComponent();
    player = new Entity().addComponent(renderer);
    player.setPosition(-0.5f, -0.5f); // Center at the origin.
    player.create();
  }

  /** Every segment sent to rectLine as {x1, y1, x2, y2}, in call order. */
  private static List<float[]> segments(ShapeRenderer shape) {
    List<float[]> result = new ArrayList<>();
    for (Invocation call : mockingDetails(shape).getInvocations()) {
      if (call.getMethod().getName().equals("rectLine") && call.getArguments().length == 5) {
        float[] segment = new float[4];
        for (int i = 0; i < 4; i++) {
          segment[i] = ((Number) call.getArgument(i)).floatValue();
        }
        result.add(segment);
      }
    }
    return result;
  }

  private static float distance(float x, float y, float cx, float cy) {
    return (float) Math.hypot(x - cx, y - cy);
  }

  private static float angleDegrees(float x, float y, float cx, float cy) {
    return (float) Math.toDegrees(Math.atan2(y - cy, x - cx));
  }

  @Test
  void shouldNotDrawOrAllocateBeforeAnAttack() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      renderer.dispose();
      assertTrue(shapes.constructed().isEmpty());
      verifyNoInteractions(batch);
      verify(service).unregister(renderer);
    }
  }

  @Test
  void shouldDrawNormalizedSlashAndRestoreSpriteBatch() {
    Vector2 aim = new Vector2(10f, 0f);
    Matrix4 projection = new Matrix4();
    when(batch.getProjectionMatrix()).thenReturn(projection);
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      player.getEvents().trigger("melee", aim);
      renderer.render(batch);
      assertEquals(new Vector2(10f, 0f), aim);
      ShapeRenderer shape = shapes.constructed().getFirst();
      InOrder order = inOrder(batch, shape);
      order.verify(batch).end();
      order.verify(batch).getProjectionMatrix();
      order.verify(shape).setProjectionMatrix(projection);
      order.verify(shape).begin(ShapeRenderer.ShapeType.Filled);
      order.verify(shape).setColor(1f, 1f, 1f, 1f);
      order
              .verify(shape, atLeastOnce())
              .rectLine(anyFloat(), anyFloat(), anyFloat(), anyFloat(), eq(LINE_WIDTH));
      order.verify(shape).end();
      order.verify(batch).begin();
      order.verifyNoMoreInteractions();
      renderer.dispose();
      verify(shape).dispose();
    }
  }

  @Test
  void shouldStopDrawingAtSwingDurationBoundary() {
    player.getEvents().trigger("melee", Vector2.X);
    when(time.getDeltaTime()).thenReturn(0.12f);
    renderer.update();
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      assertTrue(shapes.constructed().isEmpty());
      verifyNoInteractions(batch);
    }
  }

  @Test
  void shouldStillDrawBeforeSwingDurationExpires() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      player.getEvents().trigger("melee", Vector2.X);
      when(time.getDeltaTime()).thenReturn(0.1f);
      renderer.update();
      renderer.render(batch);
      verify(shapes.constructed().getFirst(), atLeastOnce())
              .rectLine(anyFloat(), anyFloat(), anyFloat(), anyFloat(), eq(LINE_WIDTH));
      renderer.dispose();
    }
  }

  @Test
  void shouldSweepAnArcAroundTheAimAsTheSwingProgresses() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      player.getEvents().trigger("melee", Vector2.X);
      when(time.getDeltaTime()).thenReturn(0.06f); // halfway through the swing
      renderer.update();
      renderer.render(batch);

      List<float[]> lines = segments(shapes.constructed().getFirst());
      assertTrue(lines.size() > 1, "a half-finished swing is drawn as several segments");

      float radius = distance(lines.getFirst()[0], lines.getFirst()[1], 0f, 0f);
      assertTrue(radius > 0.5f);
      for (float[] line : lines) {
        assertEquals(radius, distance(line[0], line[1], 0f, 0f), 0.001f);
        assertEquals(radius, distance(line[2], line[3], 0f, 0f), 0.001f);
      }

      // The arc starts at the edge of the swing, half the arc to one side of the aim...
      assertEquals(
              -ARC_DEGREES / 2f,
              angleDegrees(lines.getFirst()[0], lines.getFirst()[1], 0f, 0f),
              0.5f);
      // ...and has swept half way through it by the midpoint of the swing.
      assertEquals(0f, angleDegrees(lines.getLast()[2], lines.getLast()[3], 0f, 0f), 0.5f);
      renderer.dispose();
    }
  }

  @Test
  void shouldRestartExpiredSlashWithNewAimAndReuseRenderer() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      player.getEvents().trigger("melee", Vector2.X);
      renderer.render(batch);
      when(time.getDeltaTime()).thenReturn(1f);
      renderer.update();
      player.getEvents().trigger("melee", new Vector2(-4f, 0f));
      renderer.render(batch);
      assertEquals(1, shapes.constructed().size());

      List<float[]> lines = segments(shapes.constructed().getFirst());
      assertTrue(lines.getFirst()[0] > 0f, "first swing is on the right");
      assertTrue(lines.getLast()[0] < 0f, "second swing is on the left");
      renderer.dispose();
    }
  }

  @Test
  void shouldCopyAimAndFollowPlayerPositionWhileSwinging() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      Vector2 aim = new Vector2(0f, 5f);
      player.getEvents().trigger("melee", aim);
      aim.set(1f, 0f); // changing the original must not affect the swing
      player.setPosition(1.5f, 2.5f); // centre moves to (2, 3)
      renderer.render(batch);

      float[] first = segments(shapes.constructed().getFirst()).getFirst();
      assertTrue(distance(first[0], first[1], 2f, 3f) > 0.5f);
      // Aim was up (90 degrees), so the swing starts half the arc to one side of that.
      assertEquals(90f - ARC_DEGREES / 2f, angleDegrees(first[0], first[1], 2f, 3f), 0.5f);
      renderer.dispose();
    }
  }
}