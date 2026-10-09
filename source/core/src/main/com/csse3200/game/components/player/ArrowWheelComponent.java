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
  private ItemType previousInventorySelection;
  private boolean selectedArrowDepleted;
  private boolean bowEquipped = true;
  private boolean open = false;
  private ArrowType highlighted;
  private ArrowType selected = ArrowType.STANDARD;
  private final Vector2 screenCentre = new Vector2();
  private boolean hasScreenCentre = false;

  @Override
  public void create() {
    inventory = entity.getComponent(InventoryComponent.class);
    previousInventorySelection = inventory == null ? null : inventory.getSelectedItem();
    entity.getEvents().addListener("openArrowWheel", this::open);
    entity.getEvents().addListener("closeArrowWheel", this::close);
    entity.getEvents().addListener("arrowWheelPointerMoved", this::highlightFromPointer);
    entity.getEvents().addListener("bowEquipped", this::setBowEquipped);
    entity.getEvents().addListener("inventorySelectionChanged", this::syncInventorySelection);
    // Quantity changes may require selecting another slot. Do this on a different event so
    // selectSlot never republishes inventorySelectionChanged inside its own dispatch.
    entity.getEvents().addListener("inventoryChanged", this::handleInventoryChanged);
  }

  /** Mirrors hotbar arrow choices without changing a deliberately selected potion or rope. */
  private void syncInventorySelection() {
    if (inventory == null) {
      return;
    }
    if (previousInventorySelection != null
        && previousInventorySelection.isArrow()
        && isWheelType(previousInventorySelection.toArrowType())
        && !inventory.hasItem(previousInventorySelection)) {
      selectedArrowDepleted = true;
    }
    ItemType item = inventory.getSelectedItem();
    previousInventorySelection = item;
    if (item == null || !item.isArrow()) {
      return;
    }
    ArrowType type = item.toArrowType();
    if (isAvailable(type) && selected != type) {
      selected = type;
      entity.getEvents().trigger("arrowSelected", selected);
    }
  }

  private void handleInventoryChanged() {
    if (selectedArrowDepleted) {
      selectedArrowDepleted = false;
      selectNextAvailable();
    }
  }

  /** Returns whether the wheel should currently be drawn. */
  public boolean isOpen() {
    return open;
  }

  /**
   * Records where the wheel is drawn, so the pointer can be measured from its centre rather than
   * the screen's.
   *
   * @param x centre in screen pixels from the left edge
   * @param yFromTop centre in screen pixels from the top edge
   */
  public void setScreenCentre(float x, float yFromTop) {
    screenCentre.set(x, yFromTop);
    hasScreenCentre = true;
  }

  /**
   * @return the wheel's centre in screen pixels (x from the left, y from the top), or null if the
   *     wheel hasn't been placed on screen
   */
  public Vector2 getScreenCentre() {
    return hasScreenCentre ? screenCentre : null;
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
    if (type == null || !isWheelType(type) || !available.contains(type)) {
      return false;
    }
    if (inventory == null) {
      return true;
    }
    ItemType arrowItem = arrowItemFor(type);
    return arrowItem != null && inventory.hasItem(arrowItem);
  }

  /** Only types displayed on the wheel may be selected or unlocked. */
  private static boolean isWheelType(ArrowType type) {
    for (ArrowType wheelType : ArrowType.getWheelTypes()) {
      if (wheelType == type) {
        return true;
      }
    }
    return false;
  }

  /** Locks or unlocks an arrow type. A locked type is drawn but cannot be selected. */
  public void setAvailable(ArrowType type, boolean unlocked) {
    if (type == null || !isWheelType(type)) {
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

  /**
   * Switches to an arrow type the player still has, for when the selected one runs out. The
   * inventory empties the slot and moves the selection on by itself, but it can land on a potion,
   * which leaves the wheel showing an arrow the player can no longer fire.
   *
   * <p>Keeps the current type while it is still available, and leaves it alone if the player has no
   * arrows at all.
   *
   * @return the type now selected, or null if the player has no arrows at all
   */
  ArrowType selectNextAvailable() {
    if (isAvailable(selected)) {
      return selected;
    }

    for (ArrowType type : ArrowType.getWheelTypes()) {
      if (isAvailable(type)) {
        selected = type;
        selectInventorySlotFor(selected);
        entity.getEvents().trigger("arrowSelected", selected);
        return selected;
      }
    }

    return null;
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
