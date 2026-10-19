package com.csse3200.game.screens;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.input.InputService;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What the screens that play the game share: the services every level needs, the renderer and
 * lighting, and tidying them all up again when the screen is disposed.
 */
public abstract class BaseGameScreen extends RenderedScreen {
  private static final Logger logger = LoggerFactory.getLogger(BaseGameScreen.class);

  protected final Renderer renderer;
  protected final PhysicsEngine physicsEngine;
  protected final LightingEngine lightingEngine;

  /** Sets everything up and leaves the camera where the renderer puts it. */
  protected BaseGameScreen() {
    this(null);
  }

  /**
   * @param cameraPosition where to put the camera, or null to leave it where the renderer starts
   */
  protected BaseGameScreen(Vector2 cameraPosition) {
    logger.debug("Initialising main game screen services");
    ServiceLocator.registerTimeSource(new GameTime());

    PhysicsService physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    physicsEngine = physicsService.getPhysics();

    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());

    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    ServiceLocator.registerGameEndEventHandler(new EventHandler());

    renderer = RenderFactory.createRenderer();
    if (cameraPosition != null) {
      renderer.getCamera().getEntity().setPosition(cameraPosition);
    }
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

    LightingService lightingService =
        new LightingService(renderer.getCamera(), physicsEngine.getWorld());
    ServiceLocator.registerLightingService(lightingService);
    lightingEngine = lightingService.getEngine();
  }

  /** Unloads the assets this screen loaded. */
  protected abstract void unloadAssets();

  @Override
  protected Renderer getRenderer() {
    return renderer;
  }

  @Override
  public void dispose() {
    logger.debug("Disposing main game screen");

    renderer.dispose();
    unloadAssets();

    ServiceLocator.getEntityService().dispose();
    lightingEngine.dispose();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getResourceService().dispose();

    ServiceLocator.clear();
  }
}
