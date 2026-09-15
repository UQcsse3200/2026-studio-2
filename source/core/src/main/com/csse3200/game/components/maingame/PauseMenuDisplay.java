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
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.entities.Entity;
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
  Table controlsGraphicTable;

  private GdxGame game;
  private GameArea area;

  private Entity overlay;
  private GdxGame.ScreenType settingsScreen;

  public PauseMenuDisplay(GdxGame game, GameArea area, GdxGame.ScreenType settingsScreen) {
    this.game = game;
    this.area = area;
    this.settingsScreen = settingsScreen;
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
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
        ServiceLocator.getResourceService().getAsset("images/controls_graphic.png", Texture.class);

    resumeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (ServiceLocator.getEntityService().getPaused()) {
              ButtonSound.playClick();
              entity.getEvents().trigger("togglePause");
              area.getInput().unpause();
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
                    game.setScreen(settingsScreen);
                  });
            }
          }
        });

    quitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (ServiceLocator.getEntityService().getPaused()) {
              ButtonSound.playClick();
              game.exit();
            }
          }
        });

    controlsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            Image controlsGraphic = new Image(controlsGraphicTexture);
            ImageButton controlsBackBtn = new ImageButton(exitButtonStyle);
            controlsBackBtn.addListener(
                new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent changeEvent, Actor actor) {
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
                .getAsset("images/title_odysseus_logo.png", Texture.class));
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
