package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.areas.Level2GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.maingame.PauseMenuOverlay;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.blackjack.BlackjackOverlay;
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
 * The game screen containing the main game levels.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class LevelsGameScreen extends ScreenAdapter {

  private static final Logger logger = LoggerFactory.getLogger(LevelsGameScreen.class);

  private static final String[] mainGameTextures = createTextures();
  private static final String[] mainGameAtlas = createAtlas();

  private boolean levelSwapQueued = false;
  private GameArea currentGameArea;
  private GameArea nextGameArea;

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final SpinTheWheelOverlay wheelOverlay;
  private final PauseMenuOverlay pauseOverlay;
  private final BlackjackOverlay blackjackOverlay;
  private final MinigameOverlayManager minigameOverlayManager;
  private Entity player;
  private static final String gameplayMusic = "sounds/gameplay_bg.ogg";
  private static final String[] gameplayMusicFiles = {gameplayMusic};
  private static final String winMusic = "sounds/Win_music.mp3";
  private static final String loseMusic = "sounds/Death_music.ogg";
  private static final String[] gameEndMusic = {winMusic, loseMusic, "sounds/Main_menu_sound.mp3"};
  private static final String[] gameSounds = {
    "sounds/hit.ogg", "sounds/Arrow_release.wav", "sounds/jump.ogg", "sounds/itempick.wav"
  };
  private final Level1GameArea level1GameArea;
  private boolean cheats = false;

  public LevelsGameScreen(GdxGame game) {
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
    playMusic();

    logger.debug("Initialising level 1 game screen entities");

    // Pass the renderer's camera to the terrain factory.
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    // Pass the same camera to the Level1GameArea so that
    // the parallax background can follow camera movement.
    this.level1GameArea = new Level1GameArea(terrainFactory, renderer.getCamera());

    Level1GameArea level1GameArea = new Level1GameArea(terrainFactory, renderer.getCamera());
    level1GameArea.create();

    currentGameArea = level1GameArea;
    Entity levelChanger = currentGameArea.getLevelChanger();
    if (levelChanger != null) {
      levelChanger.getEvents().addListener("triggerNextLevel", this::queueAreaSwap);
    }

    player = level1GameArea.getPlayer();
    player.getEvents().addListener("respawnAtCheckpoint", () -> currentGameArea.respawn());

    player
        .getEvents()
        .addListener(
            "jump",
            () -> {
              try {
                com.badlogic.gdx.audio.Sound jumpSound =
                    ServiceLocator.getResourceService()
                        .getAsset("sounds/jump.ogg", com.badlogic.gdx.audio.Sound.class);
                jumpSound.play(0.5f);
              } catch (Exception e) {
                // skip
              }
            });
    player
        .getEvents()
        .addListener(
            "hurt",
            () -> {
              try {
                com.badlogic.gdx.audio.Sound hurtSound =
                    ServiceLocator.getResourceService()
                        .getAsset("sounds/hit.ogg", com.badlogic.gdx.audio.Sound.class);
                hurtSound.play(0.2f);
              } catch (Exception e) {
                // skip
              }
            });
    player
        .getEvents()
        .addListener(
            "itemPickedUp",
            (Object item) -> {
              try {
                com.badlogic.gdx.audio.Sound pickupSound =
                    ServiceLocator.getResourceService()
                        .getAsset("sounds/itempick.wav", com.badlogic.gdx.audio.Sound.class);
                pickupSound.play(0.2f);
              } catch (Exception e) {
                // skip
              }
            });
    // Follow the player with the camera.
    renderer.getCamera().setTarget(player);
    player.getEvents().addListener("deathAnimationFinished", this::onPlayerDeath);
    wheelOverlay = new SpinTheWheelOverlay(WheelConfig.ITEMS, player);
    pauseOverlay = new PauseMenuOverlay(game, level1GameArea);

    minigameOverlayManager = new MinigameOverlayManager();
    blackjackOverlay = new BlackjackOverlay(player, minigameOverlayManager);

    if (cheats) {
      level1GameArea.getPlayer().getComponent(PhysicsComponent.class).getBody().setGravityScale(0);
      level1GameArea.getPlayer().getComponent(KeyboardPlayerInputComponent.class).toggleCheats();
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
  public void queueAreaSwap(String level) {
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    switch (level) {
      case "level1":
        nextGameArea = new Level1GameArea(terrainFactory, renderer.getCamera());
        break;
      case "level2":
        nextGameArea = new Level2GameArea(terrainFactory, renderer.getCamera(), player);
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

  @Override
  public void render(float delta) {
    // at the start of the render, if there's been a level swap queued, safely perform the swap
    if (levelSwapQueued) {
      performLevelSwap();
      levelSwapQueued = false;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
      wheelOverlay.request();
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      pauseOverlay.request();
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
      blackjackOverlay.request();
    }

    physicsEngine.update();
    ServiceLocator.getEntityService().update();
    renderer.render();
    wheelOverlay.afterRender();
    pauseOverlay.afterRender();
    blackjackOverlay.afterRender();
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
   * The level's textures and spin the wheel's so it can be opened as an overlay.
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
                "images/scroll_bg.png",
                "images/Buttons/exit_down_btn.png",
                "images/rope_arrow.png",
                "images/fire_arrow.png",
                "images/cold_arrow.png"));
    paths.addAll(List.of(WheelConfig.TEXTURES));
    paths.addAll(List.of(BlackjackConfig.TEXTURES));
    return paths.toArray(new String[0]);
  }

  /**
   * The game's atlases that should not be unloaded by each game area
   *
   * @return every atlas the levels need
   */
  private static String[] createAtlas() {
    List<String> paths = new ArrayList<>(List.of("images/player.atlas"));
    return paths.toArray(new String[0]);
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(mainGameTextures);
    resourceService.loadTextureAtlases(mainGameAtlas);
    resourceService.loadSounds(WheelConfig.SOUNDS);
    resourceService.loadMusic(gameEndMusic);
    resourceService.loadSounds(gameSounds);
    resourceService.loadMusic(gameplayMusicFiles);
    ButtonSound.load(resourceService);
    resourceService.loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(mainGameTextures);
    resourceService.unloadAssets(mainGameAtlas);
    resourceService.unloadAssets(WheelConfig.SOUNDS);
    resourceService.unloadAssets(gameSounds);
    resourceService.unloadAssets(gameEndMusic);
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
        .addComponent(new Terminal(game, GdxGame.ScreenType.LEVEL_1_GAME))
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay());

    ServiceLocator.getEntityService().register(ui);
  }
}
