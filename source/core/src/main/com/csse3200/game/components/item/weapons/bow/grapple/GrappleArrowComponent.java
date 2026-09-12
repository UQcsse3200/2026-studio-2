package com.csse3200.game.components.item.weapons.bow.grapple;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.services.ServiceLocator;

/** Rides on a fired grapple arrow and hooks the player onto solid ground or a platform. */
public class GrappleArrowComponent extends Component {
  /** The grapple only sticks to solid terrain - not enemies, boundary walls, or trigger zones. */
  private static final short GRAPPLE_TARGETS = PhysicsLayer.SOLID; // GROUND | OBSTACLE

  /** The arrow must get this far from the shooter before it can hook, so it doesn't grab the
   * platform the player is standing on the instant it spawns. */
  private static final float MIN_TRAVEL = 1.2f;

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
    if (!PhysicsLayer.contains(GRAPPLE_TARGETS, other.getFilterData().categoryBits)) {
      return;
    }
    if (entity.getCenterPosition().dst2(shooter.getCenterPosition()) < MIN_TRAVEL * MIN_TRAVEL) {
      return;
    }
    spent = true;

    // If the button was already released before the arrow landed, don't attach at all - otherwise
    // the rope grabs on for a frame and then immediately lets go again.
    if (shouldAttach()) {
      GrappleComponent grapple = shooter.getComponent(GrappleComponent.class);
      if (grapple != null) {
        grapple.attachTo(other.getBody(), entity.getCenterPosition());
      }
    }

    // The rope takes over from here (or the shot's abandoned); either way the arrow is done
    if (ServiceLocator.getEntityService() != null) {
      ServiceLocator.getEntityService().scheduleRemoval(entity);
    }
  }

  private boolean shouldAttach() {
    KeyboardPlayerInputComponent input = shooter.getComponent(KeyboardPlayerInputComponent.class);
    return input == null || input.isRightMouseHeld();
  }
}
