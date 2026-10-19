package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.mainmenu.LoadSaveDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class LoadSaveScreenTest {

  @Test
  void shouldCreateRenderResizeAndDisposeLoadSaveScreen() {
    Stage stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    stage.getViewport().update(1600, 900, true);

    Renderer renderer = mock(Renderer.class);
    GdxGame game = mock(GdxGame.class);
    LoadSaveScreen screen = null;

    try (MockedStatic<RenderFactory> renderFactories = mockStatic(RenderFactory.class)) {

      renderFactories
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              invocation -> {
                ServiceLocator.getRenderService().setStage(stage);
                return renderer;
              });

      screen = new LoadSaveScreen(game);

      EntityService entityService = ServiceLocator.getEntityService();
      ResourceService resourceService = ServiceLocator.getResourceService();

      // Services are registered.
      assertNotNull(entityService);
      assertNotNull(resourceService);
      assertNotNull(ServiceLocator.getInputService());
      assertNotNull(ServiceLocator.getRenderService());

      // The UI entity is registered.
      assertFalse(entityService.getEntities().isEmpty());

      Entity loadSaveUi = null;
      for (Entity entity : entityService.getEntities()) {
        if (entity.getComponent(LoadSaveDisplay.class) != null) {
          loadSaveUi = entity;
          break;
        }
      }

      assertNotNull(loadSaveUi);
      assertNotNull(loadSaveUi.getComponent(LoadSaveDisplay.class));

      // UI actors have been added to the stage.
      assertTrue(stage.getActors().size > 0);

      // Textures are loaded.
      assertTrue(resourceService.containsAsset("images/ui/title_odysseus_logo.png", Texture.class));
      assertTrue(
          resourceService.containsAsset("images/backgrounds/main_menu_bg.png", Texture.class));
      assertTrue(resourceService.containsAsset("images/Buttons/back_up_btn.png", Texture.class));
      assertTrue(resourceService.containsAsset("images/Buttons/back_down_btn.png", Texture.class));

      // Music is loaded and configured.
      Music music = resourceService.getAsset("sounds/Main_menu_sound.mp3", Music.class);

      // Render and resize.
      screen.render(1f / 60f);
      verify(renderer).render();

      screen.resize(1280, 720);
      verify(renderer).resize(1280, 720);

      // Lifecycle methods.
      screen.pause();
      screen.resume();

      // Disposal releases resources and clears services.
      screen.dispose();
      screen = null;

      verify(renderer).dispose();

      assertTrue(entityService.getEntities().isEmpty());

      assertFalse(
          resourceService.containsAsset("images/ui/title_odysseus_logo.png", Texture.class));
      assertFalse(
          resourceService.containsAsset("images/backgrounds/main_menu_bg.png", Texture.class));
      assertFalse(resourceService.containsAsset("sounds/Main_menu_sound.mp3", Music.class));

      assertNull(ServiceLocator.getEntityService());
      assertNull(ServiceLocator.getResourceService());
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
