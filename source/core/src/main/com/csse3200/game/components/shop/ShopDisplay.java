package com.csse3200.game.components.shop;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shop page overlay opened by interacting with a shopkeeper NPC.
 *
 * <p>Buying and selling are not implemented yet; this display is the entry point for that work.
 */
public class ShopDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(ShopDisplay.class);
  private static final float Z_INDEX = 3f;

  private Table table;
  private boolean open;

  @Override
  public void create() {
    super.create();
    addActors();
    entity.getEvents().addListener("openShop", this::open);
    entity.getEvents().addListener("closeShop", this::close);
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    table.setVisible(false);

    Table panel = new Table();
    panel.setBackground(skin.getDrawable("window-c"));
    panel.pad(40f);

    Label title = new Label("Shop", skin, "title");
    Label subtitle = new Label("Welcome. Trading will be added soon.", skin);

    TextButton closeBtn = new TextButton("Close", skin);
    closeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Shop close button clicked");
            entity.getEvents().trigger("closeShop");
          }
        });

    panel.add(title);
    panel.row();
    panel.add(subtitle).padTop(20f);
    panel.row();
    panel.add(closeBtn).padTop(40f);

    table.add(panel);
    stage.addActor(table);
  }

  /** Shows the shop page and pauses the game so the player can use the UI. */
  public void open() {
    if (open) {
      return;
    }
    open = true;
    table.setVisible(true);
    setGamePaused(true);
    logger.info("Opened shop");
  }

  /** Hides the shop page and resumes the game. */
  public void close() {
    if (!open) {
      return;
    }
    open = false;
    table.setVisible(false);
    setGamePaused(false);
    logger.info("Closed shop");
  }

  /**
   * @return true if the shop page is currently visible
   */
  public boolean isOpen() {
    return open;
  }

  private void setGamePaused(boolean paused) {
    if (ServiceLocator.getEntityService() != null) {
      ServiceLocator.getEntityService().setPaused(paused);
    }
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
    if (table != null) {
      table.remove();
    }
    super.dispose();
  }
}
