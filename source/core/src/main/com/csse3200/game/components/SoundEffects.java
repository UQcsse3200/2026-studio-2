package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

public class SoundEffects {
  public static final List<String> FILES =
      List.of(
          "sounds/Arrow_release.wav",
          "sounds/jump.ogg",
          "sounds/itempickup02.wav",
          "sounds/itempick.wav",
          "sounds/Drink_01.wav",
          "sounds/Drink_02.wav",
          "sounds/Bottle Break.wav",
          "sounds/rock_break.ogg",
          "sounds/skeleton_rattle.ogg",
          "sounds/shoot.ogg",
          "sounds/osprey.ogg",
          "sounds/hurt_player_1.wav",
          "sounds/hurt_player_2.wav",
          "sounds/hurt_player_3.wav",
          "sounds/hurt_player_4.wav",
          "sounds/enemy_Death.wav",
          "sounds/skeleton_death.ogg",
          "sounds/vulture_death.ogg",
          "sounds/necromancer_death.ogg",
          "sounds/grapple_creak_1.ogg",
          "sounds/grapple_creak_2.ogg",
          "sounds/grapple_attach.ogg",
          "sounds/grapple_fail.ogg",
          "sounds/itemSwitch.wav",
          "sounds/level_complete.wav",
          "sounds/shop_door_creak.ogg",
          "sounds/shop_bell.ogg",
          "sounds/shop_kaching.ogg",
          "sounds/checkpoint.ogg",
          "sounds/necromancer_whisper.ogg",
          "sounds/necromancer_summon.ogg",
          "sounds/calypso_windup.ogg",
          "sounds/calypso_hit.ogg",
          "sounds/calypso_teleport.ogg",
          "sounds/calypso_hurt.ogg",
          "sounds/calypso_death.ogg",
          "sounds/calypso_phase_start.ogg",
          "sounds/calypso_phase2.ogg");

  private SoundEffects() {}

  public static void load(ResourceService resourceService) {
    resourceService.loadSounds(FILES.toArray(String[]::new));
  }

  public static void unload(ResourceService resourceService) {
    resourceService.unloadAssets(FILES.toArray(String[]::new));
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
