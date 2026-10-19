package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.TopRightButtonDisplay;

/**
 * Top-right button that opens the pause menu. Uses images/Buttons/pause_up_btn.png and
 * pause_down_btn.png when they exist, and falls back to the exit button art until they do.
 */
public class PauseButtonDisplay extends TopRightButtonDisplay {
  private static final String PAUSE_UP = "images/Buttons/pause_up_btn.png";
  private static final String PAUSE_DOWN = "images/Buttons/pause_down_btn.png";
  private static final String EXIT_UP = "images/Buttons/exit_up_btn.png";
  private static final String EXIT_DOWN = "images/Buttons/exit_down_btn.png";

  private final Runnable onPause;

  /**
   * @param onPause called when the button is clicked
   */
  public PauseButtonDisplay(Runnable onPause) {
    super(240f, 80f, 90f, 20f);
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

    boolean pauseArt = hasPauseImages();
    Texture up =
        ServiceLocator.getResourceService().getAsset(pauseArt ? PAUSE_UP : EXIT_UP, Texture.class);
    Texture down =
        ServiceLocator.getResourceService()
            .getAsset(pauseArt ? PAUSE_DOWN : EXIT_DOWN, Texture.class);

    addButton(up, down, () -> ButtonSound.playClickThen(onPause));
  }
}
