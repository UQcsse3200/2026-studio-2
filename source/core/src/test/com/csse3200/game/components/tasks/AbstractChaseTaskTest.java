package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.Task.Status;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * What every chase task does, on the ground or in the air: notice the player, give chase while they
 * stay close and in sight, and keep heading for wherever they are.
 */
@ExtendWith(GameExtension.class)
abstract class AbstractChaseTaskTest {
  static final int PRIORITY = 10;
  static final float VIEW_DISTANCE = 5f;
  static final float MAX_CHASE_DISTANCE = 8f;

  TaskTestWorld world;
  Entity player;
  Entity enemy;
  PhysicsMovementComponent movement;

  /** The chase task under test. */
  abstract <T extends DefaultTask & PriorityTask> T createTask(
      Entity target, int priority, float viewDistance, float maxChaseDistance);

  @BeforeEach
  void setUpWorld() {
    world = new TaskTestWorld();
    world.addGround(-50f, 50f);
    player = new Entity();
    player.setPosition(4f, 0f);
    enemy = world.enemyAt(0f, 0f);
    movement = TaskTestWorld.movementOf(enemy);
  }

  <T extends DefaultTask & PriorityTask> T task() {
    T task = createTask(player, PRIORITY, VIEW_DISTANCE, MAX_CHASE_DISTANCE);
    task.create(() -> enemy);
    return task;
  }

  /** A thin wall between the enemy and the player that blocks line of sight. */
  void putAWallBetweenThem() {
    world.addBlock(PhysicsLayer.OBSTACLE, 2f, 0.5f, 0.2f, 2f);
  }

  @Test
  void shouldNoticeAPlayerWithinViewDistance() {
    var task = task();

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldIgnoreAPlayerBeyondViewDistance() {
    var task = task();
    player.setPosition(VIEW_DISTANCE + 1f, 0f);

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldIgnoreAPlayerItCannotSee() {
    var task = task();
    putAWallBetweenThem();

    assertEquals(-1, task.getPriority(), "close enough, but there is a wall in the way");
  }

  @Test
  void shouldKeepChasingBeyondViewDistanceUpToTheChaseLimit() {
    var task = task();
    task.start();

    player.setPosition(MAX_CHASE_DISTANCE - 1f, 0f);
    assertEquals(PRIORITY, task.getPriority(), "past view distance but inside the chase limit");

    player.setPosition(MAX_CHASE_DISTANCE + 1f, 0f);
    assertEquals(-1, task.getPriority(), "past the chase limit: it gives up");
  }

  @Test
  void shouldGiveUpTheChaseWhenThePlayerDucksBehindAWall() {
    var task = task();
    task.start();
    assertEquals(PRIORITY, task.getPriority());

    putAWallBetweenThem();

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldAnnounceTheChaseAndHeadForThePlayer() {
    AtomicInteger announced = new AtomicInteger();
    enemy.getEvents().addListener("chaseStart", announced::incrementAndGet);
    var task = task();

    task.start();

    assertEquals(1, announced.get());
    assertEquals(Status.ACTIVE, task.getStatus());
    assertEquals(player.getPosition(), movement.getTarget());
    assertTrue(movement.getMoving());
  }

  @Test
  void shouldFollowThePlayerAsTheyMove() {
    var task = task();
    task.start();

    player.setPosition(6f, 0f);
    task.update();

    assertEquals(new Vector2(6f, 0f), movement.getTarget());
    assertTrue(movement.getMoving());
  }

  @Test
  void shouldKeepPushingEvenOnceItReachesWherePlayerWas() {
    var task = task();
    task.start();
    enemy.setPosition(player.getPosition()); // right on top of the player

    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
    assertTrue(movement.getMoving(), "arriving restarts the movement so the chase carries on");
  }

  @Test
  void shouldDrawItsLineOfSightToThePlayerWhenItCanSeeThem() {
    var task = task();

    task.getPriority();

    verify(world.debugRenderer).drawLine(enemy.getCenterPosition(), player.getCenterPosition());
  }

  @Test
  void shouldDrawItsLineOfSightOnlyAsFarAsTheWallWhenBlocked() {
    var task = task();
    putAWallBetweenThem();

    task.getPriority();

    verify(world.debugRenderer, never())
        .drawLine(enemy.getCenterPosition(), player.getCenterPosition());
    verify(world.debugRenderer).drawLine(any(Vector2.class), any(Vector2.class));
  }

  @Test
  void shouldStopMovingWhenStopped() {
    var task = task();
    task.start();

    task.stop();

    assertEquals(Status.INACTIVE, task.getStatus());
    assertEquals(false, movement.getMoving());
  }
}
