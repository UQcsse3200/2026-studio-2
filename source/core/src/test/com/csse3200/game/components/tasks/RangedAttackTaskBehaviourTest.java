package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.MockServices;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Enemies that shoot: when they will, how often, and where the shot starts. */
@ExtendWith(GameExtension.class)
class RangedAttackTaskBehaviourTest {
  private static final int PRIORITY = 20;
  private static final float RANGE = 8f;
  private static final float COOLDOWN = 2f;

  private TaskTestWorld world;
  private EntityService entities;
  private Entity player;
  private Entity enemy;

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld();
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);

    MockServices.registerLighting();

    // Projectiles load a texture when built: hand out a one by one texture for any path
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resources);

    player = new Entity();
    player.setPosition(4f, 0f);
    enemy = world.enemyAt(0f, 0f);
  }

  private RangedAttackTask task(String attackType) {
    RangedAttackTask task =
        new RangedAttackTask(player, PRIORITY, RANGE, COOLDOWN, 3, 4.5f, 5f, attackType);
    task.create(() -> enemy);
    return task;
  }

  private RangedAttackTask archerTask() {
    return task("range");
  }

  private int shotsSoFar() {
    return entities.getEntities().size;
  }

  private Entity lastShot() {
    return entities.getEntities().peek();
  }

  // --- deciding to shoot ---

  @Test
  void shouldNotShootStraightAwayAtTheStartOfTheGame() {
    // The cooldown is measured from time zero, so it is still cooling down
    assertEquals(-1, archerTask().getPriority());
  }

  @Test
  void shouldWantToShootOnceTheCooldownIsOver() {
    RangedAttackTask task = archerTask();

    world.advance((long) (COOLDOWN * 1000));

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldStillBeCoolingDownOneMillisecondEarly() {
    RangedAttackTask task = archerTask();

    world.advance((long) (COOLDOWN * 1000) - 1);

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldNotShootAtAPlayerOutOfRange() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);
    player.setPosition(RANGE + 1f, 0f);

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldShootAtAPlayerExactlyAtTheEdgeOfRange() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);
    player.setPosition(RANGE, 0f);

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldMeasureRangeInAnyDirection() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);

    player.setPosition(0f, RANGE - 1f);
    assertEquals(PRIORITY, task.getPriority(), "above, in range");

    player.setPosition(0f, RANGE + 1f);
    assertEquals(-1, task.getPriority(), "above, out of range");
  }

  // --- the cooldown clock ---

  @Test
  void shouldStartTheCooldownFromWhenItFirstStartsIfItNeverFired() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);

    task.start();

    assertEquals(-1, task.getPriority(), "the clock was restarted at the first start");
    world.advance((long) (COOLDOWN * 1000));
    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldNotRestartTheCooldownOnLaterStarts() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);
    task.start(); // sets the clock to 10000
    world.advance(1500);

    task.start(); // already set: must not push the cooldown back
    world.advance(500);

    assertEquals(PRIORITY, task.getPriority(), "2000ms since the first start");
  }

  @Test
  void shouldGoBackOnCooldownAfterEachShot() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);
    assertEquals(PRIORITY, task.getPriority());

    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }
    assertEquals(-1, task.getPriority());

    world.advance((long) (COOLDOWN * 1000) - 1);
    assertEquals(-1, task.getPriority());
    world.advance(1);
    assertEquals(PRIORITY, task.getPriority());
  }

  // --- firing ---

  @Test
  void shouldFireOneProjectilePerUpdate() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);

    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }

    assertEquals(1, shotsSoFar());
  }

  @Test
  void shouldFireAgainOnTheNextUpdate() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);
    task.update();

    task.update();

    assertEquals(2, shotsSoFar());
  }

  @Test
  void shouldStartTheShotJustInFrontOfAnEnemyFacingARightHandPlayer() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);

    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }

    // Enemy centre (0.5, 0.5): 0.8 towards the player and 0.15 below
    Vector2 start = lastShot().getCenterPosition();
    assertEquals(1.3f, start.x, 0.02f);
    assertEquals(0.35f, start.y, 0.02f);
  }

  @Test
  void shouldStartTheShotOnTheLeftForAPlayerOnTheLeft() {
    player.setPosition(-4f, 0f);
    RangedAttackTask task = archerTask();
    world.advance(10_000);

    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }

    Vector2 start = lastShot().getCenterPosition();
    assertEquals(-0.3f, start.x, 0.02f, "0.8 to the left of the enemy's centre at 0.5");
    assertEquals(0.35f, start.y, 0.02f);
  }

  @Test
  void shouldTreatAPlayerExactlyLevelAsOnTheRight() {
    player.setPosition(0f, 3f); // directly above: same centre x
    RangedAttackTask task = archerTask();
    world.advance(10_000);

    task.update();

    assertEquals(1.3f, lastShot().getCenterPosition().x, 0.02f);
  }

  @Test
  void shouldFireAProjectileThatHurtsThePlayer() {
    RangedAttackTask task = archerTask();
    world.advance(10_000);

    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }

    HitboxComponent hitbox = lastShot().getComponent(HitboxComponent.class);
    assertNotNull(hitbox);
    assertEquals(PhysicsLayer.ENEMY_PROJECTILE, hitbox.getFixture().getFilterData().categoryBits);
  }

  @Test
  void shouldFireNecromancerShotsWhenSummoning() {
    RangedAttackTask task = task("summon");
    world.advance(10_000);

    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }

    assertEquals(1, shotsSoFar());
    assertEquals(
        PhysicsLayer.ENEMY_PROJECTILE,
        lastShot().getComponent(HitboxComponent.class).getFixture().getFilterData().categoryBits);
  }

  @Test
  void shouldFireCalypsosOwnKindOfShot() {
    RangedAttackTask task = task("calypso");
    world.advance(10_000);

    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }

    assertEquals(1, shotsSoFar());
    assertEquals(
        PhysicsLayer.NPC,
        lastShot().getComponent(HitboxComponent.class).getFixture().getFilterData().categoryBits,
        "her shot is a different one from the archer's and the necromancer's");
  }
}
