package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.Task.Status;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** A flying enemy backing off after it hits the player: in any direction, with no ledges. */
@ExtendWith(GameExtension.class)
class FlyingRepositionTaskTest {
  private static final int PRIORITY = 6;
  private static final float DISTANCE = 5f;

  private TaskTestWorld world;
  private Entity player;
  private Entity enemy;
  private PhysicsMovementComponent movement;
  private FlyingRepositionTask task;

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld(); // note: no ground at all
    player = new Entity();
    player.setPosition(0f, 0f);
    enemy = world.enemyAt(3f, 4f);
    movement = TaskTestWorld.movementOf(enemy);
    task = new FlyingRepositionTask(player, PRIORITY, DISTANCE);
    task.create(() -> enemy);
  }

  @Test
  void shouldWantToRunOnlyAfterHittingThePlayer() {
    assertEquals(-1, task.getPriority());

    enemy.getEvents().trigger("hitPlayer");

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldKeepItsPriorityWhileRunningThenDropIt() {
    task.start();
    assertEquals(PRIORITY, task.getPriority());

    task.stop();
    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldBackAwayDirectlyAlongTheLineFromThePlayer() {
    task.start();

    // The player is at the origin and the enemy at (3, 4), five away: straight out another five
    assertEquals(6f, movement.getTarget().x, 1e-4f);
    assertEquals(8f, movement.getTarget().y, 1e-4f);
    assertTrue(movement.getMoving());
  }

  @Test
  void shouldBackAwayUpwardWhenThePlayerIsBelow() {
    enemy.setPosition(0f, 3f);

    task.start();

    assertEquals(new Vector2(0f, 3f + DISTANCE), movement.getTarget());
  }

  @Test
  void shouldKeepGoingWithNoFloorUnderneath() {
    task.start();

    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
    assertTrue(movement.getMoving());
  }

  @Test
  void shouldFinishWhenItArrives() {
    task.start();
    enemy.setPosition(6f, 8f);

    task.update();

    assertEquals(Status.FINISHED, task.getStatus());
    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldGiveUpWhenStuck() {
    task.start();

    world.advance(501);
    task.update();

    assertEquals(Status.FINISHED, task.getStatus());
  }

  @Test
  void shouldBeSafeToStopBeforeItEverStarted() {
    task.stop();

    assertEquals(Status.INACTIVE, task.getStatus());
  }
}
