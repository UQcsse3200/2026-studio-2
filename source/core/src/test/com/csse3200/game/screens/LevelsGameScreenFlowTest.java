package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import box2dLight.RayHandler;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.maingame.PauseButtonDisplay;
import com.csse3200.game.components.maingame.PauseMenuOverlay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class LevelsGameScreenFlowTest {
  @ParameterizedTest
  @CsvSource({"level2, cutscene2, CYCLOPS_MINIGAME", "level3, cutscene4, STORY_BLACKJACK"})
  void exitPreservesPlayerAndDefersTheStoryUntilAfterTheWorldUpdate(
      String nextLevel, String cutscene, GdxGame.ScreenType destination) {
    Application previousApp = Gdx.app;
    Application app = mock(Application.class);
    Gdx.app = app;
    Stage stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    Renderer renderer = mock(Renderer.class);
    when(renderer.getCamera()).thenReturn(new CameraComponent());
    DebugRenderer debug = mock(DebugRenderer.class);
    when(renderer.getDebug()).thenReturn(debug);
    GdxGame game = mock(GdxGame.class);
    Entity player =
        new Entity()
            .addComponent(new CombatStatsComponent(75, 100, 10))
            .addComponent(new InventoryComponent(50));
    Entity exit = new Entity();
    LevelsGameScreen screen = null;
    try (var renders = mockStatic(RenderFactory.class);
        var lights = mockConstruction(RayHandler.class);
        var areas =
            mockConstruction(
                Level1GameArea.class,
                (area, context) -> {
                  when(area.getPlayer()).thenReturn(player);
                  when(area.getLevelChanger()).thenReturn(exit);
                })) {
      renders
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              call -> {
                ServiceLocator.getRenderService().setStage(stage);
                return renderer;
              });
      screen = new LevelsGameScreen(game);

      exit.getEvents().trigger("triggerNextLevel", nextLevel);
      exit.getEvents().trigger("triggerNextLevel", nextLevel);

      verify(game).savePlayerState(player);
      verify(game, never()).playCutscene(anyString(), any());
      ArgumentCaptor<Runnable> transition = ArgumentCaptor.forClass(Runnable.class);
      verify(app).postRunnable(transition.capture());
      transition.getValue().run();
      verify(game).playCutscene(cutscene, destination);
    } finally {
      if (screen != null) {
        screen.physicsEngine.dispose();
        screen.dispose();
      }
      stage.dispose();
      Gdx.app = previousApp;
    }
  }

  @Test
  void removedPauseButtonIsNotCreated() {
    Stage stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    Renderer renderer = mock(Renderer.class);
    when(renderer.getCamera()).thenReturn(new CameraComponent());
    DebugRenderer debug = mock(DebugRenderer.class);
    when(renderer.getDebug()).thenReturn(debug);
    Entity player =
        new Entity()
            .addComponent(new CombatStatsComponent(75, 100, 10))
            .addComponent(new InventoryComponent(50));
    LevelsGameScreen screen = null;
    try (var renders = mockStatic(RenderFactory.class);
        var lights = mockConstruction(RayHandler.class);
        var overlays = mockConstruction(PauseMenuOverlay.class);
        var buttons = mockConstruction(PauseButtonDisplay.class);
        var areas =
            mockConstruction(
                Level1GameArea.class,
                (area, context) -> {
                  when(area.getPlayer()).thenReturn(player);
                  when(area.getLevelChanger()).thenReturn(new Entity());
                })) {
      renders
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              call -> {
                ServiceLocator.getRenderService().setStage(stage);
                return renderer;
              });
      screen = new LevelsGameScreen(mock(GdxGame.class), false);
      assertTrue(buttons.constructed().isEmpty());
      assertEquals(1, overlays.constructed().size());
      PauseMenuOverlay overlay = overlays.constructed().getFirst();
      verify(overlay, never()).request();
    } finally {
      if (screen != null) {
        screen.physicsEngine.dispose();
        screen.dispose();
      }
      stage.dispose();
    }
  }
}
