package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * What every wandering task does, on the ground or in the air: pick spots near where it started,
 * walk to them, wait, and pick another.
 */
@ExtendWith(GameExtension.class)
abstract class AbstractWanderTaskTest {
  static final float WAIT_SECONDS = 2f;
  static final Vector2 START = new Vector2(5f, 0f);

  /** Wide but flat, so every spot is on the ground line. */
  static final Vector2 RANGE = new Vector2(6f, 0f);

  TaskTestWorld world;
  Entity enemy;
  PhysicsMovementComponent movement;

  /** The wandering task under test. */
  abstract <T extends DefaultTask & PriorityTask> T createTask(Vector2 range, float waitSeconds);

  @BeforeEach
  void setUpWorld() {
    world = new TaskTestWorld();
    world.addGround(-50f, 50f);
    enemy = world.enemyAt(START.x, START.y);
    movement = TaskTestWorld.movementOf(enemy);
  }

  <T extends DefaultTask & PriorityTask> T startedTask(Vector2 range) {
    T task = createTask(range, WAIT_SECONDS);
    task.create(() -> enemy);
    task.start();
    return task;
  }

  void assertWithinRangeOfStart(Vector2 spot, Vector2 range) {
    assertEquals(START.x, spot.x, range.x / 2f + 1e-4f, "x too far from where it started");
    assertEquals(START.y, spot.y, range.y / 2f + 1e-4f, "y too far from where it started");
  }

  @Test
  void shouldAlwaysBeLowPriority() {
    assertEquals(1, createTask(RANGE, WAIT_SECONDS).getPriority());
  }

  @Test
  void shouldAnnounceItHasStartedWandering() {
    AtomicInteger announced = new AtomicInteger();
    enemy.getEvents().addListener("wanderStart", announced::incrementAndGet);

    startedTask(RANGE);

    assertEquals(1, announced.get());
  }

  @Test
  void shouldSetOffTowardsASpotNearWhereItStarted() {
    for (int attempt = 0; attempt < 50; attempt++) {
      startedTask(RANGE);

      assertTrue(movement.getMoving());
      assertWithinRangeOfStart(movement.getTarget(), RANGE);
    }
  }

  @Test
  void shouldNeverWanderFurtherThanTheRangeAllows() {
    Vector2 range = new Vector2(2f, 0f);
    for (int attempt = 0; attempt < 50; attempt++) {
      startedTask(range);

      assertWithinRangeOfStart(movement.getTarget(), range);
    }
  }

  @Test
  void shouldStopAndWaitOnceItHasArrived() {
    var task = startedTask(RANGE);
    enemy.setPosition(movement.getTarget());

    task.update(); // notices it has arrived and finishes the move
    task.update(); // so starts waiting

    assertFalse(movement.getMoving(), "it stands still while waiting");
  }

  @Test
  void shouldKeepWaitingUntilTheWaitIsOver() {
    var task = startedTask(RANGE);
    enemy.setPosition(movement.getTarget());
    task.update();
    task.update();

    world.advance((long) (WAIT_SECONDS * 1000) - 1);
    task.update();
    task.update();

    assertFalse(movement.getMoving(), "not yet: still one millisecond short of the wait");
  }

  @Test
  void shouldSetOffForAFreshSpotAfterWaiting() {
    var task = startedTask(RANGE);
    Vector2 firstSpot = movement.getTarget().cpy();
    enemy.setPosition(firstSpot);
    task.update();
    task.update();

    world.advance((long) (WAIT_SECONDS * 1000));
    task.update(); // the wait finishes
    task.update(); // so it sets off again

    assertTrue(movement.getMoving());
    assertWithinRangeOfStart(movement.getTarget(), RANGE);
  }

  @Test
  void shouldPickDifferentSpotsOverTime() {
    var task = startedTask(RANGE);
    Vector2 firstSpot = movement.getTarget().cpy();
    boolean pickedADifferentSpot = false;

    for (int cycle = 0; cycle < 20 && !pickedADifferentSpot; cycle++) {
      enemy.setPosition(movement.getTarget());
      task.update();
      task.update();
      world.advance((long) (WAIT_SECONDS * 1000));
      task.update();
      task.update();
      pickedADifferentSpot = !movement.getTarget().epsilonEquals(firstSpot, 1e-4f);
    }

    assertTrue(pickedADifferentSpot, "wandering to the same spot every time is not wandering");
    assertNotEquals(firstSpot, movement.getTarget());
  }

  @Test
  void shouldGiveUpOnASpotItCannotReachAndWaitInstead() {
    var task = startedTask(RANGE);

    world.advance(501); // stuck against something
    task.update(); // the move fails
    task.update(); // so it waits

    assertFalse(movement.getMoving());
  }
}
