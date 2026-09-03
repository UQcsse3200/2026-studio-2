package com.csse3200.game.components.mainmenu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MainMenuActionsTest {
  @Test
  void shouldOpenSandbox() {
    GdxGame game = mock(GdxGame.class);
    Entity ui = new Entity().addComponent(new MainMenuActions(game));
    ui.create();

    ui.getEvents().trigger("Sandbox");

    verify(game).setScreen(GdxGame.ScreenType.SANDBOX);
  }
}
