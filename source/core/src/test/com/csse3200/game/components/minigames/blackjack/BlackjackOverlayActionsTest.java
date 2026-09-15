package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BlackjackOverlayActionsTest {

  @Test
  void shouldCloseOnBack() {
    AtomicBoolean closed = new AtomicBoolean(false);
    Entity ui = new Entity().addComponent(new BlackjackOverlayActions(() -> closed.set(true)));
    ui.create();

    ui.getEvents().trigger("back");

    assertTrue(closed.get());
  }

  @Test
  void shouldNotCloseBeforeBack() {
    AtomicBoolean closed = new AtomicBoolean(false);
    Entity ui = new Entity().addComponent(new BlackjackOverlayActions(() -> closed.set(true)));
    ui.create();

    assertFalse(closed.get());
  }
}
