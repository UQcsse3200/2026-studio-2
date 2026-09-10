package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.ui.UIComponent;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextBoxComponent extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(TextBoxComponent.class);

  private final float xPos;
  private final float yPos;
  private final Color textColor;
  private final Color backgroundColour;
  private final Color borderColour;
  private final float charsPerSecond;
  private final int maxWidth;
  private final int padding;
  private final int borderThickness;
  private final int textAlignment;
  private final List<String> pages;
  private final BitmapFont customFont;
  private int currentPageIndex = 0;
  private NinePatchDrawable cachedBackground;
  private float typeTimer = 0f;
  private int revealedChars = 0;
  private boolean dismissed = false;
  private String fullContent = "";
  private String lastSourceContent = null;
  private Table table;
  private Label label;

  // Boxes stay on screen until TAB is pressed: first press reveals the rest of the current
  // page immediately (if it's still typing). A second press moves on to the next page, if any -
  // only once the last page has been fully shown does TAB dismiss the box entirely.

  /**
   * @param fontPath path to a bitmap font (.fnt) file, relative to assets, e.g. "fonts/scroll.fnt".
   *     Pass {@code null} to use the skin's default font.
   * @param textAlignment horizontal alignment of the text within the box, e.g. {@link Align#left},
   *     {@link Align#center}, {@link Align#right}.
   */
  public TextBoxComponent(
      float xPos,
      float yPos,
      Color textColour,
      Color backgroundColour,
      Color borderColour,
      float charsPerSecond,
      int maxWidth,
      int padding,
      int borderThickness,
      String fontPath,
      int textAlignment,
      List<String> pages) {

    this.xPos = xPos;
    this.yPos = yPos;
    this.textColor = textColour;
    this.backgroundColour = backgroundColour;
    this.charsPerSecond = charsPerSecond;
    this.maxWidth = maxWidth;
    this.padding = padding;
    this.borderColour = borderColour;
    this.borderThickness = borderThickness;
    this.textAlignment = textAlignment;
    this.pages = (pages == null || pages.isEmpty()) ? Collections.singletonList("") : pages;
    this.customFont = loadFont(fontPath);
  }

  /** Loads a bitmap font from assets, or returns null (meaning "use the skin's default font"). */
  private static BitmapFont loadFont(String fontPath) {
    if (fontPath == null || fontPath.isBlank()) {
      return null;
    }
    try {
      return new BitmapFont(Gdx.files.internal(fontPath));
    } catch (Exception e) {
      logger.error("Failed to load font from {}: {}", fontPath, e.getMessage());
      return null;
    }
  }

  /**
   * Lazily builds (and caches) a nine-patch drawable that makes the box look like an unrolled
   * scroll: a parchment centre, wooden roller bars along the top and bottom (sized off {@link
   * #borderThickness}), and rounded knobs where the rollers "poke out" past the paper at each
   * corner. Generated entirely with a Pixmap so it doesn't rely on any asset existing in the skin.
   */
  private NinePatchDrawable getBackgroundDrawable() {
    if (cachedBackground != null) {
      return cachedBackground;
    }

    // Roller bands (top/bottom) and paper-edge margins (left/right) scale with borderThickness,
    // so a chunkier configured border gives a chunkier-looking scroll.
    int rodHeight = Math.max(10, this.borderThickness * 6);
    int paperEdge = Math.max(8, this.borderThickness * 4);
    int width = paperEdge * 2 + 40;
    int height = rodHeight * 2 + 24;

    Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

    // Parchment fill
    pixmap.setColor(this.backgroundColour);
    pixmap.fillRectangle(paperEdge, 0, width - 2*paperEdge, height);

    // Shading down the paper's left and right edges
    Color edgeShadow = this.backgroundColour.cpy().mul(Color.BROWN);
    pixmap.setColor(edgeShadow);

    drawTopAndBottom(pixmap, 0, width, rodHeight);
    drawTopAndBottom(pixmap, height - rodHeight, width, rodHeight);

    // Rounded knobs where the rollers end, at all four corners
    Color knobColor = this.borderColour.cpy().mul(0.8f, 0.8f, 0.8f, 1f);
    int knobRadius = Math.min(paperEdge, rodHeight) / 2;
    pixmap.setColor(knobColor);
    // top left
    pixmap.fillCircle(knobRadius, knobRadius, knobRadius);
    // top right
    pixmap.fillCircle(width - knobRadius, knobRadius, knobRadius);
    // bottom left
    pixmap.fillCircle(knobRadius, height - knobRadius, knobRadius);
    // bottom right
    pixmap.fillCircle(width - knobRadius, height - knobRadius, knobRadius);

    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, paperEdge, paperEdge, rodHeight, rodHeight);
    this.cachedBackground = new NinePatchDrawable(patch);
    return cachedBackground;
  }

  private void drawTopAndBottom(Pixmap pixmap, int y, int width, int rodHeight) {
    pixmap.setColor(this.borderColour);

    Color shadow = this.borderColour.cpy().mul(0.7f, 0.7f, 0.7f, 1f);

    pixmap.setColor(shadow);
    pixmap.drawLine(0, y + rodHeight / 2, width, y + rodHeight / 2);
  }

  private void applyTextColor() {
    // Clone the style so we don't mutate a shared skin-wide style instance
    Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
    style.fontColor = textColor;
    if (customFont != null) {
      style.font = customFont;
    }
    label.setStyle(style);
  }

  @Override
  public void create() {
    super.create();
    this.table = new Table();
    this.table.setPosition(xPos, yPos);
    this.table.setVisible(false);
    this.table.setBackground(getBackgroundDrawable());

    label = new Label("", skin);
    label.setWrap(true);
    label.setAlignment(this.textAlignment);
    applyTextColor();

    table.add(label).width(this.maxWidth).pad(this.padding);

    stage.addActor(table);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (dismissed) {
      return;
    }

    if (label == null) {
      create();
    }

    String content = this.pages.get(this.currentPageIndex);

    if (content == null || content.isEmpty()) {
      table.setVisible(false);
      return;
    }

    if (!content.equals(this.lastSourceContent)) {
      this.lastSourceContent = content;
      this.fullContent = content;
      this.revealedChars = 0;
      this.typeTimer = 0f;
    }

    boolean fullyRevealed = this.revealedChars >= this.fullContent.length();

    // Reveal characters over time
    if (!fullyRevealed) {
      this.typeTimer += Gdx.graphics.getDeltaTime();
      int charsToShow = (int) (this.typeTimer * this.charsPerSecond);
      if (charsToShow > this.revealedChars) {
        this.revealedChars = Math.min(charsToShow, this.fullContent.length());
        this.label.setText(this.fullContent.substring(0, this.revealedChars));
        this.table.pack(); // resize box to fit the new (wrapped) text height
        fullyRevealed = this.revealedChars >= this.fullContent.length();
      }
    }

    // On TAB: skip to the full page if it's still typing; otherwise move to the next page,
    // or dismiss the box entirely if this was the last page
    if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
      if (!fullyRevealed) {
        this.revealedChars = fullContent.length();
        this.label.setText(fullContent);
        this.table.pack();
      } else if (this.currentPageIndex < this.pages.size() - 1) {
        this.currentPageIndex++;
        // next frame's content-changed check (above) will reset typing state automatically
      } else {
        this.dismissed = true;
        this.table.setVisible(false);
        this.dispose();
        return;
      }
    }

    float x = this.xPos;
    float y = this.yPos;
    if (entity != null) {
      Vector2 worldPos = entity.getCenterPosition();
      float screenWidth = Gdx.graphics.getWidth();
      float screenHeight = Gdx.graphics.getHeight();
      x = (worldPos.x / 20f) * screenWidth + 10f;
      y = (worldPos.y / 20f) * screenHeight + 18f;
    }

    // alignment = 2 for top to bottom effect
    table.setPosition(x, y, 2);
    table.setVisible(true);
    label.setVisible(true);
  }

  @Override
  public void dispose() {
    super.dispose();
    if (label != null) {
      label.remove();
    }
    if (table != null) {
      table.remove();
    }
    if (customFont != null) {
      customFont.dispose();
    }
  }
}
