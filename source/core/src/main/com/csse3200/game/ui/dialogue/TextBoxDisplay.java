package com.csse3200.game.ui.dialogue;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class TextBoxDisplay extends UIComponent {

  private boolean created = false;

  private Text text;
  private Table table;
  private Label label;

  public TextBoxDisplay(Text text) {
    this.text = text;
  }

  @Override
  public void create() {
    // Guard against double-creation which would register the component multiple times
    if (created) return;
    created = true;

    super.create();
    // create actors but don't set text here; draw will update label content
    table = new Table();
    table.setFillParent(true);
    table.bottom().padBottom(20f);

    label = new Label("", skin, "default");
    label.setWrap(true);
    label.setWidth(stage.getViewport().getWorldWidth() - 40f);

    table.add(label).expandX().left().pad(10f).width(label.getWidth());
    stage.addActor(table);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (label == null) {
      // If for some reason create() wasn't called, lazily initialise minimal actors
      table = new Table();
      table.setFillParent(true);
      table.bottom().padBottom(20f);
      label = new Label("", skin, "default");
      label.setWrap(true);
      table.add(label).expandX().left().pad(10f).width(stage.getViewport().getWorldWidth() - 40f);
      stage.addActor(table);
    }

    // Update the label text from the stored Text object
    String content = text == null ? "" : text.getContent();
    label.setText(content);
    label.setVisible(true);
    table.setVisible(true);
  }

  @Override
  public void dispose() {
    if (!created) return;
    created = false;

    super.dispose();
    if (label != null) label.remove();
    if (table != null) table.remove();
  }

  public void setText(Text newText) {
    this.text = newText;
    if (label != null) {
      label.setText(newText == null ? "" : newText.getContent());
    }
  }
}