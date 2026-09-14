package com.csse3200.game.components.gamearea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.ui.UIComponent;

/** Displays the name of the current game area. */
public class GameAreaDisplay extends UIComponent {
  private String gameAreaName = "";
  private Label title;

  public GameAreaDisplay(String gameAreaName) {
    this.gameAreaName = gameAreaName;
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    title =
        new BoldLabel(this.gameAreaName.toUpperCase(), skin.get("large", Label.LabelStyle.class));
    stage.addActor(title);
  }

  /**
   * Draws its text twice, offset by a pixel, to fake a bold weight — the pixel-font skin has no
   * dedicated bold variant.
   */
  private static class BoldLabel extends Label {
    BoldLabel(CharSequence text, LabelStyle style) {
      super(text, style);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      float x = getX();
      setX(x + 1f);
      super.draw(batch, parentAlpha);
      setX(x);
      super.draw(batch, parentAlpha);
    }
  }

  @Override
  public void draw(SpriteBatch batch) {
    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();
    float offsetY = 20f;

    // Centered at the top, clear of the heart HUD (left) and exit button (right).
    title.setPosition(
        (screenWidth - title.getWidth()) / 2f, screenHeight - offsetY - title.getHeight());
  }

  @Override
  public void dispose() {
    super.dispose();
    title.remove();
  }
}
