package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.TaskRunner;
import com.csse3200.game.components.EnemySummonComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.services.ServiceLocator;

/** AI task that allows an enemy to summon an enemy when the player is in range */
public class SummonTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float attackRange;
  private final float cooldown;

  private long lastSummonTime;
  private int currentSummonCount = 0;
  private int MAX_SUMMON_COUNT = 2;

  /**
   * Creates a summon attack task
   *
   * @param target target entity to perform the summoning
   * @param priority task priority while target is in range
   * @param attackRange maximum distance at which the enemy can fire
   * @param cooldown seconds between attacks
   */
  public SummonTask(Entity target, int priority, float attackRange, float cooldown) {
    this.target = target;
    this.priority = priority;
    this.attackRange = attackRange;
    this.cooldown = cooldown;
  }

  @Override
  public void create(TaskRunner taskRunner) {
    super.create(taskRunner);
    owner.getEntity().getEvents().addListener("summonedEnemyDied", this::summonedEnemyDied);
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  public void update() {
    long currentTime = ServiceLocator.getTimeSource().getTime();

    if (currentTime - lastSummonTime >= cooldown * 1000) {
      summonSkeleton();
      lastSummonTime = currentTime;
    }
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getPosition().dst(target.getPosition());

    long currentTime = ServiceLocator.getTimeSource().getTime();

    boolean canSummon = currentTime - lastSummonTime >= cooldown * 1000;

    if (canSummon && currentSummonCount < MAX_SUMMON_COUNT && distance <= attackRange) {
      return priority;
    }

    return -1;
  }

  private void summonSkeleton() {
    Entity necromancer = owner.getEntity();

    float x = necromancer.getPosition().x;
    float y = necromancer.getPosition().y;

    // Try positions around the Necromancer.
    float[][] offsets = {
      {1f, 0f}, // Right
      {-1f, 0f}, // Left
      {0f, 1f}, // Above
      {0f, -1f}, // Below
      {1f, 1f}, // Top-right
      {-1f, 1f}, // Top-left
      {1f, -1f}, // Bottom-right
      {-1f, -1f} // Bottom-left
    };

    for (float[] offset : offsets) {
      float spawnX = x + offset[0];
      float spawnY = y + offset[1];

      if (!isSpawnPositionOccupied(spawnX, spawnY)) {
        if (currentSummonCount < MAX_SUMMON_COUNT) {
          Entity skeletonWarrior = EnemyFactory.createSkeletonWarrior(target);
          skeletonWarrior.getComponent(EnemySummonComponent.class).setSummonedStatus(true);
          skeletonWarrior.getComponent(EnemySummonComponent.class).setSummoner(necromancer);
          skeletonWarrior.setPosition(spawnX, spawnY);
          currentSummonCount++;
          necromancer.getEvents().trigger("spawnEntity", skeletonWarrior);
          return;
        }
      }
    }

    // No available position found; skip this summon.
  }

  private boolean isSpawnPositionOccupied(float x, float y) {
    for (Entity other : ServiceLocator.getEntityService().getEntities()) {
      if (other == owner.getEntity()) {
        continue;
      }

      if (other.getComponent(com.csse3200.game.components.npc.EnemyAnimationController.class)
          == null) {
        continue;
      }

      float distance = other.getPosition().dst(x, y);

      if (distance < 1.0f) {
        return true;
      }
    }

    return false;
  }

  private void summonedEnemyDied() {
    currentSummonCount--;
  }
}
