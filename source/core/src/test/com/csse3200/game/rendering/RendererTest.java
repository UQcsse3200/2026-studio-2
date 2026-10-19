package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class RendererTest {
  @Spy OrthographicCamera camera;
  @Mock SpriteBatch spriteBatch;
  @Mock Stage stage;
  @Mock Graphics graphics;
  @Mock RenderService renderService;
  @Mock DebugRenderer debugRenderer;
  PhysicsService physicsService;

  @BeforeEach
  void beforeEach() {
    Gdx.graphics = graphics;
    physicsService = new PhysicsService(mock(PhysicsEngine.class));
  }

  @Test
  void shouldResizeCamera() {
    CameraComponent cameraComponent = makeCameraEntity(camera);

    Viewport viewportMock = mock(Viewport.class);
    when(stage.getViewport()).thenReturn(viewportMock);
    when(graphics.getWidth()).thenReturn(100);
    when(graphics.getHeight()).thenReturn(200);
    Renderer renderer =
        new Renderer(cameraComponent, 10, spriteBatch, stage, renderService, debugRenderer);

    assertEquals(Vector3.Zero, camera.position);
    assertEquals(10, camera.viewportWidth);
    assertEquals(20, camera.viewportHeight);

    renderer.resize(200, 100);
    assertEquals(10, camera.viewportWidth);
    assertEquals(5, camera.viewportHeight);
  }

  @Test
  void shouldResizeViewPort() {
    CameraComponent cameraComponent = makeCameraEntity(camera);
    ScreenViewport screenViewport = spy(ScreenViewport.class);
    Stage viewportStage = new Stage(screenViewport, spriteBatch);

    Renderer renderer =
        new Renderer(cameraComponent, 10, spriteBatch, viewportStage, renderService, debugRenderer);

    assertEquals(0, viewportStage.getViewport().getScreenWidth());
    assertEquals(0, viewportStage.getViewport().getScreenHeight());

    renderer.resize(200, 100);
    verify(screenViewport).update(200, 100, true);
    assertEquals(200, viewportStage.getViewport().getScreenWidth());
    assertEquals(100, viewportStage.getViewport().getScreenHeight());
  }

  @Test
  void shouldRender() {
    CameraComponent cameraComponent = makeCameraEntity(camera);
    Renderer renderer =
        new Renderer(cameraComponent, 10, spriteBatch, stage, renderService, debugRenderer);
    renderer.render();
    verify(renderService).render(spriteBatch);
  }

  @Test
  void shouldRestoreDefaultClearColourBeforeRendering() {
    CameraComponent cameraComponent = makeCameraEntity(camera);
    Renderer renderer =
        new Renderer(cameraComponent, 10, spriteBatch, stage, renderService, debugRenderer);
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    clearInvocations(Gdx.gl);

    renderer.render();

    var order = inOrder(Gdx.gl, renderService);
    order.verify(Gdx.gl).glClearColor(248f / 255f, 249f / 255f, 178f / 255f, 1f);
    order.verify(Gdx.gl).glClear(GL20.GL_COLOR_BUFFER_BIT);
    order.verify(renderService).render(spriteBatch);
  }

  @Test
  void shouldRestoreDefaultClearColourBeforeLitRendering() {
    CameraComponent cameraComponent = makeCameraEntity(camera);
    Renderer renderer =
        new Renderer(cameraComponent, 10, spriteBatch, stage, renderService, debugRenderer);
    LightingEngine lighting = mock(LightingEngine.class);
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    clearInvocations(Gdx.gl);

    renderer.render(lighting);

    var order = inOrder(Gdx.gl, renderService, lighting);
    order.verify(Gdx.gl).glClearColor(248f / 255f, 249f / 255f, 178f / 255f, 1f);
    order.verify(Gdx.gl).glClear(GL20.GL_COLOR_BUFFER_BIT);
    order.verify(renderService).render(spriteBatch);
    order.verify(lighting).render();
  }

  private static CameraComponent makeCameraEntity(Camera camera) {
    Entity camEntity = new Entity().addComponent(new CameraComponent(camera));
    return camEntity.getComponent(CameraComponent.class);
  }

  @Test
  void shouldSkipRenderingWithoutGlAndResumeWhenAvailable() {
    Renderer renderer =
        new Renderer(
            makeCameraEntity(camera), 10, spriteBatch, stage, renderService, debugRenderer);
    GL20 originalGl = Gdx.gl;
    clearInvocations(renderService);
    try {
      Gdx.gl = null;
      Executable renderFrame = renderer::render;
      assertDoesNotThrow(renderFrame);
      verifyNoInteractions(spriteBatch, stage, debugRenderer, renderService);

      Gdx.gl = originalGl;
      renderer.render();
      verify(renderService).render(spriteBatch);
      verify(stage).draw();
    } finally {
      Gdx.gl = originalGl;
    }
  }

  @Test
  void shouldSkipLitRenderingWithoutGlAndResumeWhenAvailable() {
    Renderer renderer =
        new Renderer(
            makeCameraEntity(camera), 10, spriteBatch, stage, renderService, debugRenderer);
    LightingEngine lighting = mock(LightingEngine.class);
    GL20 originalGl = Gdx.gl;
    clearInvocations(renderService);
    try {
      Gdx.gl = null;
      assertDoesNotThrow(() -> renderer.render(lighting));
      verifyNoInteractions(spriteBatch, stage, debugRenderer, renderService, lighting);

      Gdx.gl = originalGl;
      renderer.render(lighting);
      verify(renderService).render(spriteBatch);
      verify(lighting).render();
      verify(stage).draw();
    } finally {
      Gdx.gl = originalGl;
    }
  }
}
