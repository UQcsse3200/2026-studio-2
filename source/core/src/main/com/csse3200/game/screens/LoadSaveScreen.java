package com.csse3200.game.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.mainmenu.LoadSaveActions;
import com.csse3200.game.components.mainmenu.LoadSaveDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The save-select screen: lists the save slots so an existing game can be loaded or deleted. Styled
 * after {@link MainMenuScreen} (same background, music and title logo) and reached from the main
 * menu's Continue button or from Play when every slot is full.
 */
public class LoadSaveScreen extends MenuScreen {
  private static final Logger logger = LoggerFactory.getLogger(LoadSaveScreen.class);
  private final GdxGame game;

  private static final String[] loadSaveTextures = {
    "images/ui/title_odysseus_logo.png",
    "images/backgrounds/main_menu_bg.png",
    "images/Buttons/back_up_btn.png",
    "images/Buttons/back_down_btn.png"
  };
  private static final String loadSaveMusic = "sounds/Main_menu_sound.mp3";
  private static final String[] loadSaveMusicFiles = {loadSaveMusic};

  public LoadSaveScreen(GdxGame game) {
    this.game = game;
    logger.debug("Initialising save-select screen services");

    loadAssets();
    createUI();
  }

  @Override
  public void dispose() {
    logger.debug("Disposing save-select screen");
    renderer.dispose();
    ServiceLocator.getResourceService().getAsset(loadSaveMusic, Music.class).stop();
    unloadAssets();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getEntityService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(loadSaveTextures);
    resourceService.loadMusic(loadSaveMusicFiles);
    ButtonSound.load(resourceService);
    resourceService.loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(loadSaveTextures);
    resourceService.unloadAssets(loadSaveMusicFiles);
    ButtonSound.unload(resourceService);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(loadSaveMusic, Music.class);
    music.setLooping(true);
    GameVolume.setMusicVolume(music, 0.1f);
    music.play();
  }

  /**
   * Creates the save-select screen's ui including components for rendering UI elements to the
   * screen and capturing and handling UI input.
   */
  private void createUI() {
    logger.debug("Creating ui");
    Stage stage = ServiceLocator.getRenderService().getStage();
    Entity ui = new Entity();
    ui.addComponent(new LoadSaveDisplay())
        .addComponent(new InputDecorator(stage, 10))
        .addComponent(new LoadSaveActions(game));
    ServiceLocator.getEntityService().register(ui);
    playMusic();
  }
}
