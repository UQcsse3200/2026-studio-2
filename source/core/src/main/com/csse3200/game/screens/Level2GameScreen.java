package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.Level2GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.maingame.PauseMenuOverlay;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelOverlay;
import com.csse3200.game.components.minigames.spinthewheel.WheelConfig;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing level 2.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class Level2GameScreen extends ScreenAdapter {

  private static final Logger logger = LoggerFactory.getLogger(Level2GameScreen.class);

  private static final String[] mainGameTextures = createTextures();

  private boolean levelSwapQueued = false;
  private GameArea currentGameArea;
  private GameArea nextGameArea;

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final SpinTheWheelOverlay wheelOverlay;
  private final PauseMenuOverlay pauseOverlay;
  private Entity player;
  private static final String gameplayMusic = "sounds/gameplay_bg.ogg";
  private static final String[] gameplayMusicFiles = {gameplayMusic};
  private boolean cheats = false;

  public Level2GameScreen(GdxGame game) {
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

    logger.debug("Initialising level 2 game screen entities");

    // Pass the renderer's camera to the terrain factory.
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    // Pass the same camera to the Level2GameArea so that
    // the parallax background can follow camera movement.
    Level2GameArea level2 = new Level2GameArea(terrainFactory, renderer.getCamera(), player);

    level2.create();

    player = level2.getPlayer();

    // Follow the player with the camera.
    renderer.getCamera().setTarget(player);

    player.getEvents().addListener("death", this::onPlayerDeath);
    wheelOverlay = new SpinTheWheelOverlay(WheelConfig.ITEMS, player);
    pauseOverlay = new PauseMenuOverlay(game, currentGameArea, GdxGame.ScreenType.LEVEL_2_SETTINGS);

    if (cheats) {
      level2.getPlayer().getComponent(PhysicsComponent.class).getBody().setGravityScale(0);
      level2.getPlayer().getComponent(KeyboardPlayerInputComponent.class).toggleCheats();
    }
  }

  private void onPlayerDeath() {
    ServiceLocator.getEntityService().scheduleRemoval(player);
    Gdx.app.postRunnable(
        () -> ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.LOSE));
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

  /**
   * The tutorial's textures and spin the wheel's so it can be opened as an overlay.
   *
   * @return every texture this screen needs loaded
   */
  private static String[] createTextures() {
    List<String> paths =
        new ArrayList<>(
            List.of(
                "images/heart.png",
                "images/title_odysseus_logo.png",
                "images/Health_Bar_Background.png",
                "images/red_heart.png",
                "images/PixelArt_HeartBack.png",
                "images/Damaged_heart.png",
                "images/Last_Health.png",
                "images/Buttons/continue_up_btn.png",
                "images/Buttons/continue_down_btn.png",
                "images/Buttons/settings_up_btn.png",
                "images/Buttons/settings_down_btn.png",
                "images/Buttons/quit_up_btn.png",
                "images/Buttons/quit_down_btn.png",
                "images/Buttons/exit_up_btn.png",
                "images/Buttons/exit_down_btn.png",
                "images/rope_arrow.png",
                "images/fire_arrow.png",
                "images/cold_arrow.png",
                "images/Buttons/exit_down_btn.png",
                "images/Buttons/control_up_btn.png",
                "images/Buttons/control_down_btn.png",
                "images/controls_graphic.png",
                "images/Buttons/restart_up_btn.png",
                "images/Buttons/restart_down_btn.png",
                "images/Buttons/main_menu_up_btn.png",
                "images/Buttons/main_menu_down_btn.png",
                "images/Buttons/exit_game_up_btn.png",
                "images/Buttons/exit_game_down_btn.png",
                "images/Buttons/back_up_btn.png",
                "images/Buttons/back_down_btn.png",
                "images/scroll_bg.png"));
    paths.addAll(List.of(WheelConfig.TEXTURES));
    return paths.toArray(new String[0]);
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
        .addComponent(new Terminal(game, GdxGame.ScreenType.LEVEL_2_GAME))
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay());


    ServiceLocator.getEntityService().register(ui);
  }
}
