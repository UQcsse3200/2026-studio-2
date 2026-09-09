package com.csse3200.game.components.minigames.spinthewheel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Stores and calculates the wheel's logic */
public class WheelLogic {
  private final List<WheelItem> items = new ArrayList<>();
  private final Random random;
  private int winningIndex = -1;

  public WheelLogic(List<WheelItem> source) {
    this(source, new Random());
  }

  /**
   * Creates a wheel with a supplied source and new randomness so tests can seed it
   *
   * @param source item labels mapped to their value
   * @param random the randomness used to pick a winner
   */
  WheelLogic(List<WheelItem> source, Random random) {
    if (source.isEmpty()) {
      throw new IllegalArgumentException("Wheel needs at least one item");
    }

    this.random = random;
    this.items.addAll(source);
  }

  /**
   * Uses random to randomly select an item from the wheel
   *
   * @return winner the item that is chosen from the wheel
   */
  public WheelItem spin() {
    winningIndex = random.nextInt(items.size());
    return items.get(winningIndex);
  }

  /**
   * Gets the items within the wheel.
   *
   * @return items the list of items
   */
  public List<WheelItem> getItems() {
    return List.copyOf(items);
  }

  /**
   * Gets the winning angle in degrees for the screen to display accordingly
   *
   * @return the angle of the winningIndex
   */
  public float getWinningAngle() {
    if (winningIndex == -1) {
      throw new IllegalStateException("Wheel needs to be spun first");
    }
    float seg = 360f / items.size();
    return (seg * winningIndex) + seg / 2;
  }

  /**
   * Calculates the rotation the wheel should finish at so the winning segment sits under the
   * pointer.
   *
   * @param currentRotation the wheel's current rotation in degrees
   * @param pointerAngle the angle the pointer sits at in degrees
   * @param fullTurns whole extra rotations to spin through before landing
   * @return the absolute rotation in degrees to animate the wheel to
   */
  public float getTargetRotation(float currentRotation, float pointerAngle, int fullTurns) {
    if (fullTurns < 0) {
      throw new IllegalArgumentException("Wheel cannot spin a negative number of turns");
    }

    float landing = pointerAngle - getWinningAngle();
    float delta = wrap360(landing - currentRotation);
    return currentRotation + delta + (fullTurns * 360f);
  }

  /**
   * Wraps an angle into the range [0, 360).
   *
   * @param degrees the angle to wrap
   * @return the equivalent angle between 0 and 360
   */
  private static float wrap360(float degrees) {
    return ((degrees % 360f) + 360f) % 360f;
  }
}
