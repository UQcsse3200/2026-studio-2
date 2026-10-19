package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.MockServices;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SandboxGameScreenTest {
  @Test
  void shouldCreateRenderNavigateAndDisposeSandboxServices() {
    Stage stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    stage.getViewport().update(1600, 900, true);
    Renderer renderer = mock(Renderer.class);
    when(renderer.getCamera()).thenReturn(new CameraComponent());
    GdxGame game = mock(GdxGame.class);
    SandboxGameScreen screen = null;
    // Only native rendering and terrain construction are replaced; services and entities are real.
    try (var renders = mockStatic(RenderFactory.class);
        var rayHandlers = mockConstruction(RayHandler.class);
        var pointLights = mockConstruction(PointLightComponent.class);
        var terrainFactories =
            mockConstruction(
                TerrainFactory.class,
                (factory, context) -> {
                  MockServices.stubFlatTerrain(factory);
                })) {
      renders
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              call -> {
                ServiceLocator.getRenderService().setStage(stage);
                return renderer;
              });
      screen = new SandboxGameScreen(game);
      EntityService entities = ServiceLocator.getEntityService();
      ResourceService resources = ServiceLocator.getResourceService();
      assertNotNull(ServiceLocator.getLightingService());
      assertNotNull(ServiceLocator.getLightingService().getEngine());
      assertNotNull(ServiceLocator.getLightingService().getEngine().getRayHandler());
      assertFalse(entities.getEntities().isEmpty());
      assertTrue(stage.getActors().size > 0);
      for (ItemType type : ItemType.values()) {
        assertNotNull(resources.getAsset(type.getTexturePath(), Texture.class));
      }
      entities.setPaused(true);
      screen.render(1f / 60f);
      verify(renderer).render();
      screen.resize(1280, 720);
      verify(renderer).resize(1280, 720);
      screen.pause();
      screen.resume();
      Entity exitUi = null;
      for (Entity entity : entities.getEntities()) {
        if (entity.getComponent(MainGameActions.class) != null) {
          exitUi = entity;
          break;
        }
      }
      assertNotNull(exitUi);
      exitUi.getEvents().trigger("exit");
      verify(game).setScreen(GdxGame.ScreenType.MAIN_MENU);

      screen.dispose();
      screen = null;
      verify(renderer).dispose();
      assertTrue(entities.getEntities().isEmpty());
      assertFalse(resources.containsAsset(ItemType.FIRE_ARROW.getTexturePath(), Texture.class));
      assertNull(ServiceLocator.getEntityService());
      assertNull(ServiceLocator.getResourceService());
      assertNull(ServiceLocator.getPhysicsService());
      assertNull(ServiceLocator.getInputService());
      assertNull(ServiceLocator.getRenderService());
    } finally {
      if (screen != null) {
        screen.dispose();
      }
      stage.dispose();
    }
  }
}
