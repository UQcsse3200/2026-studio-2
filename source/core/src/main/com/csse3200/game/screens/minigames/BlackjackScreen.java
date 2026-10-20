package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.inventory.InventoryComponent;
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
  private static final int STARTING_BALANCE = 100;
  private final boolean storyMode;
  private boolean leaving = false;

  public BlackjackScreen(GdxGame game) {
    this(game, false);
  }

  /**
   * @param storyMode whether winning continues the level 2 story
   */
  public BlackjackScreen(GdxGame game, boolean storyMode) {
    super(game);
    this.storyMode = storyMode;
  }

  @Override
  public void show() {
    super.show();
    ServiceLocator.getResourceService().loadMusic(new String[] {BLACKJACK_MUSIC});
    ServiceLocator.getResourceService().loadAll();

    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);
    music.setLooping(true);
    GameVolume.setMusicVolume(music, 0.25f);
    music.play();
  }

  @Override
  protected String[] getTextures() {
    return BlackjackConfig.TEXTURES.toArray(String[]::new);
  }

  @Override
  protected String[] getSounds() {
    return BlackjackConfig.SOUNDS.toArray(String[]::new);
  }

  private void setSoundEnabled(boolean enabled) {
    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);

    if (enabled) {
      music.play();
    } else {
      music.pause();
    }
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

    Blackjack blackjack = new Blackjack(STARTING_BALANCE);
    // The flashback uses its own balance; the main player's saved inventory stays untouched.
    InventoryComponent inventory = new InventoryComponent(STARTING_BALANCE);

    Entity ui =
        new Entity()
            .addComponent(inventory)
            .addComponent(
                new BlackjackDisplay(
                    blackjack,
                    inventory,
                    this::setSoundEnabled,
                    storyMode ? this::returnToLevel : null))
            .addComponent(new BlackjackActions(game))
            .addComponent(new InputDecorator(stage, 10));
    if (storyMode) {
      ui.getEvents()
          .addListener(
              "roundCompleted",
              (Boolean won) -> {
                if (won && !leaving) {
                  leaving = true;
                  Gdx.app.postRunnable(
                      () -> game.playCutscene("cutscene5", GdxGame.ScreenType.LEVEL_3_GAME));
                } else if (!won && inventory.getGold() == 0) {
                  // Losing the flashback must not prevent another attempt.
                  inventory.setGold(STARTING_BALANCE);
                }
              });
    }
    return ui;
  }

  private void returnToLevel() {
    if (!leaving) {
      leaving = true;
      Gdx.app.postRunnable(() -> game.setScreen(GdxGame.ScreenType.LEVEL_2_GAME));
    }
  }
}
