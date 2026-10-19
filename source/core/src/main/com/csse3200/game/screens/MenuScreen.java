package com.csse3200.game.screens;

import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/** Shared service setup and frame rendering for menus without a physics world. */
public abstract class MenuScreen extends RenderedScreen {
  protected final Renderer renderer;

  protected MenuScreen() {
    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    renderer = RenderFactory.createRenderer();
  }

  @Override
  public void render(float delta) {
    ServiceLocator.getEntityService().update();
    renderer.render();
  }

  @Override
  protected Renderer getRenderer() {
    return renderer;
  }
}
