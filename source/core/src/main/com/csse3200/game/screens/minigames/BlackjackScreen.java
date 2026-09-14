package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.audio.Music;
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
  private static final String BLACKJACK_MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";

  public BlackjackScreen(GdxGame game) {
    super(game);
  }

  @Override
  public void show() {
    super.show();
    ServiceLocator.getResourceService().loadMusic(new String[] {BLACKJACK_MUSIC});
    ServiceLocator.getResourceService().loadAll();

    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);
    music.setLooping(true);
    music.setVolume(0.25f);
    music.play();
  }

  @Override
  protected String[] getTextures() {
    return BlackjackConfig.TEXTURES;
  }

  @Override
  protected String[] getSounds() {
    return BlackjackConfig.SOUNDS;
  }

  @Override
  public void dispose() {
    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);
    music.stop();
    ServiceLocator.getResourceService().unloadAssets(new String[] {BLACKJACK_MUSIC});
    super.dispose();
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
