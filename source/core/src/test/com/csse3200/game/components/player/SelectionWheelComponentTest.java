package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SelectionWheelComponentTest {

  // ---------
  // Open and close state
  // ---------

  @Test
  void shouldStartClosed() {
    SelectionWheelComponent wheel = createWheel();

    assertFalse(wheel.isOpen());
    assertNull(wheel.getHighlightedSlot());
    assertNull(wheel.getEquippedSlot());
  }

  @Test
  void shouldOpenRequestedWheel() {
    SelectionWheelComponent wheel = createWheel();

    assertTrue(wheel.openWheel(WheelType.CONSUMABLE));

    assertTrue(wheel.isOpen());
    assertEquals(WheelType.CONSUMABLE, wheel.getActiveWheel());
  }

  @Test
  void shouldTriggerWheelOpenedEvent() {
    SelectionWheelComponent wheel = createWheel();
    WheelType[] opened = {null};
    wheel.getEntity().getEvents().addListener("wheelOpened", (WheelType t) -> opened[0] = t);

    wheel.openWheel(WheelType.WEAPON);

    assertEquals(WheelType.WEAPON, opened[0]);
  }

  @Test
  void shouldIgnoreOpenWhileAlreadyOpen() {
    SelectionWheelComponent wheel = createWheel();
    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.MELEE);

    assertFalse(wheel.openWheel(WheelType.CONSUMABLE));

    // The in-progress selection must survive a repeated key-down.
    assertEquals(WheelType.WEAPON, wheel.getActiveWheel());
    assertEquals(WheelSlot.MELEE, wheel.getHighlightedSlot());
  }

  @Test
  void shouldIgnoreCloseWhileAlreadyClosed() {
    SelectionWheelComponent wheel = createWheel();

    assertFalse(wheel.closeWheel());
  }

  @Test
  void shouldTriggerWheelClosedEvent() {
    SelectionWheelComponent wheel = createWheel();
    boolean[] closed = {false};
    wheel.getEntity().getEvents().addListener("wheelClosed", () -> closed[0] = true);

    wheel.openWheel(WheelType.WEAPON);
    wheel.closeWheel();

    assertTrue(closed[0]);
    assertFalse(wheel.isOpen());
  }

  // ---------
  // Highlighting
  // ---------

  @Test
  void shouldNotHighlightWhileClosed() {
    SelectionWheelComponent wheel = createWheel();

    assertFalse(wheel.highlight(WheelSlot.HEAVY));
    assertNull(wheel.getHighlightedSlot());
  }

  @Test
  void shouldTriggerSlotHighlightedEvent() {
    SelectionWheelComponent wheel = createWheel();
    WheelSlot[] highlighted = {null};
    wheel
        .getEntity()
        .getEvents()
        .addListener("slotHighlighted", (WheelSlot s) -> highlighted[0] = s);

    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.SIDE);

    assertEquals(WheelSlot.SIDE, highlighted[0]);
  }

  @Test
  void shouldIgnoreRepeatedHighlightOfSameSlot() {
    SelectionWheelComponent wheel = createWheel();
    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.LIGHT);

    assertFalse(wheel.highlight(WheelSlot.LIGHT));
  }

  @Test
  void shouldClearHighlightWhenWheelReopens() {
    SelectionWheelComponent wheel = createWheel();
    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.HEAVY);
    wheel.closeWheel();

    wheel.openWheel(WheelType.WEAPON);

    assertNull(wheel.getHighlightedSlot());
  }

  // ---------
  // Pointer direction mapping
  // ---------

  @Test
  void shouldMapPointerDirectionsToWedges() {
    float reach = SelectionWheelComponent.DEADZONE_RADIUS * 2f;

    assertEquals(WheelSlot.HEAVY, SelectionWheelComponent.slotForDirection(new Vector2(0f, reach)));
    assertEquals(WheelSlot.SIDE, SelectionWheelComponent.slotForDirection(new Vector2(0f, -reach)));
    assertEquals(WheelSlot.MELEE, SelectionWheelComponent.slotForDirection(new Vector2(reach, 0f)));
    assertEquals(
        WheelSlot.LIGHT, SelectionWheelComponent.slotForDirection(new Vector2(-reach, 0f)));
  }

  @Test
  void shouldIgnorePointerInsideDeadzone() {
    Vector2 nearCentre = new Vector2(SelectionWheelComponent.DEADZONE_RADIUS / 2f, 0f);

    assertNull(SelectionWheelComponent.slotForDirection(nearCentre));
    assertNull(SelectionWheelComponent.slotForDirection(null));
  }

  @Test
  void shouldHighlightFromPointerMovement() {
    SelectionWheelComponent wheel = createWheel();
    wheel.openWheel(WheelType.WEAPON);

    float reach = SelectionWheelComponent.DEADZONE_RADIUS * 2f;
    assertTrue(wheel.highlightFromPointer(new Vector2(reach, 0f)));

    assertEquals(WheelSlot.MELEE, wheel.getHighlightedSlot());
  }

  // ---------
  // Equipping
  // ---------

  @Test
  void shouldEquipHighlightedSlotOnClose() {
    SelectionWheelComponent wheel = createWheel();
    wheel.setItemInSlot(WheelType.WEAPON, WheelSlot.MELEE, new HealthPotion(1));

    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.MELEE);

    assertTrue(wheel.closeWheel());
    assertEquals(WheelSlot.MELEE, wheel.getEquippedSlot());
  }

  @Test
  void shouldTriggerWeaponEquippedEvent() {
    SelectionWheelComponent wheel = createWheel();
    wheel.setItemInSlot(WheelType.WEAPON, WheelSlot.HEAVY, new HealthPotion(1));
    WheelSlot[] equipped = {null};
    wheel.getEntity().getEvents().addListener("weaponEquipped", (WheelSlot s) -> equipped[0] = s);

    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.HEAVY);
    wheel.closeWheel();

    assertEquals(WheelSlot.HEAVY, equipped[0]);
  }

  @Test
  void shouldRejectEmptySlotAndKeepPreviousSelection() {
    SelectionWheelComponent wheel = createWheel();
    wheel.setItemInSlot(WheelType.WEAPON, WheelSlot.HEAVY, new HealthPotion(1));
    WheelSlot[] rejected = {null};
    wheel
        .getEntity()
        .getEvents()
        .addListener("selectionRejected", (WheelSlot s) -> rejected[0] = s);

    // Equip a valid slot first.
    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.HEAVY);
    wheel.closeWheel();

    // Then attempt an empty one.
    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.MELEE);

    assertFalse(wheel.closeWheel());
    assertEquals(WheelSlot.MELEE, rejected[0]);
    assertEquals(WheelSlot.HEAVY, wheel.getEquippedSlot());
  }

  @Test
  void shouldCloseWithoutEquippingWhenNothingHighlighted() {
    SelectionWheelComponent wheel = createWheel();
    wheel.setItemInSlot(WheelType.WEAPON, WheelSlot.HEAVY, new HealthPotion(1));

    wheel.openWheel(WheelType.WEAPON);

    assertFalse(wheel.closeWheel());
    assertNull(wheel.getEquippedSlot());
  }

  // ---------
  // Slot contents
  // ---------

  @Test
  void shouldReadItemFromActiveWheel() {
    SelectionWheelComponent wheel = createWheel();
    Item potion = new HealthPotion(2);
    wheel.setItemInSlot(WheelType.CONSUMABLE, WheelSlot.SIDE, potion);

    wheel.openWheel(WheelType.CONSUMABLE);

    assertSame(potion, wheel.getItemInSlot(WheelSlot.SIDE));
    assertNull(wheel.getItemInSlot(WheelSlot.HEAVY));
  }

  @Test
  void shouldKeepWheelsIndependent() {
    SelectionWheelComponent wheel = createWheel();
    wheel.setItemInSlot(WheelType.CONSUMABLE, WheelSlot.SIDE, new HealthPotion(1));

    // The same slot on the weapon wheel stays empty.
    assertNull(wheel.getItemInSlot(WheelType.WEAPON, WheelSlot.SIDE));
  }

  @Test
  void shouldClearSlotAndUnequipIt() {
    SelectionWheelComponent wheel = createWheel();
    wheel.setItemInSlot(WheelType.WEAPON, WheelSlot.LIGHT, new HealthPotion(1));
    wheel.openWheel(WheelType.WEAPON);
    wheel.highlight(WheelSlot.LIGHT);
    wheel.closeWheel();

    wheel.setItemInSlot(WheelType.WEAPON, WheelSlot.LIGHT, null);

    assertNull(wheel.getItemInSlot(WheelSlot.LIGHT));
    assertNull(wheel.getEquippedSlot());
  }

  @Test
  void shouldIgnoreNullArguments() {
    SelectionWheelComponent wheel = createWheel();

    // None of these should throw.
    wheel.setItemInSlot(null, WheelSlot.HEAVY, new HealthPotion(1));
    wheel.setItemInSlot(WheelType.WEAPON, null, new HealthPotion(1));

    assertNull(wheel.getItemInSlot(null));
    assertFalse(wheel.openWheel(null));
  }

  // ---------
  // Events wired through the entity
  // ---------

  @Test
  void shouldRespondToEntityEvents() {
    SelectionWheelComponent wheel = createWheel();
    wheel.setItemInSlot(WheelType.WEAPON, WheelSlot.MELEE, new HealthPotion(1));
    Entity player = wheel.getEntity();

    player.getEvents().trigger("openWheel", WheelType.WEAPON);
    assertTrue(wheel.isOpen());

    float reach = SelectionWheelComponent.DEADZONE_RADIUS * 2f;
    player.getEvents().trigger("wheelPointerMoved", new Vector2(reach, 0f));
    assertEquals(WheelSlot.MELEE, wheel.getHighlightedSlot());

    player.getEvents().trigger("closeWheel");
    assertFalse(wheel.isOpen());
    assertEquals(WheelSlot.MELEE, wheel.getEquippedSlot());
  }

  private SelectionWheelComponent createWheel() {
    SelectionWheelComponent wheel = new SelectionWheelComponent();
    Entity player = new Entity().addComponent(wheel);
    player.create();
    return wheel;
  }
}
