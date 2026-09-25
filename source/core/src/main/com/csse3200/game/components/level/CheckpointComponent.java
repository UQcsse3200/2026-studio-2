package com.csse3200.game.components.level;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class CheckpointComponent extends Component {

  private boolean collected;
  private GridPoint2 position;
  private Entity player = null;
  private final String atlas;
  private AnimationRenderComponent torchAnimator;

  /** World size of an atlas-based checkpoint (each frame is 709 x 890, about 4:5). */
  public static final float ATLAS_WIDTH = 1.6f;

  public static final float ATLAS_HEIGHT = 2f;

  /**
   * Constructor for a new CheckpointComponent
   *
   * @param collected if the checkpoint has been collected or not
   * @param position the position of the checkpoint
   */
  public CheckpointComponent(boolean collected, GridPoint2 position) {
    this(collected, position, null);
  }

  /**
   * Constructor for a checkpoint with a level-specific animated design.
   *
   * @param collected if the checkpoint has been collected or not
   * @param position the position of the checkpoint
   * @param atlas atlas with "unlit" and "lit" regions, or null to use the default textures
   */
  public CheckpointComponent(boolean collected, GridPoint2 position, String atlas) {
    this.collected = collected;
    this.position = position;
    this.atlas = atlas;
  }

  /**
   * Gives this checkpoint the statue entity that represents it, so activating the checkpoint can
   * switch that same entity to its lit frame rather than drawing a second entity over the top.
   *
   * @param torch entity with an {@link AnimationRenderComponent} that has "unlit" and "lit"
   */
  public void setTorch(Entity torch) {
    this.torchAnimator = torch.getComponent(AnimationRenderComponent.class);
  }

  public void activate() {
    if (collected) {
      return;
    }

    this.collected = true;

    if (atlas != null && torchAnimator != null) {
      // Swap the existing statue to its lit frame so the unlit frame isn't left behind it.
      torchAnimator.startAnimation("lit");
      return;
    }

    Entity litTorch =
        new Entity().addComponent(new TextureRenderComponent("images/terrain/checkpoint_lit.png"));
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
