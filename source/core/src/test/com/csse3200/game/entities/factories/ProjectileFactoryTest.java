package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ProjectileFactoryTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  @Test
  void shouldCreatePlayerArrowsAtSmallVisualScale() {
    Entity arrow = ProjectileFactory.createPlayerArrow(Vector2.Zero, Vector2.X);

    assertEquals(0.6f, arrow.getScale().x);
    assertEquals(0.3f, arrow.getScale().y);
  }

  @Test
  void shouldLaunchGrappleArrowSlowerWhenBarelyChargedLikeAnyOtherArrow() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    Entity normal = ProjectileFactory.createGrappleArrow(null, Vector2.Zero, Vector2.X, 1f);
    ServiceLocator.getEntityService().register(normal);
    Entity tap = ProjectileFactory.createGrappleArrow(null, Vector2.Zero, Vector2.X, 0.3f);
    ServiceLocator.getEntityService().register(tap);
    Entity fullDraw = ProjectileFactory.createGrappleArrow(null, Vector2.Zero, Vector2.X, 1.5f);
    ServiceLocator.getEntityService().register(fullDraw);

    // A barely held shot is launched slowly so it falls short; a full draw is launched fastest.
    assertEquals(ProjectileFactory.GRAPPLE_ARROW_SPEED, speedOf(normal), 0.01f);
    assertEquals(ProjectileFactory.GRAPPLE_ARROW_SPEED * 0.3f, speedOf(tap), 0.01f);
    assertEquals(ProjectileFactory.GRAPPLE_ARROW_SPEED * 1.5f, speedOf(fullDraw), 0.01f);
  }

  private static float speedOf(Entity arrow) {
    return arrow.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().len();
  }

  @Test
  void shouldReportEachArrowTypesLaunchSpeedAndRangeForTheAimPreview() {
    assertEquals(
        ProjectileFactory.STANDARD_ARROW_SPEED,
        ProjectileFactory.getArrowSpeed(ArrowType.STANDARD));
    assertEquals(
        ProjectileFactory.STANDARD_ARROW_SPEED, ProjectileFactory.getArrowSpeed(ArrowType.POISON));
    assertEquals(
        ProjectileFactory.FIRE_ARROW_SPEED, ProjectileFactory.getArrowSpeed(ArrowType.FIRE));
    assertEquals(ProjectileFactory.ICE_ARROW_SPEED, ProjectileFactory.getArrowSpeed(ArrowType.ICE));
    assertEquals(
        ProjectileFactory.GRAPPLE_ARROW_SPEED, ProjectileFactory.getArrowSpeed(ArrowType.GRAPPLE));
    assertEquals(
        ProjectileFactory.POISON_POTION_SPEED, ProjectileFactory.getArrowSpeed(ArrowType.POTION));

    assertEquals(
        ProjectileFactory.STANDARD_ARROW_RANGE,
        ProjectileFactory.getArrowRange(ArrowType.STANDARD));
    assertEquals(ProjectileFactory.ICE_ARROW_RANGE, ProjectileFactory.getArrowRange(ArrowType.ICE));
    assertEquals(
        ProjectileFactory.POISON_POTION_RANGE, ProjectileFactory.getArrowRange(ArrowType.POTION));
  }

  @Test
  void shouldCreateThrownPoisonPotionWithPotionStats() {
    Entity potion = ProjectileFactory.createThrownPoisonPotion(null, Vector2.Zero, Vector2.X);

    assertEquals(0.45f, potion.getScale().x);
    assertEquals(0.55f, potion.getScale().y);
    assertEquals(
        ArrowType.POTION, potion.getComponent(ArrowProjectileComponent.class).getArrowType());
  }
}
