package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.files.SaveManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the save-select screen's events: loading a chosen slot, deleting a slot (with the
 * display's confirmation step already applied) and going back to the main menu.
 */
public class LoadSaveActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(LoadSaveActions.class);
  private final GdxGame game;

  public LoadSaveActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("Load", (Integer slot) -> onLoad(slot));
    entity.getEvents().addListener("Delete", (Integer slot) -> onDelete(slot));
    entity.getEvents().addListener("Back", this::onBack);
  }

  /** Loads the selected save and transitions to its level. */
  private void onLoad(int slot) {
    logger.info("Loading save slot {}", slot);
    game.loadSave(slot);
  }

  /** Deletes the selected slot, then tells the display to rebuild the list. */
  private void onDelete(int slot) {
    logger.info("Deleting save slot {}", slot);
    SaveManager.delete(slot);
    entity.getEvents().trigger("SlotsChanged");
  }

  /** Returns to the main menu. */
  private void onBack() {
    logger.info("Back");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }
}
