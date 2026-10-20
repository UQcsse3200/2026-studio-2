package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** When an enemy swings: after its wind-up, once per second, towards the player. */
@ExtendWith(GameExtension.class)
class DelayedAttackTaskBehaviourTest {
  private static final int PRIORITY = 9;
  private static final float RANGE = 4f;

  private TaskTestWorld world;
  private EntityService entities;
  private Entity player;
  private Entity enemy;

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld();
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    player = new Entity().addComponent(new CombatStatsComponent(10, 0));
    player.setPosition(2f, 0f);
    enemy = world.enemyAt(0f, 0f);
  }

  private DelayedAttackTask startedTask(float delaySeconds, int damage) {
    DelayedAttackTask task =
        new DelayedAttackTask(player, PRIORITY, RANGE, delaySeconds, damage, 0f);
    task.create(() -> enemy);
    task.start();
    return task;
  }

  private int attacksSoFar() {
    return entities.getEntities().size;
  }

  @Test
  void shouldWantToAttackAPlayerInRange() {
    DelayedAttackTask task = startedTask(0f, 2);

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldNotWantToAttackAPlayerOutOfRange() {
    DelayedAttackTask task = startedTask(0f, 2);
    player.setPosition(RANGE + 2f, 0f);

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldNeedThePlayerStrictlyInsideTheRange() {
    DelayedAttackTask task = startedTask(0f, 2);
    // Centres are compared: the enemy's is at (0.5, 0.5), so put the player exactly RANGE away
    player.setPosition(RANGE, 0f);

    assertEquals(-1, task.getPriority(), "exactly at the edge of range is out of range");
  }

  @Test
  void shouldHoldWithAnAttackThatHasNotFinishedEvenIfThePlayerStepsOut() {
    DelayedAttackTask task = startedTask(0f, 2);
    task.update(); // the swing begins
    player.setPosition(RANGE + 5f, 0f);

    assertEquals(PRIORITY, task.getPriority(), "it commits to the swing it has started");
  }

  @Test
  void shouldNotSwingBeforeItsWindUpIsOver() {
    DelayedAttackTask task = startedTask(0.5f, 2);

    task.update();
    world.advance(499);
    task.update();

    assertEquals(0, attacksSoFar());
  }

  @Test
  void shouldSwingOnceTheWindUpIsOver() {
    DelayedAttackTask task = startedTask(0.5f, 2);

    world.advance(500);
    task.update();

    assertEquals(1, attacksSoFar());
  }

  @Test
  void shouldSwingStraightAwayWithNoWindUp() {
    DelayedAttackTask task = startedTask(0f, 2);

    task.update();

    assertEquals(1, attacksSoFar());
  }

  @Test
  void shouldOnlySwingOncePerAttack() {
    DelayedAttackTask task = startedTask(0f, 2);

    task.update();
    task.update();
    world.advance(500);
    task.update();

    assertEquals(1, attacksSoFar(), "it is still the same swing for the first second");
  }

  @Test
  void shouldSwingAgainAfterTheFirstSwingEnds() {
    DelayedAttackTask task = startedTask(0f, 2);
    task.update(); // swing one

    world.advance(1000);
    task.update(); // swing one ends
    task.update(); // swing two begins

    assertEquals(2, attacksSoFar());
  }

  @Test
  void shouldWaitOutTheWindUpAgainBetweenSwings() {
    DelayedAttackTask task = startedTask(0.5f, 2);
    world.advance(500);
    task.update(); // swing one begins
    world.advance(1000);
    task.update(); // and ends, restarting the wind-up from now

    world.advance(499);
    task.update();
    assertEquals(1, attacksSoFar(), "the next wind-up is not over yet");

    world.advance(1);
    task.update();
    assertEquals(2, attacksSoFar());
  }

  @Test
  void shouldHurtForTheDamageItWasGiven() {
    DelayedAttackTask task = startedTask(0f, 7);

    task.update();

    Entity attack = entities.getEntities().get(0);
    assertEquals(7, attack.getComponent(CombatStatsComponent.class).getBaseAttack());
  }

  @Test
  void shouldHaveDefaultDamageOfTwoWithTheShortConstructor() {
    DelayedAttackTask task = new DelayedAttackTask(player, PRIORITY, RANGE);
    task.create(() -> enemy);
    task.start();

    task.update();

    Entity attack = entities.getEntities().get(0);
    assertEquals(2, attack.getComponent(CombatStatsComponent.class).getBaseAttack());
  }

  @Test
  void shouldSwingTowardsAPlayerOnTheRight() {
    player.setPosition(2f, 0f);
    DelayedAttackTask task = startedTask(0f, 2);

    task.update();

    // Offset is (enemy width + range) / 2 = 2.5 towards the player
    assertEquals(2.5f, hitboxCentreX(entities.getEntities().get(0)), 1e-3f);
  }

  @Test
  void shouldSwingTowardsAPlayerOnTheLeft() {
    player.setPosition(-2f, 0f);
    DelayedAttackTask task = startedTask(0f, 2);

    task.update();

    assertEquals(-2.5f, hitboxCentreX(entities.getEntities().get(0)), 1e-3f);
  }

  @Test
  void shouldSwingFromTheEnemysCentre() {
    DelayedAttackTask task = startedTask(0f, 2);

    task.update();

    Vector2 where = entities.getEntities().get(0).getPosition();
    assertEquals(enemy.getCenterPosition(), where);
  }

  @Test
  void shouldNoLongerHoldAnAttackOnceStopped() {
    DelayedAttackTask task = startedTask(0f, 2);
    task.update(); // mid-swing
    task.stop();
    player.setPosition(RANGE + 5f, 0f);

    assertEquals(-1, task.getPriority());
  }

  /** The middle of the attack's hit area, relative to where the attack entity is. */
  private static float hitboxCentreX(Entity attack) {
    PolygonShape shape =
        (PolygonShape) attack.getComponent(HitboxComponent.class).getFixture().getShape();
    Vector2 vertex = new Vector2();
    float total = 0f;
    for (int i = 0; i < shape.getVertexCount(); i++) {
      shape.getVertex(i, vertex);
      total += vertex.x;
    }
    assertTrue(shape.getVertexCount() > 0);
    return total / shape.getVertexCount();
  }
}
