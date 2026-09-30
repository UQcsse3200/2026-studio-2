package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class CutsceneCommandTest {
  @Test
  void shouldStartNamedCutscene() {
    GdxGame game = mock(GdxGame.class);
    CutsceneCommand command = new CutsceneCommand(game, GdxGame.ScreenType.LEVEL_1_GAME);

    assertTrue(command.action(new ArrayList<>(java.util.List.of("cutscene1"))));
    verify(game)
        .startCutscene(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.eq(GdxGame.ScreenType.LEVEL_1_GAME));
  }

  @Test
  void shouldRejectIncorrectArgumentCount() {
    CutsceneCommand command =
        new CutsceneCommand(mock(GdxGame.class), GdxGame.ScreenType.LEVEL_1_GAME);

    assertFalse(command.action(new ArrayList<>()));
    assertFalse(command.action(new ArrayList<>(java.util.List.of("one", "two"))));
  }
}
