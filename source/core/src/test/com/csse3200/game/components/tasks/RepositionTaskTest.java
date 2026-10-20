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

/** A ground enemy backing off after it hits the player: along the floor, and never off a ledge. */
@ExtendWith(GameExtension.class)
class RepositionTaskTest {
  private static final int PRIORITY = 7;
  private static final float DISTANCE = 4f;

  private TaskTestWorld world;
  private Entity player;
  private Entity enemy;
  private PhysicsMovementComponent movement;
  private RepositionTask task;

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld();
    world.addGround(-50f, 50f);
    player = new Entity();
    player.setPosition(3f, 0f);
    enemy = world.enemyAt(5f, 0f);
    movement = TaskTestWorld.movementOf(enemy);
    task = new RepositionTask(player, PRIORITY, DISTANCE, "ground");
    task.create(() -> enemy);
  }

  private void getHitByTheEnemy() {
    enemy.getEvents().trigger("hitPlayer");
  }

  @Test
  void shouldNotWantToRunUntilItHasHitThePlayer() {
    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldWantToRunOnceItHasHitThePlayer() {
    getHitByTheEnemy();

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldKeepItsPriorityWhileRunning() {
    task.start();

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldClearTheRequestOnceStarted() {
    getHitByTheEnemy();
    task.start();
    task.stop(); // no longer active

    assertEquals(-1, task.getPriority(), "the request was used up by starting");
  }

  @Test
  void shouldBackAwayFromThePlayerAlongTheFloor() {
    player.setPosition(3f, 4f); // up and to the left: the height must not tilt the retreat

    task.start();

    assertEquals(new Vector2(5f + DISTANCE, 0f), movement.getTarget());
    assertTrue(movement.getMoving());
  }

  @Test
  void shouldBackAwayLeftWhenThePlayerIsOnTheRight() {
    player.setPosition(8f, 0f);

    task.start();

    assertEquals(new Vector2(5f - DISTANCE, 0f), movement.getTarget());
  }

  @Test
  void shouldGoNowhereWhenDirectlyBelowOrAboveThePlayer() {
    player.setPosition(5f, 6f);

    task.start();

    assertEquals(new Vector2(5f, 0f), movement.getTarget(), "there is no side to back away to");
  }

  @Test
  void shouldKeepMovingWhileThereIsFloorAhead() {
    task.start();

    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
    assertTrue(movement.getMoving());
  }

  @Test
  void shouldFinishOnceItHasReachedItsSpot() {
    task.start();
    enemy.setPosition(5f + DISTANCE, 0f);

    task.update();

    assertEquals(Status.FINISHED, task.getStatus());
    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldGiveUpWhenStuckAgainstSomething() {
    task.start();

    world.advance(501);
    task.update();

    assertEquals(Status.FINISHED, task.getStatus());
  }

  @Test
  void shouldStopAtALedgeInsteadOfBackingOffIt() {
    var edgeWorld = new TaskTestWorld(); // a fresh world whose floor ends just ahead
    edgeWorld.addGround(-50f, 6f);
    Entity edgeEnemy = edgeWorld.enemyAt(5f, 0f);
    RepositionTask edgeTask = new RepositionTask(player, PRIORITY, DISTANCE, "ground");
    edgeTask.create(() -> edgeEnemy);
    edgeEnemy.getEvents().trigger("hitPlayer");
    edgeTask.start();

    edgeTask.update();

    assertEquals(Status.FINISHED, edgeTask.getStatus());
    assertFalse(TaskTestWorld.movementOf(edgeEnemy).getMoving(), "it stops dead at the edge");
    assertEquals(-1, edgeTask.getPriority(), "and no longer wants to run");
  }

  @Test
  void shouldStopMovingWhenStopped() {
    task.start();

    task.stop();

    assertFalse(movement.getMoving());
  }

  @Test
  void shouldBeSafeToStopBeforeItEverStarted() {
    task.stop();

    assertEquals(Status.INACTIVE, task.getStatus());
  }
}
