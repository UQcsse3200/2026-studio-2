package com.csse3200.game.components.minigames.spinthewheel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Stores and calculates the wheel's logic */
public class WheelLogic {
  private final List<Map.Entry<String, Integer>> items = new ArrayList<>();
  private final Random random;
  private Map.Entry<String, Integer> winner;
  private int winningIndex = -1;

  public WheelLogic(Map<String, Integer> source) {
    this(source, new Random());
  }

  /**
   * Creates a wheel with a supplied source and new randomness so tests can seed it
   *
   * @param source item labels mapped to their value
   * @param random the randomness used to pick a winner
   */
  WheelLogic(Map<String, Integer> source, Random random) {
    if (source.isEmpty()) {
      throw new IllegalArgumentException("Wheel needs at least one item");
    }

    this.random = random;
    for (Map.Entry<String, Integer> item : source.entrySet()) {
      items.add(Map.entry(item.getKey(), item.getValue()));
    }
  }

  /**
   * Uses random to randomly select an item from the wheel
   *
   * @return winner the item that is chosen from the wheel
   */
  public Map.Entry<String, Integer> spin() {
    int index = random.nextInt(items.size());
    winner = items.get(index);
    winningIndex = index;
    return winner;
  }

  /**
   * Gets the items within the wheel.
   *
   * @return items the list of items
   */
  public List<Map.Entry<String, Integer>> getItems() {
    return List.copyOf(items);
  }

  /**
   * Gets the winning angle in degrees for the screen to display accordingly
   *
   * @return the angle of the winningIndex
   */
  public float getWinningAngle() {
    float seg = 360f / items.size();
    return seg * winningIndex;
  }
}
