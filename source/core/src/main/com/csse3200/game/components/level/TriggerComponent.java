package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.physics.BodyUserData;

public class TriggerComponent extends Component {
  String[] ids;
  boolean oneTimeActivation;

  public TriggerComponent(String[] ids, boolean oneTimeActivation) {
    this.ids = ids;
    this.oneTimeActivation = oneTimeActivation;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  public String[] getIds() {
    return ids;
  }

  /**
   * Responds to player collision and fires events on all ids saved
   *
   * @param me the fixture that was hit
   * @param other the fixture that hit this entity
   */
  private void onCollisionStart(Fixture me, Fixture other) {
    // Only emit signals if the other colliding body is the player
    BodyUserData data = (BodyUserData) other.getBody().getUserData();
    if (data == null
        || data.entity == null
        || data.entity.getComponent(PlayerActions.class) == null) {
      return;
    }

    if (ids != null) {
      for (String id : ids) {
        entity.getEvents().trigger("activatedMapComponent", id);
      }
    }

    // disable the entity from triggering on future collisions
    if (oneTimeActivation) {
      entity.setEnabled(false);
    }
  }
}
