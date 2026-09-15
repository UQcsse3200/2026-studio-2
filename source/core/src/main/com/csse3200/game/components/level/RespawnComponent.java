package com.csse3200.game.components.level;

import com.csse3200.game.components.Component;

public class RespawnComponent extends Component {

  @Override
  public void update() {
    if (entity.getPosition().y < -10) {
      entity.getEvents().trigger("respawnAtCheckpoint");
    }
  }
}
