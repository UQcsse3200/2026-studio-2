package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.Item;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the state behind the player's radial selection wheels and equips the highlighted category
 * when a wheel closes.
 *
 * <p>The player holds a key to open a wheel, moves the mouse to highlight one of the four {@link
 * WheelSlot} categories, and releases the key to equip whatever that category holds. Both the
 * weapon wheel and the consumable wheel are driven by this one component, since an entity can only
 * carry a single component of a class; {@link WheelType} selects which set of slots an open wheel
 * reads from.
 *
 * <p>This component owns state, validation and events only. Drawing the wheel belongs to the UI,
 * which reads {@link #isOpen()}, {@link #getHighlightedSlot()}, {@link #getEquippedSlot()} and
 * {@link #getItemInSlot(WheelSlot)} and reacts to the events below. Opening a wheel deliberately
 * does not slow or pause the game.
 *
 * <p>The equipped loadout is separate from the backpack grid, so this never touches {@code
 * InventoryComponent}.
 *
 * <p>Events fired on the player entity:
 *
 * <ul>
 *   <li>{@code wheelOpened} ({@link WheelType}) - a wheel became visible
 *   <li>{@code wheelClosed} - the wheel was hidden
 *   <li>{@code slotHighlighted} ({@link WheelSlot}) - the highlighted wedge changed
 *   <li>{@code weaponEquipped} ({@link WheelSlot}) - a selection was confirmed
 *   <li>{@code selectionRejected} ({@link WheelSlot}) - an empty category was chosen
 * </ul>
 */
public class SelectionWheelComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(SelectionWheelComponent.class);

  /**
   * Distance in pixels the pointer must sit from the wheel's centre before it counts as pointing at
   * a wedge. Without it the highlight flickers between categories near the middle.
   */
  public static final float DEADZONE_RADIUS = 40f;

  private static final float DEADZONE_RADIUS_SQ = DEADZONE_RADIUS * DEADZONE_RADIUS;

  private final Map<WheelType, Map<WheelSlot, Item>> contents = new EnumMap<>(WheelType.class);
  private final Map<WheelType, WheelSlot> equippedSlots = new EnumMap<>(WheelType.class);

  private boolean open = false;
  private WheelType activeWheel = WheelType.WEAPON;
  private WheelSlot highlightedSlot;

  public SelectionWheelComponent() {
    for (WheelType wheel : WheelType.values()) {
      contents.put(wheel, new EnumMap<>(WheelSlot.class));
    }
  }

  @Override
  public void create() {
    entity.getEvents().addListener("openWheel", this::openWheel);
    entity.getEvents().addListener("closeWheel", this::closeWheel);
    entity.getEvents().addListener("wheelPointerMoved", this::highlightFromPointer);
  }

  /**
   * Returns whether a wheel should currently be drawn.
   *
   * @return true while a wheel is open
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Returns which wheel the open (or most recently opened) wheel is reading from.
   *
   * @return active wheel
   */
  public WheelType getActiveWheel() {
    return activeWheel;
  }

  /**
   * Returns the category currently under the pointer.
   *
   * @return highlighted category, or null when the wheel is closed or the pointer is in the centre
   *     deadzone
   */
  public WheelSlot getHighlightedSlot() {
    return highlightedSlot;
  }

  /**
   * Returns the category equipped on the active wheel.
   *
   * @return equipped category, or null when nothing has been equipped yet
   */
  public WheelSlot getEquippedSlot() {
    return equippedSlots.get(activeWheel);
  }

  /**
   * Returns what the given wedge of the active wheel should show.
   *
   * @param slot category to read
   * @return item assigned to that category, or null when the category is empty
   */
  public Item getItemInSlot(WheelSlot slot) {
    return getItemInSlot(activeWheel, slot);
  }

  /**
   * Returns what the given wedge of a specific wheel should show.
   *
   * @param wheel wheel to read from
   * @param slot category to read
   * @return item assigned to that category, or null when the category is empty
   */
  public Item getItemInSlot(WheelType wheel, WheelSlot slot) {
    if (wheel == null || slot == null) {
      return null;
    }
    return contents.get(wheel).get(slot);
  }

  /**
   * Assigns an item to a category, or clears the category when given null.
   *
   * <p>This is how weapons and consumables reach the wheel. Weapon item types do not exist yet, so
   * until they land the weapon wheel stays empty and every selection on it is rejected.
   *
   * @param wheel wheel to assign into
   * @param slot category to fill
   * @param item item to show in that category, or null to empty it
   */
  public void setItemInSlot(WheelType wheel, WheelSlot slot, Item item) {
    if (wheel == null || slot == null) {
      return;
    }

    Map<WheelSlot, Item> wheelContents = contents.get(wheel);
    if (item == null) {
      wheelContents.remove(slot);
      if (slot == equippedSlots.get(wheel)) {
        equippedSlots.remove(wheel);
      }
    } else {
      wheelContents.put(slot, item);
    }
  }

  /**
   * Opens a wheel. Opening a wheel that is already open does nothing, so holding the key down does
   * not restart the selection.
   *
   * @param wheel wheel to open
   * @return true if a wheel was opened
   */
  boolean openWheel(WheelType wheel) {
    if (open || wheel == null) {
      return false;
    }

    open = true;
    activeWheel = wheel;
    highlightedSlot = null;
    entity.getEvents().trigger("wheelOpened", wheel);
    return true;
  }

  /**
   * Closes the open wheel and equips the highlighted category.
   *
   * <p>Closing with nothing highlighted simply hides the wheel. Closing on an empty category fires
   * {@code selectionRejected} and leaves the previous selection equipped.
   *
   * @return true if the highlighted category was equipped
   */
  boolean closeWheel() {
    if (!open) {
      return false;
    }

    WheelSlot selected = highlightedSlot;
    open = false;
    highlightedSlot = null;
    entity.getEvents().trigger("wheelClosed");

    if (selected == null) {
      return false;
    }

    if (getItemInSlot(activeWheel, selected) == null) {
      logger.debug("Rejected selection of empty {} slot on {} wheel", selected, activeWheel);
      entity.getEvents().trigger("selectionRejected", selected);
      return false;
    }

    equippedSlots.put(activeWheel, selected);
    entity.getEvents().trigger("weaponEquipped", selected);
    return true;
  }

  /**
   * Highlights the category the pointer is aimed at.
   *
   * @param offsetFromCentre pointer position relative to the wheel's centre, with y pointing up
   * @return true if the highlight changed
   */
  boolean highlightFromPointer(Vector2 offsetFromCentre) {
    return highlight(slotForDirection(offsetFromCentre));
  }

  /**
   * Highlights a category directly.
   *
   * @param slot category to highlight, or null to clear the highlight
   * @return true if the highlight changed
   */
  boolean highlight(WheelSlot slot) {
    if (!open || slot == highlightedSlot) {
      return false;
    }

    highlightedSlot = slot;
    if (slot != null) {
      entity.getEvents().trigger("slotHighlighted", slot);
    }
    return true;
  }

  /**
   * Maps a pointer direction to the wedge it falls in. The wedges are quarters centred on up,
   * right, down and left.
   *
   * @param offsetFromCentre pointer position relative to the wheel's centre, with y pointing up
   * @return category under the pointer, or null when the pointer is inside the centre deadzone
   */
  static WheelSlot slotForDirection(Vector2 offsetFromCentre) {
    if (offsetFromCentre == null || offsetFromCentre.len2() < DEADZONE_RADIUS_SQ) {
      return null;
    }

    // 0 degrees points right and angles increase anticlockwise.
    float angle = offsetFromCentre.angleDeg();
    if (angle < 45f || angle >= 315f) {
      return WheelSlot.MELEE;
    }
    if (angle < 135f) {
      return WheelSlot.HEAVY;
    }
    if (angle < 225f) {
      return WheelSlot.LIGHT;
    }
    return WheelSlot.SIDE;
  }
}
