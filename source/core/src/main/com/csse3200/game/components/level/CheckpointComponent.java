package com.csse3200.game.components.level;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class CheckpointComponent extends Component {

  private boolean collected;
  private GridPoint2 position;
  private Entity player = null;

  /**
   * Constructor for a new CheckpointComponent
   *
   * @param collected if the checkpoint has been collected or not
   * @param position the position of the checkpoint
   */
  public CheckpointComponent(boolean collected, GridPoint2 position) {
    this.collected = collected;
    this.position = position;
  }

  public void activate() {
    if (collected) {
      return;
    }

    this.collected = true;

    Entity litTorch =
        new Entity().addComponent(new TextureRenderComponent("images/checkpoint_lit.png"));

    litTorch.setScale(1f, 1.5f);
    litTorch.setPosition(position.x, position.y);

    ServiceLocator.getEntityService().register(litTorch);
  }

  public void deactivate() {
    this.collected = false;
  }

  public boolean isActive() {
    return this.collected;
  }

  public GridPoint2 getPosition() {
    return position;
  }

  /** Check if player position comes in range of checkpoint position. */
  @Override
  public void update() {
    if (player == null) {
      for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
        if (entity.getComponent(PlayerActions.class) != null) {
          player = entity;
          break;
        }
      }
    }
    float playerPosX = player.getPosition().x;
    float playerPosY = player.getPosition().y;
    if (position.x - 1 < playerPosX && position.x + 1 > playerPosX) {
      if (position.y - 1 < playerPosY && position.y + 1 > playerPosY) {
        activate();
      }
    }
  }
}
