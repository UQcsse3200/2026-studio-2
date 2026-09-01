package com.csse3200.game.ui;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.ui.dialogue.TypewriterEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameEndDisplayTest {
  @Test
  void shouldRepresentWinState() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.WIN);

    assertEquals(GameEndState.WIN, display.getState());
    assertEquals("YOU WIN!", display.getTitleText());
    assertTrue(display.getResultText().contains("victory"));
  }

  @Test
  void shouldRepresentLoseState() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);

    assertEquals(GameEndState.LOSE, display.getState());
    assertEquals("GAME OVER!", display.getTitleText());
    assertTrue(display.getResultText().contains("luck"));
  }

  @Test
  void shouldUseTypewriterEffectForResultMessage() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);
    TypewriterEffect typewriterEffect = display.getTypewriterEffect();

    assertNotNull(typewriterEffect);
    assertFalse(typewriterEffect.isComplete());

    typewriterEffect.update(0.1f);
    assertNotEquals("", typewriterEffect.getRevealedText());

    typewriterEffect.skipToEnd();
    assertTrue(typewriterEffect.isComplete());
    assertEquals(display.getResultText(), typewriterEffect.getRevealedText());
  }
}
