package com.csse3200.game.components.item;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.ui.UIComponent;

/** Displays an item's ID and name above its world position. */
public class ItemLabelDisplay extends UIComponent {
  private static final float VERTICAL_OFFSET = 8f;

  private final CameraComponent cameraComponent;
  private Label label;

  public ItemLabelDisplay(CameraComponent cameraComponent) {
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
    super.create();
    ItemType itemType = entity.getComponent(ItemComponent.class).getItem().getItemType();
    label = new Label("ID: " + itemType.getId() + "\n" + itemType.getDisplayName(), skin, "small");
    label.setAlignment(Align.center);
    label.pack();
    stage.addActor(label);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector3 screenPosition =
        cameraComponent
            .getCamera()
            .project(
                new Vector3(entity.getCenterPosition().x, position.y + entity.getScale().y, 0f));
    label.setPosition(screenPosition.x - label.getWidth() / 2f, screenPosition.y + VERTICAL_OFFSET);
  }

  @Override
  public void dispose() {
    super.dispose();
    if (label != null) {
      label.remove();
    }
  }
}
