package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Top-right button that opens the pause menu. Uses images/Buttons/pause_up_btn.png and
 * pause_down_btn.png when they exist, and falls back to the exit button art until they do.
 */
public class PauseButtonDisplay extends UIComponent {
  private static final float Z_INDEX = 2f;
  private static final String PAUSE_UP = "images/Buttons/pause_up_btn.png";
  private static final String PAUSE_DOWN = "images/Buttons/pause_down_btn.png";
  private static final String EXIT_UP = "images/Buttons/exit_up_btn.png";
  private static final String EXIT_DOWN = "images/Buttons/exit_down_btn.png";

  private final Runnable onPause;
  private Table table;

  /**
   * @param onPause called when the button is clicked
   */
  public PauseButtonDisplay(Runnable onPause) {
    this.onPause = onPause;
  }

  /** Whether the dedicated pause button images have been added to the project. */
  public static boolean hasPauseImages() {
    return Gdx.files.internal(PAUSE_UP).exists() && Gdx.files.internal(PAUSE_DOWN).exists();
  }

  /** Paths of the button textures to load: the pause images if present, otherwise none extra. */
  public static String[] extraTextures() {
    return hasPauseImages() ? new String[] {PAUSE_UP, PAUSE_DOWN} : new String[] {};
  }

  @Override
  public void create() {
    super.create();
    table = new Table();
    table.top().right();
    table.setFillParent(true);

    boolean pauseArt = hasPauseImages();
    Texture up =
        ServiceLocator.getResourceService()
            .getAsset(pauseArt ? PAUSE_UP : EXIT_UP, Texture.class);
    Texture down =
        ServiceLocator.getResourceService()
            .getAsset(pauseArt ? PAUSE_DOWN : EXIT_DOWN, Texture.class);

    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = new TextureRegionDrawable(up);
    style.down = new TextureRegionDrawable(down);

    ImageButton button = new ImageButton(style);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            ButtonSound.playClickThen(onPause);
          }
        });

    table.add(button).width(240f).height(80f).padTop(90f).padRight(20f);
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

  public void setVisible(boolean visible) {
    if (table != null) {
      table.setVisible(visible);
    }
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
