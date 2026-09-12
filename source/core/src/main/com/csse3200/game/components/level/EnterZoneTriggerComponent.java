package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;

/**
 * Fires a callback once, the first time anything enters this entity's sensor collider. Pair with
 * {@link com.csse3200.game.entities.factories.ObstacleFactory#createTriggerZone(com.badlogic.gdx.math.Vector2)}.
 */
public class EnterZoneTriggerComponent extends Component {
  private final Runnable onEnter;
  private boolean triggered = false;

  public EnterZoneTriggerComponent(Runnable onEnter) {
    this.onEnter = onEnter;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (triggered) {
      return;
    }
    triggered = true;
    onEnter.run();
  }
}
