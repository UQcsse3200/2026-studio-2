package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.IntroTutorialGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.ButtonSound;
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
import com.csse3200.game.ui.dialogue.InstructionOverlay;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The narrative intro level that plays before {@link TutorialGameScreen}: Odysseus wakes up on a
 * beach, is taught the controls, then crosses a few platform gaps before handing off into the
 * (unmodified) existing tutorial.
 */
public class IntroTutorialScreen extends ScreenAdapter {

  private static final Logger logger = LoggerFactory.getLogger(IntroTutorialScreen.class);

  private static final String[] introScreenTextures = {
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
    "images/Buttons/exit_down_btn.png"
  };

  // Only the wake-up voice plays during the cinematic; Level 1's track starts once he stands up.
  private static final String levelMusic = "sounds/gameplay_bg.ogg";
  private static final String[] introMusicFiles = {levelMusic};

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private IntroTutorialGameArea introArea;
  private Entity player;

  public IntroTutorialScreen(GdxGame game) {
    this.game = game;

    logger.debug("Initialising intro tutorial screen services");

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
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

    loadAssets();
    InstructionOverlay instructionOverlay = createUI();

    logger.debug("Initialising intro tutorial screen entities");

    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    // The area handles the camera itself: it frames the wake-up cinematic first, then the player
    // once Odysseus stands up and the player entity is spawned (see onWakeUp).
    introArea =
        new IntroTutorialGameArea(
            terrainFactory,
            renderer.getCamera(),
            instructionOverlay,
            this::onWakeUp,
            this::onLevelComplete);
    introArea.create();
  }

  private void onWakeUp() {
    player = introArea.getPlayer();
    player.getEvents().addListener("death", this::onPlayerDeath);
    playMusic();
  }

  private void onLevelComplete() {
    Gdx.app.postRunnable(() -> game.transitionTo(GdxGame.ScreenType.TUTORIAL_GAME));
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
    logger.debug("Disposing intro tutorial screen");

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
    resourceService.loadTextures(introScreenTextures);
    resourceService.loadMusic(introMusicFiles);
    ButtonSound.load(resourceService);
    resourceService.loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(introScreenTextures);
    resourceService.unloadAssets(introMusicFiles);
    ButtonSound.unload(resourceService);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(levelMusic, Music.class);
    music.setLooping(true);
    music.setVolume(0.05f);
    music.play();
  }

  /**
   * Creates the intro screen's UI including components for rendering ui elements to the screen and
   * capturing and handling UI input.
   *
   * @return the instruction overlay, so it can be handed to the game area
   */
  private InstructionOverlay createUI() {
    logger.debug("Creating ui");

    Stage stage = ServiceLocator.getRenderService().getStage();

    InputComponent inputComponent =
        ServiceLocator.getInputService().getInputFactory().createForTerminal();

    InstructionOverlay instructionOverlay = new InstructionOverlay();

    Entity ui = new Entity();

    ui.addComponent(new InputDecorator(stage, 10))
        .addComponent(new PerformanceDisplay())
        .addComponent(new MainGameActions(this.game))
        .addComponent(new MainGameExitDisplay())
        .addComponent(new GameEndDisplay(GameEndState.LOSE))
        .addComponent(new GameEndActions(this.game))
        .addComponent(new Terminal())
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay())
        .addComponent(new PauseMenuDisplay(this.game))
        .addComponent(instructionOverlay);

    ServiceLocator.getEntityService().register(ui);

    return instructionOverlay;
  }
}
