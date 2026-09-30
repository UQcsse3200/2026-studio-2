package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.projectile.ArrowType;
import java.util.EnumSet;
import java.util.Set;

/**
 * Holds the arrow wheel's state and applies the arrow type chosen from it.
 *
 * <p>Arrows are inventory items, so choosing a type on the wheel selects the inventory slot holding
 * that arrow. Firing then goes through the normal item-use path, which picks the right projectile
 * and consumes ammo. A type the player has no arrows for cannot be chosen.
 */
public class ArrowWheelComponent extends Component {
  private final Set<ArrowType> available = EnumSet.allOf(ArrowType.class);

  private InventoryComponent inventory;
  private boolean bowEquipped = true;
  private boolean open = false;
  private ArrowType highlighted;
  private ArrowType selected = ArrowType.STANDARD;

  @Override
  public void create() {
    inventory = entity.getComponent(InventoryComponent.class);
    entity.getEvents().addListener("openArrowWheel", this::open);
    entity.getEvents().addListener("closeArrowWheel", this::close);
    entity.getEvents().addListener("arrowWheelPointerMoved", this::highlightFromPointer);
    entity.getEvents().addListener("bowEquipped", this::setBowEquipped);
  }

  /** Returns whether the wheel should currently be drawn. */
  public boolean isOpen() {
    return open;
  }

  /** Returns the arrow type under the pointer, or null when closed or aimed at the centre. */
  public ArrowType getHighlighted() {
    return highlighted;
  }

  /** Returns the arrow type the bow currently fires. */
  public ArrowType getSelected() {
    return selected;
  }

  /**
   * Returns whether an arrow type can be chosen from the wheel. A type is unavailable if it has
   * been locked, or if the player has an inventory but no arrows of that type in it.
   */
  public boolean isAvailable(ArrowType type) {
    if (type == null || !available.contains(type)) {
      return false;
    }
    if (inventory == null) {
      return true;
    }
    ItemType arrowItem = arrowItemFor(type);
    return arrowItem != null && inventory.hasItem(arrowItem);
  }

  /** Locks or unlocks an arrow type. A locked type is drawn but cannot be selected. */
  public void setAvailable(ArrowType type, boolean unlocked) {
    if (type == null) {
      return;
    }
    if (unlocked) {
      available.add(type);
    } else {
      available.remove(type);
    }
  }

  /** Records whether a bow is held. Losing it mid-selection cancels the wheel. */
  public void setBowEquipped(boolean equipped) {
    bowEquipped = equipped;
    if (!equipped) {
      cancel();
    }
  }

  /** Opens the wheel if a bow is held and it is not already open. */
  boolean open() {
    if (open || !bowEquipped) {
      return false;
    }

    open = true;
    highlighted = null;
    entity.getEvents().trigger("arrowWheelOpened");
    return true;
  }

  /** Closes the wheel and applies the highlight, keeping the old type if it fails a check. */
  boolean close() {
    if (!open) {
      return false;
    }

    ArrowType candidate = highlighted;
    open = false;
    highlighted = null;
    entity.getEvents().trigger("arrowWheelClosed");

    if (candidate == null) {
      return false;
    }

    if (!bowEquipped || !isAvailable(candidate)) {
      entity.getEvents().trigger("arrowSelectionRejected", candidate);
      return false;
    }

    selected = candidate;
    selectInventorySlotFor(selected);
    entity.getEvents().trigger("arrowSelected", selected);
    return true;
  }

  /** Moves the inventory selection onto the slot holding this arrow type, if there is one. */
  private void selectInventorySlotFor(ArrowType type) {
    if (inventory == null) {
      return;
    }
    ItemType arrowItem = arrowItemFor(type);
    for (int i = 0; i < inventory.getSlotCount(); i++) {
      if (inventory.getSlot(i).getItemType() == arrowItem) {
        inventory.selectSlot(i);
        return;
      }
    }
  }

  /** Returns the inventory item that fires as this arrow type, or null if there isn't one. */
  static ItemType arrowItemFor(ArrowType type) {
    for (ItemType item : ItemType.values()) {
      if (item.isArrow() && item.toArrowType() == type) {
        return item;
      }
    }
    return null;
  }

  /** Closes the wheel without selecting anything, for interruptions such as losing the bow. */
  void cancel() {
    if (!open) {
      return;
    }

    open = false;
    highlighted = null;
    entity.getEvents().trigger("arrowWheelClosed");
  }

  /** Highlights the type the pointer is aimed at, measured from the wheel centre. */
  boolean highlightFromPointer(Vector2 offsetFromCentre) {
    if (!open) {
      return false;
    }

    ArrowType pointedAt = ArrowType.forDirection(offsetFromCentre);
    if (pointedAt == highlighted) {
      return false;
    }

    highlighted = pointedAt;
    if (pointedAt != null) {
      entity.getEvents().trigger("arrowHighlighted", pointedAt);
    }
    return true;
  }
}
