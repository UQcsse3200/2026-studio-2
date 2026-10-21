package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.csse3200.game.GdxGame;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.extensions.GameExtension;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BlackjackMinigameCommandTest {
  @Test
  void shouldPlayCutsceneBeforeBlackjack() {
    GdxGame game = mock(GdxGame.class);
    BlackjackMinigameCommand command = new BlackjackMinigameCommand(game);

    assertTrue(command.action(new ArrayList<>(List.of("load"))));

    verify(game).startCutscene(any(), eq(GdxGame.ScreenType.MINIGAME_BLACKJACK));
    verify(game, never()).setScreen(any(GdxGame.ScreenType.class));
  }

  @Test
  void shouldGoStraightToBlackjackWhenCutsceneIsUnavailable() {
    GdxGame game = mock(GdxGame.class);
    CutsceneLoader loader = mock(CutsceneLoader.class);
    CutsceneLoader.Result failed = mock(CutsceneLoader.Result.class);
    when(failed.isSuccess()).thenReturn(false);
    when(failed.getError()).thenReturn("missing");
    when(loader.load(BlackjackMinigameCommand.INTRO_CUTSCENE)).thenReturn(failed);
    BlackjackMinigameCommand command = new BlackjackMinigameCommand(game, loader);

    assertTrue(command.action(new ArrayList<>(List.of("load"))));

    verify(game).setScreen(GdxGame.ScreenType.MINIGAME_BLACKJACK);
    verify(game, never()).startCutscene(any(), any());
  }

  @Test
  void shouldRejectInvalidArguments() {
    BlackjackMinigameCommand command = new BlackjackMinigameCommand(mock(GdxGame.class));

    assertFalse(command.action(new ArrayList<>()));
    assertFalse(command.action(new ArrayList<>(List.of("start"))));
    assertFalse(command.action(new ArrayList<>(List.of("load", "extra"))));
  }
}
