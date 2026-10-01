package com.csse3200.game.components.lighting;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.PoisonStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.services.ServiceLocator;

/**
 * Circle light component used to store the circleLight object and all of its parameters. This can
 * be added to an entity to make it product light.
 */
public class PointLightComponent extends Component implements Disposable {
  private final RayHandler rayHandler;
  private PointLight circleLight;

  private final int rays;
  private final Color color;
  private float distance;
  private boolean isActive = true;

  // Movement
  private final Vector2 velocity = new Vector2(0f, 0f);

  private static final Color fireBaseColour = new Color(0.55f, 0.05f, 0.02f, 1f);

  private static final Color iceBaseColour = new Color(0.05f, 0.25f, 0.65f, 1f);

  private static final Color poisonBaseColour = new Color(0.20f, 0.05f, 0.25f, 1f);

  private BurnStatsComponent burnStats;
  private SlowStatsComponent slowStats;
  private PoisonStatsComponent poisonStats;

  /**
   * The CircleLightComponent must be registered to the same rayHandler that is being rendered. All
   * light objects must be attached to the rayHandler.
   */
  public PointLightComponent(RayHandler rayHandler, int rays, Color color, float distance) {
    this.rayHandler = rayHandler;
    this.rays = rays;
    this.color = new Color(color);
    this.distance = distance;
  }

  @Override
  public void create() {
    // Initial position uses the entity's centre
    Vector2 p = entity.getPosition();
    circleLight = new PointLight(rayHandler, rays, color, distance, p.x, p.y);
    circleLight.setSoftnessLength(1f);
    circleLight.setXray(false);

    short categoryBits = -1;
    short maskBits = (short) ~PhysicsLayer.GROUND;
    short groupIndex = 0;
    circleLight.setContactFilter(categoryBits, groupIndex, maskBits);

    burnStats = entity.getComponent(BurnStatsComponent.class);
    slowStats = entity.getComponent(SlowStatsComponent.class);
    poisonStats = entity.getComponent(PoisonStatsComponent.class);
  }

  public void update() {
    if (circleLight == null) return;
    if (circleLight.isActive() != isActive) {
      circleLight.setActive(isActive);
    }

    boolean burning = burnStats != null && burnStats.isBurning();
    boolean slowed = slowStats != null && slowStats.isSlowed();
    boolean poisoned = poisonStats != null && poisonStats.isPoisoned();

    if (burning) {
      setColor(fireBaseColour);
    }
    if (slowed) {
      setColor(iceBaseColour);
    }
    if (poisoned) {
      setColor(poisonBaseColour);
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
    if (circleLight != null) {
      circleLight.remove();
      circleLight = null;
    }
  }

  public PointLightComponent setColor(Color c) {
    this.color.set(c);
    if (circleLight != null) circleLight.setColor(c);
    return this;
  }

  public PointLightComponent setDistance(float d) {
    this.distance = d;
    return this;
  }

  public PointLightComponent setSoftnessLength(float softness) {
    if (circleLight != null) circleLight.setSoftnessLength(softness);
    return this;
  }

  public void setActive(boolean active) {
    this.isActive = active;
  }

  public boolean isActive() {
    return this.isActive;
  }

  public PointLight getLight() {
    return circleLight;
  }

  public float getDistance() {
    return distance;
  }
}
