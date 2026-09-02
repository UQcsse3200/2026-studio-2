package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.TutorialGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.maingame.PauseMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndActions;
import com.csse3200.game.ui.GameEndDisplay;
import com.csse3200.game.ui.GameEndState;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the tutorial.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class TutorialGameScreen extends ScreenAdapter {

  private static final Logger logger = LoggerFactory.getLogger(TutorialGameScreen.class);

  private static final String[] mainGameTextures = {
    "images/heart.png", "images/title_odysseus_logo.png"
  };

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;

  public TutorialGameScreen(GdxGame game) {
    this.game = game;

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

    // renderer.getDebug().setActive(true);
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

    loadAssets();
    createUI();

    logger.debug("Initialising tutorial game screen entities");

    // Pass the renderer's camera to the terrain factory.
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    // Pass the same camera to the TutorialGameArea so that
    // the parallax background can follow camera movement.
    TutorialGameArea tutorialGameArea = new TutorialGameArea(terrainFactory, renderer.getCamera());

    tutorialGameArea.create();

    // Follow the player with the camera.
    renderer.getCamera().setTarget(tutorialGameArea.getPlayer());
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
    logger.trace("Resized renderer: ({} x {})", width, height);
  }

  @Override
  public void pause() {
    logger.info("Game paused");
  }

  @Override
  public void resume() {
    logger.info("Game resumed");
  }

  @Override
  public void dispose() {
    logger.debug("Disposing main game screen");

    renderer.dispose();
    unloadAssets();

    ServiceLocator.getEntityService().dispose();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getResourceService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");

    ResourceService resourceService = ServiceLocator.getResourceService();

    resourceService.loadTextures(mainGameTextures);
    resourceService.loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");

    ResourceService resourceService = ServiceLocator.getResourceService();

    resourceService.unloadAssets(mainGameTextures);
  }

  /**
   * Creates the main game's UI including components for rendering UI elements to the screen and
   * capturing and handling UI input.
   */
  private void createUI() {
    logger.debug("Creating ui");

    Stage stage = ServiceLocator.getRenderService().getStage();

    InputComponent inputComponent =
        ServiceLocator.getInputService().getInputFactory().createForTerminal();

    Entity ui = new Entity();

    ui.addComponent(new InputDecorator(stage, 10))
        .addComponent(new PerformanceDisplay())
        .addComponent(new MainGameActions(this.game))
        .addComponent(new MainGameExitDisplay())
        .addComponent(
            new GameEndDisplay(GameEndState.LOSE)) // Add GameEndDisplay component to the UI entity
        .addComponent(new GameEndActions(this.game))
        .addComponent(new Terminal())
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay())
        .addComponent(new PauseMenuDisplay(this.game));

    ServiceLocator.getEntityService().register(ui);
  }
}
