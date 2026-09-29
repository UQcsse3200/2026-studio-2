package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerProjectileIntegrationTest {
  private PhysicsService physics;
  private EntityService entities;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    entities = new EntityService();
    ServiceLocator.registerPhysicsService(physics);
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerRenderService(mock(RenderService.class));
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  private Entity enemy() {
    Entity enemy =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new CombatStatsComponent(30, 1));
    entities.register(enemy);
    return enemy;
  }

  private void impact(Entity arrow, Entity enemy) {
    arrow
        .getEvents()
        .trigger(
            "collisionStart",
            arrow.getComponent(HitboxComponent.class).getFixture(),
            enemy.getComponent(HitboxComponent.class).getFixture());
  }

  @Test
  void shouldCreateChargedStandardArrowWithScaledVelocityAndApplyDamageOnce() {
    Vector2 position = new Vector2(2f, 3f);
    Vector2 aim = new Vector2(3f, 4f);
    Entity arrow = ProjectileFactory.createPlayerArrow(null, position, aim, 0.5f);
    entities.register(arrow);
    assertTrue(arrow.getCenterPosition().epsilonEquals(position, 0.0001f));
    assertEquals(new Vector2(3f, 4f), aim);
    assertTrue(
        arrow
            .getComponent(PhysicsComponent.class)
            .getBody()
            .getLinearVelocity()
            .epsilonEquals(5.4f, 7.2f, 0.0001f));
    Entity enemy = enemy();
    impact(arrow, enemy);
    impact(arrow, enemy);
    assertEquals(20, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertTrue(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
    entities.update();
    assertFalse(entities.getEntities().contains(arrow, true));
    assertTrue(entities.getEntities().contains(enemy, true));
  }

  @Test
  void shouldApplyFireDamageAndOneBurnEvent() {
    Entity arrow = ProjectileFactory.createFireArrow(Vector2.Zero, Vector2.X);
    entities.register(arrow);
    Entity enemy = enemy();
    List<Vector2> effects = new ArrayList<>();
    enemy
        .getEvents()
        .addListener(
            "applyBurn",
            (Float damage, Float duration) -> effects.add(new Vector2(damage, duration)));
    impact(arrow, enemy);
    impact(arrow, enemy);
    assertEquals(25, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(List.of(new Vector2(10f, 5f)), effects);
  }

  @Test
  void shouldApplyIceDamageAndOneSlowEvent() {
    Entity arrow = ProjectileFactory.createIceArrow(Vector2.Zero, Vector2.X);
    entities.register(arrow);
    Entity enemy = enemy();
    List<Vector2> effects = new ArrayList<>();
    enemy
        .getEvents()
        .addListener(
            "applySlow",
            (Float factor, Float duration) -> effects.add(new Vector2(factor, duration)));
    impact(arrow, enemy);
    impact(arrow, enemy);
    assertEquals(22, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(List.of(new Vector2(0.5f, 5f)), effects);
  }

  @Test
  void shouldScaleChargedFireAndIceLaunchSpeeds() {
    Entity fire = ProjectileFactory.createFireArrow(null, Vector2.Zero, Vector2.Y, 1.5f);
    Entity ice = ProjectileFactory.createIceArrow(null, Vector2.Zero, Vector2.Y, 0.5f);
    entities.register(fire);
    entities.register(ice);
    assertEquals(
        new Vector2(0f, 27f),
        fire.getComponent(PhysicsComponent.class).getBody().getLinearVelocity());
    assertEquals(
        new Vector2(0f, 8f),
        ice.getComponent(PhysicsComponent.class).getBody().getLinearVelocity());
  }

  @Test
  void shouldKeepGrappleSpeedIndependentOfChargeAndIgnoreEnemyImpacts() {
    Entity arrow = ProjectileFactory.createGrappleArrow(null, Vector2.Zero, Vector2.X, 0.25f);
    entities.register(arrow);
    Body body = arrow.getComponent(PhysicsComponent.class).getBody();
    assertEquals(new Vector2(22f, 0f), body.getLinearVelocity());
    assertEquals(0f, body.getGravityScale());
    Entity enemy = enemy();
    impact(arrow, enemy);
    assertEquals(30, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldRetainShooterForFactoryProjectileCollisionFiltering() {
    Entity shooter = enemy();
    Entity arrow = ProjectileFactory.createPlayerArrow(shooter, Vector2.Zero, Vector2.X);
    entities.register(arrow);
    impact(arrow, shooter);
    assertEquals(30, shooter.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }
}
