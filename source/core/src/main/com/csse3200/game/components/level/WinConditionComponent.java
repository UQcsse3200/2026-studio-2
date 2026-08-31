package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;

public class WinConditionComponent extends Component {
  public WinConditionComponent() {
    super();
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    System.out.println("collision start"); // confirms
    // ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.WIN);
  }
}
