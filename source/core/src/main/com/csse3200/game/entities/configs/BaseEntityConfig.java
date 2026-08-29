package com.csse3200.game.entities.configs;

/**
 * Defines a basic set of properties stored in entities config files to be loaded by Entity
 * Factories.
 */
public class BaseEntityConfig {
  public int health = 1;
  public int baseAttack = 0;
  public int baseSpeed = 3;

  // Values for the entity's behaviour
  public float wanderRangeX;
  public float wanderRangeY;
  public float wanderWaitTime;
  public int chasePriority;
  public float viewDistance;
  public float maxChaseDistance;
}
