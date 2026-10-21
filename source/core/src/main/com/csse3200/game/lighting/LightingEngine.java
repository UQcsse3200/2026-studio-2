package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes lighting components using the Box2DLight library. Sets up the ray handler that is
 * responsible for rendering all lights to the screen.
 */
public class LightingEngine implements Disposable {
  private static final int MIN_RAYS = 3;
  private static final float RAY_COUNT_EQUALISATION_RATE = 24f;
  private final RayHandler rayHandler;
  private final CameraComponent camera;
  private static final List<PointLightComponent> pointLights = new ArrayList<>();
  private static final Map<PointLightComponent, Float> smoothedRayCounts = new IdentityHashMap<>();

  private float ambientLight = 0.9f;

  /**
   * Creates the lighting engine for a world and camera.
   *
   * @param camera camera used to render the current screen
   * @param world Box2D world used by the physics engine and light raycasts
   */
  public LightingEngine(CameraComponent camera, World world) {
    this.camera = camera;
    rayHandler = new RayHandler(world);

    rayHandler.setAmbientLight(ambientLight);
    rayHandler.setBlur(true);
    rayHandler.setBlurNum(LightingDefaults.BLUR_NUM);
  }

  /**
   * Registers a point light for overlap-based ray-count and color balancing.
   *
   * @param light light to register
   */
  public static void addPointLight(PointLightComponent light) {
    if (pointLights.contains(light)) {
      return;
    }
    pointLights.add(light);
    smoothedRayCounts.put(light, (float) light.getRayCount());
    updateOverlappingRayCounts();
  }

  /**
   * Removes a point light from overlap balancing.
   *
   * @param light light to unregister
   */
  public static void removePointLight(PointLightComponent light) {
    pointLights.remove(light);
    smoothedRayCounts.remove(light);
    updateOverlappingRayCounts();
  }

  /**
   * @return the Box2DLight handler that owns and renders this engine's lights
   */
  public RayHandler getRayHandler() {
    return rayHandler;
  }

  /**
   * Sets the ambient light level used to shade the scene.
   *
   * @param ambientLight ambient light color intensity
   */
  public void setAmbientLight(float ambientLight) {
    this.ambientLight = ambientLight;
    rayHandler.setAmbientLight(ambientLight);
  }

  /**
   * Render all lights registered with the ray handler to the screen. This also renders a "shadow"
   * over the entire screen which can be adjusted using {@link #setAmbientLight(float)}.
   */
  public void render() {
    rayHandler.setCombinedMatrix(camera.getProjectionMatrix());
    rayHandler.updateAndRender();
  }

  private static void updateOverlappingRayCounts() {
    float deltaTime = Math.max(0f, ServiceLocator.getTimeSource().getDeltaTime());
    float interpolation = 1f - (float) Math.exp(-RAY_COUNT_EQUALISATION_RATE * deltaTime);
    float[] nextRayCounts = new float[pointLights.size()];
    float[] targetReds = new float[pointLights.size()];
    float[] targetGreens = new float[pointLights.size()];
    float[] targetBlues = new float[pointLights.size()];

    for (int i = 0; i < pointLights.size(); i++) {
      PointLightComponent first = pointLights.get(i);
      float currentRayCount = smoothedRayCounts.get(first);
      LightTargets targets = calculateTargets(i, first, currentRayCount);
      nextRayCounts[i] = currentRayCount + (targets.rayCount - currentRayCount) * interpolation;
      targetReds[i] = targets.red;
      targetGreens[i] = targets.green;
      targetBlues[i] = targets.blue;
    }

    for (int i = 0; i < pointLights.size(); i++) {
      PointLightComponent light = pointLights.get(i);
      float smoothedRayCount = nextRayCounts[i];
      smoothedRayCounts.put(light, smoothedRayCount);
      light.setRayCount(Math.max(MIN_RAYS, Math.round(smoothedRayCount)));
      light.setSmoothedColor(
          light.getSmoothedRed() + (targetReds[i] - light.getSmoothedRed()) * interpolation,
          light.getSmoothedGreen() + (targetGreens[i] - light.getSmoothedGreen()) * interpolation,
          light.getSmoothedBlue() + (targetBlues[i] - light.getSmoothedBlue()) * interpolation);
    }
  }

  private static LightTargets calculateTargets(
      int lightIndex, PointLightComponent first, float currentRayCount) {
    float targetRayCount;
    float nearbyLightWeight = 0f;
    float totalRed = first.getBaseRed();
    float totalGreen = first.getBaseGreen();
    float totalBlue = first.getBaseBlue();
    float totalColorWeight = 1f;

    if (!first.isActive()) {
      targetRayCount = (float) first.getOldRayCount();
    } else {
      float totalRayCount = currentRayCount;
      int overlappingLights = 1;
      for (int j = 0; j < pointLights.size(); j++) {
        PointLightComponent other = pointLights.get(j);
        if (lightIndex == j || !other.isActive()) {
          continue;
        }

        float overlapStrength = getOverlapStrength(first, other);
        if (overlapStrength > 0f) {
          totalRayCount += smoothedRayCounts.get(other);
          overlappingLights++;
          nearbyLightWeight += overlapStrength;
          totalRed += other.getSmoothedRed() * overlapStrength;
          totalGreen += other.getSmoothedGreen() * overlapStrength;
          totalBlue += other.getSmoothedBlue() * overlapStrength;
          totalColorWeight += overlapStrength;
        }
      }
      targetRayCount =
          overlappingLights == 1 ? first.getOldRayCount() : totalRayCount / overlappingLights;
    }

    first.setIntensity(first.isActive() ? 1f / (1f + nearbyLightWeight) : 1f);
    return new LightTargets(
        targetRayCount,
        totalRed / totalColorWeight,
        totalGreen / totalColorWeight,
        totalBlue / totalColorWeight);
  }

  /** Target ray count and color calculated for one light during overlap balancing. */
  private static class LightTargets {
    private final float rayCount;
    private final float red;
    private final float green;
    private final float blue;

    private LightTargets(float rayCount, float red, float green, float blue) {
      this.rayCount = rayCount;
      this.red = red;
      this.green = green;
      this.blue = blue;
    }
  }

  private static float getOverlapStrength(PointLightComponent first, PointLightComponent second) {
    float dx = first.getLightX() - second.getLightX();
    float dy = first.getLightY() - second.getLightY();
    float combinedDistance = first.getLightDistance() + second.getLightDistance();
    if (combinedDistance <= 0f) {
      return 0f;
    }
    float distance = (float) Math.sqrt(dx * dx + dy * dy);
    return Math.max(0f, 1f - distance / combinedDistance);
  }

  @Override
  public void dispose() {
    pointLights.clear();
    smoothedRayCounts.clear();
    rayHandler.dispose();
  }
}
