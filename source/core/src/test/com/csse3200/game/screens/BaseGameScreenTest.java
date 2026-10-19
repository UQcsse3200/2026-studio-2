package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import box2dLight.RayHandler;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(GameExtension.class)
class BaseGameScreenTest {
  private static class TestScreen extends BaseGameScreen {
    private Runnable unload = () -> {};

    TestScreen() {
      super();
    }

    TestScreen(Vector2 position) {
      super(position);
    }

    @Override
    protected void unloadAssets() {
      unload.run();
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void initialisesSharedServicesAndOnlyOverridesCameraWhenRequested(boolean customPosition) {
    CameraComponent camera = new CameraComponent();
    Entity cameraEntity = new Entity().addComponent(camera);
    cameraEntity.setPosition(3f, 4f);
    Renderer renderer = mock(Renderer.class);
    DebugRenderer debug = mock(DebugRenderer.class);
    when(renderer.getCamera()).thenReturn(camera);
    when(renderer.getDebug()).thenReturn(debug);

    try (var factory = mockStatic(RenderFactory.class);
        var rays = mockConstruction(RayHandler.class)) {
      factory.when(RenderFactory::createRenderer).thenReturn(renderer);
      TestScreen screen = customPosition ? new TestScreen(new Vector2(12f, 8f)) : new TestScreen();
      var physics = ServiceLocator.getPhysicsService().getPhysics();
      try {
        assertEquals(
            customPosition ? new Vector2(12f, 8f) : new Vector2(3f, 4f),
            cameraEntity.getPosition());
        assertSame(physics, screen.physicsEngine);
        assertSame(ServiceLocator.getLightingService().getEngine(), screen.lightingEngine);
        assertNotNull(ServiceLocator.getTimeSource());
        assertNotNull(ServiceLocator.getInputService());
        assertNotNull(ServiceLocator.getResourceService());
        assertNotNull(ServiceLocator.getEntityService());
        assertNotNull(ServiceLocator.getRenderService());
        assertNotNull(ServiceLocator.getGameEndEventHandler());
        verify(debug).renderPhysicsWorld(physics.getWorld());
        screen.resize(1024, 768);
        verify(renderer).resize(1024, 768);
        screen.pause();
        screen.resume();
      } finally {
        screen.dispose();
        physics.dispose();
      }
    }
  }

  @Test
  void unloadsBeforeEntitiesAndLightingAreDisposedAndClearsServicesLast() {
    List<String> lifecycle = new ArrayList<>();
    Renderer renderer = mock(Renderer.class);
    when(renderer.getCamera()).thenReturn(new CameraComponent());
    DebugRenderer debug = mock(DebugRenderer.class);
    when(renderer.getDebug()).thenReturn(debug);
    doAnswer(
            call -> {
              lifecycle.add("renderer");
              return null;
            })
        .when(renderer)
        .dispose();

    try (var factory = mockStatic(RenderFactory.class);
        var rays =
            mockConstruction(
                RayHandler.class,
                (ray, context) ->
                    doAnswer(
                            call -> {
                              assertNotNull(ServiceLocator.getRenderService());
                              assertNotNull(ServiceLocator.getResourceService());
                              lifecycle.add("lighting");
                              return null;
                            })
                        .when(ray)
                        .dispose())) {
      factory.when(RenderFactory::createRenderer).thenReturn(renderer);
      TestScreen screen = new TestScreen();
      var physics = screen.physicsEngine;
      var entities = ServiceLocator.getEntityService();
      screen.unload =
          () -> {
            assertNotNull(ServiceLocator.getResourceService());
            assertEquals(1, entities.getEntities().size);
            lifecycle.add("assets");
          };
      entities.register(
          new Entity()
              .addComponent(
                  new Component() {
                    @Override
                    public void dispose() {
                      assertNotNull(ServiceLocator.getLightingService());
                      lifecycle.add("entity");
                    }
                  }));
      try {
        screen.dispose();
        assertEquals(List.of("renderer", "assets", "entity", "lighting"), lifecycle);
        assertTrue(entities.getEntities().isEmpty());
        assertNull(ServiceLocator.getEntityService());
        assertNull(ServiceLocator.getLightingService());
        assertNull(ServiceLocator.getRenderService());
        assertNull(ServiceLocator.getResourceService());
        assertNull(ServiceLocator.getPhysicsService());
        assertNull(ServiceLocator.getInputService());
        assertNull(ServiceLocator.getTimeSource());
        assertNull(ServiceLocator.getGameEndEventHandler());
      } finally {
        physics.dispose();
      }
    }
  }
}
