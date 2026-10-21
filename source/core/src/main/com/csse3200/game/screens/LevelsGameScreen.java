package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.components.gamearea.CoordinateDisplay;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.item.ItemAssets;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.PauseMenuOverlay;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.blackjack.BlackjackOverlay;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelOverlay;
import com.csse3200.game.components.minigames.spinthewheel.WheelConfig;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.player.PlayerInteractionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndActions;
import com.csse3200.game.ui.GameEndDisplay;
import com.csse3200.game.ui.GameEndState;
import com.csse3200.game.ui.GameplayCursor;
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
public class LevelsGameScreen extends BaseGameScreen {
  private static final String LEVEL3 = "level3";

  private static final String LEVEL2 = "level2";

  private static final String LEVEL1 = "level1";

  private static final Logger logger = LoggerFactory.getLogger(LevelsGameScreen.class);

  private static final String[] mainGameTextures = createTextures();
  private static final String[] mainGameAtlas = createAtlas();

  private boolean levelSwapQueued = false;
  private boolean levelCompleteQueued = false;
  private GameArea currentGameArea;
  private GameArea nextGameArea;
  private String level = LEVEL1;

  private final GdxGame game;
  private final SpinTheWheelOverlay wheelOverlay;
  private final PauseMenuOverlay pauseOverlay;
  private final BlackjackOverlay blackjackOverlay;
  private final MinigameOverlayManager minigameOverlayManager;
  private Entity player;
  private TutorialComponent tutorial;
  private GameEndDisplay gameEndDisplay;
  private Terminal terminal;
  private final GameplayCursor cursor = new GameplayCursor();
  private static final String GAMEPLAY_MUSIC = "sounds/gameplay_bg.ogg";
  private static final String[] gameplayMusicFiles = {GAMEPLAY_MUSIC};
  private static final String WIN_MUSIC = "sounds/Win_music.mp3";
  private static final String LOSE_MUSIC = "sounds/Death_music.ogg";
  private static final String[] gameEndMusic = {
    WIN_MUSIC, LOSE_MUSIC, "sounds/Main_menu_sound.mp3"
  };
  private final Level1GameArea level1GameArea;
  // Cheat mode (terminal command "cheats"): zero gravity plus free vertical movement. Starts off.
  private boolean cheats = false;
  // Gravity scale to restore when cheat mode is switched off.
  private float gravity = 1f;

  public LevelsGameScreen(GdxGame game) {
    this(game, true);
  }

  public LevelsGameScreen(GdxGame game, boolean startTutorial) {
    super();
    this.game = game;

    // Only this screen uses the save handler, so it is registered here; BaseGameScreen sets up
    // everything the game screens share
    ServiceLocator.registerGameSaveEventHandler(new EventHandler());

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

    // Checkpoints are the game's save points: GameArea forwards every activation here.
    ServiceLocator.getGameSaveEventHandler()
        .addListener("autosave", () -> game.autosave(level, currentGameArea));
    // Winning marks the active save as completed (shown on the save-select screen).
    ServiceLocator.getGameEndEventHandler()
        .addListener(
            "gameEnd",
            (GameEndState state) -> {
              if (state == GameEndState.WIN) {
                game.markSaveCompleted();
              }
            });

    player = level1GameArea.getPlayer();
    game.restorePlayerState(player);
    // A save loaded from disk contributes its player state here, and its world state too when the
    // save is in level 1; saves in other levels are restored after their area swap instead.
    game.applyPendingSavePlayer(player);
    game.applyPendingSaveWorld(currentGameArea, level);
    if (startTutorial && !game.isTutorialCompleted()) {
      tutorial = TutorialComponent.start(game::markTutorialCompleted);
    }
    player.getEvents().addListener("respawnAtCheckpoint", () -> currentGameArea.respawn());
    player.getEvents().addListener("toggleMap", () -> currentGameArea.toggleLevelMap());
    player.getEvents().addListener("toggleBossView", () -> currentGameArea.toggleBossCamera());

    // Follow the player with the camera.
    renderer.getCamera().setTarget(player);
    player.getEvents().addListener("deathAnimationFinished", this::onPlayerDeath);
    wheelOverlay = new SpinTheWheelOverlay(WheelConfig.ITEMS, player);
    player.getEvents().addListener("wheelTokenPickedUp", wheelOverlay::request);
    player.getEvents().addListener("spinTheWheel", wheelOverlay::request);
    pauseOverlay = new PauseMenuOverlay(game);

    minigameOverlayManager = new MinigameOverlayManager();
    blackjackOverlay = new BlackjackOverlay(player, minigameOverlayManager);
    player.getEvents().addListener("openBlackjack", blackjackOverlay::request);

    if (cheats) {
      level1GameArea.getPlayer().getComponent(PhysicsComponent.class).getBody().setGravityScale(0);
      level1GameArea.getPlayer().getComponent(KeyboardPlayerInputComponent.class).toggleCheats();
    }
  }

  private void onPlayerDeath() {
    // Keep the player entity so the restart button can revive at the last checkpoint.
    Gdx.app.postRunnable(
        () -> ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.LOSE));
  }

  /** Restart handler for the game-over panel: checkpoint respawn with health penalty. */
  private void restartAtCheckpoint() {
    if (currentGameArea != null) {
      currentGameArea.restart();
    }
    if (gameEndDisplay != null) {
      gameEndDisplay.hide();
    }
    try {
      Music music = ServiceLocator.getResourceService().getAsset(GAMEPLAY_MUSIC, Music.class);
      if (!music.isPlaying()) {
        music.setLooping(true);
        GameVolume.setMusicVolume(music, 0.05f);
        music.play();
      }
    } catch (Exception e) {
      logger.warn("Could not resume gameplay music: {}", e.getMessage());
    }
  }

  private void registerLevelSwap() {
    Entity levelChanger = currentGameArea.getLevelChanger();
    if (levelChanger != null) {
      levelChanger.getEvents().addListener("triggerNextLevel", this::completeLevel);
      levelChanger
          .getEvents()
          .addListener(
              "triggerNextLevel",
              (String nextLevel) -> SoundEffects.play("sounds/level_complete.wav", 1.0f));
    }
  }

  /** Runs the flashback between levels without disposing the world during its update. */
  private void completeLevel(String nextLevel) {
    if (levelCompleteQueued) {
      return;
    }
    String cutscene;
    GdxGame.ScreenType minigame;
    switch (nextLevel) {
      case LEVEL2:
        cutscene = "cutscene2";
        minigame = GdxGame.ScreenType.CYCLOPS_MINIGAME;
        break;
      case LEVEL3:
        cutscene = "cutscene4";
        minigame = GdxGame.ScreenType.STORY_BLACKJACK;
        break;
      default:
        queueAreaSwap(nextLevel);
        return;
    }
    levelCompleteQueued = true;
    game.savePlayerState(player);
    Gdx.app.postRunnable(() -> game.playCutscene(cutscene, minigame));
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
      case LEVEL1:
        nextGameArea = new Level1GameArea(terrainFactory, renderer.getCamera());
        break;
      case LEVEL2:
        nextGameArea = new Level2GameArea(terrainFactory, renderer.getCamera(), player);
        break;
      case LEVEL3:
        nextGameArea = new Level3GameArea(terrainFactory, renderer.getCamera(), player);
        break;
      case "boss":
        nextGameArea = new LevelBossGameArea(terrainFactory, renderer.getCamera(), player);
        break;
      default:
        return;
    }
    this.level = level;
    levelSwapQueued = true;
  }

  /**
   * Performs the level swap by disposing of the existing level, creating the new area and updating
   * internal references to keep track accurately of the current game area
   */
  private void performLevelSwap() {
    logger.info("Swapping level to new game area");

    boolean wasMapToggled = false;
    if (currentGameArea.isMapToggled()) {
      wasMapToggled = true;
      currentGameArea.toggleLevelMap();
    }

    nextGameArea.create();
    currentGameArea.dispose();
    currentGameArea = nextGameArea;
    nextGameArea = null;
    registerLevelSwap();

    renderer.getCamera().setTarget(currentGameArea.getPlayer());
    if (wasMapToggled) {
      currentGameArea.toggleLevelMap();
    }
    // Finish applying a loaded save now that its level's area exists (no-op otherwise).
    game.applyPendingSaveWorld(currentGameArea, level);
  }

  /**
   * Whether the shop closed since the last frame. An Esc press that closes the shop is handled by
   * the input chain, but the pause menu polls the key itself, so without this one press would do
   * both.
   */
  private boolean consumeShopClosed() {
    Entity current = currentGameArea == null ? null : currentGameArea.getPlayer();
    PlayerInteractionComponent interaction =
        current == null ? null : current.getComponent(PlayerInteractionComponent.class);
    return interaction != null && interaction.consumeShopClosed();
  }

  /**
   * Moves on to the next level, for the "skipLevel" terminal command.
   *
   * @return true if a level swap was queued, false if there is no next level or a minigame overlay
   *     is open
   */
  private boolean skipLevel() {
    if (minigameOverlayManager.isActive()) {
      return false;
    }
    String next;
    if (level.equals(LEVEL1)) {
      next = LEVEL2;
    } else if (level.equals(LEVEL2)) {
      next = LEVEL3;
    } else {
      logger.info("Already on the last level, nothing to skip to");
      return false;
    }
    queueAreaSwap(next);
    return true;
  }

  private void toggleCheatMode() {
    cheats = !cheats;
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    if (cheats) {
      gravity = physics.getBody().getGravityScale();
      physics.getBody().setGravityScale(0);
    } else {
      physics.getBody().setGravityScale(gravity);
    }
    player.getComponent(KeyboardPlayerInputComponent.class).toggleCheats();
  }

  @Override
  public void render(float delta) {
    // at the start of the render, if there's been a level swap queued, safely perform the swap
    if (levelSwapQueued) {
      performLevelSwap();
      levelSwapQueued = false;
    }

    // Overlay shortcuts bypass the input handler chain, so ignore them while the terminal is
    // open. F1 still toggles the terminal itself.
    boolean terminalOpen = terminal != null && terminal.isOpen();
    boolean shopJustClosed = consumeShopClosed();
    if (!terminalOpen
        && !shopJustClosed
        && !minigameOverlayManager.isActive()
        && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
        && !ServiceLocator.getEntityService().getSettingsOpen()) {
      pauseOverlay.request();
    }

    // F3 toggles debug mode: physics outlines plus player and mouse coordinates
    if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
      var debug = ServiceLocator.getRenderService().getDebug();
      debug.setActive(!debug.getActive());
    }

    currentGameArea.updateLevelMap();

    if (player.getPosition().y > 98) {
      player.getEvents().trigger("toggleBossView");
    }

    physicsEngine.update();
    ServiceLocator.getEntityService().update();
    renderer.render();
    renderer.render(lightingEngine);
    wheelOverlay.afterRender();
    pauseOverlay.afterRender();
    blackjackOverlay.afterRender();
    updateCursor();
  }

  /** Hides the pointer during normal play and shows it for menus and while aiming. */
  private void updateCursor() {
    boolean screenMenuOpen =
        (terminal != null && terminal.isOpen())
            || minigameOverlayManager.isActive()
            || (gameEndDisplay != null && gameEndDisplay.isVisible());
    cursor.update(currentGameArea == null ? null : currentGameArea.getPlayer(), screenMenuOpen);
  }

  @Override
  public void hide() {
    cursor.restore();
  }

  @Override
  public void dispose() {
    if (tutorial != null) {
      tutorial.dispose();
    }
    cursor.dispose();
    super.dispose();
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
    paths.addAll(WheelConfig.TEXTURES);
    paths.addAll(BlackjackConfig.TEXTURES);
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
    resourceService.loadSounds(WheelConfig.SOUNDS.toArray(String[]::new));
    resourceService.loadMusic(gameEndMusic);
    SoundEffects.load(resourceService);
    resourceService.loadMusic(gameplayMusicFiles);
    ButtonSound.load(resourceService);
    resourceService.loadAll();
  }

  @Override
  protected void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(mainGameTextures);
    resourceService.unloadAssets(mainGameAtlas);
    resourceService.unloadAssets(WheelConfig.SOUNDS.toArray(String[]::new));
    SoundEffects.unload(resourceService);
    resourceService.unloadAssets(gameEndMusic);
    resourceService.unloadAssets(gameplayMusicFiles);
    ButtonSound.unload(resourceService);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(GAMEPLAY_MUSIC, Music.class);
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

    gameEndDisplay = new GameEndDisplay(GameEndState.LOSE);
    terminal = new Terminal(game, GdxGame.ScreenType.LEVEL_1_GAME);
    terminal.addCommand(
        "cheats",
        args -> {
          toggleCheatMode();
          return true;
        });
    terminal.addCommand("skipLevel", args -> skipLevel());
    ui.addComponent(new InputDecorator(stage, 10))
        .addComponent(new PerformanceDisplay())
        .addComponent(new CoordinateDisplay(renderer.getCamera()))
        .addComponent(new MainGameActions(this.game))
        .addComponent(gameEndDisplay) // Add GameEndDisplay component to the UI entity
        .addComponent(new GameEndActions(this.game, this::restartAtCheckpoint))
        .addComponent(terminal)
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay());

    ServiceLocator.getEntityService().register(ui);
  }
}
