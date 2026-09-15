package com.csse3200.game.ui;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameEndDisplayTest {
  @Test
  void shouldRepresentWinState() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.WIN);

    assertEquals(GameEndState.WIN, display.getState());
    assertEquals("VICTORY!", display.getTitleText());
    assertTrue(display.getResultText().contains("closer to getting home"));
  }

  @Test
  void shouldRepresentLoseState() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);

    assertEquals(GameEndState.LOSE, display.getState());
    assertEquals("DEFEAT!", display.getTitleText());
    assertTrue(display.getResultText().contains("Penelope"));
  }

  @Test
  void shouldHaveZeroRevealedCharsInitially() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);

    assertEquals(0, display.getRevealedChars());
  }

  @Test
  void shouldUpdateResultTextOnStateChange() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);

    assertTrue(display.getResultText().contains("Penelope"));

    display.setState(GameEndState.WIN);
    assertTrue(display.getResultText().contains("closer to getting home"));
  }

  @Test
  void shouldResetRevealedCharsOnStateChange() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);
    display.setState(GameEndState.WIN);

    assertEquals(0, display.getRevealedChars());
  }
}
