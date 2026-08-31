package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlankTransitionScreen extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(BlankTransitionScreen.class);
  private static final float Z_INDEX = 4;

  private Texture texture;
  private TextureRegion textureRegion;
  private Image blackScreen;

  public boolean isVisible() {
    return blackScreen.isVisible();
  }

  public void setVisible(boolean visible) {
    blackScreen.setVisible(visible);
  }

  @Override
  public void create() {
    super.create();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();

    texture = new Texture(pixmap);
    textureRegion = new TextureRegion(texture);
    pixmap.dispose();

    blackScreen = new Image(textureRegion);
    blackScreen.setPosition(0, 0);
    blackScreen.setSize(stage.getWidth(), stage.getHeight());

    stage.addActor(blackScreen);
    blackScreen.setVisible(false);
  }

  /**
   * Draw the renderable. Should be called only by the renderer, not manually.
   *
   * @param batch Batch to render to.
   */
  @Override
  protected void draw(SpriteBatch batch) {
    // handled by game engine
  }

  @Override
  public void dispose() {
    super.dispose();
    if (texture != null) texture.dispose();
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }
}
