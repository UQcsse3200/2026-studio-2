package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.csse3200.game.components.inventory.BackpackDisplay;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.itemdictionary.ItemDictionaryDisplay;
import com.csse3200.game.components.player.ArrowWheelComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.player.PlayerInteractionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Hides the mouse pointer during normal play. It only shows while a menu is open or while the
 * player holds right click to aim a bow or grapple.
 *
 * <p>The pointer is hidden by swapping in a transparent cursor rather than catching it, because
 * aiming still needs the real mouse position.
 */
public class GameplayCursor {
  private static final int HIDDEN_CURSOR_SIZE = 16;

  private Cursor hiddenCursor;
  private boolean visible = true;

  /**
   * Shows or hides the pointer for this frame.
   *
   * @param player the player entity, or null if there isn't one yet
   * @param screenMenuOpen whether a menu owned by the screen (terminal, game over panel, minigame)
   *     is open
   */
  public void update(Entity player, boolean screenMenuOpen) {
    setVisible(shouldShow(player, screenMenuOpen));
  }

  /**
   * Whether the pointer should be visible.
   *
   * @param player the player entity, or null if there isn't one yet
   * @param screenMenuOpen whether a menu owned by the screen is open
   * @return true if a menu is open or the player is aiming
   */
  public static boolean shouldShow(Entity player, boolean screenMenuOpen) {
    return screenMenuOpen || isGamePaused() || isPlayerMenuOpen(player) || isAiming(player);
  }

  /** Puts the normal pointer back, e.g. when leaving the game screen. */
  public void restore() {
    setVisible(true);
  }

  /** Restores the normal pointer and frees the hidden cursor. */
  public void dispose() {
    restore();
    if (hiddenCursor != null) {
      hiddenCursor.dispose();
      hiddenCursor = null;
    }
  }

  /**
   * @return whether the pointer is currently shown
   */
  public boolean isVisible() {
    return visible;
  }

  private void setVisible(boolean show) {
    if (show == visible || Gdx.graphics == null) {
      return;
    }
    if (show) {
      Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
    } else {
      Gdx.graphics.setCursor(getHiddenCursor());
    }
    visible = show;
  }

  private Cursor getHiddenCursor() {
    if (hiddenCursor == null) {
      Pixmap pixmap = new Pixmap(HIDDEN_CURSOR_SIZE, HIDDEN_CURSOR_SIZE, Pixmap.Format.RGBA8888);
      pixmap.setBlending(Pixmap.Blending.None);
      pixmap.setColor(0f, 0f, 0f, 0f);
      pixmap.fill();
      hiddenCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
      pixmap.dispose();
    }
    return hiddenCursor;
  }

  /** Pause, settings, the shop and the minigame overlays all pause the entity service. */
  private static boolean isGamePaused() {
    EntityService entityService = ServiceLocator.getEntityService();
    return entityService != null && (entityService.getPaused() || entityService.getSettingsOpen());
  }

  private static boolean isPlayerMenuOpen(Entity player) {
    if (player == null) {
      return false;
    }
    BackpackDisplay backpack = player.getComponent(BackpackDisplay.class);
    ItemDictionaryDisplay dictionary = player.getComponent(ItemDictionaryDisplay.class);
    ArrowWheelComponent arrowWheel = player.getComponent(ArrowWheelComponent.class);
    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    return (backpack != null && backpack.isBackpackVisible())
        || (dictionary != null && dictionary.isDictionaryVisible())
        || (arrowWheel != null && arrowWheel.isOpen())
        || (interaction != null && interaction.isShopOpen());
  }

  /** Right click held with an arrow or rope arrow selected, i.e. drawing the bow or grapple. */
  private static boolean isAiming(Entity player) {
    if (player == null) {
      return false;
    }
    KeyboardPlayerInputComponent input = player.getComponent(KeyboardPlayerInputComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    if (input == null || inventory == null || !input.isRightMouseHeld()) {
      return false;
    }
    ItemType selected = inventory.getSelectedItem();
    return selected != null && selected.isArrow();
  }
}
