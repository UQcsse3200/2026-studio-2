package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.CyclopsMinigameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameActions;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameDisplay;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameLogic;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
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
import com.csse3200.game.ui.GameEndDisplay;
import com.csse3200.game.ui.GameEndState;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameRoomScreen extends ScreenAdapter {

  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameRoomScreen.class);
  private static final String[] cyclopsMinigameTextures = {
    "images/ui/title_odysseus_logo.png",
    "images/health/red_heart.png",
    "images/Buttons/exit_up_btn.png",
    "images/Buttons/exit_down_btn.png",
    "images/Buttons/restart_up_btn.png",
    "images/Buttons/restart_down_btn.png",
    "images/Buttons/main_menu_up_btn.png",
    "images/Buttons/main_menu_down_btn.png",
    "images/Buttons/exit_game_up_btn.png",
    "images/Buttons/exit_game_down_btn.png",
    "images/ui/scroll_bg.png"
  };
  private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;

  private CyclopsMinigameArea cyclopsMinigameArea;
  static boolean skipIntroOnNextLoad = false;
  private CyclopsIntro intro;

  public CyclopsMinigameRoomScreen(GdxGame game) {
    this.game = game;

    logger.debug("Initialising cyclops minigame screen services");
    ServiceLocator.registerTimeSource(new GameTime());

    PhysicsService physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    physicsEngine = physicsService.getPhysics();

    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());

    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    renderer = RenderFactory.createRenderer();
    renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

    loadAssets();
    createUI(consumeSkipIntroOnNextLoad());

    logger.debug("Initialising cyclops minigame screen entities");
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
    cyclopsMinigameArea = new CyclopsMinigameArea(renderer.getCamera(), terrainFactory);
    cyclopsMinigameArea.create();
  }

  @Override
  public void render(float delta) {
    if (intro.isShowing() && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
      intro.advance();
    }

    if (intro.takeStartDue()) {
      ServiceLocator.getCyclopsMinigameEventHandler().trigger("start");
    }

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
  public void dispose() {
    logger.debug("Disposing minigame screen");

    renderer.dispose();
    cyclopsMinigameArea.dispose();
    unloadAssets();

    ServiceLocator.getEntityService().dispose();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getResourceService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(cyclopsMinigameTextures);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(cyclopsMinigameTextures);
  }

  /**
   * Creates the main game's ui including components for rendering ui elements to the screen and
   * capturing and handling ui input.
   */
  private void createUI(boolean skipIntro) {
    logger.debug("Creating ui");
    Stage stage = ServiceLocator.getRenderService().getStage();

    TextBoxComponent introBox =
        skipIntro
            ? null
            : new TextBoxComponent(
                600,
                150,
                Color.BLACK,
                Color.TAN,
                Color.BROWN,
                20f,
                300,
                16,
                3,
                "flat-earth/skin/fonts/PixeloidSans.fnt",
                Align.center,
                List.of(
                    "Hmm... there's a cyclops in the way, I'll need to get past...",
                    "...I'll need to \"LEFT_CLICK\" to each of those statues at the right time"
                        + " (Left-click or TAB to continue)"));
    if (introBox != null) {
      introBox.create();
    }
    intro = new CyclopsIntro(introBox);

    InputComponent inputComponent =
        ServiceLocator.getInputService().getInputFactory().createForTerminal();

    Terminal terminal = new Terminal(game, GdxGame.ScreenType.LEVEL_2_GAME);

    ServiceLocator.getCyclopsMinigameEventHandler()
        .addListener(
            "win",
            () -> {
              CutsceneLoader loader = new CutsceneLoader();
              CutsceneLoader.Result result = loader.load("cutscene3");
              if (!result.isSuccess()) {
                logger.debug("Could not start cutscene '{}': {}", "cutscene3", result.getError());
              } else {
                game.startCutscene(result.getCutscene(), GdxGame.ScreenType.LEVEL_2_GAME);
              }
            });
    ServiceLocator.getCyclopsMinigameEventHandler()
        .addListener("died", CyclopsMinigameRoomScreen::showGameOver);

    Entity ui = new Entity();
    ui.addComponent(new InputDecorator(stage, 10))
        .addComponent(new PerformanceDisplay())
        .addComponent(new CyclopsMinigameActions(this.game))
        .addComponent(new CyclopsMinigameDisplay())
        .addComponent(terminal)
        .addComponent(inputComponent)
        .addComponent(new GameEndDisplay(GameEndState.LOSE))
        .addComponent(new TerminalDisplay());
    ui.getEvents().addListener("mainMenu", () -> game.setScreen(GdxGame.ScreenType.MAIN_MENU));
    ui.getEvents().addListener("exitGame", game::exit);
    ui.getEvents().addListener("restart", () -> restartFromGameOver(game));

    ServiceLocator.getEntityService().register(ui);
  }

  static void showGameOver() {
    ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.LOSE);
  }

  static void restartFromGameOver(GdxGame game) {
    ServiceLocator.getCyclopsMinigameEventHandler()
        .trigger(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT);
    skipIntroOnNextLoad = true;
    Gdx.app.postRunnable(() -> game.setScreen(GdxGame.ScreenType.CYCLOPS_MINIGAME));
  }

  static boolean consumeSkipIntroOnNextLoad() {
    boolean skip = skipIntroOnNextLoad;
    skipIntroOnNextLoad = false;
    return skip;
  }
}
