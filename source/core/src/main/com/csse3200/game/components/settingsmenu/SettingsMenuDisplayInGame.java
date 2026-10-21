package com.csse3200.game.components.settingsmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.services.ServiceLocator;

/**
 * The settings menu opened over the game from the pause menu. It is the same menu as {@link
 * SettingsMenuDisplay}, but closes back to the game instead of switching to another screen.
 */
public class SettingsMenuDisplayInGame extends SettingsMenuDisplay {
  private final Runnable onClose;
  private final String backgroundPath;

  public SettingsMenuDisplayInGame(GdxGame game, Runnable onClose) {
    this(game, onClose, null);
  }

  /**
   * @param game the game
   * @param onClose what to run when the settings close
   * @param backgroundPath the background to draw behind the settings, or null for the usual one
   */
  public SettingsMenuDisplayInGame(GdxGame game, Runnable onClose, String backgroundPath) {
    super(game);
    this.onClose = onClose;
    this.backgroundPath = backgroundPath;
  }

  @Override
  protected String backgroundPath() {
    return backgroundPath == null ? super.backgroundPath() : backgroundPath;
  }

  @Override
  protected void onExitClicked() {
    ButtonSound.playClick();
    exitMenu();
  }

  @Override
  protected void leave() {
    ServiceLocator.getEntityService().setSettingsOpen(false);
    onClose.run();
  }

  @Override
  public void dispose() {
    // The menu sits on the game's stage, which carries on after it closes
    menuBoxTable.clear();
    background.remove();
    super.dispose();
  }
}
