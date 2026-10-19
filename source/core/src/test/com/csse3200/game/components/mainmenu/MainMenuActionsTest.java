package com.csse3200.game.components.mainmenu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MainMenuActionsTest {
  private GdxGame game;
  private Entity ui;

  @BeforeEach
  void setUp() {
    game = mock(GdxGame.class);
    ui = new Entity().addComponent(new MainMenuActions(game));
    ui.create();
  }

  @Test
  void shouldOpenSandbox() {
    ui.getEvents().trigger("Sandbox");

    verify(game).setScreen(GdxGame.ScreenType.SANDBOX);
  }

  @Test
  void shouldStartNewGameWhenPlayIsTriggered() {
    ui.getEvents().trigger("Play");

    verify(game).startNewGame();
  }

  @Test
  void shouldOpenSaveSelectScreenWhenContinueIsTriggered() {
    ui.getEvents().trigger("Continue");

    verify(game).setScreen(GdxGame.ScreenType.LOAD_SAVE);
  }

  @Test
  void shouldOpenSettings() {
    ui.getEvents().trigger("Settings");

    verify(game).setScreen(GdxGame.ScreenType.SETTINGS);
  }

  @Test
  void shouldExitGame() {
    ui.getEvents().trigger("Exit");

    verify(game).exit();
  }
}
