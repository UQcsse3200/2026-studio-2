package com.csse3200.game.components;

import com.badlogic.gdx.audio.Music;
import java.util.Map;
import java.util.WeakHashMap;

public class GameVolume {
  private static float volume = 1f;
  private static final Map<Music, Float> musicVolumes = new WeakHashMap<>();

  private GameVolume() {}

  public static float get() {
    return volume;
  }

  public static void set(float newVolume) {
    volume = Math.max(0f, Math.min(1f, newVolume));
    for (Map.Entry<Music, Float> entry : musicVolumes.entrySet()) {
      entry.getKey().setVolume(entry.getValue() * volume);
    }
  }

  public static float scale(float baseVolume) {
    return baseVolume * volume;
  }

  public static void setMusicVolume(Music music, float baseVolume) {
    musicVolumes.put(music, baseVolume);
    music.setVolume(scale(baseVolume));
  }
}
