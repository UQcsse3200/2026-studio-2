package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.SandboxGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Screen containing the isolated inventory developer sandbox. */
public class SandboxGameScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(SandboxGameScreen.class);

  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final SandboxGameArea sandboxGameArea;

  /**
   * Creates the sandbox screen and its minimal service lifecycle.
   *
   * @param game game instance controlling screen navigation
   */
  public SandboxGameScreen(GdxGame game) {
    logger.debug("Initialising sandbox screen services");
    ServiceLocator.registerTimeSource(new GameTime());

    PhysicsService physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    physicsEngine = physicsService.getPhysics();

    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    renderer = RenderFactory.createRenderer();

    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
    sandboxGameArea = new SandboxGameArea(terrainFactory, renderer.getCamera());
    sandboxGameArea.create();
    renderer.getCamera().setTarget(sandboxGameArea.getPlayer());
  }

  @Override
  public void render(float delta) {
    physicsEngine.update();
    ServiceLocator.getEntityService().update();
    renderer.render();
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize(width, height);
    logger.trace("Resized sandbox renderer: ({} x {})", width, height);
  }

  @Override
  public void pause() {
    logger.info("Sandbox paused");
  }

  @Override
  public void resume() {
    logger.info("Sandbox resumed");
  }

  @Override
  public void dispose() {
    logger.debug("Disposing sandbox screen");
    sandboxGameArea.dispose();
    ServiceLocator.getEntityService().dispose();
    ServiceLocator.getRenderService().dispose();
    renderer.dispose();
    ServiceLocator.getResourceService().dispose();
    physicsEngine.dispose();
    ServiceLocator.clear();
  }
}
