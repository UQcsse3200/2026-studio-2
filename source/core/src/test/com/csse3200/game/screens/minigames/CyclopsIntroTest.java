package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.components.TextBoxComponent;
import org.junit.jupiter.api.Test;

class CyclopsIntroTest {
  @Test
  void skippedIntroStartsOnceStraightAway() {
    CyclopsIntro intro = new CyclopsIntro(null);

    assertFalse(intro.isShowing());
    assertTrue(intro.takeStartDue());
    assertFalse(intro.takeStartDue());
  }

  @Test
  void shownIntroStartsOnlyAfterItIsDismissed() {
    TextBoxComponent box = mock(TextBoxComponent.class);
    CyclopsIntro intro = new CyclopsIntro(box);

    assertTrue(intro.isShowing());
    assertFalse(intro.takeStartDue());

    when(box.isDismissed()).thenReturn(true);
    assertFalse(intro.isShowing());
    assertTrue(intro.takeStartDue());
    assertFalse(intro.takeStartDue());
  }
}
