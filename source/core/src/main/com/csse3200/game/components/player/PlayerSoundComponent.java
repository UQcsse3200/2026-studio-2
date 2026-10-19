package com.csse3200.game.components.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.SoundEffects;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.services.ServiceLocator;

public class PlayerSoundComponent extends Component {
  private static final String[] HURT_SOUNDS = {
    "sounds/hurt_player_1.wav",
    "sounds/hurt_player_2.wav",
    "sounds/hurt_player_3.wav",
    "sounds/hurt_player_4.wav"
  };
  private static final float[] HURT_VOLUMES = {0.3f, 0.3f, 1.0f, 0.6f};
  private static final long ROPE_SWING_COOLDOWN_MS = 1500;

  private long lastRopeSwingTime = -ROPE_SWING_COOLDOWN_MS;

  @Override
  public void create() {
    entity.getEvents().addListener("jump", () -> SoundEffects.play("sounds/jump.ogg", 0.5f));
    entity.getEvents().addListener("hurt", this::playHurt);
    entity
        .getEvents()
        .addListener(
            "itemPickedUp", (Object item) -> SoundEffects.play("sounds/itempickup02.wav", 0.2f));
    entity
        .getEvents()
        .addListener(
            "goldPickedUp", (Integer amount) -> SoundEffects.play("sounds/itempick.wav", 0.2f));
    entity.getEvents().addListener("itemUsed", this::playPotion);
    entity.getEvents().addListener("grappleSwing", (Float direction) -> playRopeSwing());
    entity
        .getEvents()
        .addListener(
            "inventorySelectionChanged", () -> SoundEffects.play("sounds/itemSwitch.wav", 0.15f));
  }

  private void playHurt() {
    int index = MathUtils.random(HURT_SOUNDS.length - 1);
    SoundEffects.play(HURT_SOUNDS[index], HURT_VOLUMES[index]);
  }

  private void playPotion(Object item) {
    if (item != ItemType.HEALTH_POTION && item != ItemType.SpeedPotion) {
      return;
    }
    SoundEffects.play("sounds/Drink_01.wav", 0.9f);
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            SoundEffects.play("sounds/Drink_02.wav", 0.9f);
          }
        },
        0.45f);
  }

  private void playRopeSwing() {
    if (ServiceLocator.getTimeSource() == null) {
      return;
    }
    long now = ServiceLocator.getTimeSource().getTime();
    if (now - lastRopeSwingTime < ROPE_SWING_COOLDOWN_MS) {
      return;
    }
    lastRopeSwingTime = now;
    SoundEffects.play("sounds/rope_swing.wav", 1.0f);
  }
}
