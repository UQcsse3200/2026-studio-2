package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;

/**
 * Plays a hurt voice each time its entity fires "hurt". The voice is picked by hearts remaining, so
 * each heart lost sounds heavier than the last. The fatal hit plays a heavier death voice.
 */
public class CyclopsHurtSoundComponent extends Component {
  static final String HURT_EVENT = "hurt";
  static final int HEALTH_PER_HEART = 2;
  static final float DEATH_VOLUME = 0.8f;
  static final float DEATH_PITCH = 0.75f;

  private final Sound[] voicesByHearts;
  private final float[] volumesByHearts;
  private final Sound deathVoice;

  public CyclopsHurtSoundComponent(
      Sound[] voicesByHearts, float[] volumesByHearts, Sound deathVoice) {
    this.voicesByHearts = voicesByHearts;
    this.volumesByHearts = volumesByHearts;
    this.deathVoice = deathVoice;
  }

  @Override
  public void create() {
    entity.getEvents().addListener(HURT_EVENT, this::playVoiceForHearts);
  }

  void playVoiceForHearts() {
    CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
    if (combatStats != null && combatStats.isDead()) {
      deathVoice.play(DEATH_VOLUME, DEATH_PITCH, 0f);
      return;
    }
    int hearts = combatStats == null ? 0 : combatStats.getHealth() / HEALTH_PER_HEART;
    int index = Math.min(hearts, voicesByHearts.length - 1);
    voicesByHearts[index].play(volumesByHearts[index]);
  }
}
