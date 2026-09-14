package com.csse3200.game.components.level;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;

public class RotatableMapComponent extends Component {
  float rotationAngle;
  boolean initialised = false;
  PhysicsComponent physicsComponent;

  public RotatableMapComponent(float angle) {
    rotationAngle = angle;
  }

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
  }

  @Override
  public void update() {
    if (!initialised) {
      Body body = physicsComponent.getBody();

      if (body != null) {
        body.setTransform(body.getPosition(), rotationAngle * MathUtils.degreesToRadians);
        initialised = true;
      }
    }
  }

  public float getRotation() {
    return rotationAngle;
  }
}
