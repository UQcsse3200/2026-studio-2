package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

public class AttachableMapComponent extends Component {
  PhysicsComponent physics;
  boolean attemptedDiscovery = false;
  boolean attached = false;
  Entity parent; // the parent entity that the entity with this component is attached to
  Vector2 offset = new Vector2();

  /**
   * Force the component to find a parent, or move with the parent, but only once the physics
   * component has loaded
   */
  @Override
  public void update() {
    physics = entity.getComponent(PhysicsComponent.class);
    if (physics == null || physics.getBody() == null) {
      return;
    }

    if (!attached) {
      discoverParent();
    } else {
      moveWithParent();
    }
  }

  /**
   * Performs a raycast hit from the center of this entity downwards to determine if there is a
   * valid entity it can attach to. If it finds one, it stores references. Will only be performed
   * once!
   */
  private void discoverParent() {
    if (attemptedDiscovery) {
      return;
    }

    // get position and rotation and set up the raycast
    Vector2 position = physics.getBody().getPosition();

    RotatableMapComponent rotatable = entity.getComponent(RotatableMapComponent.class);
    float rotation = rotatable != null ? rotatable.getRotation() : 0f;

    Vector2 direction = new Vector2(0, -1.5f).rotateDeg(rotation);
    Vector2 start = entity.getCenterPosition();
    Vector2 end = new Vector2(position).add(direction);

    RaycastHit hit = new RaycastHit();

    // perform raycast and save hit entity as parent to attach to
    boolean raycastSuccess =
        ServiceLocator.getPhysicsService()
            .getPhysics()
            .raycast(start, end, PhysicsLayer.OBSTACLE, hit);

    if (raycastSuccess && hit.fixture != null) {
      BodyUserData userData = (BodyUserData) hit.fixture.getBody().getUserData();
      parent = userData.entity;

      // calculate the initial difference in position so we know how to adjust the
      // transform when the parent moves
      PhysicsComponent parentPhysics = parent.getComponent(PhysicsComponent.class);
      offset.set(position).sub(parentPhysics.getBody().getPosition());
      attached = true;
    }
    attemptedDiscovery = true;
  }

  /**
   * Applies the same transformations that have been applied to the found parent entity to this
   * entity using the calculated offset from the parent.
   */
  private void moveWithParent() {
    // if we have a valid parent, and it exists in the level, we update its position every frame
    // so it moves with the parent, appearing attached
    if (parent != null) {
      PhysicsComponent parentPhysics = parent.getComponent(PhysicsComponent.class);

      if (parentPhysics != null && parentPhysics.getBody() != null) {
        Vector2 parentPosition = parentPhysics.getBody().getPosition();
        physics
            .getBody()
            .setTransform(
                parentPosition.x + offset.x,
                parentPosition.y + offset.y,
                physics.getBody().getAngle());
      }
    }
  }
}
