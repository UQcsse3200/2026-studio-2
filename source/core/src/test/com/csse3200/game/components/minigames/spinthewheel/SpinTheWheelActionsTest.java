package com.csse3200.game.components.minigames.spinthewheel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SpinTheWheelActionsTest {

  @Test
  void shouldReturnToMinigameSelectOnBack() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new SpinTheWheelActions(game));
    ui.create();

    ui.getEvents().trigger("back");

    verify(game).setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  }

  @Test
  void shouldStayBeforeBack() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new SpinTheWheelActions(game));
    ui.create();

    verifyNoInteractions(game);
  }
}
