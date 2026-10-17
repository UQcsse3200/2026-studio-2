
package com.csse3200.game;

import static com.badlogic.gdx.Gdx.app;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.csse3200.game.components.player.PlayerSnapshot;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.screens.CutsceneScreen;
import com.csse3200.game.screens.LevelsGameScreen;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.screens.MainMenuScreen;
import com.csse3200.game.screens.SandboxGameScreen;
import com.csse3200.game.screens.SettingsFromPauseScreen;
import com.csse3200.game.screens.SettingsScreen;
import com.csse3200.game.screens.TransitionScreen;
import com.csse3200.game.screens.minigames.BlackjackScreen;
import com.csse3200.game.screens.minigames.CyclopsMinigameRoomScreen;
import com.csse3200.game.screens.minigames.MinigameSelectScreen;
import com.csse3200.game.screens.minigames.spinthewheel.SpinTheWheelScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of the non-platform-specific game logic.
 * Controls which screen is currently running.
 *
 * The current screen triggers transitions to other screens.
 * This works similarly to a finite state machine (State Pattern).
 */
public class GdxGame extends Game {

  private static final Logger logger =
      LoggerFactory.getLogger(GdxGame.class);

  private boolean transitioning = false;

  // Prevents the intro cutscene from triggering multiple times
  // in one game session.
  private boolean introStarted = false;

  private PlayerSnapshot playerSnapshot;

  @Override
  public void create() {
    logger.info("Creating game");

    loadSettings();
    applyDefaultClearColor();

    // TEMPORARY: Open Level 3 directly to debug checkpoints.
    logger.info("Opening Level 3 directly for checkpoint debugging");
    setScreen(ScreenType.LEVEL_1_GAME);
  }

  /**
   * Restores the default light-yellow clear colour used behind
   * transparent terrain (e.g. Sandbox).
   */
  public static void applyDefaultClearColor() {
    if (Gdx.gl == null) {
      return;
    }

    Gdx.gl.glClearColor(
        248f / 255f,
        249f / 255f,
        178f / 255f,
        1
    );
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
    logger.info("Setting game screen to {}", screenType);

    Screen currentScreen = getScreen();

    if (currentScreen != null) {
      currentScreen.dispose();
    }

    setScreen(createScreen(screenType));
  }

  /**
   * Fades the current screen to black, swaps to the target screen,
   * then fades back in. Ignored if a transition is already in progress.
   *
   * @param screenType screen type to transition to
   */
  public void transitionTo(ScreenType screenType) {
    if (transitioning) {
      return;
    }

    logger.info("Transitioning game screen to {}", screenType);
    transitioning = true;

    setScreen(new TransitionScreen(this, getScreen(), screenType));
  }

  /**
   * Starts a validated cutscene and creates the destination level
   * after it completes.
   */
  public void startCutscene(
      CutsceneLoader.LoadedCutscene cutscene,
      ScreenType destination
  ) {
    logger.info("Starting cutscene {}", cutscene.getName());

    Screen currentScreen = getScreen();

    if (currentScreen != null) {
      currentScreen.dispose();
    }

    setScreen(new CutsceneScreen(this, cutscene, destination));
  }

  /**
   * Starts the initial cutscene once per game session,
   * then falls back to Level 1.
   */
  public void startInitialCutscene() {
    if (introStarted) {
      transitionTo(ScreenType.LEVEL_1_GAME);
      return;
    }

    CutsceneLoader.Result result =
        new CutsceneLoader().load("cutscene1");

    if (!result.isSuccess()) {
      logger.debug(
          "Initial cutscene unavailable: {}",
          result.getError()
      );

      transitionTo(ScreenType.LEVEL_1_GAME);
      return;
    }

    introStarted = true;
    startCutscene(result.getCutscene(), ScreenType.LEVEL_1_GAME);
  }

  /**
   * Called by TransitionScreen once its fade-in has finished,
   * handing control to the screen it faded into.
   *
   * @param toScreen the screen the transition faded into
   */
  public void finishTransition(Screen toScreen) {
    Screen transitionScreen = getScreen();

    transitioning = false;
    setScreen(toScreen);

    if (transitionScreen != null) {
      transitionScreen.dispose();
    }
  }

  /**
   * Collects a snapshot of the player's state components
   * (gold, health, inventory).
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

  /**
   * Restores the player's state if a snapshot is available.
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
  }

  /**
   * Creates a screen of the provided type.
   *
   * @param screenType screen type
   * @return the created screen
   */
  public Screen createScreen(ScreenType screenType) {
    switch (screenType) {
      case MAIN_MENU:
        return new MainMenuScreen(this);

      case MAIN_GAME:
        return new MainGameScreen(this);

      case LEVEL_1_GAME:
        return new LevelsGameScreen(this);

      case SANDBOX:
        return new SandboxGameScreen(this);

      case LEVEL_2_GAME:
        LevelsGameScreen screen2 = new LevelsGameScreen(this);
        screen2.queueAreaSwap("level2");
        return screen2;

      case LEVEL_3_GAME:
        LevelsGameScreen screen3 = new LevelsGameScreen(this);
        screen3.queueAreaSwap("level3");
        return screen3;

      case LEVEL_BOSS_GAME:
        LevelsGameScreen bossScreen = new LevelsGameScreen(this);
        bossScreen.queueAreaSwap("boss");
        return bossScreen;

      case SETTINGS:
        return new SettingsScreen(this);

      case SETTINGS_FROM_PAUSE:
        return new SettingsFromPauseScreen(this);

      case MINIGAME_SELECT:
        return new MinigameSelectScreen(this);

      case CYCLOPS_MINIGAME:
        return new CyclopsMinigameRoomScreen(this);

      case MINIGAME_SPIN_THE_WHEEL:
        return new SpinTheWheelScreen(this);

      case MINIGAME_BLACKJACK:
        return new BlackjackScreen(this);

      default:
        logger.error("Unknown screen type: {}", screenType);
        return null;
    }
  }

  public enum ScreenType {
    MAIN_MENU,
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
    CYCLOPS_MINIGAME,
    MINIGAME_SPIN_THE_WHEEL
  }

  /** Exit the game. */
  public void exit() {
    app.exit();
  }
}
