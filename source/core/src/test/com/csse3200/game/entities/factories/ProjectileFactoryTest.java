package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
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
  void shouldCreateThrownPoisonPotionWithPotionStats() {
    Entity potion = ProjectileFactory.createThrownPoisonPotion(null, Vector2.Zero, Vector2.X);

    assertEquals(0.45f, potion.getScale().x);
    assertEquals(0.55f, potion.getScale().y);
    assertEquals(
        ArrowType.POTION, potion.getComponent(ArrowProjectileComponent.class).getArrowType());
  }
}
