package com.csse3200.game.components.level;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

public class SeagullSoundComponent extends Component {
  public static final List<String> CLIPS =
      List.of(
          "sounds/seagull_1.ogg",
          "sounds/seagull_2.ogg",
          "sounds/seagull_3.ogg",
          "sounds/seagull_4.ogg");
  static final float VOLUME = 0.35f;
  static final float FIRST_MIN_DELAY = 15f;
  static final float FIRST_MAX_DELAY = 30f;
  static final float MIN_DELAY = 40f;
  static final float MAX_DELAY = 90f;

  private final int maxCalls;
  private int calls = 0;
  private float timeUntilNextCall;

  public SeagullSoundComponent() {
    this(MathUtils.random(2, 3));
  }

  SeagullSoundComponent(int maxCalls) {
    this.maxCalls = maxCalls;
    this.timeUntilNextCall = MathUtils.random(FIRST_MIN_DELAY, FIRST_MAX_DELAY);
  }

  @Override
  public void update() {
    if (calls >= maxCalls || ServiceLocator.getTimeSource() == null) {
      return;
    }
    timeUntilNextCall -= ServiceLocator.getTimeSource().getDeltaTime();
    if (timeUntilNextCall > 0f) {
      return;
    }
    playRandomClip();
    calls++;
    timeUntilNextCall = nextDelay();
  }

  public int getCalls() {
    return calls;
  }

  public static void stopAll() {
    ResourceService resources = ServiceLocator.getResourceService();
    if (resources == null) {
      return;
    }
    for (String clip : CLIPS) {
      if (resources.containsAsset(clip, Sound.class)) {
        resources.getAsset(clip, Sound.class).stop();
      }
    }
  }

  @Override
  public void dispose() {
    stopAll();
  }

  private float nextDelay() {
    return MathUtils.random(MIN_DELAY, MAX_DELAY);
  }

  private void playRandomClip() {
    ResourceService resources = ServiceLocator.getResourceService();
    String clip = CLIPS.get(MathUtils.random(CLIPS.size() - 1));
    if (resources == null || !resources.containsAsset(clip, Sound.class)) {
      return;
    }
    resources.getAsset(clip, Sound.class).play(GameVolume.scale(VOLUME));
  }
}
