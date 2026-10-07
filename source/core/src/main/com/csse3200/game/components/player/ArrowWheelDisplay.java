package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.inventory.InventorySlotStyle;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.EnumMap;
import java.util.Map;

/**
 * Draws the arrow wheel in the bottom-left corner of the screen while it is open, as one
 * inventory-style slot per arrow type.
 *
 * <p>Slots sit on a circle at the same angles {@link ArrowType#forDirection} uses (the first one at
 * the top, then clockwise). The wheel tells {@link ArrowWheelComponent} where its centre is on
 * screen, so the slot the pointer is aimed at is the one the wheel will select. The highlighted
 * slot gets the selected border, and slots the player has no arrows for are faded.
 */
public class ArrowWheelDisplay extends UIComponent {
  private static final float SLOT_SIZE = 80f;
  private static final float ICON_SIZE = 54f;
  // The slots are squares, so the ring is stretched vertically: the top slot sits RING_RADIUS_Y *
  // 1.5
  // above the lower two, and the lower two sit RING_RADIUS_X * 1.73 apart.
  private static final float RING_RADIUS_X = 52f;
  private static final float RING_RADIUS_Y = 62f;
  private static final float CORNER_MARGIN = 24f;
  private static final float UNAVAILABLE_ALPHA = 0.35f;

  private static final String STANDARD_ARROW_TEXTURE = "images/projectiles/arrow.png";
  private static final String FIRE_ARROW_TEXTURE = "images/projectiles/fire_arrow.png";
  private static final String ICE_ARROW_TEXTURE = "images/projectiles/ice_arrow.png";

  private final InventorySlotStyle slotStyle = new InventorySlotStyle();
  private final Map<ArrowType, Table> slots = new EnumMap<>(ArrowType.class);
  private ArrowWheelComponent wheel;
  private Group root;

  @Override
  public void create() {
    super.create();
    wheel = entity.getComponent(ArrowWheelComponent.class);

    root = new Group();
    root.setTouchable(Touchable.disabled);
    root.setVisible(false);
    for (ArrowType type : ArrowType.getWheelTypes()) {
      Table slot = createSlot(type);
      slots.put(type, slot);
      root.addActor(slot);
    }
    stage.addActor(root);
  }

  private Table createSlot(ArrowType type) {
    Table slot = new Table();
    slot.setSize(SLOT_SIZE, SLOT_SIZE);
    slot.setBackground(slotStyle.getNormalBox());

    Texture texture =
        ServiceLocator.getResourceService().getAsset(iconPathFor(type), Texture.class);
    Image icon = new Image(texture);
    icon.setScaling(Scaling.fit);
    slot.add(icon).size(ICON_SIZE);
    return slot;
  }

  private static String iconPathFor(ArrowType type) {
    return switch (type) {
      case FIRE -> FIRE_ARROW_TEXTURE;
      case ICE -> ICE_ARROW_TEXTURE;
      default -> STANDARD_ARROW_TEXTURE;
    };
  }

  @Override
  public void draw(SpriteBatch batch) {
    boolean open = wheel != null && wheel.isOpen();
    root.setVisible(open);
    if (!open) {
      return;
    }

    ArrowType[] types = ArrowType.getWheelTypes();
    float stepDegrees = 360f / types.length;

    // Slot i is i * stepDegrees clockwise from the top, matching forDirection(). Shift the whole
    // ring so its lowest and leftmost slots sit CORNER_MARGIN in from the corner.
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    for (int i = 0; i < types.length; i++) {
      float radians = (90f - i * stepDegrees) * MathUtils.degreesToRadians;
      minX = Math.min(minX, MathUtils.cos(radians) * RING_RADIUS_X);
      minY = Math.min(minY, MathUtils.sin(radians) * RING_RADIUS_Y);
    }
    float centreX = CORNER_MARGIN + SLOT_SIZE / 2f - minX;
    float centreY = CORNER_MARGIN + SLOT_SIZE / 2f - minY;

    // The input measures the pointer from the wheel's centre, in screen pixels from the top left.
    Vector2 centreOnScreen = stage.stageToScreenCoordinates(new Vector2(centreX, centreY));
    wheel.setScreenCentre(centreOnScreen.x, centreOnScreen.y);

    for (int i = 0; i < types.length; i++) {
      ArrowType type = types[i];
      Table slot = slots.get(type);

      float radians = (90f - i * stepDegrees) * MathUtils.degreesToRadians;
      slot.setPosition(
          centreX + MathUtils.cos(radians) * RING_RADIUS_X - SLOT_SIZE / 2f,
          centreY + MathUtils.sin(radians) * RING_RADIUS_Y - SLOT_SIZE / 2f);

      boolean available = wheel.isAvailable(type);
      slot.setBackground(
          available && type == wheel.getHighlighted()
              ? slotStyle.getSelectedBox()
              : slotStyle.getNormalBox());
      slot.getColor().a = available ? 1f : UNAVAILABLE_ALPHA;
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    if (root != null) {
      root.remove();
    }
    slotStyle.dispose();
  }
}
