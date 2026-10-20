package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.minigames.blackjack.BlackjackActions;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.blackjack.BlackjackDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

/**
 * Tests the stable parts of {@link BlackjackScreen}: which assets it asks for, the components it
 * builds, and how it handles the background music.
 */
@ExtendWith(GameExtension.class)
class BlackjackScreenTest {
  private static final String MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";

  private BlackjackScreen screen;
  private ResourceService resources;
  private Music music;

  @BeforeEach
  void createScreen() {
    screen = new BlackjackScreen(mock(GdxGame.class));
    resources = mock(ResourceService.class);
    music = mock(Music.class);
    when(resources.getAsset(MUSIC, Music.class)).thenReturn(music);
  }

  private void callSetSoundEnabled(boolean enabled) throws Exception {
    Method method = BlackjackScreen.class.getDeclaredMethod("setSoundEnabled", boolean.class);
    method.setAccessible(true);
    method.invoke(screen, enabled);
  }

  @Test
  void requestsTheTexturesListedInTheBlackjackConfig() {
    String[] textures = screen.getTextures();

    assertTrue(textures.length > 0);
    assertArrayEquals(BlackjackConfig.TEXTURES.toArray(String[]::new), textures);
  }

  @Test
  void requestsTheSoundsListedInTheBlackjackConfig() {
    String[] sounds = screen.getSounds();

    assertTrue(sounds.length > 0);
    assertArrayEquals(BlackjackConfig.SOUNDS.toArray(String[]::new), sounds);
  }

  @Test
  void createUiBuildsDisplayActionsAndInputComponents() {
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);

    Entity ui = screen.createUI();

    assertNotNull(ui);
    assertNotNull(ui.getComponent(BlackjackDisplay.class));
    assertNotNull(ui.getComponent(BlackjackActions.class));
    assertNotNull(ui.getComponent(InputDecorator.class));
  }

  @Test
  void disablingSoundPausesTheMusic() throws Exception {
    ServiceLocator.registerResourceService(resources);

    callSetSoundEnabled(false);

    verify(music).pause();
    verify(music, never()).play();
  }

  @Test
  void enablingSoundPlaysTheMusic() throws Exception {
    ServiceLocator.registerResourceService(resources);

    callSetSoundEnabled(true);

    verify(music).play();
    verify(music, never()).pause();
  }

  @Test
  void disposeStopsAndUnloadsTheMusicAndClearsTheServices() {
    Renderer renderer = mock(Renderer.class);
    screen.renderer = renderer;
    ServiceLocator.registerResourceService(resources);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));

    screen.dispose();

    verify(music).stop();
    verify(resources).unloadAssets(new String[] {MUSIC});
    verify(renderer).dispose();
    assertNull(ServiceLocator.getResourceService());
    assertNull(ServiceLocator.getRenderService());
    assertNull(ServiceLocator.getEntityService());
  }

  @Test
  void disposeUnloadsBlackjackTexturesAndSounds() {
    screen.renderer = mock(Renderer.class);
    ServiceLocator.registerResourceService(resources);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));

    screen.dispose();

    verify(resources).unloadAssets(BlackjackConfig.TEXTURES.toArray(String[]::new));
    verify(resources).unloadAssets(BlackjackConfig.SOUNDS.toArray(String[]::new));
  }

  @Test
  void storyWinContinuesOnceAfterTheResultIsAcknowledged() {
    GdxGame game = mock(GdxGame.class);
    screen = new BlackjackScreen(game, true);
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);
    Entity ui = screen.createUI();
    Application previousApp = Gdx.app;
    Application app = mock(Application.class);
    Gdx.app = app;
    try {
      ui.getEvents().trigger("roundCompleted", false);
      verify(game, never()).playCutscene("cutscene5", GdxGame.ScreenType.LEVEL_3_GAME);
      ui.getEvents().trigger("roundCompleted", true);
      ui.getEvents().trigger("roundCompleted", true);

      ArgumentCaptor<Runnable> next = ArgumentCaptor.forClass(Runnable.class);
      verify(app).postRunnable(next.capture());
      verify(game, never()).playCutscene("cutscene5", GdxGame.ScreenType.LEVEL_3_GAME);
      next.getValue().run();
      verify(game).playCutscene("cutscene5", GdxGame.ScreenType.LEVEL_3_GAME);
    } finally {
      Gdx.app = previousApp;
    }
  }

  @Test
  void storyLossWithNoGoldAllowsAnotherAttempt() {
    GdxGame game = mock(GdxGame.class);
    screen = new BlackjackScreen(game, true);
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);
    Entity ui = screen.createUI();
    InventoryComponent inventory = ui.getComponent(InventoryComponent.class);
    inventory.setGold(0);

    ui.getEvents().trigger("roundCompleted", false);

    org.junit.jupiter.api.Assertions.assertEquals(100, inventory.getGold());
    verify(game, never()).playCutscene("cutscene5", GdxGame.ScreenType.LEVEL_3_GAME);
  }

  @Test
  void ordinaryBlackjackWinDoesNotAdvanceTheStory() {
    GdxGame game = mock(GdxGame.class);
    screen = new BlackjackScreen(game);
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);

    screen.createUI().getEvents().trigger("roundCompleted", true);

    verify(game, never()).playCutscene("cutscene5", GdxGame.ScreenType.LEVEL_3_GAME);
  }
}
