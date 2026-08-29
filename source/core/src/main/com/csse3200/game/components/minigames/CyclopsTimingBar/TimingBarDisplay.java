package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingBarDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(TimingBarDisplay.class);

  private float SCREEN_X;
  private float SCREEN_Y;
  private final float BAR_WIDTH = 400f;
  private final float BAR_HEIGHT = 25f;

  private float location_x;
  private float location_y;

  private final TimingBarLogic logic;
  private Texture blankTexture;
  private TextureRegion blankRegion;

  private Group group;
  private Image barBackground;
  private Image scoreZone;
  private Image marker;

  private boolean visible = false;

  public TimingBarDisplay(TimingBarLogic logic) {
    this.logic = logic;
  }

  public float getScreenWidth() {
    return this.SCREEN_X;
  }

  private void setupComponentSizes() {
    logger.info("Retrieving window size");
    logger.info("Stage dimensions: ({}, {})", stage.getWidth(), stage.getHeight());
    SCREEN_X = stage.getWidth();
    SCREEN_Y = stage.getHeight();

    location_x = (SCREEN_X / 2) - (BAR_WIDTH / 2);
    location_y = (SCREEN_Y / 2) + (BAR_HEIGHT / 2);
  }

  public void setVisible(boolean visible) {
    logger.info("setting TimingBarDiplay visbility to {}.", visible);
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
    setupComponentSizes();

    // For now create a 1x1 texture (until assets are used maybe)
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();

    blankTexture = new Texture(pixmap);
    blankRegion = new TextureRegion(blankTexture);
    pixmap.dispose();

    group = new Group();

    logger.info("loading timing bar background");
    barBackground = new Image(blankRegion);
    barBackground.setColor(Color.DARK_GRAY);
    barBackground.setPosition(location_x, location_y);
    barBackground.setSize(BAR_WIDTH, BAR_HEIGHT);
    group.addActor(barBackground);
    logger.info("created timing bar background at (X: {}, Y: {})", location_x, location_y);

    logger.info("loading timing bar scoring zone");
    scoreZone = new Image(blankRegion);
    scoreZone.setColor(Color.GREEN);
    scoreZone.setPosition(
        location_x + (BAR_WIDTH / 2) - ((BAR_WIDTH * logic.scoringAreaSize) / 2), location_y);
    scoreZone.setSize((BAR_WIDTH * logic.scoringAreaSize), BAR_HEIGHT);
    group.addActor(scoreZone);

    logger.info("loading timing bar marker");
    marker = new Image(blankRegion);
    marker.setColor(Color.RED);
    marker.setSize(10f, BAR_HEIGHT);
    marker.setPosition(
        location_x + (logic.markerX * BAR_WIDTH) - marker.getWidth() / 2, location_y);
    group.addActor(marker);

    group.setVisible(visible);
    stage.addActor(group);
  }

  @Override
  public void update() {
    if (marker != null) {
      marker.setX(location_x + (logic.markerX * BAR_WIDTH) - marker.getWidth() / 2);
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
