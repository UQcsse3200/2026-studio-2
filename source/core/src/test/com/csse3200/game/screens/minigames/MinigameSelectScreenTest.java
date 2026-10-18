package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.MinigameSelectActions;
import com.csse3200.game.components.minigames.MinigameSelectDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MinigameSelectScreenTest {
  @Test
  void loadsEveryMenuButtonAndWiresItsUiToTheStageAndNavigation() {
    Stage stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    GdxGame game = mock(GdxGame.class);
    MinigameSelectScreen screen = new MinigameSelectScreen(game);
    try (var factory = mockStatic(RenderFactory.class)) {
      factory
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              call -> {
                ServiceLocator.getRenderService().setStage(stage);
                return mock(Renderer.class);
              });
      screen.show();
      ResourceService resources = ServiceLocator.getResourceService();
      Set<String> expected = new HashSet<>();
      expected.add("images/Buttons/back_up_btn.png");
      expected.add("images/Buttons/back_down_btn.png");
      for (MinigameType type : MinigameType.values()) {
        expected.add("images/Buttons/" + type.getAssetKey() + "_up_btn.png");
        expected.add("images/Buttons/" + type.getAssetKey() + "_down_btn.png");
      }
      assertEquals(expected, Set.of(screen.getTextures()));
      for (String path : expected) {
        assertTrue(resources.containsAsset(path, Texture.class), path);
      }
      Entity ui = ServiceLocator.getEntityService().getEntities().first();
      assertNotNull(ui.getComponent(MinigameSelectDisplay.class));
      assertNotNull(ui.getComponent(MinigameSelectActions.class));
      InputDecorator input = ui.getComponent(InputDecorator.class);
      assertEquals(10, input.getPriority());
      assertEquals(1, stage.getActors().size);
      ui.getEvents().trigger("selectMinigame", MinigameType.SPIN_THE_WHEEL);
      verify(game).setScreen(GdxGame.ScreenType.MINIGAME_SPIN_THE_WHEEL);
      ui.getEvents().trigger("back");
      verify(game).setScreen(GdxGame.ScreenType.MAIN_MENU);
      screen.dispose();
      for (String path : expected) {
        assertFalse(resources.containsAsset(path, Texture.class), path);
      }
      resources.dispose();
    } finally {
      stage.dispose();
    }
  }
}
