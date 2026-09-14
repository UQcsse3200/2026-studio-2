package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.blackjack.Blackjack;
import com.csse3200.game.components.minigames.blackjack.BlackjackActions;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.blackjack.BlackjackDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;

/** Screen for the Blackjack minigame. */
public class BlackjackScreen extends MinigameScreen {

  public BlackjackScreen(GdxGame game) {
    super(game);
  }

  @Override
  protected String[] getTextures() {
    return BlackjackConfig.TEXTURES;
  }

  @Override
  protected Entity createUI() {
    Stage stage = ServiceLocator.getRenderService().getStage();

    Blackjack blackjack = new Blackjack(100);

    return new Entity()
        .addComponent(new BlackjackDisplay(blackjack))
        .addComponent(new BlackjackActions(game))
        .addComponent(new InputDecorator(stage, 10));
  }
}
