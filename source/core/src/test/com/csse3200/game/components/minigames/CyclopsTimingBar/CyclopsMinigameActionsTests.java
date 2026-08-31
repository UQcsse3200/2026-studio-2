package com.csse3200.game.components.minigames.CyclopsTimingBar;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

public class CyclopsMinigameActionsTests {

  @Test
  void shouldReturnToMainMenuOnBack() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new CyclopsMinigameActions(game));
    ui.create();

    ui.getEvents().trigger("exit");

    verify(game).setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  }
}
