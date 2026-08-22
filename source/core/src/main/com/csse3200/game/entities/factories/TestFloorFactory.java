package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * Temporary test factory for creating a static floor entity, used to test player
 * movement/jump/gravity before real terrain exists. NOT for final use.
 */
public class TestFloorFactory {

  public static Entity createTestFloor() {
    Entity floor = new Entity();
    floor.addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody));
    floor.addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND));
    floor.setScale(10f, 1f);
    return floor;
  }

  private TestFloorFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
