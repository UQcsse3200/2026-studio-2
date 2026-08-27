package com.csse3200.game.entities.configs;

/**
 * Defines a basic set of properties for the Ghoul Enemy type stored in entities config files to be
 * loaded by Enemey Factories
 */
public class GhoulConfig extends EnemyConfig {
  public int health = 1;
  public int baseAttack = 0;
  public int baseSpeed = 3;

  // Enemy behaviour config
  public float wanderRangeX;
  public float wanderRangeY;
  public float wanderWaitTime;
  public int chasePriority;
  public float viewDistance;
  public float maxChaseDistance;
}
