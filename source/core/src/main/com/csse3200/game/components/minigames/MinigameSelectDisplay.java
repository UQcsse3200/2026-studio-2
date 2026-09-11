package com.csse3200.game.components.minigames;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.screens.minigames.MinigameType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ui component for displaying the minigame select menu. Builds one button per {@link
 * MinigameType}, so adding a minigame to the enum adds it to this menu automatically.
 */
public class MinigameSelectDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MinigameSelectDisplay.class);
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
    table.add(new Label("Minigames", skin, "title"));

    for (MinigameType minigame : MinigameType.values()) {
      Texture upTexture =
          ServiceLocator.getResourceService()
              .getAsset("images/Buttons/" + minigame.getAssetKey() + "_up_btn.png", Texture.class);
      Texture downTexture =
          ServiceLocator.getResourceService()
              .getAsset(
                  "images/Buttons/" + minigame.getAssetKey() + "_down_btn.png", Texture.class);

      ImageButton.ImageButtonStyle minigameBtnStyle = new ImageButton.ImageButtonStyle();
      minigameBtnStyle.up = new TextureRegionDrawable(upTexture);
      minigameBtnStyle.down = new TextureRegionDrawable(downTexture);

      ImageButton minigameBtn = new ImageButton(minigameBtnStyle);

      // Triggers an event when the button is pressed
      minigameBtn.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
              logger.debug("{} button clicked", minigame);
              ButtonSound.playClickThen(
                  () -> entity.getEvents().trigger("selectMinigame", minigame));
            }
          });

      table.row();
      table.add(minigameBtn).width(220f).height(70f).padTop(15f);
    }

    Texture backUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/back_up_btn.png", Texture.class);
    Texture backDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/back_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle backButtonStyle = new ImageButton.ImageButtonStyle();
    backButtonStyle.up = new TextureRegionDrawable(backUpTexture);
    backButtonStyle.down = new TextureRegionDrawable(backDownTexture);

    ImageButton backBtn = new ImageButton(backButtonStyle);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Back button clicked");
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("back"));
          }
        });

    table.row();
    table.add(backBtn).width(220f).height(70f).padTop(30f);

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
