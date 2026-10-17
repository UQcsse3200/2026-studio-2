package com.csse3200.game.components.level;

import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndState;

/**
 * Wins the game. The player has to stand in the entity's sensor and press F, see {@link
 * LevelEndComponent}.
 */
public class WinConditionComponent extends LevelEndComponent {
  public WinConditionComponent() {
    super();
  }

  @Override
  protected void activate() {
    ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.WIN);
  }
}
