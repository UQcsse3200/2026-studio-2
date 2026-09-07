package com.csse3200.game.components.minigames;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.screens.minigames.MinigameType;
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
      TextButton minigameBtn = new TextButton(minigame.getDisplayName(), skin);

      // Triggers an event when the button is pressed
      minigameBtn.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
              logger.debug("{} button clicked", minigame);
              entity.getEvents().trigger("selectMinigame", minigame);
            }
          });

      table.row();
      table.add(minigameBtn).padTop(15f);
    }

    TextButton backBtn = new TextButton("Back", skin);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Back button clicked");
            entity.getEvents().trigger("back");
          }
        });

    table.row();
    table.add(backBtn).padTop(30f);

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
