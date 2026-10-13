package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.SandboxGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.inventory.BackpackDisplay;
import com.csse3200.game.components.itemdictionary.ItemDictionaryDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
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

/** Screen containing the isolated inventory developer sandbox. */
public class SandboxGameScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(SandboxGameScreen.class);

  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final SandboxGameArea sandboxGameArea;
  private boolean inventoryPaused = false;
  private final LightingEngine lightingEngine;

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

    LightingService lightingService =
        new LightingService(renderer.getCamera(), physicsEngine.getWorld());
    ServiceLocator.registerLightingService(lightingService);
    lightingEngine = lightingService.getEngine();

    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
    sandboxGameArea = new SandboxGameArea(terrainFactory, renderer.getCamera());
    sandboxGameArea.create();
    createUI(game);
    renderer.getCamera().setTarget(sandboxGameArea.getPlayer());
  }

  private void createUI(GdxGame game) {
    Stage stage = ServiceLocator.getRenderService().getStage();
    Entity ui =
        new Entity()
            .addComponent(new InputDecorator(stage, 10))
            .addComponent(new MainGameActions(game))
            .addComponent(new MainGameExitDisplay());
    ServiceLocator.getEntityService().register(ui);
  }

  private boolean closeInventoryOverlay() {
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      BackpackDisplay backpack = entity.getComponent(BackpackDisplay.class);

      if (backpack != null && backpack.isBackpackVisible()) {
        backpack.hideBackpack();
        return true;
      }

      ItemDictionaryDisplay dictionary = entity.getComponent(ItemDictionaryDisplay.class);

      if (dictionary != null && dictionary.isDictionaryVisible()) {
        dictionary.hideDictionary();
        return true;
      }
    }

    return false;
  }

  private boolean isInventoryOverlayOpen() {
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      BackpackDisplay backpack = entity.getComponent(BackpackDisplay.class);

      if (backpack != null && backpack.isBackpackVisible()) {
        return true;
      }

      ItemDictionaryDisplay dictionary = entity.getComponent(ItemDictionaryDisplay.class);

      if (dictionary != null && dictionary.isDictionaryVisible()) {
        return true;
      }
    }

    return false;
  }

  private void updateInventoryPauseState() {
    boolean overlayOpen = isInventoryOverlayOpen();

    if (overlayOpen && !inventoryPaused) {
      if (!ServiceLocator.getEntityService().getPaused()) {
        ServiceLocator.getEntityService().setPaused(true);
        inventoryPaused = true;
      }
    } else if (!overlayOpen && inventoryPaused) {
      ServiceLocator.getEntityService().setPaused(false);
      inventoryPaused = false;
    }
  }

  @Override
  public void render(float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      closeInventoryOverlay();
    }

    updateInventoryPauseState();

    physicsEngine.update();
    ServiceLocator.getEntityService().update();

    updateInventoryPauseState();

    renderer.render();
    renderer.render(lightingEngine);
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
    ServiceLocator.getEntityService().dispose();
    ServiceLocator.getRenderService().dispose();
    renderer.dispose();
    lightingEngine.dispose();
    ServiceLocator.getResourceService().dispose();
    physicsEngine.dispose();
    ServiceLocator.clear();
  }
}
