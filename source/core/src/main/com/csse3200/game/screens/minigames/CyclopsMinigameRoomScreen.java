package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.CyclopsMinigameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.PauseButtonDisplay;
import com.csse3200.game.components.maingame.PauseMenuOverlay;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsMinigameActions;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsMinigameLogic;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsSoundComponent;
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
    // The pause menu's buttons and the settings screen
    "images/Buttons/continue_up_btn.png",
    "images/Buttons/continue_down_btn.png",
    "images/Buttons/settings_up_btn.png",
    "images/Buttons/settings_down_btn.png",
    "images/Buttons/quit_up_btn.png",
    "images/Buttons/quit_down_btn.png",
    // PauseMenuDisplay loads the Controls button's images even though the button is hidden here
    "images/Buttons/control_up_btn.png",
    "images/Buttons/control_down_btn.png",
    "images/ui/controls_graphic.png",
    "images/Buttons/apply_up_btn.png",
    "images/Buttons/apply_down_btn.png",
    "images/Buttons/back_up_btn.png",
    "images/Buttons/back_down_btn.png",
    "images/ui/settings_box.png",
    "images/backgrounds/main_menu_bg_2.png",
    "images/Buttons/restart_up_btn.png",
    "images/Buttons/restart_down_btn.png",
    "images/Buttons/main_menu_up_btn.png",
    "images/Buttons/main_menu_down_btn.png",
    "images/Buttons/exit_game_up_btn.png",
    "images/Buttons/exit_game_down_btn.png",
    "images/ui/scroll_bg.png"
  };
  // The settings screen shows the cave instead of the sea. The cave area loads this texture.
  private static final String SETTINGS_BACKGROUND = "images/minigames/Cyclops/cave_background.png";
  private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;

  private CyclopsMinigameArea cyclopsMinigameArea;
  static boolean skipIntroOnNextLoad = false;
  private CyclopsIntro intro;
  private final PauseMenuOverlay pauseOverlay;
  private boolean soundsPaused = false;

  public CyclopsMinigameRoomScreen(GdxGame game) {
    this.game = game;
    this.pauseOverlay = new PauseMenuOverlay(game, false, SETTINGS_BACKGROUND);

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
    if (intro.isShowing()
        && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
        && !CyclopsMinigameLogic.clickIsOnUiButton()) {
      intro.advance();
    }

    if (intro.takeStartDue()) {
      ServiceLocator.getCyclopsMinigameEventHandler().trigger("start");
    }

    // Esc opens the pause menu, unless the terminal or the settings screen has the keyboard.
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
        && !Terminal.isAnyOpen()
        && !ServiceLocator.getEntityService().getSettingsOpen()) {
      pauseOverlay.request();
    }

    physicsEngine.update();
    ServiceLocator.getEntityService().update();
    renderer.render();
    pauseOverlay.afterRender();
    syncSoundsWithPause();
  }

  /**
   * Pauses the looping snoring and footsteps while the pause menu is open, and carries on when it
   * closes. The sounds are not part of the paused entities, so they would otherwise keep playing.
   */
  private void syncSoundsWithPause() {
    boolean paused = ServiceLocator.getEntityService().getPaused();
    if (paused == soundsPaused) {
      return;
    }
    soundsPaused = paused;
    for (String path :
        List.of(CyclopsSoundComponent.SNORE_PATH, CyclopsMinigameLogic.WALKING_SOUND_PATH)) {
      if (!ServiceLocator.getResourceService().containsAsset(path, Sound.class)) {
        continue;
      }
      Sound sound = ServiceLocator.getResourceService().getAsset(path, Sound.class);
      if (paused) {
        sound.pause();
      } else {
        sound.resume();
      }
    }
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize(width, height);
    logger.trace("Resized renderer: ({} x {})", width, height);
  }

  @Override
  public void dispose() {
    logger.debug("Disposing minigame screen");

    intro.dispose();
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
    // The pause button's own images, when the project has them
    resourceService.loadTextures(PauseButtonDisplay.extraTextures());
    // The click sound for the pause button and the pause menu's buttons
    ButtonSound.load(resourceService);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(cyclopsMinigameTextures);
    resourceService.unloadAssets(PauseButtonDisplay.extraTextures());
    ButtonSound.unload(resourceService);
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
                    "...I'll need to \"LEFT_CLICK\" to each of those rocks at the right time"
                        + " (Left-click or TAB to continue)"),
                List.of());
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
        // Opens the pause menu, which is also how the player leaves the minigame
        .addComponent(new PauseButtonDisplay(() -> pauseOverlay.request(), true))
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
