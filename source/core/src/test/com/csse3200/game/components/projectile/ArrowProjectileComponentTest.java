package com.csse3200.game.components.projectile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowProjectileComponentTest {
  private EntityService entityService;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
  }

  @Test
  void shouldConfigureStraightProjectileMotion() {
    Entity arrow = createArrow(new Vector2(3f, 4f), 10f, 15f);
    PhysicsComponent physics = arrow.getComponent(PhysicsComponent.class);

    assertTrue(physics.getBody().getLinearVelocity().epsilonEquals(new Vector2(6f, 8f)));
    assertEquals(0f, physics.getBody().getGravityScale());
    assertEquals(0f, physics.getBody().getLinearDamping());
    assertTrue(physics.getBody().isBullet());
  }

  @Test
  void shouldExpireOnTerrainWithoutDamage() {
    Entity arrow = createArrow(Vector2.X, 10f, 15f);
    Entity terrain = createTarget(PhysicsLayer.GROUND, false);
    ArrowProjectileComponent projectile = arrow.getComponent(ArrowProjectileComponent.class);

    arrow
        .getEvents()
        .trigger(
            "collisionStart",
            arrow.getComponent(HitboxComponent.class).getFixture(),
            terrain.getComponent(HitboxComponent.class).getFixture());

    assertTrue(projectile.isSpent());
  }

  @Test
  void shouldIgnoreUnrelatedLayers() {
    Entity arrow = createArrow(Vector2.X, 10f, 15f);
    Entity player = createTarget(PhysicsLayer.PLAYER, true);
    ArrowProjectileComponent projectile = arrow.getComponent(ArrowProjectileComponent.class);

    arrow
        .getEvents()
        .trigger(
            "collisionStart",
            arrow.getComponent(HitboxComponent.class).getFixture(),
            player.getComponent(HitboxComponent.class).getFixture());

    assertEquals(20, player.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(projectile.isSpent());
  }

  @Test
  void shouldExpireAtMaximumRange() {
    Entity arrow = createArrow(Vector2.X, 10f, 15f);
    ArrowProjectileComponent projectile = arrow.getComponent(ArrowProjectileComponent.class);
    arrow.getComponent(PhysicsComponent.class).getBody().setTransform(15f, 0f, 0f);

    projectile.update();

    assertTrue(projectile.isSpent());
  }

  private Entity createArrow(Vector2 direction, float speed, float range) {
    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, 10))
            .addComponent(new ArrowProjectileComponent(direction, speed, range));
    arrow.setPosition(0f, 0f);
    entityService.register(arrow);
    return arrow;
  }

  private Entity createTarget(short layer, boolean hasCombatStats) {
    return createTarget(layer, hasCombatStats, 20);
  }

  private Entity createTarget(short layer, boolean hasCombatStats, int health) {
    Entity target = spy(new Entity());
    target.addComponent(new PhysicsComponent()).addComponent(new HitboxComponent().setLayer(layer));
    if (hasCombatStats) {
      target.addComponent(new CombatStatsComponent(health, 0));
    }
    entityService.register(target);
    return target;
  }
}
