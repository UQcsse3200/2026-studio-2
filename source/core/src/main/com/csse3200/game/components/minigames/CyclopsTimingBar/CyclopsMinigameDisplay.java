package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.top().right();
    table.setFillParent(true);

    Texture exitUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_up_btn.png", Texture.class);
    Texture exitDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle exitButtonStyle = new ImageButton.ImageButtonStyle();
    exitButtonStyle.up = new TextureRegionDrawable(exitUpTexture);
    exitButtonStyle.down = new TextureRegionDrawable(exitDownTexture);

    ImageButton minigameSelectMenuBtn = new ImageButton(exitButtonStyle);

    // Triggers an event when the button is pressed.
    minigameSelectMenuBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Exit button clicked");
            entity.getEvents().trigger("exit");
          }
        });

    table.add(minigameSelectMenuBtn).width(160f).height(56f).padTop(10f).padRight(10f);

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
