package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonSound {
  private ButtonSound() {
    // Utility class; all operations are static.
  }

  private static final Logger logger = LoggerFactory.getLogger(ButtonSound.class);

  private static final String CLICK_SOUND = "sounds/Button_Sound_02.wav";
  private static final float TRANSITION_DELAY = 0.35f;

  public static final List<String> SOUND_FILES = List.of(CLICK_SOUND);

  /** Call in a screen's loadAssets() */
  public static void load(ResourceService resourceService) {
    resourceService.loadSounds(SOUND_FILES.toArray(String[]::new));
  }

  /** Call in a screen's unloadAssets() */
  public static void unload(ResourceService resourceService) {
    resourceService.unloadAssets(SOUND_FILES.toArray(String[]::new));
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
            if (action != null) {
              action.run();
            }
          }
        },
        TRANSITION_DELAY);
  }

  private static void playSafe(String path) {
    try {
      if (ServiceLocator.getResourceService() == null) {
        return;
      }
      Sound sound = ServiceLocator.getResourceService().getAsset(path, Sound.class);
      if (sound == null) {
        return;
      }
      sound.play(GameVolume.scale(1.0f));
      logger.debug("ButtonSound played: {}", path);
    } catch (GdxRuntimeException e) {
      logger.warn("ButtonSound failed: {}", path, e);
    }
  }
}
