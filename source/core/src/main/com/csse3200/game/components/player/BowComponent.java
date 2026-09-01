package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Ranged attack behaviour that fires standard player arrows. Holding the fire button charges the
 * shot up over {@link #MAX_CHARGE_SECONDS}, linearly scaling arrow speed from {@link
 * #MIN_CHARGE_SPEED} up to full speed; releasing fires immediately at whatever charge has been
 * reached. A cooldown prevents firing again too soon after the last shot.
 */
public class BowComponent extends Component implements AttackBehaviour {
  private static final String ATTACK_SOUND = "sounds/Impact4.ogg";
  private static final float MIN_CHARGE_SPEED = 3f;
  private static final float MAX_CHARGE_SPEED = ProjectileFactory.STANDARD_ARROW_SPEED;
  private static final float MAX_CHARGE_SECONDS = 2f;
  private static final long COOLDOWN_MS = 200L;

  /** Spawns a projectile entity given a position, direction, and travel speed. */
  interface ProjectileSpawner {
    Entity spawn(Vector2 position, Vector2 direction, float speed);
  }

  private final ProjectileSpawner projectileFactory;
  private GameTime timeSource;
  private boolean charging;
  private float chargeSeconds;
  private long lastShotTimeMs = -1;

  /** Creates a bow which fires standard arrows. */
  public BowComponent() {
    this(ProjectileFactory::createPlayerArrow);
  }

  BowComponent(ProjectileSpawner projectileFactory) {
    this.projectileFactory = projectileFactory;
  }

  @Override
  public void create() {
    timeSource = ServiceLocator.getTimeSource();
    entity.getEvents().addListener("attackChargeStart", this::startCharging);
  }

  @Override
  public void update() {
    if (charging) {
      chargeSeconds = Math.min(chargeSeconds + timeSource.getDeltaTime(), MAX_CHARGE_SECONDS);
    }
  }

  private void startCharging() {
    charging = true;
    chargeSeconds = 0f;
  }

  @Override
  public void attack(Vector2 direction) {
    float charge = chargeSeconds;
    charging = false;
    chargeSeconds = 0f;

    if (direction == null || direction.isZero() || isOnCooldown()) {
      entity.getEvents().trigger("attackCancelled");
      return;
    }

    float chargeFraction = charge / MAX_CHARGE_SECONDS;
    float speed = MIN_CHARGE_SPEED + (MAX_CHARGE_SPEED - MIN_CHARGE_SPEED) * chargeFraction;

    Vector2 normalizedDirection = direction.cpy().nor();
    Vector2 spawnPosition =
        entity.getCenterPosition().mulAdd(normalizedDirection, entity.getScale().x * 0.6f);
    Entity projectile = projectileFactory.spawn(spawnPosition, normalizedDirection, speed);
    ServiceLocator.getEntityService().register(projectile);

    Sound attackSound = ServiceLocator.getResourceService().getAsset(ATTACK_SOUND, Sound.class);
    attackSound.play();
    entity.getEvents().trigger("attackAnimation", normalizedDirection.cpy());

    lastShotTimeMs = timeSource.getTime();
  }

  private boolean isOnCooldown() {
    return lastShotTimeMs >= 0 && timeSource.getTimeSince(lastShotTimeMs) < COOLDOWN_MS;
  }
}
