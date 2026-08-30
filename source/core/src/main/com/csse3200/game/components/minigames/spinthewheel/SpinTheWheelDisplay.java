package com.csse3200.game.components.minigames.spinthewheel;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.ui.UIComponent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A ui component for displaying the spin the wheel minigame. */
public class SpinTheWheelDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(SpinTheWheelDisplay.class);
  private static final float Z_INDEX = 2f;
  private final WheelLogic wheel;
  private Table table;
  private Label resultLabel;

  public SpinTheWheelDisplay(List<WheelItem> items) {
    this.wheel = new WheelLogic(items);
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    resultLabel = new Label("", skin);

    TextButton spinBtn = new TextButton("Spin", skin);
    spinBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Spin button clicked");
            WheelItem result = wheel.spin();
            resultLabel.setText(result.name() + " x" + result.value());
          }
        });
    table.row();
    table.add(resultLabel).padTop(30f);
    table.row();
    table.add(spinBtn).padTop(30f);

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
