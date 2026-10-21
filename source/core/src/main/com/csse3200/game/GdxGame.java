package com.csse3200.game;

import static com.badlogic.gdx.Gdx.app;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerSnapshot;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.SaveFile;
import com.csse3200.game.files.SaveManager;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.screens.CutsceneScreen;
import com.csse3200.game.screens.LevelsGameScreen;
import com.csse3200.game.screens.LoadSaveScreen;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.screens.MainMenuScreen;
import com.csse3200.game.screens.SandboxGameScreen;
import com.csse3200.game.screens.SettingsScreen;
import com.csse3200.game.screens.ThankYouScreen;
import com.csse3200.game.screens.TransitionScreen;
import com.csse3200.game.screens.minigames.BlackjackScreen;
import com.csse3200.game.screens.minigames.CyclopsMinigameRoomScreen;
import com.csse3200.game.screens.minigames.spinthewheel.SpinTheWheelScreen;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.ThemedCursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of the non-platform-specific game logic. Controls which screen is currently running.
 * The current screen triggers transitions to other screens. This works similarly to a finite state
 * machine (See the State Pattern).
 */
public class GdxGame extends Game {
  private static final Logger logger = LoggerFactory.getLogger(GdxGame.class);

  private boolean transitioning = false;

  // Prevents the intro cutscene from triggering multiple times
  // in one game session.
  private boolean introStarted = false;

  // Whether the movement tutorial has been completed in this session; persisted in the save so
  // it only auto-plays once per save slot.
  private boolean tutorialCompleted = false;
  // Whether tutorial prompts are wanted for this save; false once tut0's offer is declined.
  private boolean tutorialsAccepted = true;

  private PlayerSnapshot playerSnapshot;

  /**
   * Slot being written to during this play session; -1 when no save is active (e.g. Sandbox or a
   * session started without going through Play/Continue), in which case autosaves are skipped.
   */
  private int activeSaveSlot = -1;

  /**
   * Save loaded from disk, waiting to be applied to the world once its level's game area exists.
   * Consumed by {@link #applyPendingSaveWorld(GameArea, String)}.
   */
  private SaveFile pendingSave;

  /** Levels a save file may name; anything else is rejected and treated as level 1. */
  private static final String LEVEL_1 = "level1";

  private static final String LEVEL_2 = "level2";
  private static final String LEVEL_3 = "level3";
  private static final Set<String> VALID_LEVELS = Set.of(LEVEL_1, LEVEL_2, LEVEL_3, "boss");

  @Override
  public void create() {
    logger.info("Creating game");
    loadSettings();
    applyDefaultClearColor();
    ThemedCursor.install();

    setScreen(ScreenType.MAIN_MENU);
  }

  /**
   * Restores the default light-yellow clear colour used behind transparent terrain (e.g. Sandbox).
   *
   * <p>Cutscenes set a black clear colour while they are on screen; this puts the game colour back.
   */
  public static void applyDefaultClearColor() {
    if (Gdx.gl == null) {
      return;
    }
    Gdx.gl.glClearColor(248f / 255f, 249f / 255f, 178f / 255f, 1);
  }

  /** Loads the game's settings. */
  private void loadSettings() {
    logger.debug("Loading game settings");
    UserSettings.Settings settings = UserSettings.get();
    UserSettings.applySettings(settings);
  }

  /**
   * Sets the game's screen to a new screen of the provided type.
   *
   * @param screenType screen type
   */
  public void setScreen(ScreenType screenType) {
    if (screenType == ScreenType.MINIGAME_BLACKJACK) {
      capturePlayerBeforeBlackjack();
    }
    logger.info("Setting game screen to {}", screenType);
    Screen currentScreen = getScreen();
    if (currentScreen != null) {
      currentScreen.dispose();
    }
    setScreen(createScreen(screenType));
  }

  /**
   * Fades the current screen to black, swaps to the target screen, then fades back in. Ignored if a
   * transition is already in progress.
   *
   * @param screenType screen type to transition to
   */
  public void transitionTo(ScreenType screenType) {
    transitionTo(screenType, TransitionScreen.DEFAULT_FADE_DURATION);
  }

  /** Fades to a pre-created screen (used for ad-hoc end screens). */
  public void transitionTo(Screen toScreen, float fadeDuration) {
    if (transitioning) {
      return;
    }
    logger.info("Transitioning to custom screen");
    transitioning = true;
    setScreen(new TransitionScreen(this, getScreen(), toScreen, fadeDuration));
  }

  /**
   * Fades the current screen to black, swaps to the target screen, then fades back in, using the
   * given length for each fade phase. Ignored if a transition is already in progress.
   *
   * @param screenType screen type to transition to
   * @param fadeDuration length in seconds of each fade phase
   */
  public void transitionTo(ScreenType screenType, float fadeDuration) {
    if (transitioning) {
      return;
    }
    logger.info("Transitioning game screen to {}", screenType);
    transitioning = true;
    setScreen(new TransitionScreen(this, getScreen(), screenType, fadeDuration));
  }

  /** Starts a validated cutscene and creates the destination level after it completes. */
  public void startCutscene(CutsceneLoader.LoadedCutscene cutscene, ScreenType destination) {
    if (destination == ScreenType.MINIGAME_BLACKJACK) {
      capturePlayerBeforeBlackjack();
    }
    logger.info("Starting cutscene {}", cutscene.getName());
    Screen currentScreen = getScreen();
    if (currentScreen != null) {
      currentScreen.dispose();
    }
    setScreen(new CutsceneScreen(this, cutscene, destination));
  }

  /** Plays a story cutscene, continuing to its destination if the cutscene is unavailable. */
  public void playCutscene(String name, ScreenType destination) {
    CutsceneLoader.Result result = new CutsceneLoader().load(name);
    if (result.isSuccess()) {
      startCutscene(result.getCutscene(), destination);
    } else {
      logger.warn("Cutscene {} unavailable: {}", name, result.getError());
      transitionTo(destination);
    }
  }

  /** Starts the initial cutscene once per game session, then falls back to level 1. */
  public void startInitialCutscene() {
    if (introStarted) {
      transitionTo(ScreenType.LEVEL_1_GAME);
      return;
    }

    // logging for if the cutscene is not available, and fallback to level 1
    CutsceneLoader.Result result = new CutsceneLoader().load("cutscene1");
    if (!result.isSuccess()) {
      logger.warn("Initial cutscene unavailable, skipping to level 1: {}", result.getError());
      transitionTo(ScreenType.LEVEL_1_GAME);
      return;
    }

    // For now, the intro cutscene will be recorded as a bool until a proper save state is
    // implemented
    introStarted = true;
    startCutscene(result.getCutscene(), ScreenType.LEVEL_1_GAME);
  }

  /**
   * Starts a brand-new game in the first empty save slot, then runs the normal new-game flow (intro
   * cutscene, then level 1). When every slot is filled the save-select screen is shown instead, so
   * the player can delete a save to make room.
   */
  public void startNewGame() {
    int slot = SaveManager.findEmptySlot(SaveManager.listAll());
    if (slot < 0) {
      logger.info("No empty save slots, showing the save-select screen");
      transitionTo(ScreenType.LOAD_SAVE);
      return;
    }
    logger.info("Starting a new game in save slot {}", slot);
    playerSnapshot = null;
    pendingSave = null;
    activeSaveSlot = slot;
    introStarted = false;
    tutorialCompleted = false;
    tutorialsAccepted = true;
    SaveManager.write(slot, SaveManager.newGameSave(slot));
    startInitialCutscene();
  }

  /**
   * Loads a save slot: records it as the active slot, queues its state for the gameplay screen and
   * transitions to the saved level, skipping the intro cutscene.
   *
   * @param slot slot index to load
   */
  public void loadSave(int slot) {
    SaveFile save = SaveManager.read(slot);
    if (save == null) {
      logger.warn("Cannot load save slot {}: no readable file", slot);
      return;
    }
    if (!VALID_LEVELS.contains(save.currentLevel)) {
      logger.warn("Save slot {} names unknown level {}, loading level 1", slot, save.currentLevel);
      save.currentLevel = LEVEL_1;
    }
    logger.info("Loading save slot {} at level {}", slot, save.currentLevel);
    playerSnapshot = null;
    pendingSave = save;
    activeSaveSlot = slot;
    introStarted = true;
    tutorialCompleted = save.tutorialCompleted;
    tutorialsAccepted = save.tutorialsAccepted;
    transitionTo(levelScreenType(save.currentLevel));
  }

  /**
   * Writes the active save slot from the live game state. Called whenever a checkpoint is activated
   * (the game's only save point); does nothing without an active slot.
   *
   * @param level current level name
   * @param area the current game area
   */
  public void autosave(String level, GameArea area) {
    if (activeSaveSlot < 0 || area == null || area.getPlayer() == null) {
      return;
    }
    try {
      SaveFile save =
          SaveManager.capture(
              activeSaveSlot, level, area.getPlayer(), area.getActiveCheckpointPositions());
      save.tutorialCompleted = tutorialCompleted;
      save.tutorialsAccepted = tutorialsAccepted;
      SaveManager.write(activeSaveSlot, save);
    } catch (RuntimeException e) {
      logger.warn("Autosave of slot {} failed: {}", activeSaveSlot, e.getMessage());
    }
  }

  /** Flags the active save as completed (won the game) so the save-select screen can show it. */
  public void markSaveCompleted() {
    if (activeSaveSlot < 0) {
      return;
    }
    SaveFile save = SaveManager.read(activeSaveSlot);
    if (save == null) {
      return;
    }
    save.completed = true;
    SaveManager.write(activeSaveSlot, save);
  }

  /** Whether the movement tutorial has been completed in this session's save. */
  public boolean isTutorialCompleted() {
    return tutorialCompleted;
  }

  /**
   * Whether tutorial prompts are wanted for this save. False once the player declines tut0's offer,
   * in which case no tutorial auto-plays again for this slot.
   */
  public boolean areTutorialsAccepted() {
    return tutorialsAccepted;
  }

  /**
   * Records the player accepting tut0's offer, persisting the choice when a save slot is active.
   * The default is acceptance, so this mainly writes the explicit choice to the save.
   */
  public void acceptTutorials() {
    setTutorialsAccepted(true);
  }

  /**
   * Records the player declining tut0's offer so no tutorial prompt auto-plays for this save slot
   * again, persisting the choice when a save slot is active.
   */
  public void declineTutorials() {
    setTutorialsAccepted(false);
  }

  private void setTutorialsAccepted(boolean accepted) {
    tutorialsAccepted = accepted;
    if (activeSaveSlot < 0) {
      return;
    }
    SaveFile save = SaveManager.read(activeSaveSlot);
    if (save == null) {
      return;
    }
    save.tutorialsAccepted = accepted;
    SaveManager.write(activeSaveSlot, save);
  }

  /**
   * Marks the movement tutorial done so it never auto-plays again for this save slot, persisting
   * the flag when a save slot is active. Without one (e.g. Sandbox) the flag lasts the session.
   */
  public void markTutorialCompleted() {
    tutorialCompleted = true;
    if (activeSaveSlot < 0) {
      return;
    }
    SaveFile save = SaveManager.read(activeSaveSlot);
    if (save == null) {
      return;
    }
    save.tutorialCompleted = true;
    SaveManager.write(activeSaveSlot, save);
  }

  /**
   * Applies the loaded save's player stats (health, max health, gold, inventory) to the player
   * entity as soon as it exists. No-op when no save is being loaded or the save has no recorded
   * player state (a brand-new game keeps the level defaults).
   *
   * @param player the player entity
   */
  public void applyPendingSavePlayer(Entity player) {
    if (pendingSave == null || pendingSave.player == null || player == null) {
      return;
    }
    pendingSave.player.applyTo(player);
  }

  /**
   * Applies the loaded save's world state (activated checkpoints, and with them the respawn point)
   * to the given level. Only applies when the area is the save's level; otherwise the pending save
   * is kept for a later swap. Consumes the pending save once applied.
   *
   * <p>When the save recorded checkpoints the player is placed at the last one. A save without
   * checkpoints (a game that has not reached a checkpoint yet) keeps the level's own spawn point.
   *
   * @param area the freshly created game area
   * @param level the level that area represents
   * @return true if the pending save was applied
   */
  public boolean applyPendingSaveWorld(GameArea area, String level) {
    if (pendingSave == null || area == null || !level.equals(pendingSave.currentLevel)) {
      return false;
    }
    logger.info("Restoring saved progress for level {}", level);
    List<GridPoint2> positions = new ArrayList<>();
    if (pendingSave.activatedCheckpoints != null) {
      for (SaveFile.CheckpointSave checkpoint : pendingSave.activatedCheckpoints) {
        positions.add(checkpoint.toGridPoint2());
      }
    }
    if (!positions.isEmpty()) {
      area.restoreCheckpoints(positions);
      // Re-activating checkpoints moved the respawn point: place the player there.
      area.respawn();
    }
    pendingSave = null;
    return true;
  }

  /** The slot currently being written to, or -1 when no save is active. */
  public int getActiveSaveSlot() {
    return activeSaveSlot;
  }

  /** Screen type for a saved level name. */
  private static ScreenType levelScreenType(String level) {
    return switch (level) {
      case LEVEL_2 -> ScreenType.LEVEL_2_GAME;
      case LEVEL_3 -> ScreenType.LEVEL_3_GAME;
      case "boss" -> ScreenType.LEVEL_BOSS_GAME;
      default -> ScreenType.LEVEL_1_GAME;
    };
  }

  /**
   * Called by {@link TransitionScreen} once its fade-in has finished, handing control to the screen
   * it faded into.
   *
   * @param toScreen the screen the transition faded into
   */
  public void finishTransition(Screen toScreen) {
    Screen transitionScreen = getScreen();
    transitioning = false;
    // TransitionScreen already showed and resized the destination before fading it in.
    // Calling setScreen here would initialise screens that use show() a second time.
    screen = toScreen;
    if (transitionScreen != null) {
      transitionScreen.hide();
      transitionScreen.dispose();
    }
  }

  /**
   * Collects a snapshot of the player's state components (gold, health, inventory)
   *
   * @param player the player entity to save
   */
  public void savePlayerState(Entity player) {
    if (player == null) {
      logger.warn("Attempting to save a null player");
      return;
    }
    playerSnapshot = PlayerSnapshot.capture(player);
  }

  /** Capture the actual player's gold before opening Blackjack or its entry cutscene. */
  private void capturePlayerBeforeBlackjack() {
    if (playerSnapshot != null || ServiceLocator.getEntityService() == null) {
      return;
    }
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      if (entity.getComponent(PlayerActions.class) != null) {
        savePlayerState(entity);
        return;
      }
    }
    logger.warn("No live player found before Blackjack; saved gold was not captured");
  }

  /** Returns the saved player's gold, or null when no player state has been captured. */
  public Integer getSavedPlayerGold() {
    return playerSnapshot == null ? null : playerSnapshot.getGold();
  }

  /** Updates only the player's saved gold without changing health or inventory items. */
  public void updateSavedPlayerGold(int gold) {
    if (playerSnapshot != null) {
      playerSnapshot = playerSnapshot.withGold(gold);
    }
  }

  /**
   * If a player snapshot is available then update the given player entity to match the stored
   * snapshot data (gold, health, inventory)
   *
   * @param player the player to update with the stored snapshot
   */
  public void restorePlayerState(Entity player) {
    if (playerSnapshot == null) {
      return;
    }
    playerSnapshot.applyTo(player);
    playerSnapshot = null;
  }

  @Override
  public void dispose() {
    logger.debug("Disposing of current screen");
    if (getScreen() != null) {
      getScreen().dispose();
    }
    ThemedCursor.dispose();
  }

  /**
   * Create a new screen of the provided type.
   *
   * @param screenType screen type
   * @return new screen
   */
  public Screen createScreen(ScreenType screenType) {
    switch (screenType) {
      case THANK_YOU:
        return new ThankYouScreen(this);
      case MAIN_MENU:
        return new MainMenuScreen(this);
      case LOAD_SAVE:
        return new LoadSaveScreen(this);
      case MAIN_GAME:
        return new MainGameScreen(this);
      case LEVEL_1_GAME:
        return new LevelsGameScreen(this);
      case SANDBOX:
        return new SandboxGameScreen(this);
      case LEVEL_2_GAME:
        LevelsGameScreen screen2 = new LevelsGameScreen(this);
        screen2.queueAreaSwap(LEVEL_2);
        return screen2;
      case LEVEL_3_GAME:
        LevelsGameScreen screen3 = new LevelsGameScreen(this);
        screen3.queueAreaSwap(LEVEL_3);
        return screen3;
      case LEVEL_BOSS_GAME:
        LevelsGameScreen bossScreen = new LevelsGameScreen(this);
        bossScreen.queueAreaSwap("boss");
        return bossScreen;
      case SETTINGS:
        return new SettingsScreen(this);
      case CYCLOPS_MINIGAME:
        return new CyclopsMinigameRoomScreen(this);
      case MINIGAME_SPIN_THE_WHEEL:
        return new SpinTheWheelScreen(this);
      case MINIGAME_BLACKJACK:
        return new BlackjackScreen(this);
      case STORY_BLACKJACK:
        return new BlackjackScreen(this);
      default:
        return null;
    }
  }

  public enum ScreenType {
    THANK_YOU,
    MAIN_MENU,
    LOAD_SAVE,
    MAIN_GAME,
    LEVEL_1_GAME,
    SANDBOX,
    LEVEL_2_GAME,
    LEVEL_3_GAME,
    LEVEL_BOSS_GAME,
    SETTINGS,
    SETTINGS_FROM_PAUSE,
    MINIGAME_SELECT,
    MINIGAME_BLACKJACK,
    STORY_BLACKJACK,
    CYCLOPS_MINIGAME,
    MINIGAME_SPIN_THE_WHEEL
  }

  /** Exit the game. */
  public void exit() {
    app.exit();
  }
}
