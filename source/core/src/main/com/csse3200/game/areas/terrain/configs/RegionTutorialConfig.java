package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.Vector2;

/**
 * A tile region the player may stand in to auto-start a tutorial, e.g. level 2's arena ledge where
 * tut2 prompts the grapple lesson. Bounds are half-open: [min, max). Coordinates use the same
 * tile-as-world convention as {@link com.csse3200.game.components.level.CheckpointComponent}, with
 * half-tile margins allowed to absorb physics resting error.
 */
public class RegionTutorialConfig {
  /** Inclusive bottom-left corner of the region, in tile/world coordinates. */
  public Vector2 min;

  /** Exclusive top-right corner of the region, in tile/world coordinates. */
  public Vector2 max;

  /** Name of the tutorial to start when the player stands inside, e.g. "tut2". */
  public String tutorial;

  public RegionTutorialConfig(Vector2 min, Vector2 max, String tutorial) {
    this.min = min;
    this.max = max;
    this.tutorial = tutorial;
  }

  /**
   * Whether the given position lies inside the region (min inclusive, max exclusive).
   *
   * @param position player position in tile/world coordinates
   * @return true when inside the region
   */
  public boolean contains(Vector2 position) {
    return position.x >= min.x && position.x < max.x && position.y >= min.y && position.y < max.y;
  }
}
