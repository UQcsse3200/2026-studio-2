package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.ProjectileComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.MockServices;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ProjectileFactoryTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());

    MockServices.registerLighting();
    MockServices.registerTextures();
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
  void shouldCreateThrownPoisonPotionWithPotionStats() {
    Entity potion = ProjectileFactory.createThrownPoisonPotion(null, Vector2.Zero, Vector2.X);

    assertEquals(0.45f, potion.getScale().x);
    assertEquals(0.55f, potion.getScale().y);
    assertEquals(
        ArrowType.POTION, potion.getComponent(ArrowProjectileComponent.class).getArrowType());
  }

  @Test
  void shouldCreateFireArrowWithFireTypeAndNormalizedDirection() {
    Entity arrow = ProjectileFactory.createFireArrow(Vector2.Zero, new Vector2(3f, 4f));

    assertEquals(ArrowType.FIRE, arrow.getComponent(ArrowProjectileComponent.class).getArrowType());
    assertEquals(
        1f, arrow.getComponent(ArrowProjectileComponent.class).getDirection().len(), 0.001f);
  }

  @Test
  void shouldCreateGrappleArrowWithGrappleType() {
    Entity arrow = ProjectileFactory.createGrappleArrow(Vector2.Zero, Vector2.Y);

    assertEquals(
        ArrowType.GRAPPLE, arrow.getComponent(ArrowProjectileComponent.class).getArrowType());
  }

  @Test
  void shouldCreateSkeletonArcherProjectileWithConfiguredDamageAndLifetime() {
    Entity projectile =
        EnemyProjectileFactory.createSkeletonArcherProjectile(
            new Entity(), new Vector2(0f, 0f), new Vector2(3f, 4f), 7, 5f, 2f);

    assertEquals(7, projectile.getComponent(CombatStatsComponent.class).getBaseAttack());
    assertNotNull(projectile.getComponent(ProjectileComponent.class));
  }

  @Test
  void shouldCreateNecromancerProjectileWithConfiguredDamageAndLifetime() {
    Entity projectile =
        EnemyProjectileFactory.createNecromancerProjectile(new Vector2(3f, 4f), 9, 5f, 3f);

    assertEquals(9, projectile.getComponent(CombatStatsComponent.class).getBaseAttack());
    assertNotNull(projectile.getComponent(ProjectileComponent.class));
    assertMagicProjectileLight(projectile);
  }

  @Test
  void shouldCreateCalypsoProjectileWithConfiguredMagicLight() {
    Entity projectile =
        EnemyProjectileFactory.createCalypsoProjectile(new Vector2(3f, 4f), 9, 5f, 3f);

    assertEquals(9, projectile.getComponent(CombatStatsComponent.class).getBaseAttack());
    assertNotNull(projectile.getComponent(ProjectileComponent.class));
    assertMagicProjectileLight(projectile);
  }

  private static void assertMagicProjectileLight(Entity projectile) {
    PointLightComponent light = projectile.getComponent(PointLightComponent.class);
    assertNotNull(light);
    assertEquals(0.4f, light.getBaseRed(), 0.001f);
    assertEquals(0f, light.getBaseGreen(), 0.001f);
    assertEquals(0.65f, light.getBaseBlue(), 0.001f);
  }
}
