package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.physics.BodyUserData;

public class LevelTriggerComponent extends Component {
  String levelName;
  private boolean triggered;

  /**
   * @param name the name of the level to swap to when the component is triggered
   */
  public LevelTriggerComponent(String name) {
    super();
    this.levelName = name;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  /**
   * Responds to player collision and triggers the next level event
   *
   * @param me the fixture that was hit
   * @param other the fixture that hit this entity
   */
  private void onCollisionStart(Fixture me, Fixture other) {
    if (triggered
        || !(other.getBody().getUserData() instanceof BodyUserData data)
        || data.entity.getComponent(PlayerActions.class) == null) {
      return;
    }
    triggered = true;
    entity.getEvents().trigger("triggerNextLevel", levelName);
  }
}
