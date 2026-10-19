package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingBarDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(TimingBarDisplay.class);

  static final float MARKER_WIDTH = 10f;
  static final float BAR_WIDTH = 400f;
  static final float BAR_HEIGHT = 30f;

  static final String FRAME_TEXTURE = "images/minigames/Cyclops/timing_bar_frame.png";
  static final float FRAME_WIDTH = 478f;
  static final float FRAME_HEIGHT = 98f;
  static final float FRAME_INSET = 34f;

  private static final Color ZONE_BASE = Color.valueOf("#E8B04A");
  private static final Color ZONE_HIGHLIGHT = Color.valueOf("#FFD97A");
  private static final Color ZONE_SHADOW = Color.valueOf("#B07628");
  private static final Color ZONE_EDGE = Color.valueOf("#FFF0BE");
  private static final float ZONE_HIGHLIGHT_FRACTION = 0.3f;
  private static final float ZONE_SHADOW_HEIGHT = 2f;
  private static final float ZONE_EDGE_WIDTH = 2f;
  private static final float ZONE_EDGE_INSET = 2f;

  static final float MARKER_BODY_WIDTH = 6f;
  static final float MARKER_OVERHANG = 8f;
  static final float MARKER_OUTLINE_WIDTH = 2f;
  private static final float MARKER_LIGHT_WIDTH = 1f;
  private static final float MARKER_SHADE_WIDTH = 1f;
  private static final Color MARKER_IVORY = Color.valueOf("#FFF4D6");
  private static final Color MARKER_OUTLINE = Color.valueOf("#1A0801");
  private static final Color MARKER_LIGHT = Color.valueOf("#FFFFF5");
  private static final Color MARKER_SHADE = Color.valueOf("#DCC8A0");

  private static final Color FLASH_COLOR = Color.WHITE;
  static final int FLASH_FRAMES = 30;
  static final int FLASH_BLINK_FRAMES = 5;

  private final TimingBarLogic logic;
  private Texture blankTexture;

  Table table;
  Image frame;
  Image scoringZone;
  Image zoneHighlight;
  Image zoneShadow;
  Image zoneLeftEdge;
  Image zoneRightEdge;
  Image marker;
  Image markerOutline;
  Image markerLight;
  Image markerShade;
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

    Texture frameTexture =
        ServiceLocator.getResourceService().getAsset(FRAME_TEXTURE, Texture.class);

    Group group = new Group();
    group.setSize(BAR_WIDTH + MARKER_WIDTH, BAR_HEIGHT);

    frame = new Image(frameTexture);
    frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    frame.setPosition(-FRAME_INSET, -FRAME_INSET);
    group.addActor(frame);

    scoringZone = blankImage(blankRegion, ZONE_BASE);
    zoneHighlight = blankImage(blankRegion, ZONE_HIGHLIGHT);
    zoneShadow = blankImage(blankRegion, ZONE_SHADOW);
    zoneLeftEdge = blankImage(blankRegion, ZONE_EDGE);
    zoneRightEdge = blankImage(blankRegion, ZONE_EDGE);
    for (Image layer : scoringLayers()) {
      group.addActor(layer);
    }
    layoutScoringZone(logic.getScoringAreaSize());
    shownScoringArea = logic.getScoringAreaSize();

    markerOutline = blankImage(blankRegion, MARKER_OUTLINE);
    marker = blankImage(blankRegion, MARKER_IVORY);
    markerLight = blankImage(blankRegion, MARKER_LIGHT);
    markerShade = blankImage(blankRegion, MARKER_SHADE);
    group.addActor(markerOutline);
    group.addActor(marker);
    group.addActor(markerLight);
    group.addActor(markerShade);
    layoutMarker();

    table = new Table();
    table.setSize(BAR_WIDTH, BAR_HEIGHT);
    table.setFillParent(true);
    table.add(group).expand().center();

    table.setVisible(visible);

    stage.addActor(table);
  }

  private Image blankImage(TextureRegion region, Color colour) {
    Image image = new Image(region);
    image.setColor(colour);
    return image;
  }

  private Image[] scoringLayers() {
    return new Image[] {scoringZone, zoneHighlight, zoneShadow, zoneLeftEdge, zoneRightEdge};
  }

  @Override
  public void update() {
    if (marker != null) {
      layoutMarker();
    }
    if (scoringZone != null) {
      syncScoringZone();
    }
  }

  private static void setBox(Image image, float x, float y, float width, float height) {
    image.setPosition(x, y);
    image.setSize(width, height);
  }

  private void layoutScoringZone(float scoringArea) {
    float width = scoringArea * BAR_WIDTH;
    float x = BAR_WIDTH / 2 - width / 2;
    float highlightHeight = BAR_HEIGHT * ZONE_HIGHLIGHT_FRACTION;
    float edgeHeight = BAR_HEIGHT - 2 * ZONE_EDGE_INSET;

    setBox(scoringZone, x, 0, width, BAR_HEIGHT);
    setBox(zoneHighlight, x, BAR_HEIGHT - highlightHeight, width, highlightHeight);
    setBox(zoneShadow, x, 0, width, ZONE_SHADOW_HEIGHT);
    setBox(zoneLeftEdge, x, ZONE_EDGE_INSET, ZONE_EDGE_WIDTH, edgeHeight);
    setBox(
        zoneRightEdge, x + width - ZONE_EDGE_WIDTH, ZONE_EDGE_INSET, ZONE_EDGE_WIDTH, edgeHeight);
  }

  private void layoutMarker() {
    float bodyX = logic.getMarkerX() * BAR_WIDTH + (MARKER_WIDTH - MARKER_BODY_WIDTH) / 2;
    float bodyY = -MARKER_OVERHANG;
    float bodyHeight = BAR_HEIGHT + 2 * MARKER_OVERHANG;

    setBox(
        markerOutline,
        bodyX - MARKER_OUTLINE_WIDTH,
        bodyY - MARKER_OUTLINE_WIDTH,
        MARKER_BODY_WIDTH + 2 * MARKER_OUTLINE_WIDTH,
        bodyHeight + 2 * MARKER_OUTLINE_WIDTH);
    setBox(marker, bodyX, bodyY, MARKER_BODY_WIDTH, bodyHeight);
    setBox(markerLight, bodyX, bodyY, MARKER_LIGHT_WIDTH, bodyHeight);
    setBox(
        markerShade,
        bodyX + MARKER_BODY_WIDTH - MARKER_SHADE_WIDTH,
        bodyY,
        MARKER_SHADE_WIDTH,
        bodyHeight);
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

    boolean flashOn = false;
    if (flashFramesLeft > 0) {
      flashFramesLeft--;
      int elapsed = FLASH_FRAMES - flashFramesLeft;
      flashOn = (elapsed / FLASH_BLINK_FRAMES) % 2 == 0;
    }
    paintScoringZone(flashOn);
  }

  private void paintScoringZone(boolean flashOn) {
    scoringZone.setColor(flashOn ? FLASH_COLOR : ZONE_BASE);
    zoneHighlight.setColor(flashOn ? FLASH_COLOR : ZONE_HIGHLIGHT);
    zoneShadow.setColor(flashOn ? FLASH_COLOR : ZONE_SHADOW);
    zoneLeftEdge.setColor(flashOn ? FLASH_COLOR : ZONE_EDGE);
    zoneRightEdge.setColor(flashOn ? FLASH_COLOR : ZONE_EDGE);
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
