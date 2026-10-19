package com.csse3200.game.components.minigames.cyclopsminigame;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.GameVolume;
import java.util.List;

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
  private final List<Float> volumesByHearts;
  private final Sound deathVoice;

  public CyclopsHurtSoundComponent(
      Sound[] voicesByHearts, List<Float> volumesByHearts, Sound deathVoice) {
    this.voicesByHearts = voicesByHearts;
    this.volumesByHearts = volumesByHearts;
    this.deathVoice = deathVoice;
  }

  /**
   * Adds the "hurt" event to this component. Event handles playing the sound effect
   * differently based on the entity this component is attached to.
   */
  @Override
  public void create() {
    entity.getEvents().addListener(HURT_EVENT, this::playVoiceForHearts);
  }

  /**
   * Changes the sound effect on the attached entity that plays a hurt sound effect.
   * Hurt sound effect is pitched and volumed differently based on the attached entity's
   * CombatStatsComponent.
   */
  void playVoiceForHearts() {
    CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
    if (combatStats != null && combatStats.isDead()) {
      deathVoice.play(GameVolume.scale(DEATH_VOLUME), DEATH_PITCH, 0f);
      return;
    }
    int hearts = combatStats == null ? 0 : combatStats.getHealth() / HEALTH_PER_HEART;
    int index = Math.min(hearts, voicesByHearts.length - 1);
    voicesByHearts[index].play(GameVolume.scale(volumesByHearts.get(index)));
  }
}
