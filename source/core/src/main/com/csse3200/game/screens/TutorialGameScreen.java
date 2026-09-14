package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.Level2GameArea;
import com.csse3200.game.areas.TutorialGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.maingame.PauseMenuDisplay;
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
 * The game screen containing the tutorial.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class TutorialGameScreen extends ScreenAdapter {

  private static final Logger logger = LoggerFactory.getLogger(TutorialGameScreen.class);

  private static final String[] mainGameTextures = createTextures();

  private boolean levelSwapQueued = false;
  private GameArea currentGameArea;
  private GameArea nextGameArea;

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final SpinTheWheelOverlay wheelOverlay;
  private Entity player;
  private static final String gameplayMusic = "sounds/gameplay_bg.ogg";
  private static final String[] gameplayMusicFiles = {gameplayMusic};
  private boolean cheats = true;

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
    // playMusic();

    logger.debug("Initialising tutorial game screen entities");

    // Pass the renderer's camera to the terrain factory.
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    // Pass the same camera to the TutorialGameArea so that
    // the parallax background can follow camera movement.
    TutorialGameArea tutorialGameArea = new TutorialGameArea(terrainFactory, renderer.getCamera());
    tutorialGameArea.create();

    currentGameArea = tutorialGameArea;
    Entity levelChanger = currentGameArea.getLevelChanger();
    if (levelChanger != null) {
      levelChanger.getEvents().addListener("triggerNextLevel", this::queueAreaSwap);
    }
    player = tutorialGameArea.getPlayer();

    // Follow the player with the camera.
    renderer.getCamera().setTarget(player);
    player.getEvents().addListener("death", this::onPlayerDeath);
    wheelOverlay = new SpinTheWheelOverlay(WheelConfig.ITEMS, player);

    if (cheats) {
      tutorialGameArea.getPlayer().getComponent(PhysicsComponent.class).getBody().setGravityScale(0);
      tutorialGameArea.getPlayer().getComponent(KeyboardPlayerInputComponent.class).toggleCheats();
    }
  }

  private void onPlayerDeath() {
    ServiceLocator.getEntityService().scheduleRemoval(player);
    Gdx.app.postRunnable(
        () -> ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.LOSE));
  }

  /**
   * When the level changer triggers a level change event, this method receives and creates the
   * requested game area object and queues it to be rendered at the next available frame
   *
   * @param level the name of the level to load
   */
  private void queueAreaSwap(String level) {
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    switch (level) {
      case "tutorial":
        nextGameArea = new TutorialGameArea(terrainFactory, renderer.getCamera());
        break;
      case "level2":
        nextGameArea = new Level2GameArea(terrainFactory, renderer.getCamera());
        break;
      default:
        return;
    }
    levelSwapQueued = true;
  }

  /**
   * Performs the level swap by disposing of the existing level, creating the new area and updating
   * internal references to keep track accurately of the current game area
   */
  private void performLevelSwap() {
    logger.info("Swapping level to new game area");

    currentGameArea.dispose();
    nextGameArea.create();
    currentGameArea = nextGameArea;
    nextGameArea = null;

    renderer.getCamera().setTarget(currentGameArea.getPlayer());
  }
  /*
  @Override
  public void render(float delta) {
    // at the start of the render, if there's been a level swap queued, safely perform the swap
    if (levelSwapQueued) {
      performLevelSwap();
      levelSwapQueued = false;

      if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
        wheelOverlay.request();
      }

      physicsEngine.update();
      ServiceLocator.getEntityService().update();
      renderer.render();
      wheelOverlay.afterRender();
    }
  }
  */
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
                "images/Buttons/exit_down_btn.png"));
    paths.addAll(List.of(WheelConfig.TEXTURES));
    return paths.toArray(new String[0]);
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(mainGameTextures);
    resourceService.loadSounds(WheelConfig.SOUNDS);
    resourceService.loadMusic(gameplayMusicFiles);
    ButtonSound.load(resourceService);
    resourceService.loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(mainGameTextures);
    resourceService.unloadAssets(WheelConfig.SOUNDS);
    resourceService.unloadAssets(gameplayMusicFiles);
    ButtonSound.unload(resourceService);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(gameplayMusic, Music.class);
    music.setLooping(true);
    music.setVolume(0.05f);
    music.play();
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
