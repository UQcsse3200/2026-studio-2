package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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

public class TextBoxComponent extends UIComponent {

  private final float xPos;
  private final float yPos;
  private final Color textColor;
  private final Color backgroundColour;
  private final Color borderColour;
  private final float charsPerSecond;
  private final int maxWidth;
  private final int padding;
  private final int borderThickness;
  private final String content;
  private NinePatchDrawable cachedBackground;
  private float typeTimer = 0f;
  private int revealedChars = 0;
  private boolean dismissed = false;
  private String fullContent = "";
  private String lastSourceContent = null;
  private Table table;
  private Label label;

  // Boxes stay on screen until ENTER is pressed: first press reveals the rest of the text
  // immediately (if it's still typing), second press dismisses the box entirely.
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
      String text) {

    this.xPos = xPos;
    this.yPos = yPos;
    this.textColor = textColour;
    this.backgroundColour = backgroundColour;
    this.charsPerSecond = charsPerSecond;
    this.maxWidth = maxWidth;
    this.padding = padding;
    this.borderColour = borderColour;
    this.borderThickness = borderThickness;
    this.content = text;
  }

  /**
   * Lazily builds (and caches) a nine-patch drawable used as the dialogue box's background +
   * border, generated with a Pixmap so it doesn't rely on any particular asset existing in the
   * skin.
   */
  private NinePatchDrawable getBackgroundDrawable() {
    if (cachedBackground != null) {
      return cachedBackground;
    }

    int size = 64;
    int border = this.borderThickness + 2;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

    pixmap.setColor(this.backgroundColour);
    pixmap.fill();

    pixmap.setColor(this.borderColour);
    for (int i = 0; i < border; i++) {
      pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
    }

    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, border, border, border, border);
    cachedBackground = new NinePatchDrawable(patch);
    return cachedBackground;
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
    this.table = new Table();
    this.table.setPosition(xPos, yPos);
    this.table.setVisible(false);
    this.table.setBackground(getBackgroundDrawable());

    label = new Label("", skin);
    label.setWrap(true);
    label.setAlignment(1);
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

    if (this.content == null || this.content.isEmpty()) {
      table.setVisible(false);
      return;
    }

    if (!this.content.equals(this.lastSourceContent)) {
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

    // On ENTER: skip to the full text if it's still typing, otherwise dismiss the box entirely
    if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
      if (!fullyRevealed) {
        this.revealedChars = fullContent.length();
        this.label.setText(fullContent);
        this.table.pack();
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

    table.setPosition(x, y);
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
  }
}
