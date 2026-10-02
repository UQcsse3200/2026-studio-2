package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.item.MeleeRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class MeleeRenderComponentTest {
  private RenderService service;
  private GameTime time;
  private SpriteBatch batch;
  private Entity player;
  private MeleeRenderComponent renderer;

  @BeforeEach
  void setUp() {
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
      order.verify(shape).setColor(Color.WHITE);
      order.verify(shape).rectLine(0f, 0f, 1.2f, 0f, 0.08f);
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
      verify(shapes.constructed().getFirst()).rectLine(0f, 0f, 1.2f, 0f, 0.08f);
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
      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape).rectLine(0f, 0f, 1.2f, 0f, 0.08f);
      verify(shape).rectLine(0f, 0f, -1.2f, 0f, 0.08f);
      renderer.dispose();
    }
  }

  @Test
  void shouldCopyAimAndFollowPlayerPositionWhileSwinging() {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      Vector2 aim = new Vector2(0f, 5f);
      player.getEvents().trigger("melee", aim);
      aim.set(1f, 0f);
      player.setPosition(1.5f, 2.5f);
      renderer.render(batch);
      verify(shapes.constructed().getFirst()).rectLine(2f, 3f, 2f, 4.2f, 0.08f);
      renderer.dispose();
    }
  }
}
