package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;

/**
 * Fades the outgoing screen to black, swaps to the target screen once fully black, then fades back
 * in. Used by {@link GdxGame#transitionTo(ScreenType)} for a smooth screen change instead of an
 * instant cut.
 */
public class TransitionScreen implements Screen {
  /** Fade length used when no duration is given. */
  public static final float DEFAULT_FADE_DURATION = 0.4f;

  private enum Phase {
    FADE_OUT,
    FADE_IN
  }

  private final GdxGame game;
  private final ScreenType targetType;
  private final SpriteBatch batch;
  private final Texture blackPixel;
  private final float fadeDuration;

  private Screen fromScreen;
  private Screen toScreen;
  private Phase phase = Phase.FADE_OUT;
  private float timer = 0f;

  /**
   * @param game the game instance, used to build the target screen and hand off once faded in
   * @param fromScreen the screen being faded out of (kept rendering until fully black)
   * @param targetType the screen type to fade into
   */
  public TransitionScreen(GdxGame game, Screen fromScreen, ScreenType targetType) {
    this(game, fromScreen, targetType, DEFAULT_FADE_DURATION);
  }

  /**
   * @param fadeDuration length in seconds of each fade phase (out and in)
   */
  public TransitionScreen(
      GdxGame game, Screen fromScreen, ScreenType targetType, float fadeDuration) {
    this.game = game;
    this.fromScreen = fromScreen;
    this.targetType = targetType;
    this.fadeDuration = fadeDuration;

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();
    blackPixel = new Texture(pixmap);
    pixmap.dispose();

    batch = new SpriteBatch();
  }

  TransitionScreen(
      GdxGame game,
      Screen fromScreen,
      ScreenType targetType,
      SpriteBatch batch,
      Texture blackPixel) {
    this(game, fromScreen, targetType, batch, blackPixel, DEFAULT_FADE_DURATION);
  }

  TransitionScreen(
      GdxGame game,
      Screen fromScreen,
      ScreenType targetType,
      SpriteBatch batch,
      Texture blackPixel,
      float fadeDuration) {
    this.game = game;
    this.fromScreen = fromScreen;
    this.targetType = targetType;
    this.batch = batch;
    this.blackPixel = blackPixel;
    this.fadeDuration = fadeDuration;
  }

  @Override
  public void render(float delta) {
    float alpha;

    if (phase == Phase.FADE_OUT) {
      if (fromScreen != null) {
        fromScreen.render(delta);
      }

      timer += delta;
      alpha = Math.min(timer / fadeDuration, 1f);

      if (timer >= fadeDuration) {
        startFadeIn();
      }
    } else {
      if (toScreen != null) {
        toScreen.render(delta);
      }

      timer += delta;
      alpha = Math.max(1f - timer / fadeDuration, 0f);

      if (timer >= fadeDuration) {
        game.finishTransition(toScreen);
        return;
      }
    }

    drawOverlay(alpha);
  }

  private void startFadeIn() {
    if (fromScreen != null) {
      fromScreen.dispose();
      fromScreen = null;
    }
    toScreen = game.createScreen(targetType);
    if (toScreen != null) {
      toScreen.show();
      toScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    phase = Phase.FADE_IN;
    timer = 0f;
  }

  private void drawOverlay(float alpha) {
    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();

    batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    batch.begin();
    batch.setColor(0f, 0f, 0f, alpha);
    batch.draw(blackPixel, 0, 0, width, height);
    batch.setColor(Color.WHITE);
    batch.end();
  }

  @Override
  public void resize(int width, int height) {
    if (fromScreen != null) {
      fromScreen.resize(width, height);
    }
    if (toScreen != null) {
      toScreen.resize(width, height);
    }
  }

  @Override
  public void show() {
    // The constructor has already prepared the transition resources.
  }

  @Override
  public void pause() {
    // The transition has no separate pause or resume state.
  }

  @Override
  public void resume() {
    // The transition has no separate pause or resume state.
  }

  @Override
  public void hide() {
    // Transition resources are released by dispose().
  }

  @Override
  public void dispose() {
    batch.dispose();
    blackPixel.dispose();
  }
}
