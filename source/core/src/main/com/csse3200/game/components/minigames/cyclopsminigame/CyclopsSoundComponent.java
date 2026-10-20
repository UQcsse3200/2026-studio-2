package com.csse3200.game.components.minigames.cyclopsminigame;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/** Plays the cyclops's snoring, roar, landing and club sounds. */
public class CyclopsSoundComponent extends Component {
  public static final String SNORE_PATH = "sounds/minigames/cyclops/snore.wav";
  public static final String ROAR_PATH = "sounds/minigames/cyclops/roar.wav";
  public static final String LANDING_PATH = "sounds/minigames/cyclops/landing.wav";
  public static final String CLUB_HIT_PATH = "sounds/minigames/cyclops/club-hit.wav";

  static final float SNORE_VOLUME = 0.5f;
  static final float ROAR_VOLUME = 0.6f;
  static final float LANDING_VOLUME = 0.6f;
  static final float CLUB_HIT_VOLUME = 0.7f;

  private Sound snore;
  private Sound roar;

  /** Creates the CyclopsSoundComponent with necessary events and loads required assets */
  @Override
  public void create() {
    ResourceService resources = ServiceLocator.getResourceService();
    snore = resources.getAsset(SNORE_PATH, Sound.class);
    roar = resources.getAsset(ROAR_PATH, Sound.class);
    Sound landing = resources.getAsset(LANDING_PATH, Sound.class);
    Sound clubHit = resources.getAsset(CLUB_HIT_PATH, Sound.class);

    entity.getEvents().addListener(CyclopsAnimationController.WOKE_UP_EVENT, this::wakeUp);
    entity
        .getEvents()
        .addListener(
            CyclopsAnimationController.LANDED_EVENT,
            () -> landing.play(GameVolume.scale(LANDING_VOLUME)));
    entity
        .getEvents()
        .addListener(
            CyclopsAnimationController.CLUB_LANDED_EVENT,
            () -> clubHit.play(GameVolume.scale(CLUB_HIT_VOLUME)));
    entity
        .getEvents()
        .addListener(CyclopsAnimationController.FELL_ASLEEP_EVENT, this::startSnoring);
    startSnoring();
  }

  /** Disposes of the CyclopsSoundComponent and stops any sound effects */
  @Override
  public void dispose() {
    snore.stop();
  }

  /** Starts playing the snoring sound effects on loop */
  private void startSnoring() {
    snore.loop(GameVolume.scale(SNORE_VOLUME));
  }

  /** Stops the snoring sound effect and starts the roar sound effect */
  private void wakeUp() {
    snore.stop();
    roar.play(GameVolume.scale(ROAR_VOLUME));
  }
}
