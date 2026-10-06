package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;

/**
 * Plays a hurt voice each time its entity fires "hurt". The voice is picked by hearts remaining, so
 * each heart lost sounds heavier than the last.
 */
public class CyclopsHurtSoundComponent extends Component {
  static final String HURT_EVENT = "hurt";
  static final int HEALTH_PER_HEART = 2;

  private final Sound[] voicesByHearts;
  private final float[] volumesByHearts;

  public CyclopsHurtSoundComponent(Sound[] voicesByHearts, float[] volumesByHearts) {
    this.voicesByHearts = voicesByHearts;
    this.volumesByHearts = volumesByHearts;
  }

  @Override
  public void create() {
    entity.getEvents().addListener(HURT_EVENT, this::playVoiceForHearts);
  }

  void playVoiceForHearts() {
    CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
    int hearts = combatStats == null ? 0 : combatStats.getHealth() / HEALTH_PER_HEART;
    int index = Math.min(hearts, voicesByHearts.length - 1);
    voicesByHearts[index].play(volumesByHearts[index]);
  }
}
