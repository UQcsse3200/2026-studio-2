package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.maingame.PauseMenuOverlay;
import com.csse3200.game.components.minigames.blackjack.Blackjack;
import com.csse3200.game.components.minigames.blackjack.BlackjackActions;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.blackjack.BlackjackDialogue;
import com.csse3200.game.components.minigames.blackjack.BlackjackDisplay;
import com.csse3200.game.components.minigames.blackjack.BlackjackRulesDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import java.util.stream.Stream;

/** Screen for the Blackjack minigame. */
public class BlackjackScreen extends MinigameScreen {
  private static final String BLACKJACK_MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";
  private static final int STARTING_BALANCE = 100;

  /**
   * Textures the game's pause menu and its in-game settings panel use, matching what the level
   * screens load for them.
   */
  static final String[] PAUSE_TEXTURES = {
    "images/ui/title_odysseus_logo.png",
    "images/Buttons/continue_up_btn.png",
    "images/Buttons/continue_down_btn.png",
    "images/Buttons/settings_up_btn.png",
    "images/Buttons/settings_down_btn.png",
    "images/Buttons/quit_up_btn.png",
    "images/Buttons/quit_down_btn.png",
    "images/Buttons/control_up_btn.png",
    "images/Buttons/control_down_btn.png",
    "images/ui/controls_graphic.png",
    "images/Buttons/back_up_btn.png",
    "images/Buttons/back_down_btn.png",
    "images/Buttons/exit_up_btn.png",
    "images/Buttons/exit_down_btn.png",
    "images/Buttons/apply_up_btn.png",
    "images/Buttons/apply_down_btn.png",
    "images/backgrounds/main_menu_bg_2.png",
    "images/ui/settings_box.png"
  };

  private PauseMenuOverlay pauseOverlay;
  private Terminal terminal;
  private BlackjackDisplay display;
  private boolean soundEnabled = true;
  private boolean musicPausedForMenu;
  private boolean disposed;

  /**
   * Creates the Blackjack screen. The story and the debug terminal open the same screen: winning
   * the match plays Cutscene 5 and continues to Level 3 either way.
   *
   * @param game game used for screen changes
   */
  public BlackjackScreen(GdxGame game) {
    super(game);
  }

  @Override
  public void show() {
    super.show();
    ServiceLocator.getResourceService().loadMusic(new String[] {BLACKJACK_MUSIC});
    ServiceLocator.getResourceService().loadAll();

    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);
    music.setLooping(true);
    GameVolume.setMusicVolume(music, 0.25f);
    music.play();
  }

  @Override
  protected String[] getTextures() {
    return Stream.concat(BlackjackConfig.TEXTURES.stream(), Stream.of(PAUSE_TEXTURES))
        .distinct()
        .toArray(String[]::new);
  }

  /**
   * Runs a frame. As in the levels, Esc opens or closes the game's pause menu, and the menu is
   * built after the frame is drawn so its blurred backdrop captures the table.
   */
  @Override
  public void render(float delta) {
    // Esc is polled directly, so ignore it while the terminal is open; F1 closes the terminal.
    boolean terminalOpen = terminal != null && terminal.isOpen();
    if (!terminalOpen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      togglePause();
    }

    super.render(delta);

    // Back can change screen during the frame, which disposes this screen and its services.
    if (disposed) {
      return;
    }
    pauseOverlay.afterRender();
    syncMusicWithPause();
  }

  /**
   * Opens the pause menu, or closes it if it is already open. Ignored while the settings panel is
   * open, as in the levels, and while the table is mid-deal or fading out after a win.
   */
  void togglePause() {
    if (ServiceLocator.getEntityService().getSettingsOpen() || !display.canPause()) {
      return;
    }
    pauseOverlay.request();
  }

  /** Pauses the Blackjack music while the pause menu is open, unless the player muted it. */
  private void syncMusicWithPause() {
    boolean paused = ServiceLocator.getEntityService().getPaused();
    if (paused == musicPausedForMenu) {
      return;
    }
    musicPausedForMenu = paused;
    if (soundEnabled) {
      setMusicPlaying(!paused);
    }
  }

  @Override
  protected String[] getSounds() {
    return BlackjackConfig.SOUNDS.toArray(String[]::new);
  }

  private void setSoundEnabled(boolean enabled) {
    soundEnabled = enabled;
    setMusicPlaying(enabled);
  }

  private void setMusicPlaying(boolean playing) {
    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);

    if (playing) {
      music.play();
    } else {
      music.pause();
    }
  }

  @Override
  public void dispose() {
    disposed = true;
    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);
    music.stop();
    ServiceLocator.getResourceService().unloadAssets(new String[] {BLACKJACK_MUSIC});
    super.dispose();
  }

  @Override
  protected Entity createUI() {
    Stage stage = ServiceLocator.getRenderService().getStage();

    // The player's gold is transferred via a snapshot across screen disposal.
    // A standalone launch without a player keeps its 100-gold practice balance.
    Integer savedGold = game.getSavedPlayerGold();
    int startingGold = savedGold != null ? savedGold : STARTING_BALANCE;
    Blackjack blackjack = new Blackjack(startingGold);

    InventoryComponent inventoryComponent =
        savedGold != null
            ? new InventoryComponent(startingGold) {
              @Override
              public void setGold(int gold) {
                super.setGold(gold);
                game.updateSavedPlayerGold(getGold());
              }
            }
            : new InventoryComponent(startingGold);

    pauseOverlay = new PauseMenuOverlay(game);
    display =
        new BlackjackDisplay(
            blackjack,
            inventoryComponent,
            this::setSoundEnabled,
            null,
            () -> BlackjackDialogue.startWinCutscene(game));

    terminal = new Terminal(game, GdxGame.ScreenType.LEVEL_3_GAME);
    Entity ui =
        new Entity()
            .addComponent(inventoryComponent)
            .addComponent(display)
            .addComponent(new BlackjackRulesDisplay())
            .addComponent(new BlackjackActions(game))
            .addComponent(new InputDecorator(stage, 10))
            // Debug terminal (F1), as in the levels. Blackjack leads into Level 3.
            .addComponent(terminal)
            .addComponent(ServiceLocator.getInputService().getInputFactory().createForTerminal())
            .addComponent(new TerminalDisplay());
    // The pause button sits under the sound button in the display, like Esc it toggles the menu.
    ui.getEvents().addListener(BlackjackDisplay.PAUSE_REQUESTED, this::togglePause);
    return ui;
  }
}
