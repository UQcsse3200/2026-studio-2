package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.audio.Music;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.minigames.MinigameOverlayInputComponent;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlurredBackdropDisplay;
import com.csse3200.game.ui.ScreenBlur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shows the Blackjack minigame over the game, pausing it while the overlay is open. */
public class BlackjackOverlay {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackOverlay.class);
  private static final String BLACKJACK_MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";

  private boolean openRequested = false;
  private Entity overlay;
  private final InventoryComponent inventory;
  private final MinigameOverlayManager overlayManager;

  public BlackjackOverlay() {
    this(null, new MinigameOverlayManager());
  }

  public BlackjackOverlay(Entity player) {
    this(player, new MinigameOverlayManager());
  }

  public BlackjackOverlay(Entity player, MinigameOverlayManager overlayManager) {
    inventory = player == null ? null : player.getComponent(InventoryComponent.class);
    this.overlayManager = overlayManager;
  }

  /** Asks for Blackjack to open at the end of the current frame. */
  public void request() {
    if (overlay != null || openRequested || !overlayManager.tryOpen()) {
      return;
    }

    logger.debug("Opening the blackjack overlay");
    openRequested = true;
    ServiceLocator.getEntityService().setPaused(true);
  }

  /** Opens Blackjack after the game has rendered the frame used as its backdrop. */
  public void afterRender() {
    if (!openRequested) {
      return;
    }

    openRequested = false;
    open();
  }

  private void open() {
    BlurredBackdropDisplay backdrop = new BlurredBackdropDisplay(ScreenBlur.capture());

    ServiceLocator.getResourceService().loadMusic(new String[] {BLACKJACK_MUSIC});
    ServiceLocator.getResourceService().loadSounds(BlackjackConfig.SOUNDS);
    ServiceLocator.getResourceService().loadAll();

    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);
    music.setLooping(true);
    music.setVolume(0.25f);
    music.play();

    BlackjackDisplay display =
        new BlackjackDisplay(new Blackjack(100), inventory, this::setSoundEnabled, this::close);

    overlay =
        new Entity()
            .addComponent(backdrop)
            .addComponent(display)
            .addComponent(new BlackjackOverlayActions(this::close))
            .addComponent(new MinigameOverlayInputComponent(this::close));
    ServiceLocator.getEntityService().register(overlay);

    backdrop.toFront();
    display.toFront();
  }

  private void setSoundEnabled(boolean enabled) {
    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);

    if (enabled) {
      music.play();
    } else {
      music.pause();
    }
  }

  private void close() {
    if (overlay == null) {
      return;
    }

    if (ServiceLocator.getResourceService().containsAsset(BLACKJACK_MUSIC, Music.class)) {
      Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);
      music.stop();
      ServiceLocator.getResourceService().unloadAssets(new String[] {BLACKJACK_MUSIC});
    }

    ServiceLocator.getEntityService().scheduleRemoval(overlay);
    overlay = null;
    overlayManager.close();
    ServiceLocator.getEntityService().setPaused(false);
  }
}
