package com.csse3200.game.components.mainmenu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
  void shouldStartInitialCutsceneWhenPlayIsTriggered() {
    ui.getEvents().trigger("Play");

    verify(game).startInitialCutscene();
  }

  @Test
  void shouldOpenMinigameSelection() {
    ui.getEvents().trigger("Minigames");

    verify(game).setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
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

  @Test
  void shouldLeaveContinueActionUnchangedUntilImplemented() {
    ui.getEvents().trigger("Continue");

    verifyNoInteractions(game);
  }
}
