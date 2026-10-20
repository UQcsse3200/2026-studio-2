package com.csse3200.game.components.level;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class RainSoundComponent extends Component {
  public static final String RAIN_MUSIC = "sounds/rain_loop.ogg";
  static final float MIN_VOLUME = 0.25f;
  static final float MAX_VOLUME = 0.7f;
  private static final float BOTTOM_HEIGHT = 6f;
  private static final float TOP_HEIGHT = 98f;

  private final Entity player;

  public RainSoundComponent(Entity player) {
    this.player = player;
  }

  @Override
  public void update() {
    ResourceService resources = ServiceLocator.getResourceService();
    if (player == null || resources == null || !resources.containsAsset(RAIN_MUSIC, Music.class)) {
      return;
    }
    Music rain = resources.getAsset(RAIN_MUSIC, Music.class);
    if (rain != null) {
      GameVolume.setMusicVolume(rain, volumeForHeight(player.getPosition().y));
    }
  }

  static float volumeForHeight(float height) {
    if (height >= TOP_HEIGHT) {
      return MAX_VOLUME;
    }
    float climbed =
        MathUtils.clamp((height - BOTTOM_HEIGHT) / (TOP_HEIGHT - BOTTOM_HEIGHT), 0f, 1f);
    float rainIntensity = 1f - climbed * 0.75f;
    return MIN_VOLUME + (MAX_VOLUME - MIN_VOLUME) * (1f - rainIntensity);
  }
}
