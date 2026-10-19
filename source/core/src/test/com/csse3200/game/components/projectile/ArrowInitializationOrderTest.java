package com.csse3200.game.components.projectile;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowInitializationOrderTest {
  @Test
  void shouldExcludePlayerWhenHitboxCreatedFirst() {
    verifyCreationOrder(true);
  }

  @Test
  void shouldExcludePlayerWhenArrowCreatedFirst() {
    verifyCreationOrder(false);
  }

  @Test
  void shouldFilterExistingBodyFixtureWithoutHitboxComponent() {
    PhysicsService physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    try {
      PhysicsComponent body = new PhysicsComponent();
      ArrowProjectileComponent arrow = new ArrowProjectileComponent(Vector2.X, 10f, 15f);
      new Entity().addComponent(body).addComponent(arrow);
      CircleShape shape = new CircleShape();
      shape.setRadius(0.1f);
      var fixture = body.getBody().createFixture(shape, 0f);
      shape.dispose();
      var filter = fixture.getFilterData();
      filter.maskBits = (short) (PhysicsLayer.PLAYER | PhysicsLayer.NPC);
      fixture.setFilterData(filter);
      body.create();
      arrow.create();
      assertEquals(PhysicsLayer.NPC, fixture.getFilterData().maskBits);
      assertEquals(new Vector2(10f, 0f), body.getBody().getLinearVelocity());
    } finally {
      physics.getPhysics().dispose();
    }
  }

  private void verifyCreationOrder(boolean hitboxFirst) {
    PhysicsService physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    try {
      PhysicsComponent body = new PhysicsComponent();
      HitboxComponent hitbox = new HitboxComponent();
      hitbox.setLayer(PhysicsLayer.PLAYER_PROJECTILE);
      ArrowProjectileComponent arrow = new ArrowProjectileComponent(Vector2.X, 10f, 15f);
      new Entity().addComponent(body).addComponent(hitbox).addComponent(arrow);

      // Explicitly exercise both orders without relying on global ComponentType IDs.
      body.create();
      if (hitboxFirst) hitbox.create();
      arrow.create();
      if (!hitboxFirst) hitbox.create();

      short mask = hitbox.getFixture().getFilterData().maskBits;
      assertFalse(PhysicsLayer.contains(mask, PhysicsLayer.PLAYER));
      assertTrue(PhysicsLayer.contains(mask, PhysicsLayer.NPC));
      assertTrue(PhysicsLayer.contains(mask, PhysicsLayer.GROUND));
      assertTrue(PhysicsLayer.contains(mask, PhysicsLayer.OBSTACLE));
      assertEquals(PhysicsLayer.PLAYER_PROJECTILE, hitbox.getLayer());
    } finally {
      physics.getPhysics().dispose();
    }
  }
}
