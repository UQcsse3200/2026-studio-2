package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Displays a pause menu on the main game screen. Must be opened via the "showPauseMenu" event (in
 * this case, triggered via the ESC key), and contains the game logo and buttons to resume gameplay,
 * go to the settings menu, or quit the game. While the pause menu is open, the player and enemies
 * cannot move.
 */
public class PauseMenuDisplay extends UIComponent {
  Table table;
  boolean paused = false;

  private GdxGame game;

  public PauseMenuDisplay(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    super.create();
    addActors();

    entity.getEvents().addListener("showPauseMenu", this::pause);
    entity.getEvents().addListener("hidePauseMenu", this::unpause);
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    table.setColor(1, 1, 1, 0);

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

    ImageButton.ImageButtonStyle exitButtonStyle = new ImageButton.ImageButtonStyle();
    exitButtonStyle.up = new TextureRegionDrawable(quitUpTexture);
    exitButtonStyle.down = new TextureRegionDrawable(quitDownTexture);

    ImageButton exitBtn = new ImageButton(exitButtonStyle);

    resumeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (ServiceLocator.getEntityService().getPaused()) {
              ButtonSound.playClick();
              unpause();
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
                    entity.getEvents().trigger("settingsFromPause");
                    game.setScreen(GdxGame.ScreenType.SETTINGS_FROM_PAUSE);
                  });
            }
          }
        });

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (ServiceLocator.getEntityService().getPaused()) {
              ButtonSound.playClick();
              game.exit();
            }
          }
        });

    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/title_odysseus_logo.png", Texture.class));
    table.add(title).width(350f).height(122f).padTop(-35f);
    table.row();
    table.add(resumeBtn).width(200f).height(70f).padTop(30f);
    table.row();
    table.add(settingsBtn).width(200f).height(70f).padTop(15f);
    table.row();
    table.add(exitBtn).width(200f).height(70f).padTop(15f);
    table.row();

    stage.addActor(table);
  }

  private void pause() {
    table.setColor(1, 1, 1, 1);
    ServiceLocator.getEntityService().setPaused(true);
  }

  private void unpause() {
    table.setColor(1, 1, 1, 0);
    ServiceLocator.getEntityService().setPaused(false);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }
}
