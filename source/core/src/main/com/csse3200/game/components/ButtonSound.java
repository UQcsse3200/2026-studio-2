package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class ButtonSound {
  private static final String CLICK_SOUND = "sounds/mouse-click-press.wav";
  private static final String RELEASE_SOUND = "sounds/mouse-click-release.wav";
  private static final float TRANSITION_DELAY = 0.2f;

  public static final String[] SOUND_FILES = {CLICK_SOUND, RELEASE_SOUND};

  /** Call in a screen's loadAssets() */
  public static void load(ResourceService resourceService) {
    resourceService.loadSounds(SOUND_FILES);
  }

  /** Call in a screen's unloadAssets() */
  public static void unload(ResourceService resourceService) {
    resourceService.unloadAssets(SOUND_FILES);
  }

  /** Plays just the click sound immediately, no delay, no follow-up action. */
  public static void playClick() {
    playSafe(CLICK_SOUND);
  }

  /** Plays click, then runs the given action after a short delay (for screen transitions). */
  public static void playClickThen(Runnable action) {
    playSafe(CLICK_SOUND);
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            playSafe(RELEASE_SOUND);
            if (action != null) {
              action.run();
            }
          }
        },
        TRANSITION_DELAY);
  }

  private static void playSafe(String path) {
    try {
      Sound sound = ServiceLocator.getResourceService().getAsset(path, Sound.class);
      sound.play(1.0f);
      System.out.println("ButtonSound played: " + path);
    } catch (GdxRuntimeException e) {
      System.out.println("ButtonSound FAILED: " + path + " - " + e.getMessage());
    }
  }
}
