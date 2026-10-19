package com.csse3200.game.entities.factories;

import box2dLight.RayHandler;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.services.ServiceLocator;

/**
 * The light factory is used to create the various light entities and apply the corresponding
 * components to them. The implementation is currently up for change as it has been configured for
 * debugging.
 */
public class LightFactory {

  /**
   * Helper class to get the rayHandler from the ServiceLocator. Checks to see that the lighting
   * engine as well as the lighting service has been initialised.
   *
   * @return the rayHandler if located, otherwise throws an exception.
   */
  private static RayHandler rayHandler() {
    LightingService lighting = ServiceLocator.getLightingService();
    if (lighting == null || lighting.getEngine() == null) {
      throw new IllegalStateException("LightingService/Engine not initialised.");
    }
    return lighting.getEngine().getRayHandler();
  }

  public static Entity createPointLight() {
    Entity pointLight = new Entity();
    RayHandler rayHandler = rayHandler();
    pointLight.addComponent(
        new PointLightComponent(
            rayHandler,
            LightingDefaults.RAYS,
            LightingDefaults.NORMAL_COLOR,
            LightingDefaults.DIST));
    return pointLight;
  }

  private LightFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
