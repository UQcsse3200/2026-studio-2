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
 * Attaches a Box2DLight point light to an entity and keeps its position, color, and activity in
 * sync with that entity.
 */
public class PointLightComponent extends Component implements Disposable {
  private final RayHandler rayHandler;
  private PointLight circleLight;

  private int rays;
  private final int oldRayCount;
  private final Color color;
  private final Color smoothedColor;
  private float intensity = 1f;
  private float distance;
  private boolean isActive = true;

  // Movement
  private final Vector2 velocity = new Vector2(0f, 0f);

  /**
   * Creates a point light attached to the supplied ray handler.
   *
   * @param rayHandler handler that will render and own this light
   * @param rays number of rays used for the light's shadows
   * @param color base light color
   * @param distance light radius in world units
   */
  public PointLightComponent(RayHandler rayHandler, int rays, Color color, float distance) {
    this.rayHandler = rayHandler;
    this.rays = rays;
    this.oldRayCount = rays;
    this.color = new Color(color);
    this.smoothedColor = new Color(color);
    this.distance = distance;
  }

  /** Creates the Box2DLight point light and registers it with the lighting engine. */
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

  /**
   * @return current number of rays used to render this light
   */
  public int getRayCount() {
    return rays;
  }

  /**
   * @return current world-space horizontal position of the light
   */
  public float getLightX() {
    return circleLight.getX();
  }

  /**
   * @return current world-space vertical position of the light
   */
  public float getLightY() {
    return circleLight.getY();
  }

  /**
   * @return current radius of the light in world units
   */
  public float getLightDistance() {
    return circleLight.getDistance();
  }

  /**
   * Sets the number of rays used to render this light.
   *
   * @param rayCount new ray count; applied to the Box2DLight object during the next update
   */
  public void setRayCount(int rayCount) {
    if (circleLight == null || circleLight.getRayNum() == rayCount) {
      return;
    }
    this.rays = rayCount;
  }

  /** Reconciles the light's state and position with its owning entity. */
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

  /** Unregisters and removes this light from its ray handler. */
  @Override
  public void dispose() {
    LightingEngine.removePointLight(this);
    if (circleLight != null) {
      circleLight.remove();
      circleLight = null;
    }
  }

  /**
   * Sets the base color of the light.
   *
   * @param c new base color
   * @return this component for method chaining
   */
  public PointLightComponent setColor(Color c) {
    this.color.set(c);
    applyEffectiveColor();
    return this;
  }

  /**
   * @return red channel of the configured base color
   */
  public float getBaseRed() {
    return color.r;
  }

  /**
   * @return green channel of the configured base color
   */
  public float getBaseGreen() {
    return color.g;
  }

  /**
   * @return blue channel of the configured base color
   */
  public float getBaseBlue() {
    return color.b;
  }

  /**
   * @return current smoothed red channel used to render the light
   */
  public float getSmoothedRed() {
    return smoothedColor.r;
  }

  /**
   * @return current smoothed green channel used to render the light
   */
  public float getSmoothedGreen() {
    return smoothedColor.g;
  }

  /**
   * @return current smoothed blue channel used to render the light
   */
  public float getSmoothedBlue() {
    return smoothedColor.b;
  }

  /**
   * Sets the color channels produced by lighting overlap smoothing.
   *
   * @param red smoothed red channel
   * @param green smoothed green channel
   * @param blue smoothed blue channel
   */
  public void setSmoothedColor(float red, float green, float blue) {
    smoothedColor.set(red, green, blue, color.a);
    applyEffectiveColor();
  }

  /**
   * Sets the light intensity, clamped to the range from zero to one.
   *
   * @param intensity requested intensity
   */
  public void setIntensity(float intensity) {
    float clampedIntensity = Math.clamp(intensity, 0f, 1f);
    if (this.intensity == clampedIntensity) {
      return;
    }
    this.intensity = clampedIntensity;
    applyEffectiveColor();
  }

  private Color getEffectiveColor() {
    return new Color(
        smoothedColor.r * intensity,
        smoothedColor.g * intensity,
        smoothedColor.b * intensity,
        color.a);
  }

  private void applyEffectiveColor() {
    if (circleLight != null) {
      circleLight.setColor(getEffectiveColor());
    }
  }

  /**
   * Sets the light's radius.
   *
   * @param d radius in world units
   * @return this component for method chaining
   */
  public PointLightComponent setDistance(float d) {
    this.distance = d;
    if (circleLight != null) {
      circleLight.setDistance(d);
    }
    return this;
  }

  /**
   * Enables or disables this light.
   *
   * @param active true to enable the light, false to disable it
   */
  public void setActive(boolean active) {
    this.isActive = active;
  }

  /**
   * @return true if this light is enabled
   */
  public boolean isActive() {
    return this.isActive;
  }

  /**
   * @return configured light radius in world units
   */
  public float getDistance() {
    return distance;
  }

  /**
   * @return ray count supplied when this component was constructed
   */
  public int getOldRayCount() {
    return oldRayCount;
  }
}
