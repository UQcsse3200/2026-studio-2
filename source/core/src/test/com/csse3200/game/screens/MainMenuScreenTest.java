package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.mainmenu.MainMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class MainMenuScreenTest {

  private void setUi(MainMenuScreen screen, Entity ui) throws Exception {
    Field field = MainMenuScreen.class.getDeclaredField("ui");
    field.setAccessible(true);
    field.set(screen, ui);
  }

  @Test
  void shouldCreateRenderResizePauseResumeAndDisposeMainMenu() throws Exception {
    Stage stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    stage.getViewport().update(1600, 900, true);

    Renderer renderer = mock(Renderer.class);
    GdxGame game = mock(GdxGame.class);
    MainMenuScreen screen = null;

    try (MockedStatic<RenderFactory> renderFactories = mockStatic(RenderFactory.class)) {

      // Replace only the renderer, as in SandboxGameScreenTest.
      renderFactories
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              invocation -> {
                ServiceLocator.getRenderService().setStage(stage);
                return renderer;
              });

      // Construct the screen with real services and loaded assets.
      screen = new MainMenuScreen(game);

      EntityService entityService = ServiceLocator.getEntityService();
      ResourceService resourceService = ServiceLocator.getResourceService();

      assertNotNull(entityService);
      assertNotNull(resourceService);
      assertNotNull(ServiceLocator.getInputService());
      assertNotNull(ServiceLocator.getRenderService());

      assertFalse(entityService.getEntities().isEmpty());
      assertTrue(stage.getActors().size > 0);

      // Verify the main menu textures and music were loaded.
      assertTrue(resourceService.containsAsset("images/ui/title_odysseus_logo.png", Texture.class));
      assertTrue(
          resourceService.containsAsset("images/backgrounds/main_menu_bg.png", Texture.class));
      assertTrue(resourceService.containsAsset("sounds/Main_menu_sound.mp3", Music.class));

      // Find the UI entity.
      Entity ui = null;
      for (Entity entity : entityService.getEntities()) {
        if (entity.getComponent(MainMenuDisplay.class) != null) {
          ui = entity;
          break;
        }
      }

      assertNotNull(ui);
      assertNotNull(ui.getComponent(MainMenuDisplay.class));

      // Render.
      screen.render(1f / 60f);
      verify(renderer).render();

      // Resize with a UI containing MainMenuDisplay.
      screen.resize(1280, 720);
      verify(renderer).resize(1280, 720);

      // Resize with a UI entity that has no MainMenuDisplay.
      setUi(screen, new Entity());
      screen.resize(1024, 768);
      verify(renderer).resize(1024, 768);

      // Resize with a null UI entity.
      setUi(screen, null);
      screen.resize(800, 600);
      verify(renderer).resize(800, 600);

      // Lifecycle methods.
      screen.pause();
      screen.resume();

      // Dispose the screen and its services.
      screen.dispose();
      screen = null;

      verify(renderer).dispose();

      assertTrue(entityService.getEntities().isEmpty());
      assertFalse(
          resourceService.containsAsset("images/ui/title_odysseus_logo.png", Texture.class));

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
