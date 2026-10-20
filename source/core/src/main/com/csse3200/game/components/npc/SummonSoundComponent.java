package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.components.SoundEffects;
import com.csse3200.game.entities.Entity;

public class SummonSoundComponent extends Component {
  private final String soundPath;
  private final float volume;

  public SummonSoundComponent(String soundPath, float volume) {
    this.soundPath = soundPath;
    this.volume = volume;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("spawnEntity", this::onSpawn);
  }

  private void onSpawn(Entity spawned) {
    if (spawned != null && spawned.getComponent(EnemyDeathComponent.class) != null) {
      SoundEffects.play(soundPath, volume);
    }
  }
}
