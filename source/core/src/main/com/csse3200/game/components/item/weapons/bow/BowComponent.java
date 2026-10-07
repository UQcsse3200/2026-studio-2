package com.csse3200.game.components.item.weapons.bow;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.services.ServiceLocator;

/** Ranged attack behaviour that fires player arrow variants (Standard, Ice, Fire, Grapple). */
public class BowComponent extends Component implements PrimaryWeapon {

  private static final String ATTACK_SOUND = "sounds/shoot.ogg";
  private static final float BOW_COOLDOWN = 0.4f;

  /** How far in front of the player's centre, in player widths, a fired arrow spawns. */
  public static final float SPAWN_OFFSET = 0.8f;

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
    entity.getEvents().addListener("chargeCancel", this::cancelCharge);
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
      case ICE:
        this.projectileCreator = ProjectileFactory::createIceArrow;
        break;
      case POISON:
        this.projectileCreator = ProjectileFactory::createPoisonArrow;
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
   * Fires the currently charging shot, if any, with speed scaled by how long it was held (see
   * {@link BowCharge}). No-ops if nothing was charging.
   *
   * @param direction Aim direction at release time.
   */
  public void releaseCharge(Vector2 direction) {
    if (!isCharging) {
      return;
    }
    // Read the multiplier while still charging; it reports the minimum once the draw is cleared.
    float speedMultiplier = getChargeSpeedMultiplier();
    isCharging = false;

    if (fire(direction, speedMultiplier)) {
      cooldownTimer = BOW_COOLDOWN;
    }
  }

  /**
   * @return true while a shot is being drawn and has not yet been released or cancelled
   */
  public boolean isCharging() {
    return isCharging;
  }

  /**
   * Returns the speed multiplier a shot released right now would get (see {@link BowCharge}).
   * Returns the minimum factor when nothing is charging.
   */
  public float getChargeSpeedMultiplier() {
    long heldMs = 0L;
    if (isCharging) {
      heldMs = ServiceLocator.getTimeSource().getTime() - chargeStartTimeMs;
    }
    return BowCharge.speedMultiplier(heldMs);
  }

  /**
   * @return the launch speed a shot released right now would have, in world units per second
   */
  public float getLaunchSpeed() {
    return ProjectileFactory.getBaseSpeed(currentArrowType) * getChargeSpeedMultiplier();
  }

  /**
   * @return the y acceleration every arrow type, grapple included, experiences in flight
   */
  public float getArrowGravityY() {
    return PhysicsEngine.GRAVITY_Y * ArrowProjectileComponent.ARC_GRAVITY_SCALE;
  }

  /**
   * Returns where an arrow shot in the given direction appears, just in front of the shooter.
   *
   * @param direction aim direction (does not need to be normalised)
   * @return world spawn position
   */
  public Vector2 getSpawnPosition(Vector2 direction) {
    return entity
        .getCenterPosition()
        .mulAdd(direction.cpy().nor(), entity.getScale().x * SPAWN_OFFSET);
  }

  /**
   * Cancels an in-progress charge without firing, e.g. if the player dies mid-draw or a UI overlay
   * steals the mouse-up.
   */
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
    Vector2 spawnPosition = getSpawnPosition(normalizedDirection);

    Entity projectile =
        projectileCreator.create(entity, spawnPosition, normalizedDirection, speedMultiplier);
    ServiceLocator.getEntityService().register(projectile);

    if (ServiceLocator.getResourceService() != null
        && ServiceLocator.getResourceService().containsAsset(ATTACK_SOUND, Sound.class)) {
      Sound attackSound = ServiceLocator.getResourceService().getAsset(ATTACK_SOUND, Sound.class);
      if (attackSound != null) {
        attackSound.play(GameVolume.scale(1f));
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
