package com.csse3200.game.ui;

import static org.mockito.Mockito.*;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameEndActionsTest {
  @Test
  void shouldRunRestartHandlerInsteadOfReloading() {
    GdxGame game = mock(GdxGame.class);
    AtomicBoolean handlerRan = new AtomicBoolean(false);
    GameEndActions actions = new GameEndActions(game, () -> handlerRan.set(true));
    Entity ui = new Entity().addComponent(actions);
    ui.create();

    ui.getEvents().trigger("restart");

    org.junit.jupiter.api.Assertions.assertTrue(handlerRan.get());
    verify(game, never()).transitionTo(any());
  }

  @Test
  void shouldFallBackToLevelReloadWithoutHandler() {
    GdxGame game = mock(GdxGame.class);
    GameEndActions actions = new GameEndActions(game);
    Entity ui = new Entity().addComponent(actions);
    ui.create();

    ui.getEvents().trigger("restart");

    verify(game).transitionTo(GdxGame.ScreenType.LEVEL_1_GAME);
  }

  @Test
  void shouldHideDisplayWithoutCrashing() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(display::hide);
    org.junit.jupiter.api.Assertions.assertFalse(display.isVisible());
  }
}
