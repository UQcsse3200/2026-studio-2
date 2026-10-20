package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.minigames.MinigameOverlayInputComponent;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlurredBackdropDisplay;
import com.csse3200.game.ui.ScreenBlur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Blackjack UI overlay. Pauses normal gameplay and blocks gameplay input while active. */
public class BlackjackOverlay {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackOverlay.class);

  private static final String BLACKJACK_MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";

  private final Entity player;
  private final InventoryComponent inventory;
  private final MinigameOverlayManager overlayManager;

  private Entity overlay;
  private boolean openRequested;
  private boolean closing;

  /** Creates an overlay with no player, so no gold is available. */
  public BlackjackOverlay() {
    this(null, new MinigameOverlayManager());
  }

  /**
   * Creates an overlay that plays with the given player's gold.
   *
   * @param player player entity whose inventory holds the gold
   */
  public BlackjackOverlay(Entity player) {
    this(player, new MinigameOverlayManager());
  }

  /**
   * Creates an overlay that shares an overlay manager with other minigame overlays.
   *
   * @param player player entity whose inventory holds the gold
   * @param overlayManager manager ensuring only one minigame overlay is open at a time
   */
  public BlackjackOverlay(Entity player, MinigameOverlayManager overlayManager) {

    this.player = player;

    this.inventory = player == null ? null : player.getComponent(InventoryComponent.class);

    this.overlayManager = overlayManager;
  }

  /**
   * Asks for the overlay to open and pauses the game. The overlay is built on the next {@link
   * #afterRender()}, so the blurred backdrop captures a fully drawn frame. Ignored if this or
   * another minigame overlay is already open.
   */
  public void request() {
    if (overlay != null || openRequested) {
      return;
    }

    if (!overlayManager.tryOpen()) {
      return;
    }

    logger.info("Blackjack requested");

    openRequested = true;
    ServiceLocator.getEntityService().setPaused(true);
  }

  /** Opens a requested overlay. Call once per frame, after the game has been rendered. */
  public void afterRender() {
    if (!openRequested) {
      return;
    }

    openRequested = false;
    open();
  }

  private void open() {
    logger.info("Opening Blackjack UI");

    BlurredBackdropDisplay backdrop = new BlurredBackdropDisplay(ScreenBlur.capture());

    ServiceLocator.getResourceService().loadMusic(new String[] {BLACKJACK_MUSIC});

    ServiceLocator.getResourceService().loadSounds(BlackjackConfig.SOUNDS.toArray(String[]::new));

    ServiceLocator.getResourceService().loadAll();

    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);

    music.setLooping(true);
    GameVolume.setMusicVolume(music, 0.25f);
    music.play();

    int startingGold = inventory == null ? 0 : inventory.getGold();

    BlackjackDisplay display =
        new BlackjackDisplay(
            new Blackjack(startingGold), inventory, this::setSoundEnabled, this::requestClose);
    overlay =
        new Entity()
            .addComponent(backdrop)
            .addComponent(display)
            .addComponent(new BlackjackOverlayActions(this::requestClose))
            .addComponent(new MinigameOverlayInputComponent(this::requestClose, true));

    ServiceLocator.getEntityService().register(overlay);

    backdrop.toFront();
    display.toFront();

    logger.info("Blackjack UI opened");
  }

  /**
   * Delay removal very slightly so the Back mouse event completely finishes before the Blackjack
   * actors disappear.
   */
  private void requestClose() {
    if (closing || overlay == null) {
      return;
    }

    closing = true;

    logger.info("Blackjack close requested");

    /*
     * Immediately remove keyboard focus from the bet TextField.
     */
    if (ServiceLocator.getRenderService() != null
        && ServiceLocator.getRenderService().getStage() != null) {

      ServiceLocator.getRenderService().getStage().unfocusAll();
    }

    /*
     * Keep the Blackjack UI alive for a fraction of a second.
     * This prevents the Back click from falling through to the game UI.
     */
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            Gdx.app.postRunnable(BlackjackOverlay.this::closeNow);
          }
        },
        0.10f);
  }

  private void closeNow() {
    logger.info("Removing Blackjack UI");

    if (ServiceLocator.getResourceService().containsAsset(BLACKJACK_MUSIC, Music.class)) {

      Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);

      music.stop();

      ServiceLocator.getResourceService().unloadAssets(new String[] {BLACKJACK_MUSIC});
    }

    if (ServiceLocator.getRenderService() != null
        && ServiceLocator.getRenderService().getStage() != null) {

      ServiceLocator.getRenderService().getStage().unfocusAll();
    }

    if (overlay != null) {
      ServiceLocator.getEntityService().scheduleRemoval(overlay);

      overlay = null;
    }

    overlayManager.close();

    // Return the player to the shop after leaving Blackjack.
    if (player != null) {
      player.getEvents().trigger("openShop");
    }

    closing = false;

    logger.info("Blackjack removed - returned to shop");
  }

  private void setSoundEnabled(boolean enabled) {
    if (!ServiceLocator.getResourceService().containsAsset(BLACKJACK_MUSIC, Music.class)) {
      return;
    }

    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);

    if (enabled) {
      music.play();
    } else {
      music.pause();
    }
  }
}
