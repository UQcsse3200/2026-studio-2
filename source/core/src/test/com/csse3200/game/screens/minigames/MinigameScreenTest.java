package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderable;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MinigameScreenTest {
  private static final String TEXTURE = "images/Buttons/back_up_btn.png";
  private static final String SOUND = "sounds/hit.ogg";
  private Input previousInput;
  private Input inputBackend;

  @BeforeEach
  void installInputBackend() {
    previousInput = Gdx.input;
    // Headless MockInput ignores setInputProcessor; verify registration at this boundary.
    inputBackend = mock(Input.class);
    Gdx.input = inputBackend;
  }

  @AfterEach
  void restoreInputBackend() {
    Gdx.input = previousInput;
  }

  @Test
  void ownsServicesAssetsEntityLifecycleAndRenderer() {
    Renderer renderer = mock(Renderer.class);
    LifecycleComponent lifecycle = new LifecycleComponent();
    Entity ui = new Entity().addComponent(lifecycle);
    MinigameScreen screen =
        new TestScreen(ui) {
          @Override
          protected String[] getSounds() {
            return new String[] {SOUND};
          }
        };
    try (var factory = mockStatic(RenderFactory.class)) {
      factory.when(RenderFactory::createRenderer).thenReturn(renderer);
      screen.show();
      EntityService entities = ServiceLocator.getEntityService();
      ResourceService resources = ServiceLocator.getResourceService();
      RenderService renders = ServiceLocator.getRenderService();
      InputService input = ServiceLocator.getInputService();
      assertNotNull(input);
      verify(inputBackend).setInputProcessor(input);
      assertEquals(1, entities.getEntities().size);
      assertSame(ui, entities.getEntities().first());
      assertEquals(1, lifecycle.creates);
      assertTrue(resources.containsAsset(TEXTURE, Texture.class));
      assertTrue(resources.containsAsset(SOUND, Sound.class));
      for (String click : ButtonSound.SOUND_FILES) {
        assertTrue(resources.containsAsset(click, Sound.class));
      }
      Renderable renderable = mock(Renderable.class);
      renders.register(renderable);
      screen.render(1f / 60f);
      assertEquals(1, lifecycle.updates);
      verify(renderer).render();
      screen.resize(1280, 720);
      verify(renderer).resize(1280, 720);

      screen.dispose();

      verify(renderer).dispose();
      assertEquals(1, lifecycle.disposes);
      assertTrue(entities.getEntities().isEmpty());
      assertFalse(resources.containsAsset(TEXTURE, Texture.class));
      assertFalse(resources.containsAsset(SOUND, Sound.class));
      for (String click : ButtonSound.SOUND_FILES) {
        assertFalse(resources.containsAsset(click, Sound.class));
      }
      renders.render(mock(SpriteBatch.class));
      verify(renderable, never()).render(any());
      assertNull(ServiceLocator.getInputService());
      assertNull(ServiceLocator.getResourceService());
      assertNull(ServiceLocator.getEntityService());
      assertNull(ServiceLocator.getRenderService());
      resources.dispose();
    }
  }

  @Test
  void silentMinigamesStillLoadAndUnloadButtonFeedback() {
    TestScreen screen = new TestScreen(new Entity());
    Renderer renderer = mock(Renderer.class);
    assertArrayEquals(new String[0], screen.getSounds());
    try (var factory = mockStatic(RenderFactory.class)) {
      factory.when(RenderFactory::createRenderer).thenReturn(renderer);
      screen.show();
      ResourceService resources = ServiceLocator.getResourceService();
      assertTrue(resources.containsAsset(ButtonSound.SOUND_FILES.getFirst(), Sound.class));
      screen.dispose();
      assertFalse(resources.containsAsset(ButtonSound.SOUND_FILES.getFirst(), Sound.class));
      resources.dispose();
    }
  }

  private static class TestScreen extends MinigameScreen {
    private final Entity ui;

    TestScreen(Entity ui) {
      super(mock(GdxGame.class));
      this.ui = ui;
    }

    @Override
    protected String[] getTextures() {
      return new String[] {TEXTURE};
    }

    @Override
    protected Entity createUI() {
      return ui;
    }
  }

  private static class LifecycleComponent extends Component {
    int creates;
    int updates;
    int disposes;

    @Override
    public void create() {
      creates++;
    }

    @Override
    public void update() {
      updates++;
    }

    @Override
    public void dispose() {
      disposes++;
    }
  }
}
