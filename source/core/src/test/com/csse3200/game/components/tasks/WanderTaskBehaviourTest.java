package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import org.junit.jupiter.api.Test;

/** The ground version of wandering: it will not wander off a ledge. */
class WanderTaskBehaviourTest extends AbstractWanderTaskTest {
  /** Does not wander anywhere: its spot is right where it started. */
  private static final Vector2 NO_RANGE = new Vector2(0f, 0f);

  @Override
  @SuppressWarnings("unchecked")
  <T extends DefaultTask & PriorityTask> T createTask(Vector2 range, float waitSeconds) {
    return (T) new WanderTask(range, waitSeconds);
  }

  @Test
  void shouldCarryOnWhenThereIsFloorAhead() {
    var task = startedTask(NO_RANGE); // its spot is where it started, x = 5
    enemy.setPosition(2f, 0f); // so the spot is now ahead on the right, over solid floor

    task.update();

    assertTrue(movement.getMoving());
  }

  @Test
  void shouldStopAtALedgeAheadOnTheRight() {
    var edgeWorld = new TaskTestWorld();
    edgeWorld.addGround(-50f, 3f); // the floor ends before the probe at x = 2 + 1 + 0.3
    var edgeEnemy = edgeWorld.enemyAt(START.x, START.y);
    var task = new WanderTask(NO_RANGE, WAIT_SECONDS);
    task.create(() -> edgeEnemy);
    task.start();
    edgeEnemy.setPosition(2f, 0f);

    task.update();

    assertFalse(TaskTestWorld.movementOf(edgeEnemy).getMoving());
  }

  @Test
  void shouldStopAtALedgeAheadOnTheLeft() {
    var edgeWorld = new TaskTestWorld();
    edgeWorld.addGround(7.9f, 50f); // nothing under the probe at x = 8 - 0.3
    var edgeEnemy = edgeWorld.enemyAt(START.x, START.y);
    var task = new WanderTask(NO_RANGE, WAIT_SECONDS);
    task.create(() -> edgeEnemy);
    task.start();
    edgeEnemy.setPosition(8f, 0f); // the spot at x = 5 is now to the left

    task.update();

    assertFalse(TaskTestWorld.movementOf(edgeEnemy).getMoving());
  }

  @Test
  void shouldNotCheckForALedgeWhenTheSpotIsStraightAboveOrBelow() {
    var edgeWorld = new TaskTestWorld(); // no floor anywhere: any ledge check would stop it
    var edgeEnemy = edgeWorld.enemyAt(START.x, START.y);
    var task = new WanderTask(NO_RANGE, WAIT_SECONDS); // its spot is (5, 0)
    task.create(() -> edgeEnemy);
    task.start();
    edgeEnemy.setPosition(START.x, 3f); // same x as the spot: it has no side to look to

    task.update();

    assertTrue(
        TaskTestWorld.movementOf(edgeEnemy).getMoving(),
        "with no sideways direction there is no ledge to stop at");
  }
}
