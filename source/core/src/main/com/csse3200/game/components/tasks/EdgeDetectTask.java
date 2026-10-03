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

    Vector2 enemyPos = enemy.getCenterPosition().cpy();

    enemyPos.x += direction * forwardDistance;
    enemyPos.y = enemy.getPosition().y + 0.1f;

    Vector2 end = enemyPos.cpy().add(0f, -1f);

    boolean isGround = physics.raycast(enemyPos, end, PhysicsLayer.GROUND, hit);

    if (isGround) {
      return true;
    }
    return false;
  }
}
