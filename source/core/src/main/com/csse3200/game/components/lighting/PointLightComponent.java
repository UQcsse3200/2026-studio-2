package com.csse3200.game.components.lighting;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.Component;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.services.ServiceLocator;

/**
 * Circle light component used to store the circleLight object and all of its parameters. This can
 * be added to an entity to make it product light.
 */
public class PointLightComponent extends Component implements Disposable {
  private final RayHandler rayHandler;
  private PointLight circleLight;

  private int rays;
  private final int oldRayCount;
  private final Color color;
  private float intensity = 1f;
  private float distance;
  private boolean isActive = true;

  // Movement
  private final Vector2 velocity = new Vector2(0f, 0f);

  /**
   * The CircleLightComponent must be registered to the same rayHandler that is being rendered. All
   * light objects must be attached to the rayHandler.
   */
  public PointLightComponent(RayHandler rayHandler, int rays, Color color, float distance) {
    this.rayHandler = rayHandler;
    this.rays = rays;
    this.oldRayCount = rays;
    this.color = new Color(color);
    this.distance = distance;
  }

  @Override
  public void create() {
    // Initial position uses the entity's centre
    Vector2 p = entity.getPosition();
    createLight(p.x, p.y, rays);
    LightingEngine.addPointLight(this);
  }

  private void createLight(float x, float y, int rayCount) {
    circleLight = new PointLight(rayHandler, rayCount, getEffectiveColor(), distance, x, y);
    circleLight.setSoftnessLength(1f);
    circleLight.setXray(false);

    short categoryBits = -1;
    // CHARACTER is excluded alongside DEFAULT purely to preserve existing behaviour: enemy
    // colliders used to sit on DEFAULT, so light has always passed straight through them.
    short maskBits = (short) ~(PhysicsLayer.DEFAULT | PhysicsLayer.CHARACTER);
    short groupIndex = 0;
    circleLight.setContactFilter(categoryBits, groupIndex, maskBits);
    if (!isActive) {
      circleLight.setActive(false);
    }
  }

  public int getRayCount() {
    return rays;
  }

  public float getLightX() {
    return circleLight.getX();
  }

  public float getLightY() {
    return circleLight.getY();
  }

  public float getLightDistance() {
    return circleLight.getDistance();
  }

  public void setRayCount(int rayCount) {
    if (circleLight == null || circleLight.getRayNum() == rayCount) {
      return;
    }
    this.rays = rayCount;
  }

  @Override
  public void update() {
    if (circleLight == null) {
      return;
    }
    if (circleLight.isActive() != this.isActive) {
      circleLight.setActive(this.isActive);
    }
    if (circleLight.getRayNum() != this.rays) {
      float x = circleLight.getX();
      float y = circleLight.getY();
      circleLight.remove();
      createLight(x, y, this.rays);
    }

    // get the amount of time passed
    float dt = ServiceLocator.getTimeSource().getDeltaTime();
    if (dt <= 0f) dt = 0f;

    // kinematic motion
    if (velocity.len2() > 0f) {
      // gets the position vector of the entity
      Vector2 pos = entity.getPosition();
      // applies velocity to the entity
      pos.mulAdd(velocity, dt);
      entity.setPosition(pos);
    }

    // keep light synced to entity position if following
    Vector2 c = entity.getCenterPosition();
    circleLight.setPosition(c.x, c.y);
  }

  @Override
  public void dispose() {
    LightingEngine.removePointLight(this);
    if (circleLight != null) {
      circleLight.remove();
      circleLight = null;
    }
  }

  public PointLightComponent setColor(Color c) {
    this.color.set(c);
    applyEffectiveColor();
    return this;
  }

  public void setIntensity(float intensity) {
    float clampedIntensity = Math.max(0f, Math.min(1f, intensity));
    if (this.intensity == clampedIntensity) {
      return;
    }
    this.intensity = clampedIntensity;
    applyEffectiveColor();
  }

  private Color getEffectiveColor() {
    return new Color(color.r * intensity, color.g * intensity, color.b * intensity, color.a);
  }

  private void applyEffectiveColor() {
    if (circleLight != null) {
      circleLight.setColor(getEffectiveColor());
    }
  }

  public PointLightComponent setDistance(float d) {
    this.distance = d;
    if (circleLight != null) {
      circleLight.setDistance(d);
    }
    return this;
  }

  public void setActive(boolean active) {
    this.isActive = active;
  }

  public boolean isActive() {
    return this.isActive;
  }

  public float getDistance() {
    return distance;
  }

  public int getOldRayCount() {
    return oldRayCount;
  }
}
