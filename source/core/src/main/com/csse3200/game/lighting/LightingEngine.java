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
   * Constructor method for the lighting engine. This is where some of the rayHandler's global
   * variables are set and can be changed.
   *
   * @param camera The camera associated with the current renderer for the screen.
   * @param world The same world registered with the physics engine.
   */
  public LightingEngine(CameraComponent camera, World world) {
    this.camera = camera;
    rayHandler = new RayHandler(world);

    rayHandler.setAmbientLight(ambientLight);
    rayHandler.setBlur(true);
    rayHandler.setBlurNum(LightingDefaults.BLUR_NUM);
  }

  public static void addPointLight(PointLightComponent light) {
    if (pointLights.contains(light)) {
      return;
    }
    pointLights.add(light);
    smoothedRayCounts.put(light, (float) light.getRayCount());
  }

  public static void removePointLight(PointLightComponent light) {
    pointLights.remove(light);
    smoothedRayCounts.remove(light);
  }

  public RayHandler getRayHandler() {
    return rayHandler;
  }

  public void setAmbientLight(float ambientLight) {
    this.ambientLight = ambientLight;
    rayHandler.setAmbientLight(ambientLight);
  }

  /**
   * Render all lights registered with the ray handler to the screen. This also renders a "shadow"
   * over the entire screen which can be adjusted using the setAmbientLight function.
   */
  public void render() {
    updateOverlappingRayCounts();
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
      float targetRayCount;
      float nearbyLightWeight = 0f;
      float totalRed = first.getBaseRed();
      float totalGreen = first.getBaseGreen();
      float totalBlue = first.getBaseBlue();
      float totalColorWeight = 1f;

      if (!first.isActive()) {
        targetRayCount = first.getOldRayCount();
      } else {
        float totalRayCount = currentRayCount;
        int overlappingLights = 1;
        for (int j = 0; j < pointLights.size(); j++) {
          PointLightComponent other = pointLights.get(j);
          if (i != j && other.isActive()) {
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
        }
        targetRayCount =
            overlappingLights == 1 ? first.getOldRayCount() : totalRayCount / overlappingLights;
      }

      nextRayCounts[i] = currentRayCount + (targetRayCount - currentRayCount) * interpolation;
      targetReds[i] = totalRed / totalColorWeight;
      targetGreens[i] = totalGreen / totalColorWeight;
      targetBlues[i] = totalBlue / totalColorWeight;
      first.setIntensity(first.isActive() ? 1f / (1f + nearbyLightWeight) : 1f);
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
