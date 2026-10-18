package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

/** The flying version of chasing: ledges mean nothing to it. */
class FlyingChaseTaskTest extends AbstractChaseTaskTest {
  @Override
  @SuppressWarnings("unchecked")
  <T extends DefaultTask & PriorityTask> T createTask(
      Entity target, int priority, float viewDistance, float maxChaseDistance) {
    return (T) new FlyingChaseTask(target, priority, viewDistance, maxChaseDistance);
  }

  @Test
  void shouldCarryOnChasingOverAnEmptyDrop() {
    var skyWorld = new TaskTestWorld(); // no floor anywhere
    Entity flyer = skyWorld.enemyAt(0f, 10f);
    FlyingChaseTask task =
        new FlyingChaseTask(player, PRIORITY, VIEW_DISTANCE + 10f, MAX_CHASE_DISTANCE + 10f);
    task.create(() -> flyer);
    task.start();

    player.setPosition(6f, 10f);
    task.update();

    assertTrue(TaskTestWorld.movementOf(flyer).getMoving());
    assertEquals(new Vector2(6f, 10f), TaskTestWorld.movementOf(flyer).getTarget());
  }

  @Test
  void shouldFollowThePlayerUpAndDown() {
    FlyingChaseTask task = new FlyingChaseTask(player, PRIORITY, 20f, 30f);
    task.create(() -> enemy);
    task.start();

    player.setPosition(4f, 9f);
    task.update();

    assertEquals(new Vector2(4f, 9f), movement.getTarget());
  }
}
