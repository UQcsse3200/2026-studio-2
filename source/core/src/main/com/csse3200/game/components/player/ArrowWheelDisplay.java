package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.inventory.InventorySlotStyle;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.HashMap;
import java.util.Map;

public class ArrowWheelDisplay extends UIComponent {
  private static final float SLOT_SIZE = 80f;
  private static final float ICON_SIZE = 54f;

  private ArrowWheelComponent arrowWheel;
  private Table root;
  private final Map<ArrowType, Table> arrowTypeTables = new HashMap<>();

  private static final String STANDARD_ARROW_TEXTURE = "images/projectiles/arrow.png";
  private static final String FIRE_ARROW_TEXTURE = "images/projectiles/fire_arrow.png";
  private static final String ICE_ARROW_TEXTURE = "images/projectiles/ice_arrow.png";
  private static final String POISON_ARROW_TEXTURE = "images/projectiles/poison_arrow.png";

  private static final float PAD = 20f; // Padding from the bottom left corner of the screen

  /**
   * (non-Javadoc)
   *
   * @see com.csse3200.game.ui.UIComponent#create()
   */
  @Override
  public void create() {
    super.create();
    arrowWheel = entity.getComponent(ArrowWheelComponent.class);
    entity.getEvents().addListener("arrowWheelOpened", this::onArrowWheelOpened);
    entity.getEvents().addListener("arrowWheelClosed", this::onArrowWheelClosed);

    root = new Table();
    root.setFillParent(true);
    root.setVisible(false);
    root.left().bottom();
    root.padBottom(PAD).padLeft(PAD);

    for (ArrowType type : ArrowType.WHEEL_TYPES) {
      arrowTypeTables.put(type, createArrowTypeTable(type));
    }

    // WHEEL_TYPES is ordered top, right, bottom, left (the order forDirection expects).
    Table top = arrowTypeTables.get(ArrowType.WHEEL_TYPES[0]);
    Table right = arrowTypeTables.get(ArrowType.WHEEL_TYPES[1]);
    Table bottom = arrowTypeTables.get(ArrowType.WHEEL_TYPES[2]);
    Table left = arrowTypeTables.get(ArrowType.WHEEL_TYPES[3]);

    Table grid = new Table();
    grid.add().size(SLOT_SIZE);
    grid.add(top).size(SLOT_SIZE);
    grid.add().size(SLOT_SIZE);
    grid.row();
    grid.add(left).size(SLOT_SIZE);
    grid.add().size(SLOT_SIZE);
    grid.add(right).size(SLOT_SIZE);
    grid.row();
    grid.add().size(SLOT_SIZE);
    grid.add(bottom).size(SLOT_SIZE);
    grid.add().size(SLOT_SIZE);

    root.add(grid);
    stage.addActor(root);
  }

  /** Creates a table for an arrow type, with the appropriate icon and background. */
  private Table createArrowTypeTable(ArrowType type) {
    Table table = new Table();
    table.setBackground(InventorySlotStyle.getNormalBox());

    Texture texture;
    String texturePath;

    switch (type) {
      case FIRE -> texturePath = FIRE_ARROW_TEXTURE;
      case ICE -> texturePath = ICE_ARROW_TEXTURE;
      case POISON -> texturePath = POISON_ARROW_TEXTURE;
      default -> texturePath = STANDARD_ARROW_TEXTURE;
    }

    texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    Image image = new Image(texture);
    image.setScaling(Scaling.fit);
    table.add(image).size(ICON_SIZE);
    return table;
  }

  /** Updates the display to show the currently highlighted arrow type. */
  private void onArrowWheelOpened() {
    arrowTypeTables.forEach(
        (type, table) -> {
          table.setColor(1, 1, 1, arrowWheel.isAvailable(type) ? 1f : 0.3f);
        });

    root.setVisible(true);
    updateHighlight(null);
  }

  /** Updates the display to hide the arrow wheel when it is closed. */
  private void onArrowWheelClosed() {
    root.setVisible(false);
    updateHighlight(null);
  }

  /** Updates the display to show the currently highlighted arrow type. */
  private void updateHighlight(ArrowType highlighted) {
    arrowTypeTables.forEach(
        (type, table) ->
            table.setBackground(
                type == highlighted && arrowWheel.isAvailable(type)
                    ? InventorySlotStyle.getSelectedBox()
                    : InventorySlotStyle.getNormalBox()));
  }

  @Override
  public void draw(SpriteBatch batch) {
    if (!arrowWheel.isOpen()) {
      return;
    }

    // The wheel is centred on screen. Mouse Y points down, but forDirection expects Y up.
    float centreX = PAD + 1.5f * SLOT_SIZE;
    float centreY = PAD + 1.5f * SLOT_SIZE;

    Vector2 offset =
        new Vector2(
            Gdx.input.getX() - centreX, (Gdx.graphics.getHeight() - Gdx.input.getY()) - centreY);

    arrowWheel.highlightFromPointer(offset);
    updateHighlight(arrowWheel.getHighlighted());
  }

  @Override
  public void dispose() {
    super.dispose();
    if (root != null) {
      root.remove();
    }
  }
}
