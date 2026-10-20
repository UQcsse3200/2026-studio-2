package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.Task.Status;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** How a movement task finishes, fails or carries on. */
@ExtendWith(GameExtension.class)
class MovementTaskBehaviourTest {
  private TaskTestWorld world;
  private Entity enemy;
  private PhysicsMovementComponent movement;

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld();
    enemy = world.enemyAt(0f, 0f);
    movement = TaskTestWorld.movementOf(enemy);
  }

  private MovementTask startedTask(Vector2 target) {
    MovementTask task = new MovementTask(target);
    task.create(() -> enemy);
    task.start();
    return task;
  }

  @Test
  void shouldStillBeActiveJustAfterStarting() {
    MovementTask task = startedTask(new Vector2(10f, 0f));

    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
    assertTrue(movement.getMoving());
  }

  @Test
  void shouldFailOnceStuckForMoreThanHalfASecond() {
    MovementTask task = startedTask(new Vector2(10f, 0f));

    world.advance(501);
    task.update();

    assertEquals(Status.FAILED, task.getStatus());
    assertFalse(movement.getMoving(), "a stuck enemy stops pushing");
  }

  @Test
  void shouldNotFailAtExactlyHalfASecond() {
    MovementTask task = startedTask(new Vector2(10f, 0f));

    world.advance(500);
    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
  }

  @Test
  void shouldNeverCountAsStuckWhileItKeepsMoving() {
    MovementTask task = startedTask(new Vector2(10f, 0f));

    for (int step = 1; step <= 8; step++) {
      world.advance(400); // far longer than the grace period overall
      enemy.setPosition(step * 0.5f, 0f); // but it has moved well clear of the last spot
      task.update();
      assertEquals(Status.ACTIVE, task.getStatus(), "still progressing at step " + step);
    }
  }

  @Test
  void shouldStartTheStuckClockFromTheLastTimeItMoved() {
    MovementTask task = startedTask(new Vector2(10f, 0f));
    world.advance(400);
    enemy.setPosition(1f, 0f);
    task.update(); // moved: the clock restarts here

    world.advance(400);
    task.update();
    assertEquals(Status.ACTIVE, task.getStatus(), "only 400ms since it last moved");

    world.advance(101);
    task.update();
    assertEquals(Status.FAILED, task.getStatus(), "501ms since it last moved");
  }

  @Test
  void shouldTreatTinyJitterAsNotMoving() {
    MovementTask task = startedTask(new Vector2(10f, 0f));

    world.advance(300);
    enemy.setPosition(0.01f, 0f); // under the movement threshold
    task.update();
    world.advance(300);
    task.update();

    assertEquals(Status.FAILED, task.getStatus());
  }

  @Test
  void shouldFinishWhenWithinTheStopDistance() {
    MovementTask task = startedTask(new Vector2(5f, 0f));
    enemy.setPosition(4.975f, 0f); // 0.025 away: inside the default stop distance of 0.03

    task.update();

    assertEquals(Status.FINISHED, task.getStatus());
    assertFalse(movement.getMoving());
  }

  @Test
  void shouldNotFinishJustOutsideTheStopDistance() {
    MovementTask task = startedTask(new Vector2(5f, 0f));
    enemy.setPosition(4.9f, 0f);

    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
  }

  @Test
  void shouldUseACustomStopDistance() {
    MovementTask task = new MovementTask(new Vector2(5f, 0f), 2f);
    task.create(() -> enemy);
    task.start();
    enemy.setPosition(3.1f, 0f); // 1.9 away

    task.update();

    assertEquals(Status.FINISHED, task.getStatus());
  }

  @Test
  void shouldHeadForTheNewTargetOnceItIsChanged() {
    MovementTask task = startedTask(new Vector2(5f, 0f));
    Vector2 newTarget = new Vector2(-3f, 4f);

    task.setTarget(newTarget);

    assertEquals(newTarget, movement.getTarget());
    enemy.setPosition(-3f, 4f);
    task.update();
    assertEquals(Status.FINISHED, task.getStatus(), "finishing is judged against the new target");
  }

  @Test
  void shouldStopMovingWhenStopped() {
    MovementTask task = startedTask(new Vector2(5f, 0f));
    assertTrue(movement.getMoving());

    task.stop();

    assertFalse(movement.getMoving());
    assertEquals(Status.INACTIVE, task.getStatus());
  }
}
