package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A ui component for displaying the Main menu. */
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
    // Background image generated using Google Gemini for sprint 1
    Image background =
        new Image(
            ServiceLocator.getResourceService().getAsset("images/main_menu_bg.jpg", Texture.class));
    background.setFillParent(true);
    stage.addActor(background);

    // title made in Canva
    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/title_odysseus_logo.png", Texture.class));

    TextButton PlayBtn = new TextButton("Play", skin);
    TextButton ContinueBtn = new TextButton("Continue", skin);
    TextButton MinigamesBtn = new TextButton("Minigames", skin);
    TextButton SettingsBtn = new TextButton("Settings", skin);
    TextButton ExitBtn = new TextButton("Quit", skin);

    // Triggers an event when the button is pressed
    PlayBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Play button clicked");
            entity.getEvents().trigger("Play");
          }
        });

    ContinueBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Continue button clicked");
            entity.getEvents().trigger("Continue");
          }
        });

    MinigamesBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Minigames button clicked");
            entity.getEvents().trigger("Minigames");
          }
        });

    SettingsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Settings button clicked");
            entity.getEvents().trigger("Settings");
          }
        });

    ExitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {

            logger.debug("Exit button clicked");
            entity.getEvents().trigger("Exit");
          }
        });

    table.add(title).width(screenWidth * 0.5f).height(screenHeight * 0.3f).padBottom(pad);
    table.row();
    table.add(PlayBtn);
    table.row();
    table.add(ContinueBtn).padTop(pad);
    table.row();
    table.add(MinigamesBtn).padTop(pad);
    table.row();
    table.add(SettingsBtn).padTop(pad);
    table.row();
    table.add(ExitBtn).padTop(pad);

    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
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
