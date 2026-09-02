package com.csse3200.game.components.minigames;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.minigames.MinigameType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MinigameSelectActionsTest {

  /** Back button on minigame screen properly disposes and returns to main screen */
  @Test
  void shouldReturnToMainMenuOnBack() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new MinigameSelectActions(game));
    ui.create();

    ui.getEvents().trigger("back");

    verify(game).setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  @Test
  void shouldChangeScreenForCyclopsMinigameSelected() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new MinigameSelectActions(game));
    ui.create();

    ui.getEvents().trigger("selectMinigame", MinigameType.CYCLOPS_TIMING);
    verify(game).setScreen(GdxGame.ScreenType.CYCLOPS_MINIGAME);
  }

  /** Selecting spin the wheel swaps to its screen. */
  @Test
  void shouldOpenSpinTheWheelOnSelect() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new MinigameSelectActions(game));
    ui.create();

    ui.getEvents().trigger("selectMinigame", MinigameType.SPIN_THE_WHEEL);

    verify(game).setScreen(GdxGame.ScreenType.MINIGAME_SPIN_THE_WHEEL);
  }
}
