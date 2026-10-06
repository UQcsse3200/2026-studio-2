package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlurredBackdropDisplay;
import com.csse3200.game.ui.ScreenBlur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Blackjack UI overlay.
 *
 * <p>This overlay deliberately does NOT pause EntityService and does NOT install
 * a MinigameOverlayInputComponent. Normal gameplay input therefore remains
 * intact when the overlay is removed.
 */
public class BlackjackOverlay {
  private static final Logger logger =
      LoggerFactory.getLogger(BlackjackOverlay.class);

  private static final String BLACKJACK_MUSIC =
      "sounds/minigames/blackjack/blackjack-bgm.mp3";

  private final Entity player;
  private final InventoryComponent inventory;
  private final MinigameOverlayManager overlayManager;

  private Entity overlay;
  private boolean openRequested;
  private boolean closing;

  public BlackjackOverlay() {
    this(null, new MinigameOverlayManager());
  }

  public BlackjackOverlay(Entity player) {
    this(player, new MinigameOverlayManager());
  }

  public BlackjackOverlay(
      Entity player,
      MinigameOverlayManager overlayManager) {

    this.player = player;

    this.inventory =
        player == null
            ? null
            : player.getComponent(InventoryComponent.class);

    this.overlayManager = overlayManager;
  }

  public void request() {
    if (overlay != null || openRequested) {
      return;
    }

    if (!overlayManager.tryOpen()) {
      return;
    }

    logger.info("Blackjack requested");

    openRequested = true;

    // IMPORTANT:
    // Do NOT call EntityService.setPaused(true).
    // Do NOT disable player keyboard input.
  }

  public void afterRender() {
    if (!openRequested) {
      return;
    }

    openRequested = false;
    open();
  }

  private void open() {
    logger.info("Opening Blackjack UI");

    BlurredBackdropDisplay backdrop =
        new BlurredBackdropDisplay(ScreenBlur.capture());

    ServiceLocator.getResourceService()
        .loadMusic(new String[] {BLACKJACK_MUSIC});

    ServiceLocator.getResourceService()
        .loadSounds(BlackjackConfig.SOUNDS);

    ServiceLocator.getResourceService().loadAll();

    Music music =
        ServiceLocator.getResourceService()
            .getAsset(BLACKJACK_MUSIC, Music.class);

    music.setLooping(true);
    music.setVolume(0.25f);
    music.play();

    int startingGold =
        inventory == null ? 0 : inventory.getGold();

    BlackjackDisplay display =
        new BlackjackDisplay(
            new Blackjack(startingGold),
            inventory,
            this::setSoundEnabled,
            this::requestClose);

    /*
     * IMPORTANT:
     *
     * There is intentionally NO MinigameOverlayInputComponent here.
     *
     * BlackjackDisplay's full-screen table handles the UI mouse input.
     * We do not touch the player's gameplay keyboard input.
     */
    overlay =
        new Entity()
            .addComponent(backdrop)
            .addComponent(display)
            .addComponent(
                new BlackjackOverlayActions(this::requestClose));

    ServiceLocator.getEntityService().register(overlay);

    backdrop.toFront();
    display.toFront();

    logger.info("Blackjack UI opened");
  }

  /**
   * Delay removal very slightly so the Back mouse event completely finishes
   * before the Blackjack actors disappear.
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

      ServiceLocator.getRenderService()
          .getStage()
          .unfocusAll();
    }

    /*
     * Keep the Blackjack UI alive for a fraction of a second.
     * This prevents the Back click from falling through to the game UI.
     */
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            Gdx.app.postRunnable(
                BlackjackOverlay.this::closeNow);
          }
        },
        0.10f);
  }

  private void closeNow() {
    logger.info("Removing Blackjack UI");

    if (ServiceLocator.getResourceService()
        .containsAsset(BLACKJACK_MUSIC, Music.class)) {

      Music music =
          ServiceLocator.getResourceService()
              .getAsset(BLACKJACK_MUSIC, Music.class);

      music.stop();

      ServiceLocator.getResourceService()
          .unloadAssets(new String[] {BLACKJACK_MUSIC});
    }

    if (ServiceLocator.getRenderService() != null
        && ServiceLocator.getRenderService().getStage() != null) {

      ServiceLocator.getRenderService()
          .getStage()
          .unfocusAll();
    }

    if (overlay != null) {
      ServiceLocator.getEntityService()
          .scheduleRemoval(overlay);

      overlay = null;
    }

    overlayManager.close();

    // Return the player to the shop after leaving Blackjack.
    if (player != null) {
      player.getEvents().trigger("openShop");
    }

    /*
     * NO:
     *   setPaused(false)
     *   KeyboardPlayerInputComponent.setEnabled(...)
     *
     * We never changed those states in the first place.
     */

    closing = false;

    logger.info("Blackjack removed - normal gameplay untouched");
  }

  private void setSoundEnabled(boolean enabled) {
    if (!ServiceLocator.getResourceService()
        .containsAsset(BLACKJACK_MUSIC, Music.class)) {
      return;
    }

    Music music =
        ServiceLocator.getResourceService()
            .getAsset(BLACKJACK_MUSIC, Music.class);

    if (enabled) {
      music.play();
    } else {
      music.pause();
    }
  }
}






