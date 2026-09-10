package com.csse3200.game.entities.configs;

import java.util.ArrayList;
import java.util.List;

public class EnemyConfig extends BaseEntityConfig {
  public float speed = 3f;
  public String behaviour = "chase";
  public String attackType = "melee";
  public float attackRange = 1.5f;

  // Values for the entity's behaviour
  public float wanderRangeX;
  public float wanderRangeY;
  public float wanderWaitTime;
  public int chasePriority;
  public float viewDistance;
  public float maxChaseDistance;
  public float maxSpeed;

  // !!! ADD COINS AND STUFF ONCE MERGED WITH ITEMS TEAM
  public List<ItemDrop> itemDrops = new ArrayList<>();
}
