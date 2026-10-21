package com.csse3200.game.screens.minigames;

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
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;

/** Screen for the Blackjack minigame. */
public class BlackjackScreen extends MinigameScreen {
  private static final String BLACKJACK_MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";
  private static final int STARTING_BALANCE = 100;

  /**
   * Creates the Blackjack screen. The story and the debug terminal open the same screen: winning
   * the match plays Cutscene 5 and continues to Level 3 either way.
   *
   * @param game game used for screen changes
   */
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
    setMusicPlaying(enabled);
  }

  private void setMusicPlaying(boolean playing) {
    Music music = ServiceLocator.getResourceService().getAsset(BLACKJACK_MUSIC, Music.class);

    if (playing) {
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
    int startingGold = savedGold != null ? savedGold : STARTING_BALANCE;
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

    BlackjackDisplay display =
        new BlackjackDisplay(
            blackjack,
            inventoryComponent,
            this::setSoundEnabled,
            null,
            () -> BlackjackDialogue.startWinCutscene(game));

    Terminal terminal = new Terminal(game, GdxGame.ScreenType.LEVEL_3_GAME);
    return new Entity()
        .addComponent(inventoryComponent)
        .addComponent(display)
        .addComponent(new BlackjackRulesDisplay())
        .addComponent(new BlackjackActions(game))
        .addComponent(new InputDecorator(stage, 10))
        // Debug terminal (F1), as in the levels. Blackjack leads into Level 3.
        .addComponent(terminal)
        .addComponent(ServiceLocator.getInputService().getInputFactory().createForTerminal())
        .addComponent(new TerminalDisplay());
  }
}
