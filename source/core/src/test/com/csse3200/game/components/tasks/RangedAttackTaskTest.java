package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.MockServices;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(GameExtension.class)
class RangedAttackTaskTest {
  private PhysicsService physics;
  private EntityService entities;
  private GameTime time;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerRenderService(new RenderService());
    MockServices.registerTextures();
    MockServices.registerLighting();
    time = mock(GameTime.class);
    when(time.getTime()).thenReturn(1000L);
    ServiceLocator.registerTimeSource(time);
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  @ParameterizedTest
  @CsvSource({"calypso, -4, 1.7", "calypso, 8, 3.3", "summon, -4, 1.7", "summon, 8, 3.3"})
  void firesConfiguredProjectileTowardsTargetFromTheFacingSide(
      String attackType, float targetX, float expectedSpawnX) {
    Entity enemy = new Entity();
    enemy.setPosition(2f, 3f);
    Entity target = new Entity();
    target.setPosition(targetX, 3f);
    RangedAttackTask task = new RangedAttackTask(target, 10, 10f, 1f, 7, 5f, 3f, attackType);
    task.create(() -> enemy);
    task.start();
    assertEquals(-1, task.getPriority());
    when(time.getTime()).thenReturn(2000L);
    assertEquals(10, task.getPriority());
    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }

    assertEquals(1, entities.getEntities().size);
    Entity projectile = entities.getEntities().first();
    assertEquals(expectedSpawnX, projectile.getCenterPosition().x, 0.001f);
    assertEquals(3.35f, projectile.getCenterPosition().y, 0.001f);
    assertEquals(7, projectile.getComponent(CombatStatsComponent.class).getBaseAttack());
    assertEquals(
        attackType.equals("calypso") ? PhysicsLayer.NPC : PhysicsLayer.ENEMY_PROJECTILE,
        projectile.getComponent(HitboxComponent.class).getLayer());
    PhysicsMovementComponent movement = projectile.getComponent(PhysicsMovementComponent.class);
    Vector2 directionToTarget =
        new Vector2(targetX + 0.5f, 3.5f).sub(projectile.getCenterPosition()).nor();

    Vector2 directionToAimPoint =
        movement.getTarget().cpy().sub(projectile.getCenterPosition()).nor();

    assertEquals(directionToTarget.x, directionToAimPoint.x, 0.01f);
    assertEquals(directionToTarget.y, directionToAimPoint.y, 0.01f);
    movement.update();
    Vector2 velocity =
        projectile.getComponent(PhysicsComponent.class).getBody().getLinearVelocity();
    assertEquals(5f, velocity.len(), 0.001f);
    assertTrue(velocity.x * (targetX - 2f) > 0f);
    assertEquals(-1, task.getPriority(), "firing starts the cooldown again");

    when(time.getDeltaTime()).thenReturn(2f);
    entities.update();
    assertTrue(entities.getEntities().contains(projectile, true));
    when(time.getDeltaTime()).thenReturn(1.1f);
    entities.update();
    assertFalse(entities.getEntities().contains(projectile, true));
  }

  @ParameterizedTest
  @ValueSource(strings = {"calypso", "summon"})
  void magicProjectilesHurtPlayersButNotOtherEnemies(String attackType) {
    Entity player = character(PhysicsLayer.PLAYER);
    Entity bystander = character(PhysicsLayer.NPC);
    Entity enemy = new Entity();
    RangedAttackTask task = new RangedAttackTask(player, 10, 10f, 1f, 7, 5f, 3f, attackType);
    task.create(() -> enemy);
    task.start();
    try (var pointLights = mockConstruction(PointLightComponent.class)) {
      task.update();
    }
    Entity projectile = entities.getEntities().peek();
    Fixture hitbox = projectile.getComponent(HitboxComponent.class).getFixture();

    projectile
        .getEvents()
        .trigger(
            "collisionStart", hitbox, bystander.getComponent(HitboxComponent.class).getFixture());
    assertEquals(20, bystander.getComponent(CombatStatsComponent.class).getHealth());
    projectile
        .getEvents()
        .trigger("collisionStart", hitbox, player.getComponent(HitboxComponent.class).getFixture());
    assertEquals(13, player.getComponent(CombatStatsComponent.class).getHealth());
  }

  private Entity character(short layer) {
    Entity character =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(layer))
            .addComponent(new CombatStatsComponent(20, 0));
    entities.register(character);
    return character;
  }
}
