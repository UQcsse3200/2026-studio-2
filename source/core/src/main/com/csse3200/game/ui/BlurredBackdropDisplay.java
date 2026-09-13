package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/** Fills the screen with a blurred snapshot of the game, dimmed to sit behind a modal ui. */
public class BlurredBackdropDisplay extends UIComponent {
  private static final Color TINT = new Color(0.45f, 0.45f, 0.5f, 1f);

  private final TextureRegion backdrop;
  private Image image;

  /**
   * @param backdrop the blurred snapshot to show, which this component takes ownership of
   */
  public BlurredBackdropDisplay(TextureRegion backdrop) {
    this.backdrop = backdrop;
  }

  @Override
  public void create() {
    super.create();
    image = new Image(new TextureRegionDrawable(backdrop));
    image.setFillParent(true);
    image.setColor(TINT);
    stage.addActor(image);
  }

  /** Brings the backdrop above the gameplay hud, which the snapshot already contains. */
  public void toFront() {
    image.toFront();
  }

  @Override
  public void draw(SpriteBatch batch) {}

  @Override
  public void dispose() {
    image.remove();
    backdrop.getTexture().dispose();
    super.dispose();
  }
}
