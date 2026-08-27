package com.csse3200.game.entities.configs;

public class EnemyConfig extends BaseEntityConfig {
    public float speed = 3f;
    public String behaviour = "chase";
    public String attackType = "melee";
    public float attackRange = 1.5f;

    public float wanderRangeX;
    public float wanderRangeY;
    public float wanderWaitTime;
    public int chasePriority;
    public float viewDistance;
    public float maxChaseDistance;
}
