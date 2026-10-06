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
import com.csse3200.game.areas.Level3GameArea;
import com.csse3200.game.areas.LevelBossGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.SoundEffects;
import com.csse3200.game.components.gamearea.CoordinateDisplay;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.item.ItemAssets;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.PauseButtonDisplay;
import com.csse3200.game.components.maingame.PauseMenuOverlay;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.blackjack.BlackjackOverlay;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelOverlay;
import com.csse3200.game.components.minigames.spinthewheel.WheelConfig;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
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
  private String level = "level1";

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final SpinTheWheelOverlay wheelOverlay;
  private final PauseMenuOverlay pauseOverlay;
  private final LightingEngine lightingEngine;
  private final BlackjackOverlay blackjackOverlay;
  private final MinigameOverlayManager minigameOverlayManager;
  private Entity player;
  private static final String gameplayMusic = "sounds/gameplay_bg.ogg";
  private static final String[] gameplayMusicFiles = {gameplayMusic};
  private static final String winMusic = "sounds/Win_music.mp3";
  private static final String loseMusic = "sounds/Death_music.ogg";
  private static final String[] gameEndMusic = {winMusic, loseMusic, "sounds/Main_menu_sound.mp3"};
  private final Level1GameArea level1GameArea;
  private boolean cheats = false;
  private float gravity;

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

    LightingService lightingService =
        new LightingService(renderer.getCamera(), physicsEngine.getWorld());
    ServiceLocator.registerLightingService(lightingService);
    lightingEngine = lightingService.getEngine();

    loadAssets();
    createUI();
    playMusic();

    logger.debug("Initialising level 1 game screen entities");

    // Pass the renderer's camera to the terrain factory.
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    // Pass the same camera to the Level1GameArea so that
    // the parallax background can follow camera movement.
    level1GameArea = new Level1GameArea(terrainFactory, renderer.getCamera());
    level1GameArea.create();

    currentGameArea = level1GameArea;
    registerLevelSwap();

    player = level1GameArea.getPlayer();
    player.getEvents().addListener("respawnAtCheckpoint", () -> currentGameArea.respawn());
    player.getEvents().addListener("toggleMap", () -> currentGameArea.toggleLevelMap());

    // Follow the player with the camera.
    renderer.getCamera().setTarget(player);
    player.getEvents().addListener("deathAnimationFinished", this::onPlayerDeath);
    wheelOverlay = new SpinTheWheelOverlay(WheelConfig.ITEMS, player);
    player.getEvents().addListener("wheelTokenPickedUp", wheelOverlay::request);
    player.getEvents().addListener("spinTheWheel", wheelOverlay::request);
    pauseOverlay = new PauseMenuOverlay(game, level1GameArea);

    minigameOverlayManager = new MinigameOverlayManager();
    blackjackOverlay = new BlackjackOverlay(player, minigameOverlayManager);
  }

  private void onPlayerDeath() {
    ServiceLocator.getEntityService().scheduleRemoval(player);
    Gdx.app.postRunnable(
        () -> ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.LOSE));
  }

  private void registerLevelSwap() {
    Entity levelChanger = currentGameArea.getLevelChanger();
    if (levelChanger != null) {
      levelChanger.getEvents().addListener("triggerNextLevel", this::queueAreaSwap);
      levelChanger
          .getEvents()
          .addListener(
              "triggerNextLevel",
              (String level) -> SoundEffects.play("sounds/level_complete.wav", 0.5f));
    }
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
      case "level3":
        nextGameArea = new Level3GameArea(terrainFactory, renderer.getCamera(), player);
        break;
      case "boss":
        nextGameArea = new LevelBossGameArea(terrainFactory, renderer.getCamera(), player);
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

    nextGameArea.create();
    currentGameArea.dispose();
    currentGameArea = nextGameArea;
    nextGameArea = null;
    registerLevelSwap();

    renderer.getCamera().setTarget(currentGameArea.getPlayer());
  }

  @Override
  public void render(float delta) {
    // at the start of the render, if there's been a level swap queued, safely perform the swap
    if (levelSwapQueued) {
      performLevelSwap();
      levelSwapQueued = false;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
        && !ServiceLocator.getEntityService().getSettingsOpen()) {
      pauseOverlay.request();
    }

    // F3 toggles debug mode: physics outlines plus player and mouse coordinates
    if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
      var debug = ServiceLocator.getRenderService().getDebug();
      debug.setActive(!debug.getActive());
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
      blackjackOverlay.request();
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
      cheats = !cheats;
      if (cheats) {
        gravity = player.getComponent(PhysicsComponent.class).getBody().getGravityScale();
        player.getComponent(PhysicsComponent.class).getBody().setGravityScale(0);
        player.getComponent(KeyboardPlayerInputComponent.class).toggleCheats();
      } else {
        player.getComponent(PhysicsComponent.class).getBody().setGravityScale(gravity);
        player.getComponent(KeyboardPlayerInputComponent.class).toggleCheats();
      }
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      if (level.equals("level1")) {
        level = "level2";
      } else if (level.equals("level2")) {
        level = "level3";
      } else if (level.equals("level3")) {
        level = "boss";
      } else if (level.equals("boss")) {
        level = "none";
      }
      queueAreaSwap(level);
    }

    physicsEngine.update();
    ServiceLocator.getEntityService().update();
    renderer.render();
    renderer.render(lightingEngine);
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
    lightingEngine.dispose();
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
                "images/ui/title_odysseus_logo.png",
                "images/health/red_heart.png",
                "images/health/PixelArt_HeartBack.png",
                "images/health/Damaged_heart.png",
                "images/health/Last_Health.png",
                "images/Buttons/apply_up_btn.png",
                "images/Buttons/apply_down_btn.png",
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
                "images/ui/controls_graphic.png",
                "images/Buttons/restart_up_btn.png",
                "images/Buttons/restart_down_btn.png",
                "images/Buttons/main_menu_up_btn.png",
                "images/Buttons/main_menu_down_btn.png",
                "images/Buttons/exit_game_up_btn.png",
                "images/Buttons/exit_game_down_btn.png",
                "images/Buttons/back_up_btn.png",
                "images/Buttons/back_down_btn.png",
                "images/ui/scroll_bg.png",
                "images/Buttons/exit_down_btn.png",
                "images/projectiles/rope_arrow.png",
                "images/projectiles/fire_arrow.png",
                "images/projectiles/ice_arrow.png",
                "images/backgrounds/main_menu_bg_2.png",
                "images/ui/settings_box.png"));
    // The player's HUD (gold coin, arrow wheel and inventory icons) keeps these textures across
    // level swaps. If a game area owned them, unloading that area would leave the HUD drawing
    // disposed textures as black boxes in the next level.
    paths.add(ItemFactory.GOLD_TEXTURE);
    paths.add("images/projectiles/poison_arrow.png");
    paths.addAll(List.of(PauseButtonDisplay.extraTextures()));
    paths.addAll(List.of(WheelConfig.TEXTURES));
    paths.addAll(List.of(BlackjackConfig.TEXTURES));
    paths.addAll(List.of(ItemAssets.getTextures()));
    return paths.stream().distinct().toArray(String[]::new);
  }

  /**
   * The game's atlases that should not be unloaded by each game area
   *
   * @return every atlas the levels need
   */
  private static String[] createAtlas() {
    List<String> paths = new ArrayList<>(List.of("images/player/player.atlas"));
    return paths.toArray(new String[0]);
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(mainGameTextures);
    resourceService.loadTextureAtlases(mainGameAtlas);
    resourceService.loadSounds(WheelConfig.SOUNDS);
    resourceService.loadMusic(gameEndMusic);
    SoundEffects.load(resourceService);
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
    SoundEffects.unload(resourceService);
    resourceService.unloadAssets(gameEndMusic);
    resourceService.unloadAssets(gameplayMusicFiles);
    ButtonSound.unload(resourceService);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(gameplayMusic, Music.class);
    music.setLooping(true);
    GameVolume.setMusicVolume(music, 0.05f);
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
        .addComponent(new CoordinateDisplay(renderer.getCamera()))
        .addComponent(new MainGameActions(this.game))
        .addComponent(new PauseButtonDisplay(() -> pauseOverlay.request()))
        .addComponent(
            new GameEndDisplay(GameEndState.LOSE)) // Add GameEndDisplay component to the UI entity
        .addComponent(new GameEndActions(this.game))
        .addComponent(new Terminal(game, GdxGame.ScreenType.LEVEL_1_GAME))
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay());

    ServiceLocator.getEntityService().register(ui);
  }
}
