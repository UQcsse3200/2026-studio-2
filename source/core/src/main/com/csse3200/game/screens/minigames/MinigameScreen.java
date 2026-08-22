package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.ScreenAdapter;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for minigame screens. Handles the service setup, rendering and teardown that every
 * minigame needs.
 */
public abstract class MinigameScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(MinigameScreen.class);

  protected final GdxGame game;
  private Renderer renderer;

  protected MinigameScreen(GdxGame game) {
    this.game = game;
  }

  @Override
  public void show() {
    logger.debug("Initialising minigame screen services");
    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    renderer = RenderFactory.createRenderer();

    loadAssets();
    ServiceLocator.getEntityService().register(createUI());
  }

  protected abstract String[] getTextures();
  protected abstract Entity createUI();

  @Override
  public void render(float delta) {
    ServiceLocator.getEntityService().update();
    renderer.render();
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize(width, height);
    logger.trace("Resized renderer: ({} x {})", width, height);
  }

  @Override
  public void dispose() {
    logger.debug("Disposing minigame screen");

    renderer.dispose();
    unloadAssets();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getEntityService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(getTextures());
    resourceService.loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(getTextures());
  }
}
