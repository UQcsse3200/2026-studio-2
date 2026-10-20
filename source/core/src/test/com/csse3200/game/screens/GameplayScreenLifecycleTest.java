package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.areas.Level3GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level1Config;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.entities.factories.TutorialFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndActions;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class GameplayScreenLifecycleTest {
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void constructsRealGameplayScreensAndDisposesTheirSharedServices(boolean levels) {
    try (ScreenFixture fixture = new ScreenFixture(levels)) {
      var entities = ServiceLocator.getEntityService();
      var resources = ServiceLocator.getResourceService();
      assertEquals(
          levels ? new Vector2(2f, 3f) : new Vector2(7.5f, 7.5f),
          fixture.cameraEntity.getPosition());
      assertFalse(entities.getEntities().isEmpty());
      assertTrue(fixture.stage.getActors().size > 0);
      assertTrue(resources.containsAsset("images/Buttons/exit_up_btn.png", Texture.class));
      assertNotNull(ServiceLocator.getLightingService());
      if (levels) {
        verify(fixture.levelAreas.constructed().getFirst()).create();
      } else {
        verify(fixture.forests.constructed().getFirst()).create();
        assertNull(ServiceLocator.getGameSaveEventHandler());
      }
      fixture.screen.render(1f / 60f);
      verify(fixture.renderer).render();
      verify(fixture.renderer).render(ServiceLocator.getLightingService().getEngine());
      fixture.screen.resize(1280, 720);
      verify(fixture.renderer).resize(1280, 720);
      for (Entity entity : entities.getEntities()) {
        if (entity.getComponent(MainGameActions.class) != null) {
          entity.getEvents().trigger("exit");
        }
      }
      verify(fixture.game).setScreen(GdxGame.ScreenType.MAIN_MENU);
      fixture.disposeScreen();
      verify(fixture.renderer).dispose();
      assertTrue(entities.getEntities().isEmpty());
      assertFalse(resources.containsAsset("images/Buttons/exit_up_btn.png", Texture.class));
      assertNull(ServiceLocator.getResourceService());
      assertNull(ServiceLocator.getGameSaveEventHandler());
    }
  }

  @Test
  void levelConstructorRestoresSavesAndConnectsPlayerEventsAndCamera() {
    try (ScreenFixture fixture = new ScreenFixture(true)) {
      Level1GameArea area = fixture.levelAreas.constructed().getFirst();
      verify(fixture.game).restorePlayerState(fixture.player);
      verify(fixture.game).applyPendingSavePlayer(fixture.player);
      verify(fixture.game).applyPendingSaveWorld(area, "level1");
      ServiceLocator.getGameSaveEventHandler().trigger("autosave");
      verify(fixture.game).autosave("level1", area);
      fixture.player.getEvents().trigger("respawnAtCheckpoint");
      verify(area).respawn();
      fixture.player.getEvents().trigger("toggleMap");
      verify(area).toggleLevelMap();
      fixture.player.setPosition(22f, 9f);
      fixture.camera.update();
      assertEquals(22f, fixture.camera.getCamera().position.x);
      assertEquals(9f, fixture.camera.getCamera().position.y);
    }
  }

  @Test
  void deathRestartQueuesWorldRebuildWhenAreaRequestsIt() throws Exception {
    try (ScreenFixture fixture = new ScreenFixture(true);
        MockedConstruction<Level3GameArea> rebuilds = mockConstruction(Level3GameArea.class)) {
      Level1GameArea area = fixture.levelAreas.constructed().getFirst();
      when(area.requiresWorldResetOnDeath()).thenReturn(true);

      triggerRestart();

      verify(area).restart();
      // The world rebuild is queued as a normal level swap: a fresh area with un-triggered
      // triggers, spawners and platforms, created on the next render frame.
      assertTrue(swapQueued(fixture));
      assertEquals(rebuilds.constructed().getFirst(), nextArea(fixture));
    }
  }

  @Test
  void deathRestartOnlyRespawnsWhenWorldRebuildNotNeeded() throws Exception {
    try (ScreenFixture fixture = new ScreenFixture(true);
        MockedConstruction<Level3GameArea> rebuilds = mockConstruction(Level3GameArea.class)) {
      Level1GameArea area = fixture.levelAreas.constructed().getFirst();
      when(area.requiresWorldResetOnDeath()).thenReturn(false);

      triggerRestart();

      verify(area).restart();
      assertFalse(swapQueued(fixture));
      assertNull(nextArea(fixture));
      assertTrue(rebuilds.constructed().isEmpty());
    }
  }

  @Test
  void movementTutorialStartsAfterDelayAndCompletionIsSaved() {
    try (ScreenFixture fixture = new ScreenFixture(true);
        MockedStatic<TutorialFactory> tutorials = mockStatic(TutorialFactory.class)) {
      TutorialComponent tutorial = mock(TutorialComponent.class);
      tutorials.when(() -> TutorialFactory.start(eq("tut1"), any())).thenReturn(tutorial);

      // The movement tutorial waits a few seconds after spawning so the player can look around.
      fixture.screen.render(1f);
      fixture.screen.render(1f);
      tutorials.verify(() -> TutorialFactory.start(anyString(), any()), never());

      fixture.screen.render(1f);
      fixture.screen.render(1f);
      tutorials.verify(() -> TutorialFactory.start(eq("tut1"), any()), times(1));

      // It never restarts on later frames.
      fixture.screen.render(1f);
      tutorials.verify(() -> TutorialFactory.start(eq("tut1"), any()), times(1));

      // Completing it (reaching the final step) persists the seen flag to the save.
      ArgumentCaptor<Runnable> onFinished = ArgumentCaptor.forClass(Runnable.class);
      tutorials.verify(() -> TutorialFactory.start(eq("tut1"), onFinished.capture()), times(1));
      onFinished.getValue().run();
      verify(fixture.game).markTutorialCompleted();
    }
  }

  @Test
  void checkpointActivationsStartTheirTutorialsOnce() {
    try (ScreenFixture fixture = new ScreenFixture(true);
        MockedStatic<TutorialFactory> tutorials = mockStatic(TutorialFactory.class)) {
      Level1GameArea area = fixture.levelAreas.constructed().getFirst();
      when(area.getConfig()).thenReturn(new Level1Config());
      @SuppressWarnings("unchecked")
      ArgumentCaptor<Consumer<GridPoint2>> listener = ArgumentCaptor.forClass(Consumer.class);
      verify(area).setCheckpointListener(listener.capture());

      // Level 1's first checkpoint teaches melee; reaching it again must not replay the lesson.
      listener.getValue().accept(new GridPoint2(16, 5));
      listener.getValue().accept(new GridPoint2(16, 5));
      tutorials.verify(() -> TutorialFactory.start(eq("tut5"), any()), times(1));

      listener.getValue().accept(new GridPoint2(19, 17));
      tutorials.verify(() -> TutorialFactory.start(eq("tut6"), any()), times(1));

      // A checkpoint with no wired tutorial starts nothing.
      listener.getValue().accept(new GridPoint2(44, 17));
      tutorials.verify(() -> TutorialFactory.start(anyString(), any()), times(2));
    }
  }

  /** Fires the game-over panel's restart button event, which runs the screen's restart handler. */
  private static void triggerRestart() {
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      if (entity.getComponent(GameEndActions.class) != null) {
        entity.getEvents().trigger("restart");
        return;
      }
    }
    fail("No entity with a GameEndActions component found");
  }

  private static boolean swapQueued(ScreenFixture fixture) throws Exception {
    Field field = LevelsGameScreen.class.getDeclaredField("levelSwapQueued");
    field.setAccessible(true);
    return (boolean) field.get(fixture.screen);
  }

  private static GameArea nextArea(ScreenFixture fixture) throws Exception {
    Field field = LevelsGameScreen.class.getDeclaredField("nextGameArea");
    field.setAccessible(true);
    return (GameArea) field.get(fixture.screen);
  }

  /** Real screens, services, UI and assets, with rendering and map generation isolated. */
  private static class ScreenFixture implements AutoCloseable {
    private final Stage stage;
    private final CameraComponent camera = new CameraComponent();
    private final Entity cameraEntity = new Entity().addComponent(camera);
    private final Entity player = new Entity();
    private final Renderer renderer = mock(Renderer.class);
    private final GdxGame game = mock(GdxGame.class);
    private final MockedStatic<RenderFactory> factories;
    private final MockedConstruction<RayHandler> rays;
    private final MockedConstruction<TerrainFactory> terrains;
    private final MockedConstruction<ForestGameArea> forests;
    private final MockedConstruction<Level1GameArea> levelAreas;
    private BaseGameScreen screen;

    ScreenFixture(boolean levels) {
      SpriteBatch batch = mock(SpriteBatch.class);
      stage = new Stage(new ScreenViewport(), batch);
      stage.getViewport().update(1600, 900, true);
      cameraEntity.setPosition(2f, 3f);
      DebugRenderer debug = mock(DebugRenderer.class);
      when(renderer.getCamera()).thenReturn(camera);
      when(renderer.getDebug()).thenReturn(debug);
      factories = mockStatic(RenderFactory.class);
      rays = mockConstruction(RayHandler.class);
      terrains = mockConstruction(TerrainFactory.class);
      forests =
          mockConstruction(
              ForestGameArea.class, (area, context) -> when(area.getPlayer()).thenReturn(player));
      levelAreas =
          mockConstruction(
              Level1GameArea.class, (area, context) -> when(area.getPlayer()).thenReturn(player));
      factories
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              call -> {
                ServiceLocator.getRenderService().setStage(stage);
                ServiceLocator.getRenderService().setDebug(debug);
                return renderer;
              });
      try {
        screen = levels ? new LevelsGameScreen(game) : new MainGameScreen(game);
      } catch (RuntimeException | Error exception) {
        close();
        throw exception;
      }
    }

    void disposeScreen() {
      if (screen != null) {
        var physics = ServiceLocator.getPhysicsService().getPhysics();
        screen.dispose();
        screen = null;
        physics.dispose();
      }
    }

    @Override
    public void close() {
      try {
        disposeScreen();
      } finally {
        stage.dispose();
        levelAreas.close();
        forests.close();
        terrains.close();
        rays.close();
        factories.close();
      }
    }
  }
}
