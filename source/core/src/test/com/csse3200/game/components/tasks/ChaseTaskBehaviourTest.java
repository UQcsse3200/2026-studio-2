package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

/** The ground version of chasing: it stops at a ledge rather than follow the player off it. */
class ChaseTaskBehaviourTest extends AbstractChaseTaskTest {
  @Override
  @SuppressWarnings("unchecked")
  <T extends DefaultTask & PriorityTask> T createTask(
      Entity target, int priority, float viewDistance, float maxChaseDistance) {
    return (T) new ChaseTask(target, priority, viewDistance, maxChaseDistance);
  }

  /** An enemy on a floor that ends at x = 1, just ahead of it, with the same player as ever. */
  private Entity enemyAtALedge(ChaseTask task) {
    var edgeWorld = new TaskTestWorld();
    edgeWorld.addGround(-50f, 1f);
    Entity edgeEnemy = edgeWorld.enemyAt(0f, 0f);
    task.create(() -> edgeEnemy);
    return edgeEnemy;
  }

  @Test
  void shouldHoldItsGroundAtALedgeAheadOnTheRight() {
    ChaseTask task = new ChaseTask(player, PRIORITY, VIEW_DISTANCE, MAX_CHASE_DISTANCE);
    Entity edgeEnemy = enemyAtALedge(task);
    task.start();

    player.setPosition(6f, 0f);
    task.update();

    assertFalse(TaskTestWorld.movementOf(edgeEnemy).getMoving(), "it will not step off the edge");
    assertEquals(
        new Vector2(4f, 0f),
        TaskTestWorld.movementOf(edgeEnemy).getTarget(),
        "and does not even update its aim while held");
  }

  @Test
  void shouldHoldItsGroundAtALedgeAheadOnTheLeft() {
    var edgeWorld = new TaskTestWorld();
    edgeWorld.addGround(4.9f, 50f); // nothing under the probe at x = 5 - 0.3
    Entity edgeEnemy = edgeWorld.enemyAt(5f, 0f);
    ChaseTask task = new ChaseTask(player, PRIORITY, VIEW_DISTANCE, MAX_CHASE_DISTANCE);
    task.create(() -> edgeEnemy);
    player.setPosition(2f, 0f); // to the left
    task.start();

    task.update();

    assertFalse(TaskTestWorld.movementOf(edgeEnemy).getMoving());
  }

  @Test
  void shouldChaseOverSolidFloor() {
    ChaseTask task = new ChaseTask(player, PRIORITY, VIEW_DISTANCE, MAX_CHASE_DISTANCE);
    task.create(() -> enemy);
    task.start();

    player.setPosition(6f, 0f);
    task.update();

    assertTrue(movement.getMoving());
    assertEquals(new Vector2(6f, 0f), movement.getTarget());
  }

  @Test
  void shouldGoOnceThePlayerIsOnTheOtherSideOfTheLedgeToo() {
    // The ledge only matters in the direction it is travelling: turning round clears it
    ChaseTask task = new ChaseTask(player, PRIORITY, VIEW_DISTANCE, MAX_CHASE_DISTANCE);
    Entity edgeEnemy = enemyAtALedge(task);
    task.start();
    player.setPosition(-3f, 0f); // behind it now, over floor

    task.update();

    assertTrue(TaskTestWorld.movementOf(edgeEnemy).getMoving());
  }
}
