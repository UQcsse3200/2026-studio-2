package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class KeyboardPlayerInputComponentTest {
  // ---------
  // Hotbar number keys
  // ---------

  @Test
  void shouldSelectNinthHotbarSlot() {
    KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(input);
    int[] selectedSlot = {-1};
    player.getEvents().addListener("selectQuickSlot", (Integer index) -> selectedSlot[0] = index);

    assertTrue(input.keyDown(Keys.NUM_9));

    assertEquals(8, selectedSlot[0]);
  }

  // ---------
  // Selection wheel keys
  // ---------

  @Test
  void shouldOpenWeaponWheelOnTab() {
    KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(input);
    WheelType[] opened = {null};
    player.getEvents().addListener("openWheel", (WheelType wheel) -> opened[0] = wheel);

    assertTrue(input.keyDown(Keys.TAB));

    assertEquals(WheelType.WEAPON, opened[0]);
  }

  @Test
  void shouldOpenConsumableWheelOnC() {
    KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(input);
    WheelType[] opened = {null};
    player.getEvents().addListener("openWheel", (WheelType wheel) -> opened[0] = wheel);

    assertTrue(input.keyDown(Keys.C));

    assertEquals(WheelType.CONSUMABLE, opened[0]);
  }

  @Test
  void shouldCloseWheelOnKeyRelease() {
    KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(input);
    int[] closes = {0};
    player.getEvents().addListener("closeWheel", () -> closes[0]++);

    assertTrue(input.keyUp(Keys.TAB));
    assertTrue(input.keyUp(Keys.C));

    assertEquals(2, closes[0]);
  }
}
