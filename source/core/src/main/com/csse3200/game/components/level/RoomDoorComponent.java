package com.csse3200.game.components.level;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/** An optional, explicit interaction that preserves the player and level state between rooms. */
public class RoomDoorComponent extends InputComponent {
  private final Entity player;
  private final CameraComponent camera;
  private final Vector2 destination;
  private final Rectangle bounds;
  private final String label;

  public RoomDoorComponent(
      Entity player, CameraComponent camera, Vector2 destination, Rectangle bounds, String label) {
    super(9);
    this.player = player;
    this.camera = camera;
    this.destination = new Vector2(destination);
    this.bounds = new Rectangle(bounds);
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public boolean canEnter() {
    CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
    return (stats == null || stats.getHealth() > 0)
        && Math.abs(player.getCenterPosition().x - entity.getCenterPosition().x) < 1.8f
        && Math.abs(player.getPosition().y - entity.getPosition().y) < 1.5f;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode != Input.Keys.F || !canEnter() || ServiceLocator.getEntityService().getPaused())
      return false;
    player.getEvents().trigger("grappleRelease");
    player.setPosition(destination);
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    if (physics != null) {
      physics.getBody().setLinearVelocity(0, 0);
      physics.getBody().setAwake(true);
    }
    camera.setRoomBounds(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
    camera.setTarget(player);
    return true;
  }
}
