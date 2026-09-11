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
 * Fades the outgoing screen to black, swaps to the target screen once fully black, then fades
 * back in. Used by {@link GdxGame#transitionTo(ScreenType)} for a smooth screen change instead of
 * an instant cut.
 */
public class TransitionScreen implements Screen {
  private static final float FADE_DURATION = 0.4f;

  private enum Phase {
    FADE_OUT,
    FADE_IN
  }

  private final GdxGame game;
  private final ScreenType targetType;
  private final SpriteBatch batch;
  private final Texture blackPixel;

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
    this.game = game;
    this.fromScreen = fromScreen;
    this.targetType = targetType;

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();
    blackPixel = new Texture(pixmap);
    pixmap.dispose();

    batch = new SpriteBatch();
  }

  @Override
  public void render(float delta) {
    float alpha;

    if (phase == Phase.FADE_OUT) {
      if (fromScreen != null) {
        fromScreen.render(delta);
      }

      timer += delta;
      alpha = Math.min(timer / FADE_DURATION, 1f);

      if (timer >= FADE_DURATION) {
        if (fromScreen != null) {
          fromScreen.dispose();
          fromScreen = null;
        }
        toScreen = game.createScreen(targetType);
        phase = Phase.FADE_IN;
        timer = 0f;
      }
    } else {
      if (toScreen != null) {
        toScreen.render(delta);
      }

      timer += delta;
      alpha = Math.max(1f - timer / FADE_DURATION, 0f);

      if (timer >= FADE_DURATION) {
        game.finishTransition(toScreen);
        return;
      }
    }

    drawOverlay(alpha);
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
  public void show() {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    batch.dispose();
    blackPixel.dispose();
  }
}
