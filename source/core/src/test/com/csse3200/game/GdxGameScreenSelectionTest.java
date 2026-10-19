package com.csse3200.game;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.LevelsGameScreen;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class GdxGameScreenSelectionTest {
  @ParameterizedTest
  @CsvSource({"LEVEL_2_GAME, level2", "LEVEL_3_GAME, level3"})
  void queuesRequestedLevelOnTheReturnedScreen(GdxGame.ScreenType type, String level) {
    // Isolate rendering/assets while exercising the real game's screen selection.
    try (MockedConstruction<LevelsGameScreen> screens = mockConstruction(LevelsGameScreen.class)) {
      var selected = new GdxGame().createScreen(type);
      LevelsGameScreen screen = screens.constructed().getFirst();
      assertSame(screen, selected);
      verify(screen).queueAreaSwap(level);
    }
  }
}
