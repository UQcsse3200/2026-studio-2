package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BlackjackMinigameCommandTest {
  @Test
  void shouldLoadBlackjackMinigame() {
    GdxGame game = mock(GdxGame.class);
    BlackjackMinigameCommand command =
        new BlackjackMinigameCommand(game, GdxGame.ScreenType.LEVEL_1_GAME);

    assertTrue(command.action(new ArrayList<>(List.of("load"))));

    verify(game).setScreen(GdxGame.ScreenType.MINIGAME_BLACKJACK, GdxGame.ScreenType.LEVEL_1_GAME);
  }

  @Test
  void shouldRejectInvalidArguments() {
    BlackjackMinigameCommand command = new BlackjackMinigameCommand(mock(GdxGame.class));

    assertFalse(command.action(new ArrayList<>()));
    assertFalse(command.action(new ArrayList<>(List.of("start"))));
    assertFalse(command.action(new ArrayList<>(List.of("load", "extra"))));
  }
}
