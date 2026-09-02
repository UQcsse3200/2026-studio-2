package com.csse3200.game.ui.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.ui.UIComponent;

public class TextBoxDisplay extends UIComponent {

  // --- Tunables ---
  private static final int MAX_WIDTH = 200;
  private static final int PADDING = 16;
  private static final int BORDER_THICKNESS = 3;
  // change the characters per second based on the size of the given text
  private static final int CHARS_PER_SECOND = 30;
  private static final Color DEFAULT_TEXT_COLOR = Color.WHITE;

  private static NinePatchDrawable cachedBackground;

  private final float lifetime;
  private final float xPos;
  private final float yPos;
  private final Text text;
  private final TextBox parentTextBox;
  private float age = 0f;
  private float typeTimer = 0f;
  private int revealedChars = 0;
  private String fullContent = "";
  private String lastSourceContent = null;
  private Color textColor = DEFAULT_TEXT_COLOR;
  private Table table;
  private Label label;

  // make the boxes stay on screen until an input is given to cycle to the next one.
  // (make this a method or builder or whatever)
  // Make it so that the box can stay on screen forever if there is no specified input.
  public TextBoxDisplay(Text text, float lifetime, float xPos, float yPos, TextBox parentBox) {
    this.text = text;
    this.lifetime = lifetime;
    this.xPos = xPos;
    this.yPos = yPos;
    this.table = new Table();
    this.table.setPosition(xPos, yPos);
    this.parentTextBox = parentBox;
  }

  /**
   * Lazily builds (and caches) a nine-patch drawable used as the dialogue box's background +
   * border, generated with a Pixmap so it doesn't rely on any particular asset existing in the
   * skin.
   */
  private static NinePatchDrawable getBackgroundDrawable() {
    if (cachedBackground != null) {
      return cachedBackground;
    }

    int size = 16;
    int border = BORDER_THICKNESS + 2;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

    pixmap.setColor(new Color(0.08f, 0.08f, 0.1f, 0.85f));
    pixmap.fill();

    pixmap.setColor(new Color(0.85f, 0.75f, 0.4f, 1f));
    for (int i = 0; i < border; i++) {
      pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
    }

    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, border, border, border, border);
    cachedBackground = new NinePatchDrawable(patch);
    return cachedBackground;
  }

  public void setTextColor(Color color) {
    this.textColor = color == null ? DEFAULT_TEXT_COLOR : color;
    if (label != null) {
      applyTextColor();
    }
  }

  private void applyTextColor() {
    // Clone the style so we don't mutate a shared skin-wide style instance
    Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
    style.fontColor = textColor;
    label.setStyle(style);
  }

  @Override
  public void create() {
    super.create();
    table = new Table();
    table.setVisible(false);
    table.setBackground(getBackgroundDrawable());

    label = new Label("", skin);
    label.setWrap(true);
    label.setAlignment(1);
    applyTextColor();

    table.add(label).width(MAX_WIDTH).pad(PADDING);

    stage.addActor(table);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (label == null) {
      create();
    }

    String content = text == null ? "" : text.getContent();

    if (content == null || content.isEmpty()) {
      table.setVisible(false);
      return;
    }

    // New/changed text -> restart the typing animation and lifetime timer
    if (!content.equals(lastSourceContent)) {
      lastSourceContent = content;
      fullContent = content;
      revealedChars = 0;
      typeTimer = 0f;
      age = 0f;
    }

    // Reveal characters over time
    if (revealedChars < fullContent.length()) {
      typeTimer += Gdx.graphics.getDeltaTime();
      int charsToShow = (int) (typeTimer * CHARS_PER_SECOND);
      if (charsToShow > revealedChars) {
        revealedChars = Math.min(charsToShow, fullContent.length());
        label.setText(fullContent.substring(0, revealedChars));
        table.pack(); // resize box to fit the new (wrapped) text height
      }
    }

    this.age += Gdx.graphics.getDeltaTime();
    if (this.age >= lifetime) {
      table.setVisible(false);
      this.dispose();
      return;
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

    table.setPosition(x, y);
    table.setVisible(true);
    label.setVisible(true);
  }

  @Override
  public void dispose() {
    super.dispose();
    this.parentTextBox.dispose();
    if (label != null) {
      label.remove();
    }
    if (table != null) {
      table.remove();
    }
  }
}
