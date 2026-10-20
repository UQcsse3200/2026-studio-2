package com.csse3200.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Screen;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.minigames.BlackjackScreen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class GdxGameStoryTest {
  @ParameterizedTest
  @CsvSource({
    "cutscene2, CYCLOPS_MINIGAME",
    "cutscene3, LEVEL_2_GAME",
    "cutscene4, STORY_BLACKJACK",
    "cutscene5, LEVEL_3_GAME"
  })
  void loadsTheStoryAssetsAndPassesTheDestinationToTheCutscene(
      String name, GdxGame.ScreenType destination) {
    GdxGame game = spy(new GdxGame());
    doNothing().when(game).startCutscene(any(CutsceneLoader.LoadedCutscene.class), any());

    game.playCutscene(name, destination);

    ArgumentCaptor<CutsceneLoader.LoadedCutscene> loaded =
        ArgumentCaptor.forClass(CutsceneLoader.LoadedCutscene.class);
    verify(game).startCutscene(loaded.capture(), eq(destination));
    assertEquals(name, loaded.getValue().getName());
  }

  @Test
  void unavailableCutsceneStillContinuesToItsDestination() {
    GdxGame game = spy(new GdxGame());
    doNothing().when(game).transitionTo(any());

    game.playCutscene("missing-cutscene", GdxGame.ScreenType.LEVEL_3_GAME);

    verify(game).transitionTo(GdxGame.ScreenType.LEVEL_3_GAME);
    verify(game, never()).startCutscene(any(CutsceneLoader.LoadedCutscene.class), any());
  }

  @Test
  void storyBlackjackIsCreatedInStoryMode() {
    try (var screens =
        mockConstruction(
            BlackjackScreen.class,
            (screen, context) -> {
              assertEquals(true, context.arguments().get(1));
            })) {
      Screen selected = new GdxGame().createScreen(GdxGame.ScreenType.STORY_BLACKJACK);
      assertSame(screens.constructed().getFirst(), selected);
    }
  }

  @Test
  void endingTheFadeDoesNotInitialiseTheDestinationTwice() {
    GdxGame game = new GdxGame();
    Screen transition = mock(Screen.class);
    Screen destination = mock(Screen.class);
    game.setScreen(transition);

    game.finishTransition(destination);

    assertSame(destination, game.getScreen());
    verify(destination, never()).show();
    verify(transition).hide();
    verify(transition).dispose();
  }
}
