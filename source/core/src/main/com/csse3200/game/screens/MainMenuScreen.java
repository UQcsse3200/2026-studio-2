package com.csse3200.game.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.mainmenu.MainMenuActions;
import com.csse3200.game.components.mainmenu.MainMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The game screen containing the main menu. */
public class MainMenuScreen extends RenderedScreen {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuScreen.class);
  private final GdxGame game;
  private final Renderer renderer;
  private Entity ui;
  private static final String[] mainMenuTextures = {
    "images/ui/title_odysseus_logo.png",
    "images/backgrounds/main_menu_bg.png",
    "images/Buttons/play_up_btn.png",
    "images/Buttons/play_down_button.png",
    "images/Buttons/continue_up_btn.png",
    "images/Buttons/continue_down_btn.png",
    "images/Buttons/settings_up_btn.png",
    "images/Buttons/settings_down_btn.png",
    "images/Buttons/quit_up_btn.png",
    "images/Buttons/quit_down_btn.png",
    "images/ui/scroll_bg.png"
  };
  private static final String MAIN_MENU_MUSIC = "sounds/Main_menu_sound.mp3";
  private static final String[] mainMenuMusicFiles = {MAIN_MENU_MUSIC};

  public MainMenuScreen(GdxGame game) {
    this.game = game;
    logger.debug("Initialising main menu screen services");
    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    renderer = RenderFactory.createRenderer();

    loadAssets();
    createUI();
  }

  @Override
  public void render(float delta) {
    ServiceLocator.getEntityService().update();
    renderer.render();
  }

  @Override
  protected Renderer getRenderer() {
    return renderer;
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    MainMenuDisplay display = ui == null ? null : ui.getComponent(MainMenuDisplay.class);
    if (display != null) {
      display.resize();
    }
  }

  @Override
  public void dispose() {
    logger.debug("Disposing main menu screen");
    renderer.dispose();
    ServiceLocator.getResourceService().getAsset(MAIN_MENU_MUSIC, Music.class).stop();
    unloadAssets();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getEntityService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(mainMenuTextures);
    resourceService.loadMusic(mainMenuMusicFiles);
    ButtonSound.load(resourceService);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(mainMenuTextures);
    resourceService.unloadAssets(mainMenuMusicFiles);
    ButtonSound.unload(resourceService);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(MAIN_MENU_MUSIC, Music.class);
    music.setLooping(true);
    GameVolume.setMusicVolume(music, 0.1f);
    music.play();
  }

  /**
   * Creates the main menu's ui including components for rendering ui elements to the screen and
   * capturing and handling ui input.
   */
  private void createUI() {
    logger.debug("Creating ui");
    Stage stage = ServiceLocator.getRenderService().getStage();
    ui = new Entity();
    ui.addComponent(new MainMenuDisplay())
        .addComponent(new InputDecorator(stage, 10))
        .addComponent(new MainMenuActions(game));
    ServiceLocator.getEntityService().register(ui);
    playMusic();
  }
}
