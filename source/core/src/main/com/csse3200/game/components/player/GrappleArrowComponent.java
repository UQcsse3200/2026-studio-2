package com.csse3200.game.components.player;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;

/** Rides on a fired grapple arrow and hooks the player on when it hits something solid. */
public class GrappleArrowComponent extends Component {
  private static final short TARGETS = (short) (PhysicsLayer.OBSTACLE | PhysicsLayer.GROUND);

  private final Entity shooter;
  private boolean spent = false;

  public GrappleArrowComponent(Entity shooter) {
    this.shooter = shooter;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollision);
  }

  private void onCollision(Fixture me, Fixture other) {
    if (spent) {
      return;
    }
    // Only latch onto terrain, not enemies or pickups
    if (!PhysicsLayer.contains(TARGETS, other.getFilterData().categoryBits)) {
      return;
    }
    spent = true;

    GrappleComponent grapple = shooter.getComponent(GrappleComponent.class);
    if (grapple != null) {
      grapple.attachTo(other.getBody(), entity.getCenterPosition());
    }
  }
}
