package com.csse3200.game.components.projectile;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Uses real entities and Box2D bodies, with explicit collision events for edge cases. */
@ExtendWith(GameExtension.class)
class ArrowProjectileLifecycleTest {
  private PhysicsService physics;
  private EntityService entities;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    entities = new EntityService();
    ServiceLocator.registerPhysicsService(physics);
    ServiceLocator.registerEntityService(entities);
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  private Entity arrow(Entity shooter, boolean damage) {
    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE));
    if (damage) arrow.addComponent(new CombatStatsComponent(1, 3));
    arrow.addComponent(
        new ArrowProjectileComponent(shooter, Vector2.X, 10f, 15f, ArrowType.STANDARD));
    entities.register(arrow);
    return arrow;
  }

  private Entity target(short layer) {
    Entity target =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(layer))
            .addComponent(new CombatStatsComponent(20, 1));
    entities.register(target);
    return target;
  }

  private void collide(Entity arrow, Entity target) {
    arrow
        .getEvents()
        .trigger(
            "collisionStart",
            arrow.getComponent(HitboxComponent.class).getFixture(),
            target.getComponent(HitboxComponent.class).getFixture());
  }

  @Test
  void shouldRejectNullAndZeroDirection() {
    assertThrows(IllegalArgumentException.class, () -> new ArrowProjectileComponent(null, 1f, 1f));
    assertThrows(
        IllegalArgumentException.class, () -> new ArrowProjectileComponent(Vector2.Zero, 1f, 1f));
  }

  @Test
  void shouldRejectNonPositiveSpeedOrRange() {
    assertThrows(
        IllegalArgumentException.class, () -> new ArrowProjectileComponent(Vector2.X, 0f, 1f));
    assertThrows(
        IllegalArgumentException.class, () -> new ArrowProjectileComponent(Vector2.X, -1f, 1f));
    assertThrows(
        IllegalArgumentException.class, () -> new ArrowProjectileComponent(Vector2.X, 1f, 0f));
    assertThrows(
        IllegalArgumentException.class, () -> new ArrowProjectileComponent(Vector2.X, 1f, -1f));
  }

  @Test
  void shouldCopyDirectionAndDefaultNullType() {
    Vector2 direction = new Vector2(3f, 4f);
    ArrowProjectileComponent projectile = new ArrowProjectileComponent(direction, 10f, 15f, null);
    direction.set(-1f, 0f);
    projectile.getDirection().set(-1f, 0f);
    projectile.getCurrentDirection().set(-1f, 0f);
    assertTrue(projectile.getDirection().epsilonEquals(0.6f, 0.8f, 0.0001f));
    assertTrue(projectile.getCurrentDirection().epsilonEquals(0.6f, 0.8f, 0.0001f));
    assertEquals(ArrowType.STANDARD, projectile.getArrowType());
  }

  @Test
  void shouldRotateWithVelocityAboutItsCentre() {
    Entity arrow = arrow(null, true);
    Body body = arrow.getComponent(PhysicsComponent.class).getBody();
    body.setTransform(2f, 3f, 0f);
    body.setLinearVelocity(0f, 10f);
    ArrowProjectileComponent projectile = arrow.getComponent(ArrowProjectileComponent.class);
    Vector2 centreBefore = projectile.getWorldCenter();
    projectile.update();
    assertEquals(MathUtils.PI / 2f, body.getAngle(), 0.0001f);
    // It turns about the middle of its box, not the body origin (the box's corner), so the hitbox
    // stays where the sprite is drawn.
    assertTrue(projectile.getWorldCenter().epsilonEquals(centreBefore, 0.0001f));
    Vector2 current = projectile.getCurrentDirection();
    assertTrue(current.epsilonEquals(Vector2.Y));
    current.setZero();
    assertEquals(new Vector2(0f, 10f), body.getLinearVelocity());
  }

  @Test
  void shouldKeepRotationWhenStoppedAndUseLaunchAim() {
    Entity arrow = arrow(null, true);
    Body body = arrow.getComponent(PhysicsComponent.class).getBody();
    body.setTransform(2f, 3f, 0.7f);
    body.setLinearVelocity(Vector2.Zero);
    ArrowProjectileComponent projectile = arrow.getComponent(ArrowProjectileComponent.class);
    projectile.update();
    assertEquals(0.7f, body.getAngle(), 0.0001f);
    assertEquals(Vector2.X, projectile.getCurrentDirection());
    assertFalse(projectile.isSpent());
  }

  @Test
  void shouldDisableThenRemoveAtExactRangeBoundaryOnlyOnce() {
    Entity arrow = arrow(null, true);
    Body body = arrow.getComponent(PhysicsComponent.class).getBody();
    ArrowProjectileComponent projectile = arrow.getComponent(ArrowProjectileComponent.class);
    body.setTransform(14.99f, 0f, 0f);
    projectile.update();
    assertFalse(projectile.isSpent());
    body.setTransform(15f, 0f, 0f);
    projectile.update();
    projectile.update();
    assertTrue(projectile.isSpent());
    assertTrue(entities.getEntities().contains(arrow, true));
    entities.update();
    assertFalse(entities.getEntities().contains(arrow, true));
    assertEquals(0, physics.getPhysics().getWorld().getBodyCount());
  }

  @Test
  void shouldIgnoreShooterEvenWhenOnNpcLayer() {
    Entity shooter = target(PhysicsLayer.NPC);
    Entity arrow = arrow(shooter, true);
    collide(arrow, shooter);
    assertEquals(20, shooter.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldIgnoreCollisionFromAnotherFixture() {
    Entity arrow = arrow(null, true);
    Entity enemy = target(PhysicsLayer.NPC);
    arrow
        .getEvents()
        .trigger(
            "collisionStart",
            enemy.getComponent(HitboxComponent.class).getFixture(),
            enemy.getComponent(HitboxComponent.class).getFixture());
    assertEquals(20, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldIgnoreNpcBodyWithMissingOrUnrecognisedMetadata() {
    Entity arrow = arrow(null, true);
    Entity enemy = target(PhysicsLayer.NPC);
    Body body = enemy.getComponent(PhysicsComponent.class).getBody();
    body.setUserData("unrecognised");
    collide(arrow, enemy);
    body.setUserData(new BodyUserData());
    collide(arrow, enemy);
    assertEquals(20, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldNotDamageItselfThroughAnNpcFixture() {
    Entity arrow = arrow(null, true);
    Entity enemy = target(PhysicsLayer.NPC);
    BodyUserData data = new BodyUserData();
    data.entity = arrow;
    enemy.getComponent(PhysicsComponent.class).getBody().setUserData(data);
    collide(arrow, enemy);
    assertEquals(1, arrow.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldPassThroughTargetsWhenProjectileHasNoDamageComponent() {
    Entity arrow = arrow(null, false);
    Entity enemy = target(PhysicsLayer.NPC);
    collide(arrow, enemy);
    assertEquals(20, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldIgnoreBoundaryWalls() {
    Entity arrow = arrow(null, true);
    Entity wall = target(PhysicsLayer.WALL);
    arrow.getComponent(PhysicsComponent.class).getBody().setTransform(2f, 0f, 0f);
    collide(arrow, wall);
    assertEquals(20, wall.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldClearSpawnDistanceBeforeTerrainCanExpireArrow() {
    Entity arrow = arrow(null, true);
    Entity terrain = target(PhysicsLayer.GROUND);
    Body body = arrow.getComponent(PhysicsComponent.class).getBody();
    body.setTransform(0.5f, 0f, 0f);
    collide(arrow, terrain);
    assertFalse(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
    body.setTransform(0.51f, 0f, 0f);
    collide(arrow, terrain);
    assertTrue(arrow.getComponent(ArrowProjectileComponent.class).isSpent());
  }

  @Test
  void shouldExcludePlayerButRetainNpcAndTerrainInCollisionMask() {
    Entity arrow = arrow(null, true);
    short mask = arrow.getComponent(HitboxComponent.class).getFixture().getFilterData().maskBits;
    assertFalse(PhysicsLayer.contains(mask, PhysicsLayer.PLAYER));
    assertTrue(PhysicsLayer.contains(mask, PhysicsLayer.NPC));
    assertTrue(PhysicsLayer.contains(mask, PhysicsLayer.GROUND));
    assertTrue(PhysicsLayer.contains(mask, PhysicsLayer.OBSTACLE));
  }
}
