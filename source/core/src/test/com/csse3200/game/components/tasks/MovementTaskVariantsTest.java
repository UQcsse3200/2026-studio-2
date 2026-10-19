package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.ai.tasks.Task.Status;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Checks the ground and flying specialisations against real movement and floor raycasts. */
@ExtendWith(GameExtension.class)
class MovementTaskVariantsTest {
  private PhysicsService physics;
  private GameTime time;
  private Random previousRandom;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    RenderService rendering = new RenderService();
    DebugRenderer debug = mock(DebugRenderer.class);
    rendering.setDebug(debug);
    ServiceLocator.registerRenderService(rendering);
    previousRandom = MathUtils.random;
    MathUtils.random = new Random(123L);
  }

  @AfterEach
  void tearDown() {
    MathUtils.random = previousRandom;
    physics.getPhysics().dispose();
  }

  @ParameterizedTest
  @CsvSource({"false, -4", "false, 4", "true, -4", "true, 4"})
  void onlyFlyingChasersMoveAcrossAnUnsupportedEdge(boolean flying, float targetX) {
    Entity enemy = mover(0f, 0f);
    Entity target = positioned(targetX, 0f);
    ChaseTask task = chase(flying, target);
    task.create(() -> enemy);
    task.start();
    task.update();
    movement(enemy).update();

    assertEquals(flying, movement(enemy).getMoving());
    if (flying) {
      assertTrue(velocity(enemy).x * targetX > 0f);
    } else {
      assertEquals(0f, velocity(enemy).len(), 0.001f);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void chasersFollowATargetChangingDirectionAndStopOnCancellation(boolean flying) {
    floor();
    Entity enemy = mover(0f, 0f);
    Entity target = positioned(4f, 0f);
    ChaseTask task = chase(flying, target);
    task.create(() -> enemy);
    task.start();
    task.update();
    movement(enemy).update();
    assertTrue(velocity(enemy).x > 0f);

    target.setPosition(-4f, 0f);
    task.update();
    movement(enemy).update();
    assertEquals(new Vector2(-4f, 0f), movement(enemy).getTarget());
    assertTrue(velocity(enemy).x < 0f);

    task.stop();
    assertFalse(movement(enemy).getMoving());
    assertEquals(0f, velocity(enemy).len(), 0.001f);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void repositionUsesHorizontalDistanceOnGroundAndFullDirectionInFlight(boolean flying) {
    floor();
    Entity enemy = mover(0f, 0f);
    Entity target = positioned(3f, 4f);
    RepositionTask task = reposition(flying, target);
    task.create(() -> enemy);
    assertEquals(-1, task.getPriority());
    enemy.getEvents().trigger("hitPlayer");
    assertEquals(10, task.getPriority());
    task.start();

    Vector2 destination = flying ? new Vector2(-3f, -4f) : new Vector2(-5f, 0f);
    assertEquals(destination.x, movement(enemy).getTarget().x, 0.001f);
    assertEquals(destination.y, movement(enemy).getTarget().y, 0.001f);
    assertEquals(new Vector2(0f, 0f), enemy.getPosition());
    assertEquals(new Vector2(3f, 4f), target.getPosition());
    task.update();
    task.update();
    movement(enemy).update();
    assertTrue(velocity(enemy).x < 0f);
    assertEquals(flying, velocity(enemy).y < 0f);
    assertEquals(Status.ACTIVE, task.getStatus());

    enemy.setPosition(destination);
    task.update();
    assertEquals(Status.FINISHED, task.getStatus());
    assertEquals(-1, task.getPriority());
    assertFalse(movement(enemy).getMoving());
    assertEquals(0f, velocity(enemy).len(), 0.001f);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void onlyGroundRepositionFinishesAtAnUnsupportedEdge(boolean flying) {
    Entity enemy = mover(0f, 0f);
    RepositionTask task = reposition(flying, positioned(3f, 4f));
    task.create(() -> enemy);
    task.start();
    movement(enemy).update();
    assertTrue(velocity(enemy).len() > 0f);
    task.update();

    assertEquals(flying ? Status.ACTIVE : Status.FINISHED, task.getStatus());
    assertEquals(flying ? 10 : -1, task.getPriority());
    assertEquals(flying, movement(enemy).getMoving());
    if (!flying) {
      assertEquals(0f, velocity(enemy).len(), 0.001f);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void onlyFlyingWanderersKeepMovingWithoutFloor(boolean flying) {
    Entity enemy = mover(0f, 0f);
    WanderTask task = wander(flying);
    task.create(() -> enemy);
    task.start();
    assertTrue(movement(enemy).getTarget().x > 0.03f);
    task.update();
    movement(enemy).update();

    assertEquals(flying, movement(enemy).getMoving());
    assertEquals(flying, velocity(enemy).x > 0f);
  }

  @Test
  void groundWanderWaitsAtAnEdgeThenResumesWhenFloorIsAvailable() {
    Entity enemy = mover(0f, 0f);
    WanderTask task = wander(false);
    task.create(() -> enemy);
    task.start();
    task.update();
    assertFalse(movement(enemy).getMoving());

    floor();
    when(time.getTime()).thenReturn(999L);
    task.update();
    assertFalse(movement(enemy).getMoving());
    when(time.getTime()).thenReturn(1000L);
    task.update();
    task.update();
    movement(enemy).update();
    assertTrue(movement(enemy).getMoving());
    assertTrue(velocity(enemy).len() > 0f);
    assertTrue(Math.abs(movement(enemy).getTarget().x) <= 5f);
    assertEquals(0f, movement(enemy).getTarget().y);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void wanderWaitsAfterArrivingThenChoosesAnotherDestination(boolean flying) {
    floor();
    Entity enemy = mover(0f, 0f);
    WanderTask task = wander(flying);
    task.create(() -> enemy);
    task.start();
    task.update();
    Vector2 firstDestination = movement(enemy).getTarget().cpy();
    enemy.setPosition(firstDestination);
    task.update();
    task.update();
    assertFalse(movement(enemy).getMoving());

    when(time.getTime()).thenReturn(1000L);
    task.update();
    task.update();
    assertTrue(movement(enemy).getMoving());
    assertTrue(firstDestination.dst(movement(enemy).getTarget()) > 0.03f);
    assertTrue(Math.abs(movement(enemy).getTarget().x) <= 5f);
  }

  private ChaseTask chase(boolean flying, Entity target) {
    return flying ? new FlyingChaseTask(target, 10, 5f, 10f) : new ChaseTask(target, 10, 5f, 10f);
  }

  private RepositionTask reposition(boolean flying, Entity target) {
    return flying ? new FlyingRepositionTask(target, 10, 5f) : new RepositionTask(target, 10, 5f);
  }

  private WanderTask wander(boolean flying) {
    return flying
        ? new FlyingWanderTask(new Vector2(10f, 0f), 1f)
        : new WanderTask(new Vector2(10f, 0f), 1f);
  }

  private Entity positioned(float x, float y) {
    Entity entity = new Entity();
    entity.setPosition(x, y);
    return entity;
  }

  private Entity mover(float x, float y) {
    Entity entity =
        positioned(x, y)
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent());
    entity.create();
    return entity;
  }

  private void floor() {
    Entity floor =
        positioned(-20f, -1f)
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND));
    floor.setScale(40f, 1f);
    floor.create();
  }

  private PhysicsMovementComponent movement(Entity entity) {
    return entity.getComponent(PhysicsMovementComponent.class);
  }

  private Vector2 velocity(Entity entity) {
    return entity.getComponent(PhysicsComponent.class).getBody().getLinearVelocity();
  }
}
