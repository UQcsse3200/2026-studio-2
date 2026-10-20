package com.csse3200.game.components.maingame;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.areas.Level2GameArea;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.level.RainSoundComponent;
import com.csse3200.game.components.level.SeagullSoundComponent;
import com.csse3200.game.components.level.ThunderSoundComponent;
import com.csse3200.game.components.settingsmenu.SettingsMenuDisplayInGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a pause menu on the main game screen. Must be opened via the "showPauseMenu" event (in
 * this case, triggered via the ESC key), and contains the game logo and buttons to resume gameplay,
 * go to the settings menu, or quit the game. While the pause menu is open, the player and enemies
 * cannot move.
 */
public class PauseMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(PauseMenuDisplay.class);

  Table table;
  Table controlsGraphicTable;

  private GdxGame game;

  public PauseMenuDisplay(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    try {
      Music gameplay =
          ServiceLocator.getResourceService().getAsset("sounds/gameplay_bg.ogg", Music.class);
      gameplay.pause();
      for (String ambience :
          List.of(
              Level1GameArea.AMBIENCE_MUSIC,
              Level2GameArea.AMBIENCE_MUSIC,
              Level2GameArea.WATERFALL_MUSIC,
              RainSoundComponent.RAIN_MUSIC)) {
        if (ServiceLocator.getResourceService().containsAsset(ambience, Music.class)) {
          ServiceLocator.getResourceService().getAsset(ambience, Music.class).pause();
        }
      }
      SeagullSoundComponent.stopAll();
      ThunderSoundComponent.stopAll();
      Music mainMenu =
          ServiceLocator.getResourceService().getAsset("sounds/Main_menu_sound.mp3", Music.class);
      mainMenu.setLooping(true);
      GameVolume.setMusicVolume(mainMenu, 0.1f);
      mainMenu.play();
    } catch (Exception e) {
      logger.warn("Could not switch pause menu music", e);
    }
    table = new Table();
    table.setFillParent(true);

    Texture continueUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/continue_up_btn.png", Texture.class);
    Texture continueDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/continue_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle resumeButtonStyle = new ImageButton.ImageButtonStyle();
    resumeButtonStyle.up = new TextureRegionDrawable(continueUpTexture);
    resumeButtonStyle.down = new TextureRegionDrawable(continueDownTexture);

    ImageButton resumeBtn = new ImageButton(resumeButtonStyle);

    Texture settingsUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/settings_up_btn.png", Texture.class);
    Texture settingsDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/settings_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle settingsButtonStyle = new ImageButton.ImageButtonStyle();
    settingsButtonStyle.up = new TextureRegionDrawable(settingsUpTexture);
    settingsButtonStyle.down = new TextureRegionDrawable(settingsDownTexture);

    ImageButton settingsBtn = new ImageButton(settingsButtonStyle);

    Texture quitUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/quit_up_btn.png", Texture.class);
    Texture quitDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/quit_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle quitButtonStyle = new ImageButton.ImageButtonStyle();
    quitButtonStyle.up = new TextureRegionDrawable(quitUpTexture);
    quitButtonStyle.down = new TextureRegionDrawable(quitDownTexture);

    ImageButton quitBtn = new ImageButton(quitButtonStyle);

    Texture backUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/back_up_btn.png", Texture.class);
    Texture backDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/back_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle exitButtonStyle = new ImageButton.ImageButtonStyle();
    exitButtonStyle.up = new TextureRegionDrawable(backUpTexture);
    exitButtonStyle.down = new TextureRegionDrawable(backDownTexture);

    Texture controlsUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/control_up_btn.png", Texture.class);
    Texture controlsDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/control_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle controlsButtonStyle = new ImageButton.ImageButtonStyle();
    controlsButtonStyle.up = new TextureRegionDrawable(controlsUpTexture);
    controlsButtonStyle.down = new TextureRegionDrawable(controlsDownTexture);

    ImageButton controlsBtn = new ImageButton(controlsButtonStyle);

    Texture controlsGraphicTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/ui/controls_graphic.png", Texture.class);

    resumeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (ServiceLocator.getEntityService().getPaused()) {
              ButtonSound.playClick();
              entity.getEvents().trigger("togglePause");
            }
          }
        });

    settingsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (ServiceLocator.getEntityService().getPaused()) {
              ButtonSound.playClickThen(
                  () -> {
                    ServiceLocator.getEntityService().setSettingsOpen(true);
                    Entity ui = new Entity();
                    SettingsMenuDisplayInGame settings =
                        new SettingsMenuDisplayInGame(game, ui::dispose);
                    ui.addComponent(settings);
                    ServiceLocator.getEntityService().register(ui);
                  });
            }
          }
        });

    quitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (ServiceLocator.getEntityService().getPaused()) {
              ButtonSound.playClickThen(() -> game.setScreen(GdxGame.ScreenType.MAIN_MENU));
            }
          }
        });

    controlsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            ButtonSound.playClick();
            Image controlsGraphic = new Image(controlsGraphicTexture);
            ImageButton controlsBackBtn = new ImageButton(exitButtonStyle);
            controlsBackBtn.addListener(
                new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent changeEvent, Actor actor) {
                    ButtonSound.playClick();
                    controlsGraphicTable.remove();
                  }
                });
            controlsGraphicTable = new Table();
            controlsGraphicTable.setFillParent(true);
            controlsGraphicTable.add(controlsGraphic).width(1000f).height(630f);
            controlsGraphicTable.row();
            controlsGraphicTable.add(controlsBackBtn).width(200f).height(70f).padTop(15f);
            stage.addActor(controlsGraphicTable);
          }
        });

    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/ui/title_odysseus_logo.png", Texture.class));
    table.add(title).width(350f).height(122f).padTop(-35f);
    table.row();
    table.add(resumeBtn).width(200f).height(70f).padTop(30f);
    table.row();
    table.add(settingsBtn).width(200f).height(70f).padTop(15f);
    table.row();
    table.add(controlsBtn).width(200f).height(70f).padTop(15f);
    table.row();
    table.add(quitBtn).width(200f).height(70f).padTop(15f);
    table.row();

    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  public void toFront() {
    table.toFront();
  }

  @Override
  public void dispose() {
    table.remove();
    super.dispose();
    if (controlsGraphicTable != null) {
      controlsGraphicTable.remove();
    }
  }
}
