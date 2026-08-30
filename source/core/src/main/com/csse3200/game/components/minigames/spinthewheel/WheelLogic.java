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
}
