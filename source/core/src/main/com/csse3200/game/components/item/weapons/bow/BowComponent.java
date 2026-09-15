package com.csse3200.game.components.item.weapons.bow;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;

/** Ranged attack behaviour that fires player arrow variants (Standard, Cold, Fire, Grapple). */
public class BowComponent extends Component implements PrimaryWeapon {

  private static final String ATTACK_SOUND = "sounds/Impact4.ogg";
  private static final float BOW_COOLDOWN = 0.4f;
  private static final float MAX_CHARGE_SECONDS = 1.5f;
  // Fraction of full speed a shot has at zero charge - keeps a tap-release shot weak/short-range
  // rather than firing at full power or not firing at all.
  private static final float MIN_CHARGE_SPEED_FACTOR = 0.3f;

  @FunctionalInterface
  public interface ProjectileCreator {
    Entity create(Entity shooter, Vector2 position, Vector2 direction, float speedMultiplier);
  }

  private ProjectileCreator projectileCreator;
  private ArrowType currentArrowType = ArrowType.STANDARD;
  private float cooldownTimer = 0f;
  private boolean isCharging = false;
  private long chargeStartTimeMs = 0;

  public BowComponent() {
    this(ArrowType.STANDARD);
  }

  public BowComponent(ArrowType arrowType) {
    setArrowType(arrowType);
  }

  public BowComponent(ProjectileCreator projectileCreator) {
    this.projectileCreator = projectileCreator;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("setArrowType", this::setArrowType);
    entity.getEvents().addListener("chargeStart", this::startCharge);
    entity.getEvents().addListener("chargeRelease", this::releaseCharge);
    entity.getEvents().addListener("death", this::cancelCharge);
  }

  @Override
  public void update() {
    if (cooldownTimer > 0f) {
      cooldownTimer -= ServiceLocator.getTimeSource().getDeltaTime();
    }
  }

  /** Swaps the active arrow type fired by the bow. */
  public void setArrowType(ArrowType arrowType) {
    if (arrowType == null) {
      arrowType = ArrowType.STANDARD;
    }
    this.currentArrowType = arrowType;
    switch (arrowType) {
      case COLD:
        this.projectileCreator = ProjectileFactory::createColdArrow;
        break;
      case FIRE:
        this.projectileCreator = ProjectileFactory::createFireArrow;
        break;
      case GRAPPLE:
        this.projectileCreator = ProjectileFactory::createGrappleArrow;
        break;
      case STANDARD:
      default:
        this.projectileCreator = ProjectileFactory::createPlayerArrow;
        break;
    }
  }

  public ArrowType getArrowType() {
    return currentArrowType;
  }

  @Override
  public void attack(Vector2 direction) {
    if (!isReady()) {
      return;
    }
    if (fire(direction, 1f)) {
      cooldownTimer = BOW_COOLDOWN;
    }
  }

  /**
   * Begins charging a shot, e.g. while the player holds the shoot button down. No-ops if the bow is
   * on cooldown. Does not start the cooldown itself - that happens once the shot actually fires, so
   * a long charge doesn't eat into the post-shot cooldown window.
   *
   * @param direction Aim direction at the moment charging started (used only for facing/animation).
   */
  public void startCharge(Vector2 direction) {
    if (direction == null || direction.isZero() || !isReady()) {
      return;
    }
    isCharging = true;
    chargeStartTimeMs = ServiceLocator.getTimeSource().getTime();
  }

  /**
   * Fires the currently charging shot, if any, with speed scaled linearly by how long it was held
   * (from {@link #MIN_CHARGE_SPEED_FACTOR} at 0s up to full speed at {@link #MAX_CHARGE_SECONDS}).
   * No-ops if nothing was charging.
   *
   * @param direction Aim direction at release time.
   */
  public void releaseCharge(Vector2 direction) {
    if (!isCharging) {
      return;
    }
    isCharging = false;

    long now = ServiceLocator.getTimeSource().getTime();
    float elapsedSeconds = Math.min(MAX_CHARGE_SECONDS, (now - chargeStartTimeMs) / 1000f);
    float chargeFraction = elapsedSeconds / MAX_CHARGE_SECONDS;
    float speedMultiplier =
        MIN_CHARGE_SPEED_FACTOR + (1.5f - MIN_CHARGE_SPEED_FACTOR) * chargeFraction;

    if (fire(direction, speedMultiplier)) {
      cooldownTimer = BOW_COOLDOWN;
    }
  }

  /** Cancels an in-progress charge without firing, e.g. if the player dies mid-draw. */
  private void cancelCharge() {
    isCharging = false;
  }

  /**
   * Spawns a projectile in the given direction at the given speed scale.
   *
   * @return true if a projectile was actually spawned.
   */
  private boolean fire(Vector2 direction, float speedMultiplier) {
    if (direction == null || direction.isZero()) {
      return false;
    }

    Vector2 normalizedDirection = direction.cpy().nor();
    Vector2 spawnPosition =
        entity.getCenterPosition().mulAdd(normalizedDirection, entity.getScale().x * 0.8f);

    Entity projectile =
        projectileCreator.create(entity, spawnPosition, normalizedDirection, speedMultiplier);
    ServiceLocator.getEntityService().register(projectile);

    if (ServiceLocator.getResourceService() != null
        && ServiceLocator.getResourceService().containsAsset(ATTACK_SOUND, Sound.class)) {
      Sound attackSound = ServiceLocator.getResourceService().getAsset(ATTACK_SOUND, Sound.class);
      if (attackSound != null) {
        attackSound.play();
      }
    }

    entity.getEvents().trigger("attackAnimation", normalizedDirection.cpy());
    return true;
  }

  @Override
  public boolean isReady() {
    return !isCharging && cooldownTimer <= 0f;
  }

  @Override
  public float getCooldownRemaining() {
    return Math.max(0f, cooldownTimer);
  }
}
