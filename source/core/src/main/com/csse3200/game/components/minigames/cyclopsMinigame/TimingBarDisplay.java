package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingBarDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(TimingBarDisplay.class);

  static final float MARKER_WIDTH = 10f;
  static final float BAR_WIDTH = 400f;
  static final float BAR_HEIGHT = 30f;

  private static final Color MARKER_COLOR = Color.valueOf("#FFFFFF");
  private static final Color SCORING_COLOR = Color.valueOf("#009A66");
  private static final Color BACKGROUND_COLOR = Color.TAN;
  private static final Color BORDER_COLOR = Color.BROWN;
  private static final Color FLASH_COLOR = Color.WHITE;
  static final int FLASH_FRAMES = 30;
  static final int FLASH_BLINK_FRAMES = 5;

  private final TimingBarLogic logic;
  private Texture blankTexture;

  Table table;
  Image marker;
  Image scoringZone;
  private float shownScoringArea = -1f;
  private boolean shrinkPending = false;
  private int flashFramesLeft = 0;

  private boolean visible = false;

  public TimingBarDisplay(TimingBarLogic logic) {
    this.logic = logic;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    if (table != null) {
      table.setVisible(visible);
    }
  }

  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public void create() {
    super.create();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();

    blankTexture = new Texture(pixmap);
    TextureRegion blankRegion = new TextureRegion(blankTexture);
    pixmap.dispose();

    Group group = new Group();
    group.setSize(BAR_WIDTH + MARKER_WIDTH, BAR_HEIGHT);

    Image border = new Image(blankRegion);
    border.setColor(BORDER_COLOR);
    border.setSize(BAR_WIDTH + MARKER_WIDTH + 6f, BAR_HEIGHT + 6f);
    border.setPosition(-3, -3);
    group.addActor(border);

    Image background = new Image(blankRegion);
    background.setColor(BACKGROUND_COLOR);
    background.setSize(BAR_WIDTH + MARKER_WIDTH, BAR_HEIGHT);
    background.setPosition(0, 0);
    group.addActor(background);

    scoringZone = new Image(blankRegion);
    scoringZone.setColor(SCORING_COLOR);
    layoutScoringZone(logic.getScoringAreaSize());
    shownScoringArea = logic.getScoringAreaSize();
    group.addActor(scoringZone);

    marker = new Image(blankRegion);
    marker.setColor(MARKER_COLOR);
    marker.setSize(10f, BAR_HEIGHT);
    marker.setPosition(logic.getMarkerX(), 0);
    group.addActor(marker);

    table = new Table();
    table.setSize(BAR_WIDTH, BAR_HEIGHT);
    table.setFillParent(true);
    table.add(group).expand().center();

    table.setVisible(visible);

    stage.addActor(table);
  }

  @Override
  public void update() {
    if (marker != null) {
      marker.setX(logic.getMarkerX() * BAR_WIDTH);
    }
    if (scoringZone != null) {
      syncScoringZone();
    }
  }

  private void layoutScoringZone(float scoringArea) {
    scoringZone.setSize(scoringArea * BAR_WIDTH, BAR_HEIGHT);
    scoringZone.setPosition(BAR_WIDTH / 2 - scoringZone.getWidth() / 2, 0);
  }

  /** Follows the logic's scoring width, and blinks the zone briefly when it shrinks. */
  private void syncScoringZone() {
    float scoringArea = logic.getScoringAreaSize();
    if (scoringArea != shownScoringArea) {
      shrinkPending |= scoringArea < shownScoringArea;
      shownScoringArea = scoringArea;
      layoutScoringZone(scoringArea);
    }

    if (shrinkPending && visible) {
      shrinkPending = false;
      flashFramesLeft = FLASH_FRAMES;
    }

    if (flashFramesLeft > 0) {
      flashFramesLeft--;
      int elapsed = FLASH_FRAMES - flashFramesLeft;
      boolean on = (elapsed / FLASH_BLINK_FRAMES) % 2 == 0;
      scoringZone.setColor(on ? FLASH_COLOR : SCORING_COLOR);
    } else {
      scoringZone.setColor(SCORING_COLOR);
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
    batch.setColor(Color.WHITE);
  }

  @Override
  public void dispose() {
    super.dispose();

    if (blankTexture != null) {
      blankTexture.dispose();
    }
  }
}
