package com.csse3200.game.components.npc;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.services.ServiceLocator;

public class ChaseSoundComponent extends Component {
  private static final long COOLDOWN_MS = 10000;

  private final String soundPath;
  private final float volume;
  private long lastPlayTime = -COOLDOWN_MS;

  public ChaseSoundComponent(String soundPath, float volume) {
    this.soundPath = soundPath;
    this.volume = volume;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("chaseStart", this::playChaseSound);
  }

  private void playChaseSound() {
    if (ServiceLocator.getTimeSource() == null || ServiceLocator.getResourceService() == null) {
      return;
    }
    long now = ServiceLocator.getTimeSource().getTime();
    if (now - lastPlayTime < COOLDOWN_MS
        || !ServiceLocator.getResourceService().containsAsset(soundPath, Sound.class)) {
      return;
    }
    lastPlayTime = now;
    ServiceLocator.getResourceService()
        .getAsset(soundPath, Sound.class)
        .play(GameVolume.scale(volume));
  }
}
