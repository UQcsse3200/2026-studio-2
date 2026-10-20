package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsMinigameActions;
import com.csse3200.game.components.minigames.cyclopsminigame.TimingBarDisplay;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndDisplay;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class CyclopsMinigameRoomLifecycleTest {
  @Test
  void clickingThroughRealIntroStartsOnceAndUpdatesTheTimingDisplay() {
    try (Room room = new Room(false)) {
      AtomicInteger starts = new AtomicInteger();
      ServiceLocator.getCyclopsMinigameEventHandler().addListener("start", starts::incrementAndGet);
      TimingBarDisplay timing =
          room.find(TimingBarDisplay.class).getComponent(TimingBarDisplay.class);
      room.frame(false);
      assertFalse(timing.isVisible());
      assertEquals(0, starts.get());

      // Each of the two pages is revealed by one click and advanced by the next.
      for (int click = 0; click < 3; click++) {
        room.frame(true);
        assertEquals(0, starts.get());
        assertFalse(timing.isVisible());
      }
      room.frame(true);
      assertEquals(1, starts.get());
      room.frame(false);
      room.frame(false);
      assertEquals(1, starts.get());
      assertTrue(timing.isVisible());
      verify(room.renderer, times(7)).render();
      room.screen.resize(1280, 720);
      verify(room.renderer).resize(1280, 720);
    }
  }

  @Test
  void leavingDuringIntroRemovesItsDirectlyOwnedTextBox() {
    try (Room room = new Room(false)) {
      // The intro is created before all entity-owned UI and is the first stage actor.
      Table intro = (Table) room.stage.getActors().first();
      room.frame(false);
      assertTrue(intro.isVisible());
      assertSame(room.stage, intro.getStage());
      room.disposeScreen();
      assertNull(intro.getStage());
    }
  }

  @Test
  void skippedIntroRoutesWinDeathAndUiActionsAndDisposesRoom() {
    try (Room room = new Room(true)) {
      AtomicInteger starts = new AtomicInteger();
      ServiceLocator.getCyclopsMinigameEventHandler().addListener("start", starts::incrementAndGet);
      room.frame(false);
      room.frame(false);
      assertEquals(1, starts.get());
      assertFalse(CyclopsMinigameRoomScreen.consumeSkipIntroOnNextLoad());
      assertTrue(
          room.find(TimingBarDisplay.class).getComponent(TimingBarDisplay.class).isVisible());

      ServiceLocator.getCyclopsMinigameEventHandler().trigger("win");
      ArgumentCaptor<CutsceneLoader.LoadedCutscene> cutscene =
          ArgumentCaptor.forClass(CutsceneLoader.LoadedCutscene.class);
      verify(room.game).startCutscene(cutscene.capture(), eq(GdxGame.ScreenType.LEVEL_2_GAME));
      assertEquals("cutscene3", cutscene.getValue().getName());
      assertTrue(cutscene.getValue().getImagePaths().length > 0);

      Entity ui = room.find(CyclopsMinigameActions.class);
      GameEndDisplay gameOver = ui.getComponent(GameEndDisplay.class);
      assertFalse(gameOver.isVisible());
      ServiceLocator.getCyclopsMinigameEventHandler().trigger("died");
      assertTrue(gameOver.isVisible());
      ui.getEvents().trigger("mainMenu");
      verify(room.game).setScreen(GdxGame.ScreenType.MAIN_MENU);
      ui.getEvents().trigger("exitGame");
      verify(room.game).exit();

      Gdx.app = mock(Application.class);
      ui.getEvents().trigger("restart");
      verify(room.game, never()).setScreen(GdxGame.ScreenType.CYCLOPS_MINIGAME);
      ArgumentCaptor<Runnable> reload = ArgumentCaptor.forClass(Runnable.class);
      verify(Gdx.app).postRunnable(reload.capture());
      assertTrue(CyclopsMinigameRoomScreen.consumeSkipIntroOnNextLoad());
      reload.getValue().run();
      verify(room.game).setScreen(GdxGame.ScreenType.CYCLOPS_MINIGAME);

      EntityService entities = ServiceLocator.getEntityService();
      ResourceService resources = ServiceLocator.getResourceService();
      assertTrue(resources.containsAsset("images/Buttons/exit_up_btn.png", Texture.class));
      room.disposeScreen();
      verify(room.renderer).dispose();
      assertTrue(entities.getEntities().isEmpty());
      assertEquals(0, room.physics.getWorld().getBodyCount());
      assertFalse(resources.containsAsset("images/Buttons/exit_up_btn.png", Texture.class));
      assertNull(ServiceLocator.getEntityService());
      assertNull(ServiceLocator.getResourceService());
      assertNull(ServiceLocator.getRenderService());
      assertNull(ServiceLocator.getPhysicsService());
      assertNull(ServiceLocator.getInputService());
      assertNull(ServiceLocator.getCyclopsMinigameEventHandler());
    }
  }

  @Test
  void missingWinCutsceneContinuesToLevelTwo() {
    try (Room room = new Room(true)) {
      Files files = Gdx.files;
      try {
        Gdx.files = spy(files);
        doReturn(mock(FileHandle.class)).when(Gdx.files).internal("images/cutscenes/cutscene3");
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("win");
        verify(room.game, never()).startCutscene(any(), any());
        verify(room.game, never()).setScreen(any(GdxGame.ScreenType.class));
        verify(room.game).transitionTo(GdxGame.ScreenType.LEVEL_2_GAME);
        room.frame(false);
        verify(room.renderer).render();
      } finally {
        Gdx.files = files;
      }
    }
  }

  /**
   * Real screen, area, assets, physics, components and event buses; only GL rendering is replaced.
   */
  private static class Room implements AutoCloseable {
    final Application previousApp = Gdx.app;
    final Input previousInput = Gdx.input;
    final Input input = mock(Input.class);
    final GdxGame game = mock(GdxGame.class);
    final Renderer renderer = mock(Renderer.class);
    final SpriteBatch batch = mock(SpriteBatch.class);
    Stage stage;
    MockedStatic<RenderFactory> renderFactory;
    MockedConstruction<OrthogonalTiledMapRenderer> terrainRenderers;
    MockedConstruction<GameTime> clocks;
    PhysicsEngine physics;
    CyclopsMinigameRoomScreen screen;

    Room(boolean skipIntro) {
      try {
        initialise(skipIntro);
      } catch (RuntimeException | Error failure) {
        try {
          close();
        } catch (RuntimeException | Error cleanupFailure) {
          failure.addSuppressed(cleanupFailure);
        }
        throw failure;
      }
    }

    private void initialise(boolean skipIntro) {
      doCallRealMethod().when(game).playCutscene(anyString(), any());
      Gdx.input = input;
      ServiceLocator.registerCyclopsMinigameEventHandler(new EventHandler());
      ServiceLocator.registerGameEndEventHandler(new EventHandler());
      CyclopsMinigameRoomScreen.skipIntroOnNextLoad = skipIntro;
      when(batch.getColor()).thenReturn(Color.WHITE.cpy());
      stage = new Stage(new ScreenViewport(), batch);
      stage.getViewport().update(1600, 900, true);
      CameraComponent camera = new CameraComponent();
      camera.resize(1600, 900, 20f);
      Entity cameraEntity = new Entity().addComponent(camera);
      DebugRenderer debug = mock(DebugRenderer.class);
      when(renderer.getCamera()).thenReturn(camera);
      when(renderer.getDebug()).thenReturn(debug);
      doAnswer(
              call -> {
                ServiceLocator.getRenderService().render(batch);
                return null;
              })
          .when(renderer)
          .render();
      terrainRenderers = mockConstruction(OrthogonalTiledMapRenderer.class);
      clocks =
          mockConstruction(
              GameTime.class, (clock, context) -> when(clock.getDeltaTime()).thenReturn(0.2f));
      renderFactory = mockStatic(RenderFactory.class);
      renderFactory
          .when(RenderFactory::createRenderer)
          .thenAnswer(
              call -> {
                ServiceLocator.getRenderService().setStage(stage);
                ServiceLocator.getRenderService().setDebug(debug);
                ServiceLocator.getEntityService().register(cameraEntity);
                return renderer;
              });
      screen = new CyclopsMinigameRoomScreen(game);
      physics = ServiceLocator.getPhysicsService().getPhysics();
      verify(input).setInputProcessor(ServiceLocator.getInputService());
      assertTrue(stage.getActors().size > 0);
    }

    Entity find(Class<? extends com.csse3200.game.components.Component> type) {
      for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
        if (entity.getComponent(type) != null) return entity;
      }
      throw new AssertionError("Missing room component " + type.getSimpleName());
    }

    void frame(boolean click) {
      when(input.isButtonJustPressed(Input.Buttons.LEFT)).thenReturn(click);
      screen.render(0.2f);
    }

    void disposeScreen() {
      screen.dispose();
      screen = null;
    }

    @Override
    public void close() {
      try {
        if (screen != null) disposeScreen();
        if (physics != null) physics.dispose();
        if (stage != null) stage.dispose();
      } finally {
        if (renderFactory != null) renderFactory.close();
        if (clocks != null) clocks.close();
        if (terrainRenderers != null) terrainRenderers.close();
        CyclopsMinigameRoomScreen.skipIntroOnNextLoad = false;
        Gdx.input = previousInput;
        Gdx.app = previousApp;
      }
    }
  }
}
