package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.ProjectileComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ProjectileFactoryTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerPhysicsService(new PhysicsService());
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resources);
  }

  @Test
  void shouldCreatePlayerArrowsAtSmallVisualScale() {
    Entity arrow = ProjectileFactory.createPlayerArrow(Vector2.Zero, Vector2.X);

    assertEquals(0.6f, arrow.getScale().x);
    assertEquals(0.3f, arrow.getScale().y);
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
    assertEquals(1f, arrow.getComponent(ArrowProjectileComponent.class).getDirection().len(), 0.001f);
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
        ProjectileFact.createSkeletonArcherProjectile(new Vector2(3f, 4f), 7, 5f, 2f);

    assertEquals(7, projectile.getComponent(CombatStatsComponent.class).getBaseAttack());
    assertNotNull(projectile.getComponent(ProjectileComponent.class));
  }

  @Test
  void shouldCreateNecromancerProjectileWithConfiguredDamageAndLifetime() {
    Entity projectile = ProjectileFact.createNecromancerProjectile(new Vector2(3f, 4f), 9, 5f, 3f);

    assertEquals(9, projectile.getComponent(CombatStatsComponent.class).getBaseAttack());
    assertNotNull(projectile.getComponent(ProjectileComponent.class));
  }
}
