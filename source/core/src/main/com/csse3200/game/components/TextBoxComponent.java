package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextBoxComponent extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(TextBoxComponent.class);

  /** Portrait image edge length in pixels. JPEG aspect is preserved via fit scaling. */
  private static final float PORTRAIT_SIZE = 100f;

  /**
   * Fraction of the portrait overlapping the box's top edge. Zero keeps the portrait fully above
   * the box so it can never cover the first text line; its bottom edge stays connected to the top.
   */
  private static final float PORTRAIT_OVERLAP = 0f;

  private float posX;
  private float posY;
  private int posAlign = Align.top;
  private final Color textColor;
  private final Color backgroundColour;
  private final Color borderColour;
  private final float charsPerSecond;
  private final int maxWidth;
  private final int padding;
  private final int borderThickness;
  private final int textAlignment;
  private final List<String> pages;
  private final List<String> portraitPaths;
  private final BitmapFont customFont;
  private int currentPageIndex = 0;
  private NinePatchDrawable cachedBackground;
  private float typeTimer = 0f;
  private int revealedChars = 0;
  private boolean dismissed = false;
  private boolean externallyControlled = false;
  private String fullContent = "";
  private String lastSourceContent = null;
  private Table table;
  private Label label;
  private Image portraitImage;
  private Texture portraitTexture;

  // Boxes stay on screen until TAB is pressed: first press reveals the rest of the current
  // page immediately (if it's still typing). A second press moves on to the next page, if any -
  // only once the last page has been fully shown does TAB dismiss the box entirely.

  /**
   * @param fontPath path to a bitmap font (.fnt) file, relative to assets, e.g. "fonts/scroll.fnt".
   *     Pass {@code null} to use the skin's default font.
   * @param textAlignment horizontal alignment of the text within the box, e.g. {@link Align#left},
   *     {@link Align#center}, {@link Align#right}.
   * @param portraitPaths optional per-page portrait image paths (asset-relative JPEG/PNG), parallel
   *     to {@code pages}: entry {@code i} is shown overlapping the top edge of the box while page
   *     {@code i} is displayed. Blank or missing entries mean no portrait for that page. Pass
   *     {@code null} or an empty list for no portraits.
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
      List<String> pages,
      List<String> portraitPaths) {

    this.posX = xPos;
    this.posY = yPos;
    this.textColor = textColour;
    this.backgroundColour = backgroundColour;
    this.charsPerSecond = charsPerSecond;
    this.maxWidth = maxWidth;
    this.padding = padding;
    this.borderColour = borderColour;
    this.borderThickness = borderThickness;
    this.textAlignment = textAlignment;
    this.pages = (pages == null || pages.isEmpty()) ? Collections.singletonList("") : pages;
    this.portraitPaths =
        (portraitPaths == null) ? Collections.emptyList() : new ArrayList<>(portraitPaths);
    this.customFont = loadFont(fontPath);
  }

  /** Enables cutscene-style input control instead of polling TAB while rendering. */
  public void setExternallyControlled(boolean externallyControlled) {
    this.externallyControlled = externallyControlled;
  }

  /**
   * @return whether the current page has finished revealing.
   */
  public boolean isCurrentPageComplete() {
    return revealedChars >= fullContent.length();
  }

  /**
   * @return whether the current page is the final page.
   */
  public boolean isOnLastPage() {
    return currentPageIndex >= pages.size() - 1;
  }

  /**
   * @return whether this textbox has been dismissed.
   */
  public boolean isDismissed() {
    return dismissed;
  }

  /**
   * Handles one externally supplied advance request.
   *
   * <p>The first request completes the current page, the next requests move through pages, and a
   * request on the final page dismisses the textbox.
   *
   * @return the action consumed by this request
   */
  public AdvanceResult advance() {
    if (dismissed) {
      return AdvanceResult.DISMISSED;
    }
    if (!isCurrentPageComplete()) {
      revealCurrentPage();
      return AdvanceResult.REVEALED_PAGE;
    }
    if (!isOnLastPage()) {
      currentPageIndex++;
      lastSourceContent = null;
      return AdvanceResult.NEXT_PAGE;
    }
    if (externallyControlled) {
      return AdvanceResult.DISMISSED;
    }
    dismiss();
    return AdvanceResult.DISMISSED;
  }

  /** Immediately reveals the current page without changing pages. */
  public void revealCurrentPage() {
    revealedChars = fullContent.length();
    if (label != null) {
      label.setText(fullContent);
      table.pack();
    }
  }

  /** Returns the portrait path for the given page, or null when that page has no portrait. */
  private String portraitPathForPage(int pageIndex) {
    if (pageIndex < 0 || pageIndex >= portraitPaths.size()) {
      return null;
    }
    String path = portraitPaths.get(pageIndex);
    return (path == null || path.isBlank()) ? null : path;
  }

  /**
   * Swaps the portrait image to the given page's portrait, hiding it when the page has none. The
   * previous portrait texture is disposed to avoid leaking GPU memory across page turns.
   */
  private void updatePortraitForPage(int pageIndex) {
    if (portraitTexture != null) {
      portraitTexture.dispose();
      portraitTexture = null;
    }
    String path = portraitPathForPage(pageIndex);
    if (path == null) {
      if (portraitImage != null) {
        portraitImage.setVisible(false);
      }
      return;
    }
    try {
      portraitTexture = new Texture(Gdx.files.internal(path));
    } catch (Exception e) {
      logger.error("Failed to load portrait from {}: {}", path, e.getMessage());
      portraitTexture = null;
      if (portraitImage != null) {
        portraitImage.setVisible(false);
      }
      return;
    }
    if (portraitImage == null) {
      portraitImage = new Image();
      portraitImage.setScaling(Scaling.fit);
      portraitImage.setSize(PORTRAIT_SIZE, PORTRAIT_SIZE);
      portraitImage.setVisible(false);
      stage.addActor(portraitImage);
    }
    portraitImage.setDrawable(new TextureRegionDrawable(new TextureRegion(portraitTexture)));
    // Keep the portrait directly above its own box (and below fade overlays etc.).
    if (table != null
        && table.getParent() != null
        && portraitImage.getParent() == table.getParent()) {
      table.getParent().addActorAfter(table, portraitImage);
    }
  }

  /**
   * Pins the portrait centered above the box's top edge, connected to but never covering the text.
   * Tracks the live table bounds so it follows the box as typing packs it taller and under either
   * alignment mode.
   */
  private void layoutPortrait() {
    if (portraitImage == null || portraitTexture == null) {
      return;
    }
    float x = table.getX() + (table.getWidth() - PORTRAIT_SIZE) / 2f;
    float y = table.getY() + table.getHeight() - PORTRAIT_SIZE * PORTRAIT_OVERLAP;
    portraitImage.setPosition(x, y);
    portraitImage.setVisible(table.isVisible());
  }

  /** Repositions the textbox table, e.g. to track a responsive layout each frame. */
  public void setPosition(float x, float y) {
    setPosition(x, y, Align.top);
  }

  /**
   * Repositions the textbox table with an explicit alignment so it pins the box's bottom edge above
   * another entity. The alignment survives redraws.
   */
  public void setPosition(float x, float y, int align) {
    posX = x;
    posY = y;
    posAlign = align;
    if (table != null) {
      table.setPosition(x, y, align);
    }
  }

  /** Brings the textbox table above other stage actors. */
  public void toFront() {
    if (table != null) {
      table.toFront();
    }
  }

  /** Dismisses the textbox and removes its scene2d actors. */
  public void dismiss() {
    if (dismissed) {
      return;
    }
    dismissed = true;
    if (table != null) {
      table.setVisible(false);
    }
    dispose();
  }

  /** Sets the opacity of the textbox while it remains mounted. */
  public void setOpacity(float opacity) {
    float clampedOpacity = Math.max(0f, Math.min(1f, opacity));
    if (table != null) {
      table.getColor().a = clampedOpacity;
    }
    if (label != null) {
      Color fontColor = label.getStyle().fontColor.cpy();
      fontColor.a = clampedOpacity;
      Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
      style.fontColor = fontColor;
      label.setStyle(style);
    }
  }

  public enum AdvanceResult {
    REVEALED_PAGE,
    NEXT_PAGE,
    DISMISSED
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
    pixmap.fillRectangle(paperEdge, 0, width - 2 * paperEdge, height);

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
    this.table.setPosition(posX, posY);
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
      if (portraitImage != null) {
        portraitImage.setVisible(false);
      }
      return;
    }

    if (!content.equals(this.lastSourceContent)) {
      this.lastSourceContent = content;
      this.fullContent = content;
      this.revealedChars = 0;
      this.typeTimer = 0f;
      updatePortraitForPage(this.currentPageIndex);
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
    // or dismiss the box entirely if this was the last page. Externally controlled boxes (e.g.
    // cutscenes, game-end screens) own advancement themselves, so TAB must not dismiss them.
    if (!externallyControlled && Gdx.input.isKeyJustPressed(Keys.TAB)) {
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

    float x = this.posX;
    float y = this.posY;
    if (entity != null) {
      Vector2 worldPos = entity.getCenterPosition();
      float screenWidth = Gdx.graphics.getWidth();
      float screenHeight = Gdx.graphics.getHeight();
      x = (worldPos.x / 20f) * screenWidth + 10f;
      y = (worldPos.y / 20f) * screenHeight + 18f;
    }

    // Position with the stored alignment (top by default, see setPosition).
    table.setPosition(x, y, posAlign);
    table.setVisible(true);
    label.setVisible(true);
    layoutPortrait();
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
    if (portraitImage != null) {
      portraitImage.remove();
      portraitImage = null;
    }
    if (portraitTexture != null) {
      portraitTexture.dispose();
      portraitTexture = null;
    }
    if (customFont != null) {
      customFont.dispose();
    }
  }
}
