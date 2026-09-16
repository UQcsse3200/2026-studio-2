package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.ui.UIComponent;

public class BlankTransitionScreen extends UIComponent {
  private static final float Z_INDEX = 10f;

  private Texture texture;
  private Table table;

  public void fadeIn(float duration) {
    table.clearActions();
    table.setVisible(true);
    table.addAction(Actions.fadeIn(duration));
  }

  public void fadeOut(float duration) {
    table.clearActions();
    table.addAction(Actions.sequence(Actions.fadeOut(duration), Actions.visible(false)));
  }

  public void setVisible(boolean visible) {
    table.setVisible(visible);
  }

  @Override
  public void create() {
    super.create();
    table = new Table();
    table.setFillParent(true);

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();

    texture = new Texture(pixmap);
    TextureRegion textureRegion = new TextureRegion(texture);
    pixmap.dispose();

    table.setBackground(new TextureRegionDrawable(textureRegion));
    table.setColor(1, 1, 1, 0);
    table.setVisible(false);
    stage.addActor(table);
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
