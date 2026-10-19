package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.settingsmenu.SettingsMenuDisplay;
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
class SettingsScreenTest {
  private static class TestDisplayMode extends DisplayMode {
    public TestDisplayMode(int width, int height, int refreshRate, int bitsPerPixel) {
      super(width, height, refreshRate, bitsPerPixel);
    }
  }

  @Test
  void shouldCreateRenderResizeAndDisposeSettingsScreen() {
    Stage stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    stage.getViewport().update(1600, 900, true);

    Renderer renderer = mock(Renderer.class);
    GdxGame game = mock(GdxGame.class);
    SettingsScreen screen = null;

    try (MockedStatic<RenderFactory> renderFactories = mockStatic(RenderFactory.class)) {

      // Replace the renderer while retaining real services and entities.
      renderFactories
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              invocation -> {
                ServiceLocator.getRenderService().setStage(stage);
                return renderer;
              });

      com.csse3200.game.components.CameraComponent camera =
          new com.csse3200.game.components.CameraComponent();

      Entity cameraEntity = new Entity();
      cameraEntity.addComponent(camera);

      when(renderer.getCamera()).thenReturn(camera);

      DisplayMode displayMode = new TestDisplayMode(1280, 720, 60, 32);
      Graphics graphics = mock(Graphics.class);
      Monitor monitor = mock(Monitor.class);

      when(graphics.getWidth()).thenReturn(1280);
      when(graphics.getHeight()).thenReturn(720);
      when(graphics.getMonitor()).thenReturn(monitor);
      when(graphics.getDisplayModes(monitor)).thenReturn(new DisplayMode[] {displayMode});
      when(graphics.getDisplayMode()).thenReturn(displayMode);

      Gdx.graphics = graphics;

      screen = new SettingsScreen(game);

      EntityService entityService = ServiceLocator.getEntityService();
      ResourceService resourceService = ServiceLocator.getResourceService();

      assertNotNull(entityService);
      assertNotNull(resourceService);
      assertNotNull(ServiceLocator.getInputService());
      assertNotNull(ServiceLocator.getRenderService());
      assertNotNull(ServiceLocator.getTimeSource());

      assertFalse(entityService.getEntities().isEmpty());
      assertTrue(stage.getActors().size > 0);

      // Verify that the settings UI was registered.
      Entity settingsUi = null;
      for (Entity entity : entityService.getEntities()) {
        if (entity.getComponent(SettingsMenuDisplay.class) != null) {
          settingsUi = entity;
          break;
        }
      }

      assertNotNull(settingsUi);
      assertNotNull(settingsUi.getComponent(SettingsMenuDisplay.class));

      // Verify that settings textures were loaded.
      assertTrue(resourceService.containsAsset("images/Buttons/exit_up_btn.png", Texture.class));
      assertTrue(resourceService.containsAsset("images/Buttons/exit_down_btn.png", Texture.class));
      assertTrue(resourceService.containsAsset("images/Buttons/apply_up_btn.png", Texture.class));
      assertTrue(resourceService.containsAsset("images/Buttons/apply_down_btn.png", Texture.class));
      assertTrue(
          resourceService.containsAsset("images/backgrounds/main_menu_bg_2.png", Texture.class));
      assertTrue(resourceService.containsAsset("images/ui/settings_box.png", Texture.class));

      // Render the screen.
      screen.render(1f / 60f);
      verify(renderer).render();

      // Resize the screen.
      screen.resize(1280, 720);
      verify(renderer).resize(1280, 720);

      // Dispose the screen and its services.
      screen.dispose();
      screen = null;

      verify(renderer).dispose();

      assertTrue(entityService.getEntities().isEmpty());

      assertFalse(resourceService.containsAsset("images/Buttons/exit_up_btn.png", Texture.class));
      assertFalse(resourceService.containsAsset("images/ui/settings_box.png", Texture.class));

      assertNull(ServiceLocator.getEntityService());
      assertNull(ServiceLocator.getResourceService());
      assertNull(ServiceLocator.getInputService());
      assertNull(ServiceLocator.getRenderService());
      assertNull(ServiceLocator.getTimeSource());
    } finally {
      if (screen != null) {
        screen.dispose();
      }
      stage.dispose();
    }
  }
}
