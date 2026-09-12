package com.csse3200.game.areas.terrain.configs;

public class SpikeClusterConfig {
  int xMin;
  int xMax;
  int yMin;
  int yMax;
  float rotation;

  /**
   * Creates a new Spike Cluster from in the range from xMin to xMax and yMin to yMax
   *
   * @param xMin the minimum x coordinate this cluster starts at
   * @param xMax the maximum x coordinate this cluster ends at
   * @param yMin the minimum y coordinate this cluster starts at
   * @param yMax the maximum y coordinate this clusters ends at
   * @param rotation the rotation to apply to the hitbox and texture of this set of spikes
   */
  public SpikeClusterConfig(int xMin, int xMax, int yMin, int yMax, float rotation) {
    this.xMin = xMin;
    this.xMax = xMax;
    this.yMin = yMin;
    this.yMax = yMax;
    this.rotation = rotation;
  }
}
