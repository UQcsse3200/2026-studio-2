package com.csse3200.game.components.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Logger;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.SoundEffects;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class CheckpointComponent extends Component {

  static final String ACTIVATE_SOUND = "sounds/checkpoint.ogg";

  private boolean collected;
  private boolean activateSoundPlayed = false;
  private final GridPoint2 position;
  private Entity player;
  private PlayerActions playerActions;
  private AnimationRenderComponent torchAnimator;
  private PointLightComponent pointLightComponent;

  /** World size of an atlas-based checkpoint (each frame is 709 x 890, about 4:5). */
  public static final float ATLAS_WIDTH = 1.6f;

  public static final float ATLAS_HEIGHT = 2f;
  Logger logger = new Logger("Lighting Engine");

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
  }

  @Override
  public void create() {
    try {
      pointLightComponent = entity.getComponent(PointLightComponent.class);
    } catch (Exception e) {
      logger.error(PointLightComponent.class.getSimpleName() + " could not be created. " + e);
      pointLightComponent = null;
    }
    // The checkpoint entity draws itself; null when the level has no checkpoint atlas.
    torchAnimator = entity.getComponent(AnimationRenderComponent.class);
  }

  public void activate() {
    if (collected) {
      return;
    }
    this.collected = true;
    entity.getEvents().trigger("checkpointActivated", position);
    if (!activateSoundPlayed) {
      activateSoundPlayed = true;
      SoundEffects.play(ACTIVATE_SOUND, 0.5f);
    }

    if (torchAnimator != null) {
      // Swap the existing statue to its lit frame so the unlit frame isn't left behind it.
      torchAnimator.startAnimation("lit");
    }
    if (pointLightComponent != null) {
      pointLightComponent.setColor(new Color(228f / 255f, 112f / 255f, 37f / 255f, 1f));
      pointLightComponent.setActive(true);
    }
  }

  public void deactivate() {
    if (pointLightComponent != null) {
      pointLightComponent.setActive(false);
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

  /** Check if player position comes in range of checkpoint position. */
  @Override
  public void update() {
    if (pointLightComponent != null) {
      pointLightComponent.update();
    }
    if (player == null) {
      for (Entity candidate : ServiceLocator.getEntityService().getEntities()) {
        PlayerActions actions = candidate.getComponent(PlayerActions.class);
        if (actions != null) {
          player = candidate;
          playerActions = actions;
          break;
        }
      }
    }

    // The player may not have been registered yet.
    if (player == null || playerActions == null) {
      return;
    }

    // A dead player's body can be shoved into a checkpoint by enemies; respawning must not
    // silently retarget to wherever the corpse ended up.
    if (playerActions.isDead()) {
      return;
    }

    float playerPosX = player.getPosition().x;
    float playerPosY = player.getPosition().y;
    if (position.x - 1 < playerPosX
        && position.x + 1 > playerPosX
        && position.y - 1 < playerPosY
        && position.y + 1 > playerPosY) {
      activate();
    }
  }
}
