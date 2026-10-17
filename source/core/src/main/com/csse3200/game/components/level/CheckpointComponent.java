
package com.csse3200.game.components.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class CheckpointComponent extends Component {

  private boolean collected;
  private final GridPoint2 position;
  private Entity player;
  private final String atlas;
  private AnimationRenderComponent torchAnimator;
  private PointLightComponent pointLightComponent;

  public static final float ATLAS_WIDTH = 1.6f;
  public static final float ATLAS_HEIGHT = 2f;

  public CheckpointComponent(boolean collected, GridPoint2 position) {
    this(collected, position, null);
  }

  public CheckpointComponent(boolean collected, GridPoint2 position, String atlas) {
    this.collected = collected;
    this.position = position;
    this.atlas = atlas;
  }

  @Override
  public void create() {
    pointLightComponent = entity.getComponent(PointLightComponent.class);
    // The checkpoint entity draws itself; null when the level has no checkpoint atlas.
    torchAnimator = entity.getComponent(AnimationRenderComponent.class);
  }

  public void activate() {
    if (collected) {
      return;
    }

    collected = true;
    entity.getEvents().trigger("checkpointActivated", position);

    if (torchAnimator != null) {
      torchAnimator.startAnimation("lit");
    }

    if (pointLightComponent != null) {
      pointLightComponent.setColor(new Color((228f / 255f), (112f / 255f), (37f / 255f), 1f));
    }
  }

  public void deactivate() {
    if (pointLightComponent != null) {
      pointLightComponent.setColor(Color.CLEAR);
    }

    collected = false;

    if (torchAnimator != null) {
      torchAnimator.startAnimation("unlit");
    }
  }

  public boolean isActive() {
    return collected;
  }

  public GridPoint2 getPosition() {
    return position;
  }

  @Override
  public void update() {
    if (player == null) {
      for (Entity candidate : ServiceLocator.getEntityService().getEntities()) {
        if (candidate.getComponent(PlayerActions.class) != null) {
          player = candidate;
          break;
        }
      }
    }

    // The player may not have been registered yet.
    if (player == null) {
      return;
    }

    float playerPosX = player.getPosition().x;
    float playerPosY = player.getPosition().y;

<<<<<<< HEAD
    if (position.x - 1 < playerPosX
        && position.x + 1 > playerPosX
        && position.y - 1 < playerPosY
        && position.y + 1 > playerPosY) {
=======
    if (position.x - 1 < playerPosX && position.x + 1 > playerPosX
        && position.y - 1 < playerPosY && position.y + 1 > playerPosY) {
>>>>>>> 625ea0b5 (Fix invisible checkpoint sprites)
      activate();
    }
  }
}
