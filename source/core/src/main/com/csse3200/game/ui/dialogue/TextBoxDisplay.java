package com.csse3200.game.ui.dialogue;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.ui.UIComponent;

public class TextBoxDisplay extends UIComponent {

  private final float lifetime;
  private final float xPos;
  private final float yPos;
  private float age = 0;

  private Text text;
  private Table table;
  private Label label;

  public TextBoxDisplay(Text text, float lifetime, float xPos, float yPos) {
    this.text = text;
    this.lifetime = lifetime;
    this.xPos = xPos;
    this.yPos = yPos;
    this.table = new Table();
    this.table.setPosition(xPos, yPos);
  }

  @Override
  public void create() {
    super.create();
    table = new Table();
    table.setVisible(false);
    label = new Label("", skin);
    label.setWrap(true);
    label.setAlignment(1);
    label.setWidth(200f);
    table.add(label).width(200f).pad(20f);

    stage.addActor(table);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (label == null) {
      create();
    }

    String content = text == null ? "" : text.getContent();
    label.setText(content);

    if (content == null || content.isEmpty()) {
      table.setVisible(false);
      return;
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
    if (label != null) {
      label.remove();
    }
    if (table != null) {
      table.remove();
    }
    System.out.println("Text box disposed");
  }

  public void setText(Text newText) {
    this.text = newText;
    if (label != null) {
      label.setText(newText == null ? "" : newText.getContent());
    }
    this.age = 0f;
  }
}