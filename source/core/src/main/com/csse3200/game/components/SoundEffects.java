package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class SoundEffects {
  public static final String[] FILES = {
    "sounds/Arrow_release.wav",
    "sounds/jump.ogg",
    "sounds/itempickup02.wav",
    "sounds/itempick.wav",
    "sounds/Drink_01.wav",
    "sounds/Drink_02.wav",
    "sounds/Bottle Break.wav",
    "sounds/melee sound.wav",
    "sounds/rock_break.ogg",
    "sounds/evil cyber laugh.wav",
    "sounds/shoot.ogg",
    "sounds/osprey.ogg",
    "sounds/hurt_player_1.wav",
    "sounds/hurt_player_2.wav",
    "sounds/hurt_player_3.wav",
    "sounds/hurt_player_4.wav",
    "sounds/enemy_Death.wav",
    "sounds/rope_swing.wav",
    "sounds/itemSwitch.wav",
    "sounds/level_complete.wav"
  };

  private SoundEffects() {}

  public static void load(ResourceService resourceService) {
    resourceService.loadSounds(FILES);
  }

  public static void unload(ResourceService resourceService) {
    resourceService.unloadAssets(FILES);
  }

  public static void play(String path, float volume) {
    if (ServiceLocator.getResourceService() != null
        && ServiceLocator.getResourceService().containsAsset(path, Sound.class)) {
      ServiceLocator.getResourceService()
          .getAsset(path, Sound.class)
          .play(GameVolume.scale(volume));
    }
  }
}
