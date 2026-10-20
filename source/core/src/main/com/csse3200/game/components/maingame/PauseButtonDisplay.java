package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.ButtonSound;
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
  private final boolean instant;

  /**
   * @param onPause called when the button is clicked, a moment after the click sound
   */
  public PauseButtonDisplay(Runnable onPause) {
    this(onPause, false);
  }

  /**
   * @param onPause called when the button is clicked
   * @param instant true to call it straight away, false to wait for the click sound first
   */
  public PauseButtonDisplay(Runnable onPause, boolean instant) {
    super(240f, 80f, 90f, 20f, EXIT_UP, EXIT_DOWN);
    this.onPause = onPause;
    this.instant = instant;
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
  protected void createButton() {
    boolean pauseArt = hasPauseImages();
    addButton(pauseArt ? PAUSE_UP : EXIT_UP, pauseArt ? PAUSE_DOWN : EXIT_DOWN);
  }

  @Override
  protected void onButtonClicked() {
    if (instant) {
      ButtonSound.playClick();
      onPause.run();
    } else {
      ButtonSound.playClickThen(onPause);
    }
  }
}
