package com.csse3200.game.screens;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TransitionScreenTest {

  private static final float HALF_FADE = 0.2f;

  private TransitionScreen createTransitionScreen(
      GdxGame game,
      Screen fromScreen,
      ScreenType targetType,
      SpriteBatch batch,
      Texture blackPixel) {
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());

    return new TransitionScreen(game, fromScreen, targetType, batch, blackPixel);
  }

  @Test
  void shouldFadeOutCreateTargetFadeInAndFinishTransition() {
    GdxGame game = mock(GdxGame.class);
    Screen fromScreen = mock(Screen.class);
    Screen toScreen = mock(Screen.class);
    SpriteBatch batch = mock(SpriteBatch.class);
    Texture blackPixel = mock(Texture.class);

    when(game.createScreen(ScreenType.MAIN_MENU)).thenReturn(toScreen);

    TransitionScreen transition =
        createTransitionScreen(game, fromScreen, ScreenType.MAIN_MENU, batch, blackPixel);

    try {
      transition.show();
      transition.pause();
      transition.resume();
      transition.hide();

      // Resize while the outgoing screen is active.
      transition.resize(1280, 720);
      verify(fromScreen).resize(1280, 720);

      // First half of fade-out.
      transition.render(HALF_FADE);
      verify(fromScreen).render(HALF_FADE);
      verify(fromScreen, never()).dispose();
      verify(game, never()).createScreen(ScreenType.MAIN_MENU);

      // Complete fade-out.
      transition.render(HALF_FADE);
      verify(fromScreen, times(2)).render(HALF_FADE);
      verify(fromScreen).dispose();
      verify(game).createScreen(ScreenType.MAIN_MENU);

      // Resize after the target screen has been created.
      transition.resize(1920, 1080);
      verify(toScreen).resize(1920, 1080);

      // First half of fade-in.
      transition.render(HALF_FADE);
      verify(toScreen).render(HALF_FADE);
      verify(game, never()).finishTransition(any());

      // Complete fade-in.
      transition.render(HALF_FADE);
      verify(toScreen, times(2)).render(HALF_FADE);
      verify(game).finishTransition(toScreen);

      // Verify the overlay was drawn during the transition.
      verify(batch, atLeastOnce()).begin();
      verify(batch, atLeastOnce()).draw(eq(blackPixel), eq(0f), eq(0f), anyFloat(), anyFloat());
      verify(batch, atLeastOnce()).end();

    } finally {
      transition.dispose();
    }

    verify(batch).dispose();
    verify(blackPixel).dispose();
  }

  @Test
  void shouldHandleNullOutgoingScreen() {
    GdxGame game = mock(GdxGame.class);
    Screen toScreen = mock(Screen.class);
    SpriteBatch batch = mock(SpriteBatch.class);
    Texture blackPixel = mock(Texture.class);

    when(game.createScreen(ScreenType.MAIN_MENU)).thenReturn(toScreen);

    TransitionScreen transition =
        createTransitionScreen(game, null, ScreenType.MAIN_MENU, batch, blackPixel);

    try {
      // Resize with no screens assigned.
      transition.resize(800, 600);

      // Fade-out with no outgoing screen.
      transition.render(HALF_FADE);
      verify(game, never()).createScreen(ScreenType.MAIN_MENU);

      transition.render(HALF_FADE);
      verify(game).createScreen(ScreenType.MAIN_MENU);

      // Target screen receives resize and render calls.
      transition.resize(1024, 768);
      verify(toScreen).resize(1024, 768);

      transition.render(HALF_FADE);
      verify(toScreen).render(HALF_FADE);

      transition.render(HALF_FADE);
      verify(game).finishTransition(toScreen);

    } finally {
      transition.dispose();
    }

    verify(batch).dispose();
    verify(blackPixel).dispose();
  }

  @Test
  void shouldUseTheConfiguredFadeDuration() {
    GdxGame game = mock(GdxGame.class);
    Screen fromScreen = mock(Screen.class);
    Screen toScreen = mock(Screen.class);
    SpriteBatch batch = mock(SpriteBatch.class);
    Texture blackPixel = mock(Texture.class);

    when(game.createScreen(ScreenType.MAIN_MENU)).thenReturn(toScreen);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());

    // Twice the default fade length: half a second in, the fade-out is not yet complete.
    TransitionScreen transition =
        new TransitionScreen(game, fromScreen, ScreenType.MAIN_MENU, batch, blackPixel, 1f);

    try {
      transition.render(0.5f);
      verify(fromScreen).render(0.5f);
      verify(game, never()).createScreen(ScreenType.MAIN_MENU);

      transition.render(0.5f);
      verify(game).createScreen(ScreenType.MAIN_MENU);

      transition.render(0.5f);
      verify(game, never()).finishTransition(any());

      transition.render(0.5f);
      verify(game).finishTransition(toScreen);
    } finally {
      transition.dispose();
    }
  }
}
