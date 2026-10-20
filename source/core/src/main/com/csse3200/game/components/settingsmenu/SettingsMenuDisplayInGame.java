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

  public SettingsMenuDisplayInGame(GdxGame game, Runnable onClose) {
    super(game);
    this.onClose = onClose;
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
