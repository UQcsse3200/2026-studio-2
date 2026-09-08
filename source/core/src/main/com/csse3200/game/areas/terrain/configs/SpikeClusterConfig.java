package com.csse3200.game.areas.terrain.configs;

public class SpikeClusterConfig {
  int xMin;
  int xMax;
  int yMin;
  int yMax;

    /**
     * Creates a new Spike Cluster from in the range from xMin to xMax and yMin to yMax
     * @param xMin the minimum x coordinate this cluster starts at
     * @param xMax the maximum x coordinate this cluster ends at
     * @param yMin the minimum y coordinate this cluster starts at
     * @param yMax the maximum y coordinate this clusters ends at
     */
  public SpikeClusterConfig(int xMin, int xMax, int yMin, int yMax) {
    this.xMin = xMin;
    this.xMax = xMax;
    this.yMin = yMin;
    this.yMax = yMax;
  }
}
