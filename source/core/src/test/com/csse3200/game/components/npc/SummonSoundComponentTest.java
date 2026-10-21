package com.csse3200.game.components.npc;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SummonSoundComponentTest {
  private static final String SUMMON_SOUND = "sounds/necromancer_summon.ogg";

  private Sound sound;
  private Entity necromancer;

  @BeforeEach
  void setUp() {
    ResourceService resources = mock(ResourceService.class);
    sound = mock(Sound.class);
    when(resources.containsAsset(SUMMON_SOUND, Sound.class)).thenReturn(true);
    when(resources.getAsset(SUMMON_SOUND, Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    necromancer = new Entity().addComponent(new SummonSoundComponent(SUMMON_SOUND, 0.5f));
    necromancer.create();
  }

  @Test
  void shouldPlayWhenAnEnemyIsSummoned() {
    Entity skeleton = new Entity().addComponent(new EnemyDeathComponent());

    necromancer.getEvents().trigger("spawnEntity", skeleton);

    verify(sound).play(0.5f);
  }

  @Test
  void shouldNotPlayWhenAnItemIsDropped() {
    Entity item = new Entity();

    necromancer.getEvents().trigger("spawnEntity", item);

    verify(sound, never()).play(anyFloat());
  }
}
