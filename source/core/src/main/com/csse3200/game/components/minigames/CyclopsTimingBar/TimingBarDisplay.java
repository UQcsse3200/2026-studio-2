package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.ui.UIComponent;

public class TimingBarDisplay extends UIComponent {

  private static final float SCALE = 10f;
  private static final float SCREEN_X = 100f;
  private static final float SCREEN_Y = 500f;
  private static final float BAR_HEIGHT = 20f;

  private final TimingBarLogic logic;
  private Texture blankTexture;
  private TextureRegion blankRegion;

  private Group group;
  private Image barBackground;
  private Image greenZone;
  private Image marker;

  private boolean visible = true;

  public TimingBarDisplay(TimingBarLogic logic) {
    this.logic = logic;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    if (group != null) {
      group.setVisible(visible);
    }
  }

  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public void create() {
    super.create();

    // For now create a 1x1 texture (until assets are used maybe)
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();

    blankTexture = new Texture(pixmap);
    blankRegion = new TextureRegion(blankTexture);
    pixmap.dispose();

    group = new Group();

    barBackground = new Image(blankRegion);
    barBackground.setColor(Color.DARK_GRAY);
    barBackground.setPosition(SCREEN_X + logic.barStart * SCALE, SCREEN_Y);
    barBackground.setSize(logic.barWidth * SCALE, BAR_HEIGHT);
    group.addActor(barBackground);

    greenZone = new Image(blankRegion);
    greenZone.setColor(Color.GREEN);
    greenZone.setPosition(SCREEN_X + logic.greenStart * SCALE, SCREEN_Y);
    greenZone.setSize((logic.greenEnd - logic.greenStart) * SCALE, BAR_HEIGHT);
    group.addActor(greenZone);

    marker = new Image(blankRegion);
    marker.setColor(Color.RED);
    marker.setSize(4f, BAR_HEIGHT + 8f);
    marker.setPosition(SCREEN_X + logic.markerX * SCALE - marker.getWidth() / 2, SCREEN_Y - 4f);
    group.addActor(marker);

    group.setVisible(visible);
    stage.addActor(group);
  }

  @Override
  public void update() {
    if (marker != null) {
      marker.setX(SCREEN_X + logic.markerX * SCALE - marker.getWidth() / 2);
    }
  }

  /**
   * Draw the renderable. Should be called only by the renderer, not manually.
   *
   * @param batch Batch to render to.
   */
  @Override
  protected void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public void dispose() {
    super.dispose();

    if (blankTexture != null) {
      blankTexture.dispose();
    }
  }
}
