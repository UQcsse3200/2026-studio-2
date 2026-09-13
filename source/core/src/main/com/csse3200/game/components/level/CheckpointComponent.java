package com.csse3200.game.components.level;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

public class CheckpointComponent extends Component {

  private boolean collected;
  private GridPoint2 position;
  private Entity entity;
  private GridPoint2 respawnPoint;

  public CheckpointComponent(boolean collected, GridPoint2 position) {
    this.collected = collected;
    this.position = position;
  }

  public void activate() {
    this.collected = true;
    respawnPoint = position;
  }

  public void deactivate() {
    this.collected = false;
  }

  public boolean getActive() {
    return this.collected;
  }

  public GridPoint2 getRespawnPoint() {
    return respawnPoint;
  }

  @Override
  public void update() {
    entity = this.getEntity();
    float posX = entity.getPosition().x;
    float posY = entity.getPosition().y;

    if (position.x - 1 < posX && position.x + 1 > posX) {
      if (position.y - 1 < posY && position.y + 1 > posY) {
        activate();
      }
    }
  }
}
