package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.ServiceLocator;

public class EdgeDetectTask {
  private final float forwardDistance;
  private final PhysicsEngine physics;
  private final DebugRenderer debugRenderer;
  private final RaycastHit hit = new RaycastHit();

  public EdgeDetectTask(float forwardDistance) {
    this.forwardDistance = forwardDistance;

    physics = ServiceLocator.getPhysicsService().getPhysics();
    debugRenderer = ServiceLocator.getRenderService().getDebug();
  }

  public boolean isGroundAhead(Entity enemy, float direction) {
    Vector2 start = enemy.getPosition().cpy();

    if (direction > 0) {
      start.x += enemy.getScale().x + forwardDistance;
    } else {
      start.x -= forwardDistance;
    }

    start.y += 0.5f;

    Vector2 end = start.cpy().add(0f, -2f);

    if (physics.raycast(start, end, PhysicsLayer.GROUND, hit)) {
      return true;
    }

    if (physics.raycast(start, end, PhysicsLayer.OBSTACLE, hit)) {
      return true;
    }

    return false;
  }
}
