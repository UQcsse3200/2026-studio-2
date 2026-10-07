package com.csse3200.game.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.Vector2Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PhysicsUtilsTest {
  Entity entity;

  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());

    entity =
        new Entity().addComponent(new ColliderComponent()).addComponent(new PhysicsComponent());
  }

  @Test
  void shouldScaleFullCollider() {
    setAndCheckScale(new Vector2(3.5f, 4.5f), Vector2Utils.ONE);
  }

  @Test
  void shouldScaleHalfCollider() {
    setAndCheckScale(Vector2Utils.ONE, new Vector2(0.5f, 0.5f));
  }

  @Test
  void shouldScaleBigCollider() {
    setAndCheckScale(new Vector2(0.5f, 0.5f), new Vector2(2f, 2f));
  }

  @Test
  void shouldPlaceColliderRelativeToTheEntityRatherThanTheWorldOrigin() {
    entity.setScale(2f, 4f);
    entity.setPosition(37f, -12f);

    PhysicsUtils.setScaledCollider(entity, 0.5f, 0.5f);
    ServiceLocator.getEntityService().register(entity);

    // The offsets passed to Box2D are entity-local, so a half-size box is centred horizontally and
    // rests on the entity's base no matter where in the world the entity happens to be. Reading a
    // world coordinate here used to shift the collider by the entity's own position.
    Vector2 bottomLeft =
        PhysicsTestUtils.getRectanglePosition(entity.getComponent(ColliderComponent.class));
    assertEquals(0.5f, bottomLeft.x, 0.0001f);
    assertEquals(0f, bottomLeft.y, 0.0001f);
  }

  private void setAndCheckScale(Vector2 entityScale, Vector2 colliderScale) {
    entity.setScale(entityScale);
    PhysicsUtils.setScaledCollider(entity, colliderScale.x, colliderScale.y);
    ServiceLocator.getEntityService().register(entity);

    PhysicsTestUtils.checkPolygonCollider(
        entity.getComponent(ColliderComponent.class), entityScale.cpy().scl(colliderScale));
  }
}
