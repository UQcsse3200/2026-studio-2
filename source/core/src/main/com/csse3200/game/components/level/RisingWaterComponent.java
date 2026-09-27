package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;

public class RisingWaterComponent extends Component {
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
    targetHitboxPosition = new Vector2(hitbox.getPosition().x, entity.getPosition().y);
    updatedHeight = true;
  }

  public float getCurrentHeight() {
    return currentHeight;
  }

  @Override
  public void create() {
    RenderService renderer = ServiceLocator.getRenderService();

    entity.setScale(renderer.getStage().getWidth(), initialHeight);
    hitbox.setScale(renderer.getStage().getWidth(), initialHeight);

    entity.getEvents().addListener("changeSpeed", this::setSpeed);
    entity.getEvents().addListener("setHeight", this::setCurrentHeight);
  }

  @Override
  public void update() {
    // if the height was changed, react to the new hitbox position requested
    if (updatedHeight) {
      hitbox.setPosition(targetHitboxPosition);
      updatedHeight = false;
    }

    // increases the height of this entity based on the speed and delta time
    float heightIncrease = (speed / 60f) * ServiceLocator.getTimeSource().getDeltaTime();
    currentHeight += heightIncrease;

    entity.setScale(new Vector2(entity.getScale().x, currentHeight));
    hitbox.setPosition(new Vector2(hitbox.getPosition().x, hitbox.getPosition().y + heightIncrease));
  }

  @Override
  public void dispose() {
    super.dispose();

    if (hitbox != null) {
      hitbox.dispose();
    }
  }
}
