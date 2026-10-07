package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class PlayerSoundComponentTest {
  private ResourceService resources;
  private Sound sound;
  private GameTime time;
  private Entity player;

  @BeforeEach
  void setUp() {
    resources = mock(ResourceService.class);
    sound = mock(Sound.class);
    when(resources.containsAsset(anyString(), eq(Sound.class))).thenReturn(true);
    when(resources.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    player = new Entity().addComponent(new PlayerSoundComponent());
    player.create();
  }

  @Test
  void shouldPlayJumpSound() {
    player.getEvents().trigger("jump");

    verify(resources).getAsset("sounds/jump.ogg", Sound.class);
    verify(sound).play(0.5f);
  }

  @Test
  void shouldPlayOneOfTheHurtSounds() {
    player.getEvents().trigger("hurt");

    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    verify(resources).getAsset(path.capture(), eq(Sound.class));
    assertTrue(path.getValue().startsWith("sounds/hurt_player_"));
    verify(sound).play(anyFloat());
  }

  @Test
  void shouldPlayDifferentSoundsForItemsAndGold() {
    player.getEvents().trigger("itemPickedUp", ItemType.HEALTH_POTION);
    player.getEvents().trigger("goldPickedUp", 5);

    verify(resources).getAsset("sounds/itempickup02.wav", Sound.class);
    verify(resources).getAsset("sounds/itempick.wav", Sound.class);
  }

  @Test
  void shouldPlayGulpForHealthPotion() {
    player.getEvents().trigger("itemUsed", ItemType.HEALTH_POTION);

    verify(resources).getAsset("sounds/Drink_01.wav", Sound.class);
    verify(sound).play(0.9f);
  }

  @Test
  void shouldNotPlayGulpForNonPotionItems() {
    player.getEvents().trigger("itemUsed", ItemType.STANDARD_ARROW);

    verify(resources, never()).getAsset("sounds/Drink_01.wav", Sound.class);
  }

  @Test
  void shouldLimitRopeSwingSoundToOncePerCooldown() {
    when(time.getTime()).thenReturn(0L, 500L, 2000L);

    player.getEvents().trigger("grappleSwing", 1f);
    player.getEvents().trigger("grappleSwing", 1f);
    player.getEvents().trigger("grappleSwing", 1f);

    verify(resources, times(2)).getAsset("sounds/rope_swing.wav", Sound.class);
  }

  @Test
  void shouldPlayItemSwitchSound() {
    player.getEvents().trigger("inventorySelectionChanged");

    verify(resources).getAsset("sounds/itemSwitch.wav", Sound.class);
    verify(sound).play(0.15f);
  }
}
