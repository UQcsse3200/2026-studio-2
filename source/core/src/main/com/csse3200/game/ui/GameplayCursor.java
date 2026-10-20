package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
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
 * Disables the mouse pointer during normal play. It only shows while a menu is open (as the themed
 * arrow) or while the player holds right click to aim a bow or grapple (as the themed crosshair).
 *
 * <p>While disabled the pointer is captured: hidden and locked to the window, so it can't click
 * outside the game. The mouse position keeps updating, so aiming still works.
 */
public class GameplayCursor {
  private boolean visible = true;

  /**
   * Shows or disables the pointer for this frame.
   *
   * @param player the player entity, or null if there isn't one yet
   * @param screenMenuOpen whether a menu owned by the screen (terminal, game over panel, minigame)
   *     is open
   */
  public void update(Entity player, boolean screenMenuOpen) {
    boolean menuOpen = isMenuOpen(player, screenMenuOpen);
    boolean aiming = isAiming(player);
    // Pick the image before releasing the pointer so it never appears as the wrong one.
    if (menuOpen) {
      ThemedCursor.use(ThemedCursor.Style.ARROW);
    } else if (aiming) {
      ThemedCursor.use(ThemedCursor.Style.CROSSHAIR);
    }
    setVisible(menuOpen || aiming, takeAimStart(player));
  }

  /** Taken every frame so a stale aim start is never used for a later release. */
  private static GridPoint2 takeAimStart(Entity player) {
    KeyboardPlayerInputComponent input =
        player == null ? null : player.getComponent(KeyboardPlayerInputComponent.class);
    return input == null ? null : input.takePendingPointerPosition();
  }

  /**
   * Whether the pointer should be visible.
   *
   * @param player the player entity, or null if there isn't one yet
   * @param screenMenuOpen whether a menu owned by the screen is open
   * @return true if a menu is open or the player is aiming
   */
  public static boolean shouldShow(Entity player, boolean screenMenuOpen) {
    return isMenuOpen(player, screenMenuOpen) || isAiming(player);
  }

  private static boolean isMenuOpen(Entity player, boolean screenMenuOpen) {
    return screenMenuOpen || isGamePaused() || isPlayerMenuOpen(player);
  }

  /** Puts the normal arrow pointer back, e.g. when leaving the game screen. */
  public void restore() {
    ThemedCursor.use(ThemedCursor.Style.ARROW);
    setVisible(true, null);
  }

  /** Puts the normal pointer back before the screen is thrown away. */
  public void dispose() {
    restore();
  }

  /**
   * @return whether the pointer is currently shown
   */
  public boolean isVisible() {
    return visible;
  }

  private void setVisible(boolean show, GridPoint2 position) {
    if (show == visible || Gdx.input == null) {
      return;
    }
    if (show) {
      releasePointer(position);
    } else {
      Gdx.input.setCursorCatched(true);
    }
    visible = show;
  }

  /**
   * Releases the captured pointer at {@code position}, or where the mouse has been tracking if that
   * is null, clamped to the window. Otherwise it would reappear wherever it was when it was
   * captured.
   */
  private static void releasePointer(GridPoint2 position) {
    int x = position == null ? Gdx.input.getX() : position.x;
    int y = position == null ? Gdx.input.getY() : position.y;
    Gdx.input.setCursorCatched(false);
    if (Gdx.graphics != null && Gdx.graphics.getWidth() > 0 && Gdx.graphics.getHeight() > 0) {
      Gdx.input.setCursorPosition(
          MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - 1),
          MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - 1));
    }
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

  /**
   * Right click held with an aimed item selected: drawing the bow or grapple, or lining up a potion
   * throw.
   */
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
    return selected != null && selected.isAimed();
  }
}
