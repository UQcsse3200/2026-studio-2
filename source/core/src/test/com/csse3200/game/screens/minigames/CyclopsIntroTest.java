package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.csse3200.game.components.TextBoxComponent;
import org.junit.jupiter.api.Test;

class CyclopsIntroTest {
  @Test
  void skippedIntroStartsOnceStraightAway() {
    CyclopsIntro intro = new CyclopsIntro(null);

    assertFalse(intro.isShowing());
    intro.advance();
    intro.dispose();
    assertTrue(intro.takeStartDue());
    assertFalse(intro.takeStartDue());
  }

  @Test
  void advanceOnlyForwardsWhileTheBoxIsShowing() {
    TextBoxComponent box = mock(TextBoxComponent.class);
    CyclopsIntro intro = new CyclopsIntro(box);
    intro.advance();
    verify(box).advance();
    when(box.isDismissed()).thenReturn(true);
    intro.advance();
    verify(box).advance();
  }

  @Test
  void dismissedIntroNeverAdvancesAgain() {
    TextBoxComponent box = mock(TextBoxComponent.class);
    when(box.isDismissed()).thenReturn(true);
    CyclopsIntro intro = new CyclopsIntro(box);
    intro.advance();
    verify(box, never()).advance();
    assertTrue(intro.takeStartDue());
    assertFalse(intro.takeStartDue());
  }

  @Test
  void disposalDismissesTheOwnedBoxOnlyWhileShowing() {
    TextBoxComponent box = mock(TextBoxComponent.class);
    CyclopsIntro intro = new CyclopsIntro(box);
    intro.dispose();
    verify(box).dismiss();
    when(box.isDismissed()).thenReturn(true);
    intro.dispose();
    verify(box).dismiss();
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
