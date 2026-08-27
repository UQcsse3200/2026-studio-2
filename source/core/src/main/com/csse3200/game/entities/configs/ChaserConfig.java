package com.csse3200.game.entities.configs;

/**
 * Defines a basic set of properties for a basic enemy type
 */
public class ChaserConfig {
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
