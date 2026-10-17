package com.csse3200.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A down-pointing arrow that bobs up and down above something, with a text label above it, to draw
 * the player's attention to it. Drawn in world units, centred above a point.
 *
 * <p>Owned by a render component, which calls {@link #create()}, {@link #update(float)}, {@link
 * #draw(SpriteBatch, float, float)} and {@link #dispose()} at the matching points of its own
 * lifecycle.
 */
public class BobbingArrowIndicator {
  private static final Logger logger = LoggerFactory.getLogger(BobbingArrowIndicator.class);

  private static final String FONT_PATH = "flat-earth/skin/fonts/PixeloidSans.fnt";
  private static final float LABEL_HEIGHT = 0.3f;
  private static final Color LABEL_COLOR = Color.WHITE;
  private static final Color LABEL_SHADOW_COLOR = new Color(0f, 0f, 0f, 0.8f);
  private static final float LABEL_SHADOW_OFFSET = 0.02f;

  private static final float ARROW_SIZE = 0.45f;
  private static final float GAP = 0.1f;
  private static final float BOB_AMPLITUDE = 0.15f;
  private static final float BOB_SPEED = 4f;

  // Pixel-art arrow pointing down, '#' is the outline and 'o' the fill.
  private static final String[] ARROW_PIXELS = {
    "#########",
    "#ooooooo#",
    "#ooooooo#",
    "#ooooooo#",
    "###ooo###",
    "  #ooo#  ",
    "   #o#   ",
    "    #    ",
  };
  private static final Color ARROW_OUTLINE_COLOR = new Color(0.15f, 0.08f, 0.03f, 1f);
  private static final Color ARROW_FILL_COLOR = new Color(1f, 0.78f, 0.1f, 1f);

  private final String label;
  private final GlyphLayout layout = new GlyphLayout();
  private Texture arrowTexture;
  private BitmapFont font;
  private float time;

  /**
   * @param label text shown above the arrow
   */
  public BobbingArrowIndicator(String label) {
    this.label = label;
  }

  /** Loads the arrow and the label font. Call once the GL context is available. */
  public void create() {
    arrowTexture = createArrowTexture();
    font = loadFont();
  }

  /**
   * Advances the bobbing animation.
   *
   * @param deltaTime seconds since the last update
   */
  public void update(float deltaTime) {
    time += deltaTime;
  }

  /**
   * Draws the arrow centred above a point, with the label above the arrow.
   *
   * @param batch batch to draw with
   * @param centreX world x the arrow and label are centred on
   * @param bottomY world y of the lowest point the arrow can reach, usually the top of the object
   */
  public void draw(SpriteBatch batch, float centreX, float bottomY) {
    // Never dips below the starting point, so it doesn't sink into the object it points at.
    float bob = (float) Math.sin(time * BOB_SPEED) * BOB_AMPLITUDE + BOB_AMPLITUDE;
    float arrowBottom = bottomY + GAP + bob;

    if (arrowTexture != null) {
      batch.draw(arrowTexture, centreX - ARROW_SIZE / 2f, arrowBottom, ARROW_SIZE, ARROW_SIZE);
    }

    if (font != null) {
      layout.setText(font, label);
      float labelX = centreX - layout.width / 2f;
      float labelTop = arrowBottom + ARROW_SIZE + GAP + layout.height;
      font.setColor(LABEL_SHADOW_COLOR);
      font.draw(batch, label, labelX + LABEL_SHADOW_OFFSET, labelTop - LABEL_SHADOW_OFFSET);
      font.setColor(LABEL_COLOR);
      font.draw(batch, label, labelX, labelTop);
    }
  }

  /** Releases the arrow texture and the font. */
  public void dispose() {
    if (arrowTexture != null) {
      arrowTexture.dispose();
      arrowTexture = null;
    }
    if (font != null) {
      font.dispose();
      font = null;
    }
  }

  /** Builds the arrow texture from ARROW_PIXELS, so it doesn't rely on an extra asset. */
  private static Texture createArrowTexture() {
    int width = ARROW_PIXELS[0].length();
    Pixmap pixmap = new Pixmap(width, ARROW_PIXELS.length, Pixmap.Format.RGBA8888);
    for (int y = 0; y < ARROW_PIXELS.length; y++) {
      for (int x = 0; x < width; x++) {
        char pixel = ARROW_PIXELS[y].charAt(x);
        if (pixel == '#') {
          pixmap.setColor(ARROW_OUTLINE_COLOR);
          pixmap.drawPixel(x, y);
        } else if (pixel == 'o') {
          pixmap.setColor(ARROW_FILL_COLOR);
          pixmap.drawPixel(x, y);
        }
      }
    }
    Texture arrow = new Texture(pixmap);
    arrow.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    pixmap.dispose();
    return arrow;
  }

  /**
   * Loads the label font and scales it to world units, or returns null (so only the arrow shows) if
   * it can't be loaded.
   */
  private static BitmapFont loadFont() {
    try {
      BitmapFont loaded = new BitmapFont(Gdx.files.internal(FONT_PATH));
      loaded.setUseIntegerPositions(false);
      loaded.getData().setScale(LABEL_HEIGHT / loaded.getCapHeight());
      loaded
          .getRegion()
          .getTexture()
          .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      return loaded;
    } catch (Exception e) {
      logger.warn("Could not load label font {}: {}", FONT_PATH, e.getMessage());
      return null;
    }
  }
}
