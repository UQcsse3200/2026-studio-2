package com.csse3200.game.components.level;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.SoundEffects;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

public class ThunderSoundComponent extends Component {
  public static final String CLOSE_THUNDER = "sounds/thunder_close.ogg";
  public static final String FAR_THUNDER = "sounds/thunder_far.ogg";
  static final float CYCLE = 20f;
  static final float CLOSE_AT = 5f;
  static final float FAR_AT = 13f;
  static final float CLOSE_VOLUME = 0.6f;
  static final float FAR_VOLUME = 0.35f;

  private float time = 0f;

  @Override
  public void update() {
    if (ServiceLocator.getTimeSource() == null) {
      return;
    }
    float previous = time;
    time = (time + ServiceLocator.getTimeSource().getDeltaTime()) % CYCLE;
    if (crossed(previous, time, CLOSE_AT)) {
      SoundEffects.play(CLOSE_THUNDER, CLOSE_VOLUME);
    }
    if (crossed(previous, time, FAR_AT)) {
      SoundEffects.play(FAR_THUNDER, FAR_VOLUME);
    }
  }

  static boolean crossed(float previous, float current, float mark) {
    if (current >= previous) {
      return previous < mark && current >= mark;
    }
    return previous < mark || current >= mark;
  }

  public static void stopAll() {
    ResourceService resources = ServiceLocator.getResourceService();
    if (resources == null) {
      return;
    }
    for (String thunder : List.of(CLOSE_THUNDER, FAR_THUNDER)) {
      if (resources.containsAsset(thunder, Sound.class)) {
        resources.getAsset(thunder, Sound.class).stop();
      }
    }
  }

  @Override
  public void dispose() {
    stopAll();
  }
}
