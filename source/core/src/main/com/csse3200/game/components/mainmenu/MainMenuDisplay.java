package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    float screenWidth = Gdx.graphics.getWidth();
    float screenHeight = Gdx.graphics.getHeight();
    float pad = screenHeight * 0.02f;
    Image background =
        new Image(
            ServiceLocator.getResourceService().getAsset("images/main_menu_bg.png", Texture.class));
    background.setFillParent(true);
    stage.addActor(background);

    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/title_odysseus_logo.png", Texture.class));

    Texture playUpTexture =
        ServiceLocator.getResourceService().getAsset("images/Buttons/play_up_btn.png", Texture.class);
    Texture playDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/play_down_button.png", Texture.class);

    ImageButton.ImageButtonStyle playButtonStyle = new ImageButton.ImageButtonStyle();
    playButtonStyle.up = new TextureRegionDrawable(playUpTexture);
    playButtonStyle.down = new TextureRegionDrawable(playDownTexture);

    ImageButton playButton = new ImageButton(playButtonStyle);

    Texture continueUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/continue_up_btn.png", Texture.class);
    Texture continueDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/continue_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle continueButtonStyle = new ImageButton.ImageButtonStyle();
    continueButtonStyle.up = new TextureRegionDrawable(continueUpTexture);
    continueButtonStyle.down = new TextureRegionDrawable(continueDownTexture);

    ImageButton continueButton = new ImageButton(continueButtonStyle);

    Texture minigamesUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/minigames_up_btn.png", Texture.class);
    Texture minigamesDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/minigames_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle minigamesButtonStyle = new ImageButton.ImageButtonStyle();
    minigamesButtonStyle.up = new TextureRegionDrawable(minigamesUpTexture);
    minigamesButtonStyle.down = new TextureRegionDrawable(minigamesDownTexture);

    ImageButton minigamesButton = new ImageButton(minigamesButtonStyle);

    Texture settingsUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/settings_up_btn.png", Texture.class);
    Texture settingsDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/settings_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle settingsButtonStyle = new ImageButton.ImageButtonStyle();
    settingsButtonStyle.up = new TextureRegionDrawable(settingsUpTexture);
    settingsButtonStyle.down = new TextureRegionDrawable(settingsDownTexture);

    ImageButton settingsButton = new ImageButton(settingsButtonStyle);

    Texture quitUpTexture =
        ServiceLocator.getResourceService().getAsset("images/Buttons/quit_up_btn.png", Texture.class);
    Texture quitDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/quit_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle exitButtonStyle = new ImageButton.ImageButtonStyle();
    exitButtonStyle.up = new TextureRegionDrawable(quitUpTexture);
    exitButtonStyle.down = new TextureRegionDrawable(quitDownTexture);

    ImageButton exitButton = new ImageButton(exitButtonStyle);

    // Triggers an event when the button is pressed
    // ImageButton automatically swaps to the "down" drawable while pressed and back to "up" on
    // release, so no manual setDrawable(...) call is needed here.
    playButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Play button clicked");
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("Play"));
          }
        });

    continueButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Continue button clicked");
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("Continue"));
          }
        });

    minigamesButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Minigames button clicked");
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("Minigames"));
          }
        });

    settingsButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Settings button clicked");
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("Settings"));
          }
        });

    exitButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Exit button clicked");
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("Exit"));
          }
        });

    table.add(title).width(screenWidth * 0.3f).height(screenHeight * 0.18f).padBottom(pad);
    table.row();
    table.add(playButton).width(screenWidth * 0.12f).height(screenHeight * 0.06f);
    table.row();
    table
        .add(continueButton)
        .width(screenWidth * 0.12f)
        .height(screenHeight * 0.06f)
        .padTop(pad);
    table.row();
    table
        .add(minigamesButton)
        .width(screenWidth * 0.12f)
        .height(screenHeight * 0.06f)
        .padTop(pad);
    table.row();
    table
        .add(settingsButton)
        .width(screenWidth * 0.12f)
        .height(screenHeight * 0.06f)
        .padTop(pad);
    table.row();
    table.add(exitButton).width(screenWidth * 0.12f).height(screenHeight * 0.06f).padTop(pad);

    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {}

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
