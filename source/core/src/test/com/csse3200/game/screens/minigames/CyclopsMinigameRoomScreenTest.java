package com.csse3200.game.screens.minigames;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameLogic;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class CyclopsMinigameRoomScreenTest {
  @AfterEach
  void resetApp() {
    Gdx.app = null;
  }

  @Test
  void diedShowsGameOverPanel() {
    EventHandler gameEnd = mock(EventHandler.class);
    ServiceLocator.registerGameEndEventHandler(gameEnd);

    CyclopsMinigameRoomScreen.showGameOver();

    verify(gameEnd).trigger("gameEnd", GameEndState.LOSE);
  }

  @Test
  void restartReloadsCyclopsMinigameAndFiresSleep() {
    Application app = mock(Application.class);
    Gdx.app = app;
    EventHandler cyclopsEvents = mock(EventHandler.class);
    ServiceLocator.registerCyclopsMinigameEventHandler(cyclopsEvents);
    GdxGame game = mock(GdxGame.class);

    CyclopsMinigameRoomScreen.restartFromGameOver(game);

    ArgumentCaptor<Runnable> reload = ArgumentCaptor.forClass(Runnable.class);
    verify(app).postRunnable(reload.capture());
    reload.getValue().run();
    verify(game).setScreen(GdxGame.ScreenType.CYCLOPS_MINIGAME);
    verify(cyclopsEvents).trigger(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT);
  }
}
