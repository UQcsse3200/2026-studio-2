package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

public class CheckpointComponent extends Component {

  private boolean collected = false;
  private Vector2 position;
  private Entity checkpoint;

  public CheckpointComponent() {
    this.checkpoint = super.getEntity();
  }

  public void activate() {
    this.collected = true;
  }

  public void deactivate() {
    this.collected = false;
  }

  public void setPosition(Vector2 pos) {
    this.position = pos;
  }
}
