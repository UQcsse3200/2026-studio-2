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
  private Image backgroundImage;

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();
    float pad = screenHeight * 0.02f;
    backgroundImage = MenuBackground.create(screenWidth, screenHeight);
    stage.addActor(backgroundImage);

    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/ui/title_odysseus_logo.png", Texture.class));

    Texture playUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/play_up_btn.png", Texture.class);
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
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/quit_up_btn.png", Texture.class);
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

    float buttonWidth = screenWidth * 0.14f;
    float buttonHeight = screenHeight * 0.07f;

    // One centred column under the logo: Play, Continue, Settings, then Exit.
    Table buttons = new Table();
    buttons.add(playButton).width(buttonWidth).height(buttonHeight).padBottom(pad);
    buttons.row();
    buttons.add(continueButton).width(buttonWidth).height(buttonHeight).padBottom(pad);
    buttons.row();
    buttons.add(settingsButton).width(buttonWidth).height(buttonHeight).padBottom(pad);
    buttons.row();
    buttons.add(exitButton).width(buttonWidth).height(buttonHeight);

    table.add().expandY(); // pushes the logo and buttons down from the very top
    table.row();
    table.add(title).width(screenWidth * 0.3f).height(screenHeight * 0.18f).padBottom(pad * 2f);
    table.row();
    table.add(buttons).center().padBottom(screenHeight * 0.08f);

    stage.addActor(table);
  }

  /**
   * Lays the page out again for a new window size. The background and the buttons are sized from
   * the window when they are built, so without this they no longer fit after a resize.
   */
  public void resize() {
    if (table == null) {
      return;
    }
    table.remove();
    if (backgroundImage != null) {
      backgroundImage.remove();
    }
    addActors();
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Scene2D actors on the stage render this UI.
  }

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
