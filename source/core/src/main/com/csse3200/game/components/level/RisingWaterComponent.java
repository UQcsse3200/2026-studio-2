package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;

public class RisingWaterComponent extends Component {
  float CHECKPOINT_LENIENCE = 7f;
  float storedHeight;

  float speed;
  float initialHeight;
  float currentHeight;

  public Entity hitbox;
  Vector2 targetHitboxPosition;
  boolean updatedHeight = false;

  /**
   * Creates a new rising water component for an entity
   *
   * @param speed how much the water should rise each second
   * @param initialHeight what y level the water should start rising from
   */
  public RisingWaterComponent(float speed, float initialHeight, Entity hitbox) {
    this.speed = speed;
    this.initialHeight = initialHeight;
    currentHeight = initialHeight;
    storedHeight = initialHeight;
    this.hitbox = hitbox;
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public float getSpeed() {
    return speed;
  }

  public void setCurrentHeight(float currentHeight) {
    this.currentHeight = currentHeight;
    entity.setScale(entity.getScale().x, currentHeight);

    // updates hitbox to be positioned at the top of the new water position
    targetHitboxPosition =
        new Vector2(hitbox.getPosition().x, entity.getPosition().y + currentHeight - 1);
    updatedHeight = true;
  }

  public float getCurrentHeight() {
    return currentHeight;
  }

  public float getStoredHeight() {
    return storedHeight;
  }

  @Override
  public void create() {
    RenderService renderer = ServiceLocator.getRenderService();

    entity.setScale(renderer.getStage().getWidth(), initialHeight);

    // set hitbox size to 1f and position it at the very top of the water
    hitbox.setScale(renderer.getStage().getWidth(), 1f);
    targetHitboxPosition =
        new Vector2(entity.getPosition().x, entity.getPosition().y + currentHeight - 1);
    updatedHeight = true;

    entity.getEvents().addListener("changeSpeed", this::setSpeed);
    entity.getEvents().addListener("setHeight", this::setCurrentHeight);
    entity.getEvents().addListener("checkpointEncountered", this::captureValues);
  }

  @Override
  public void update() {
    // if the height was changed, react to the new hitbox position requested
    if (updatedHeight) {
      hitbox.setPosition(targetHitboxPosition);
      updatedHeight = false;
    }

    // increases the height of this entity based on the speed and delta time
    float heightIncrease = speed * ServiceLocator.getTimeSource().getDeltaTime();
    currentHeight += heightIncrease;

    entity.setScale(new Vector2(entity.getScale().x, currentHeight));
    hitbox.setPosition(
        new Vector2(hitbox.getPosition().x, hitbox.getPosition().y + heightIncrease));
  }

  @Override
  public void dispose() {
    super.dispose();

    if (hitbox != null) {
      hitbox.dispose();
    }
  }

  /**
   * Uses the y coordinate of the checkpoint encountered and the current top position of the water
   * to determine against the lenience of water levels what height the water should reset to when
   * the player falls into the water and the player respawns at the last checkpoint.
   *
   * <p>This method takes the lowest of two values (clamped by the initial height): the height of
   * the water or the current y position - the lenience factor.
   *
   * <p>This system rewards fast players with a greater head start against the rising water when
   * they fall into the water. On the other hand, it also does not punish slower players, allowing
   * the level to still be completable and not add too much time pressure.
   *
   * <p>For example, if the lenience factor was 8f, the water level was at 10f and the checkpoint y
   * was at 20f. 20f - 8f = 12f > 10f (water level), so we store 10f. If the water level was at 15f,
   * then we'd store 12f. Lastly, if the checkpoint y was at 5f and the water's initial height was
   * at 2f, 5f - 8f = -3f, but this is clamped by 2f, so 2f would be stored.
   *
   * @param checkpointY the y coordinate of the checkpoint that triggered the event to call this
   *     method
   */
  protected void captureValues(int checkpointY) {
    // calculate the smallest between the current water surface level and the lenience water level
    // and convert it into a height relative to the entity's bottom left position
    float lenientSurfaceLevel =
        Math.min(entity.getPosition().y + currentHeight, checkpointY - CHECKPOINT_LENIENCE);
    float lenientHeight = lenientSurfaceLevel - entity.getPosition().y;

    // store the initial height if the lenient height is calculated below the starting height
    // otherwise accept the lenient height
    storedHeight = Math.max(initialHeight, lenientHeight);
  }
}
