package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

  // Asset paths - make sure these are registered wherever this screen's
  // textures are loaded (e.g. the Screen class that calls resourceService.loadTextures(...)).
  private static final String BG_TEXTURE = "images/minigames/cyclops/cyclops_bg.png";
  private static final String CYCLOPS_OPEN_TEXTURE = "images/minigames/cyclops/cyclops_open.png";
  private static final String CYCLOPS_CLOSED_TEXTURE =
      "images/minigames/cyclops/cyclops_closed.png";

  // Cyclops placement as a fraction of the background image, matched to the
  // updated scene mockup (feet anchored, rotation happens around the feet).
  private static final float CYCLOPS_LEFT_PCT = 0.731959f;
  private static final float CYCLOPS_TOP_PCT = 0.217228f;
  private static final float CYCLOPS_WIDTH_PCT = 0.192010f;
  private static final float CYCLOPS_HEIGHT_PCT = 0.644195f;

  private Table table;

  @Override
  public void create() {
    super.create();
    addBackground();
    addCyclops();
    addActors();
  }

  private void addBackground() {
    Texture bgTexture = ServiceLocator.getResourceService().getAsset(BG_TEXTURE, Texture.class);
    Image background = new Image(bgTexture);
    background.setFillParent(true);
    background.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
    stage.addActor(background);
    // Component create() order isn't guaranteed, so other UI (e.g. the win/lose
    // popup) may get added to the stage either before or after this. Force the
    // background to the very back explicitly so it can never cover other UI.
    background.setZIndex(0);
  }

  private void addCyclops() {
    Texture openTexture =
        ServiceLocator.getResourceService().getAsset(CYCLOPS_OPEN_TEXTURE, Texture.class);
    Texture closedTexture =
        ServiceLocator.getResourceService().getAsset(CYCLOPS_CLOSED_TEXTURE, Texture.class);

    CyclopsActor cyclops = new CyclopsActor(openTexture, closedTexture);

    float stageWidth = stage.getWidth();
    float stageHeight = stage.getHeight();

    float width = stageWidth * CYCLOPS_WIDTH_PCT;
    float height = stageHeight * CYCLOPS_HEIGHT_PCT;
    float x = stageWidth * CYCLOPS_LEFT_PCT;
    // CSS "top" is measured from the top of the stage; libGDX Y is measured
    // from the bottom, so convert and account for the actor's own height.
    float y = stageHeight - (stageHeight * CYCLOPS_TOP_PCT) - height;

    cyclops.setSize(width, height);
    cyclops.setPosition(x, y);
    cyclops.setOrigin(width / 2f, 0f);

    stage.addActor(cyclops);
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

    stage.addActor(table); // added last -> renders on top
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
