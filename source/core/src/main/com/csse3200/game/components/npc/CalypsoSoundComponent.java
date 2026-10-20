package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.SoundEffects;
import com.csse3200.game.components.tasks.SweepAttackTask;
import com.csse3200.game.services.ServiceLocator;

public class CalypsoSoundComponent extends Component {
  static final String WINDUP_SOUND = "sounds/calypso_windup.ogg";
  static final String HIT_SOUND = "sounds/calypso_hit.ogg";
  static final String TELEPORT_SOUND = "sounds/calypso_teleport.ogg";
  static final String HURT_SOUND = "sounds/calypso_hurt.ogg";
  static final String PHASE_START_SOUND = "sounds/calypso_phase_start.ogg";
  static final String PHASE_TWO_SOUND = "sounds/calypso_phase2.ogg";
  static final long TELEPORT_COOLDOWN_MS = 2000;

  private long lastTeleportSound = -TELEPORT_COOLDOWN_MS;

  @Override
  public void create() {
    entity
        .getEvents()
        .addListener(
            SweepAttackTask.ATTACK_START_EVENT, () -> SoundEffects.play(WINDUP_SOUND, 0.5f));
    entity
        .getEvents()
        .addListener(SweepAttackTask.SWEEP_HIT_EVENT, () -> SoundEffects.play(HIT_SOUND, 0.6f));
    entity.getEvents().addListener(SweepAttackTask.HIT_EVENT, this::playTeleport);
    entity.getEvents().addListener("hurt", () -> SoundEffects.play(HURT_SOUND, 1.0f));
    entity
        .getEvents()
        .addListener("beginBossPhaseChange", () -> SoundEffects.play(PHASE_START_SOUND, 0.6f));
    entity
        .getEvents()
        .addListener(
            "bossPhaseChanged", (Integer phase) -> SoundEffects.play(PHASE_TWO_SOUND, 0.7f));
  }

  private void playTeleport() {
    if (ServiceLocator.getTimeSource() != null) {
      long now = ServiceLocator.getTimeSource().getTime();
      if (now - lastTeleportSound < TELEPORT_COOLDOWN_MS) {
        return;
      }
      lastTeleportSound = now;
    }
    SoundEffects.play(TELEPORT_SOUND, 0.5f);
  }
}
