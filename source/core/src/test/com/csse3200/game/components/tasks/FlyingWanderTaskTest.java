package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import org.junit.jupiter.api.Test;

/** The flying version of wandering: floor and ledges mean nothing to it. */
class FlyingWanderTaskTest extends AbstractWanderTaskTest {
  @Override
  @SuppressWarnings("unchecked")
  <T extends DefaultTask & PriorityTask> T createTask(Vector2 range, float waitSeconds) {
    return (T) new FlyingWanderTask(range, waitSeconds);
  }

  @Test
  void shouldWanderFreelyWithNoFloorAtAll() {
    var skyWorld = new TaskTestWorld(); // nothing to stand on anywhere
    var flyer = skyWorld.enemyAt(START.x, START.y + 10f);
    var task = new FlyingWanderTask(new Vector2(0f, 0f), WAIT_SECONDS);
    task.create(() -> flyer);
    task.start();
    flyer.setPosition(2f, START.y + 10f); // its spot is ahead; a ground wanderer would stop here

    task.update();

    assertTrue(TaskTestWorld.movementOf(flyer).getMoving());
  }

  @Test
  void shouldPickSpotsAboveAndBelowAsWellAsToTheSide() {
    Vector2 tallRange = new Vector2(0f, 10f);
    boolean wentUp = false;
    boolean wentDown = false;
    for (int attempt = 0; attempt < 60 && !(wentUp && wentDown); attempt++) {
      startedTask(tallRange);
      wentUp |= movement.getTarget().y > START.y + 0.5f;
      wentDown |= movement.getTarget().y < START.y - 0.5f;
    }

    assertTrue(wentUp && wentDown, "flying wanderers use the height of their range");
  }
}
