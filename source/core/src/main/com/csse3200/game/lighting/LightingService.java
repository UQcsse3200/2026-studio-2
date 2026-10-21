package com.csse3200.game.lighting;

import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.components.CameraComponent;

/**
 * Provides a global access point to the lighting engine. This is necessary for the lighting based
 * entities to register themselves with the engine.
 */
public class LightingService {
  private final LightingEngine engine;

  /**
   * Creates and owns the lighting engine for a camera and physics world.
   *
   * @param camera camera used to render lights
   * @param world Box2D world used for light raycasts
   */
  public LightingService(CameraComponent camera, World world) {
    this.engine = new LightingEngine(camera, world);
  }

  /**
   * @return the lighting engine managed by this service
   */
  public LightingEngine getEngine() {
    return this.engine;
  }
}
