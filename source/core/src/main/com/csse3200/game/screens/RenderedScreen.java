package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.csse3200.game.rendering.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared window lifecycle for screens that own a renderer. */
public abstract class RenderedScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(RenderedScreen.class);

  /** Returns the renderer owned and disposed by the concrete screen. */
  protected abstract Renderer getRenderer();

  @Override
  public void resize(int width, int height) {
    getRenderer().resize(width, height);
    logger.trace("Resized renderer: ({} x {})", width, height);
  }

  @Override
  public void pause() {
    logger.info("Game paused");
  }

  @Override
  public void resume() {
    logger.info("Game resumed");
  }
}
