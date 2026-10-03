package com.csse3200.game.components.minigames.blackjack;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BlackjackActionsTest {
  @Test
  void shouldReturnToMinigameSelectOnBack() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new BlackjackActions(game));
    ui.create();

    ui.getEvents().trigger("back");

    verify(game).setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  }

  @Test
  void shouldReturnToTheProvidedLaunchScreenOnBack() {
    GdxGame game = mock(GdxGame.class);
    Entity ui =
        new Entity().addComponent(new BlackjackActions(game, GdxGame.ScreenType.LEVEL_1_GAME));
    ui.create();

    ui.getEvents().trigger("back");

    verify(game).setScreen(GdxGame.ScreenType.LEVEL_1_GAME);
  }
}
