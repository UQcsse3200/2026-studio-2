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
import com.csse3200.game.components.minigames.blackjack.BlackjackDialogue;
import com.csse3200.game.components.minigames.blackjack.BlackjackDisplay;
import com.csse3200.game.components.minigames.blackjack.BlackjackRulesDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;

/** Screen for the Blackjack minigame. */
public class BlackjackScreen extends MinigameScreen {
  private final boolean storyMode;
  private static final String BLACKJACK_MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";
  private static final int STARTING_BALANCE = 100;
  private final boolean storyMode;
  private boolean leaving = false;

  /**
   * Creates the standalone Blackjack screen.
   *
   * @param game game used for screen changes
   */
  public BlackjackScreen(GdxGame game) {
    this(game, false);
  }
/**
 * Creates Blackjack in standalone mode or as the story encounter.
 * Winning the match plays Cutscene 5 and continues to Level 3 in both modes.
 *
 * @param storyMode whether Blackjack is launched as part of the Level 2 story
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


    // The player's gold is transferred via a snapshot across screen disposal.
    // A standalone launch without a player keeps its 100-gold practice balance.
    Integer savedGold = game.getSavedPlayerGold();
    int startingGold = savedGold != null ? savedGold : 100;
    Blackjack blackjack = new Blackjack(startingGold);

    InventoryComponent inventoryComponent =
        savedGold != null
            ? new InventoryComponent(startingGold) {
              @Override
              public void setGold(int gold) {
                super.setGold(gold);
                game.updateSavedPlayerGold(getGold());
              }
            }
            : new InventoryComponent(startingGold);

    return new Entity()
        .addComponent(inventoryComponent)
        .addComponent(
            new BlackjackDisplay(
                blackjack,
                inventoryComponent,
                this::setSoundEnabled,
                null,
                () -> BlackjackDialogue.startWinCutscene(game)))
        .addComponent(new BlackjackRulesDisplay())
        .addComponent(new BlackjackActions(game))
        .addComponent(new InputDecorator(stage, 10));

  }
}
