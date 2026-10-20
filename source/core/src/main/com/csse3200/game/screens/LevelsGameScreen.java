package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.areas.Level2GameArea;
import com.csse3200.game.areas.Level3GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.SoundEffects;
import com.csse3200.game.components.TutorialComponent;
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
import com.csse3200.game.components.player.PlayerInteractionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.TutorialFactory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  private GameArea currentGameArea;
  private GameArea nextGameArea;
  private String level = LEVEL1;

  private final GdxGame game;
  private final SpinTheWheelOverlay wheelOverlay;
  private final PauseMenuOverlay pauseOverlay;
  private final BlackjackOverlay blackjackOverlay;
  private final MinigameOverlayManager minigameOverlayManager;
  private Entity player;
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
  // Cheat mode (Backspace): zero gravity plus free vertical movement. Starts off.
  private boolean cheats = false;
  // Gravity scale to restore when cheat mode is switched off.
  private float gravity = 1f;

  // The movement tutorial (tut1) auto-starts this many seconds after spawning in level 1, so
  // the player has a moment to look around first.
  private static final float MOVEMENT_TUTORIAL_DELAY = 3f;
  private static final String MOVEMENT_TUTORIAL = "tut1";

  private float movementTutorialDelay = MOVEMENT_TUTORIAL_DELAY;
  // Whether the movement tutorial has run, or is not wanted for this save.
  private boolean movementTutorialStarted = false;
  // Tutorial currently on screen, so a new one can dismiss it and dispose can clean it up.
  private TutorialComponent activeTutorial;
  // Checkpoint-linked tutorials already shown this session, so each plays at most once.
  private final Set<String> shownCheckpointTutorials = new HashSet<>();

  public LevelsGameScreen(GdxGame game) {
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
    wireCheckpointTutorials(level1GameArea);

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
      if (currentGameArea.requiresWorldResetOnDeath()) {
        // Rebuild the level world so one-shot triggers, spawners and platforms start
        // un-triggered; the player keeps its state and lands at the fresh area's spawn.
        queueAreaSwap(LEVEL3);
      }
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
      levelChanger.getEvents().addListener("triggerNextLevel", this::queueAreaSwap);
      levelChanger
          .getEvents()
          .addListener(
              "triggerNextLevel",
              (String nextLevel) -> SoundEffects.play("sounds/level_complete.wav", 0.5f));
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
      case LEVEL1:
        nextGameArea = new Level1GameArea(terrainFactory, renderer.getCamera());
        break;
      case LEVEL2:
        nextGameArea = new Level2GameArea(terrainFactory, renderer.getCamera(), player);
        break;
      case LEVEL3:
        nextGameArea = new Level3GameArea(terrainFactory, renderer.getCamera(), player);
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
    wireCheckpointTutorials(currentGameArea);

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

    updateMovementTutorial(delta);

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

    if (!terminalOpen && Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
      toggleCheatMode();
    }

    if (!minigameOverlayManager.isActive() && Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
      if (level.equals(LEVEL1)) {
        level = LEVEL2;
      } else if (level.equals(LEVEL2)) {
        level = LEVEL3;
      } else if (level.equals(LEVEL3)) {
        level = "none";
      }
      queueAreaSwap(level);
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

  /**
   * Auto-starts the level 1 movement tutorial a few seconds after spawning, so the player has a
   * moment to look around first. Runs once per screen and only when the save has not already
   * completed the tutorial; completion (reaching the final step) is persisted via {@link
   * GdxGame#markTutorialCompleted()}.
   */
  private void updateMovementTutorial(float delta) {
    if (movementTutorialStarted || !LEVEL1.equals(level)) {
      return;
    }
    if (game.isTutorialCompleted()) {
      movementTutorialStarted = true;
      return;
    }
    if (movementTutorialDelay > 0f) {
      movementTutorialDelay -= delta;
      return;
    }
    movementTutorialStarted = true;
    startNamedTutorial(MOVEMENT_TUTORIAL, game::markTutorialCompleted);
  }

  /**
   * Starts a named tutorial via {@link TutorialFactory}, dismissing any tutorial currently on
   * screen. Returns null when the tutorial's config is missing or invalid.
   */
  private TutorialComponent startNamedTutorial(String name, Runnable onFinished) {
    if (activeTutorial != null) {
      activeTutorial.dispose();
      activeTutorial = null;
    }
    activeTutorial = TutorialFactory.start(name, onFinished);
    if (activeTutorial == null) {
      logger.warn("Could not start tutorial {}", name);
    }
    return activeTutorial;
  }

  /**
   * Wires checkpoint-linked tutorials for a game area: when one of its checkpoints activates, the
   * level config may name a tutorial (e.g. tut5 at level 1's first checkpoint) to auto-start.
   */
  private void wireCheckpointTutorials(GameArea area) {
    area.setCheckpointListener(this::onCheckpointReached);
  }

  /** Starts the tutorial wired to the checkpoint at the given position, once per session. */
  private void onCheckpointReached(GridPoint2 position) {
    if (currentGameArea == null) {
      return;
    }
    String tutorial = currentGameArea.getConfig().getTutorialForCheckpoint(position);
    if (tutorial != null && shownCheckpointTutorials.add(tutorial)) {
      startNamedTutorial(tutorial, null);
    }
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
    if (activeTutorial != null) {
      activeTutorial.dispose();
      activeTutorial = null;
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
    paths.addAll(List.of(PauseButtonDisplay.extraTextures()));
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
    ui.addComponent(new InputDecorator(stage, 10))
        .addComponent(new PerformanceDisplay())
        .addComponent(new CoordinateDisplay(renderer.getCamera()))
        .addComponent(new MainGameActions(this.game))
        // Keep this a lambda: pauseOverlay is assigned after createUI() runs in the constructor,
        // and a
        // method reference would read it (still null) immediately.
        .addComponent(new PauseButtonDisplay(() -> pauseOverlay.request()))
        .addComponent(gameEndDisplay) // Add GameEndDisplay component to the UI entity
        .addComponent(new GameEndActions(this.game, this::restartAtCheckpoint))
        .addComponent(terminal)
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay());

    ServiceLocator.getEntityService().register(ui);
  }
}
