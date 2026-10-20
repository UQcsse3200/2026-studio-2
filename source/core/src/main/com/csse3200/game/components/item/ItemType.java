package com.csse3200.game.components.item;

import com.csse3200.game.components.projectile.ArrowType;

/**
 * Catalog of item kinds. Static attributes (id, name, combat stats, texture) live here so inventory
 * UI and item use do not keep a second copy.
 */
public enum ItemType {
  STANDARD_ARROW(
      1,
      "Standard Arrow",
      "A basic arrow used as ammunition.",
      "images/projectiles/arrow.png",
      new ItemStats(10, 15f, 0.3f, 0, true),
      StatusEffects.none()),
  ROPE_ARROW(
      2,
      "Rope Arrow",
      "An arrow used for grappling.",
      "images/projectiles/rope_arrow.png",
      new ItemStats(0, 15f, 5f, 0, false),
      StatusEffects.none()),
  HEALTH_POTION(
      3,
      "Health Potion",
      "Restores a small amount of health.",
      "images/health/heart_potion.png",
      new ItemStats(0, 0f, 0f, 25, false),
      StatusEffects.none()),
  FIRE_ARROW(
      4,
      "Fire Arrow",
      "Deals 5 direct damage and burns enemies for 10 damage per second for 5 seconds.",
      "images/projectiles/fire_arrow.png",
      new ItemStats(5, 16f, 0.3f, 0, true),
      new StatusEffects(
          new TimedEffect(10f, 5f), TimedEffect.none(), TimedEffect.none(), TimedEffect.none())),
  ICE_ARROW(
      5,
      "Ice Arrow",
      "Slows enemies for 5 seconds.",
      "images/projectiles/ice_arrow.png",
      new ItemStats(8, 16f, 0.3f, 0, true),
      new StatusEffects(
          TimedEffect.none(), new TimedEffect(0.5f, 5f), TimedEffect.none(), TimedEffect.none())),
  SPEED_POTION(
      8,
      "Speed Potion",
      "Increases movement speed by 70% for 3 seconds.",
      "images/items/speed_potion.png",
      new ItemStats(0, 0f, 0f, 0, true),
      new StatusEffects(
          TimedEffect.none(), TimedEffect.none(), new TimedEffect(0.7f, 3f), TimedEffect.none())),
  POISON_POTION(
      9,
      "Poison Potion",
      "Throws a poison flask that applies poison damage over time.",
      "images/items/poison_potion.png",
      new ItemStats(0, 0f, 0f, 0, true),
      new StatusEffects(
          TimedEffect.none(), TimedEffect.none(), TimedEffect.none(), new TimedEffect(3f, 3f)));

  private final int id;
  private final String displayName;
  private final String description;
  private final String texturePath;
  private final ItemStats stats;
  private final StatusEffects effects;

  ItemType(
      int id,
      String displayName,
      String description,
      String texturePath,
      ItemStats stats,
      StatusEffects effects) {
    this.id = id;
    this.displayName = displayName;
    this.description = description;
    this.texturePath = texturePath;
    this.stats = stats;
    this.effects = effects;
  }

  public int getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public String getTexturePath() {
    return texturePath;
  }

  public String getProjectileTexturePath() {
    return switch (this) {
      case FIRE_ARROW -> "images/projectiles/fireArr_animation.png";
      case ICE_ARROW -> "images/projectiles/coldArr_animation.png";
      default -> "images/projectiles/arrow.png";
    };
  }

  public int getDamage() {
    return stats.damage();
  }

  public float getRange() {
    return stats.range();
  }

  public float getCooldown() {
    return stats.cooldown();
  }

  public int getHealAmount() {
    return stats.healAmount();
  }

  public boolean consumesAmmo() {
    return stats.consumeAmmo();
  }

  /** Checks if the item is arrow ammunition. */
  public boolean isArrow() {
    return switch (this) {
      case STANDARD_ARROW, ROPE_ARROW, FIRE_ARROW, ICE_ARROW -> true;
      case HEALTH_POTION, SPEED_POTION, POISON_POTION -> false;
    };
  }

  /**
   * Checks if the item is aimed with right click: every arrow, plus the thrown poison potion.
   *
   * @return true for items that show the crosshair and trajectory while right click is held
   */
  public boolean isAimed() {
    return isArrow() || this == POISON_POTION;
  }

  /** Converts this inventory item type into its corresponding combat arrow type. */
  public ArrowType toArrowType() {
    return switch (this) {
      case FIRE_ARROW -> ArrowType.FIRE;
      case ICE_ARROW -> ArrowType.ICE;
      case ROPE_ARROW -> ArrowType.GRAPPLE;
      case STANDARD_ARROW -> ArrowType.STANDARD;
      default -> null;
    };
  }

  public float getBurnDamagePerSecond() {
    return effects.burn().amount();
  }

  public float getBurnTime() {
    return effects.burn().duration();
  }

  public float getSlowSpeed() {
    return effects.slow().amount();
  }

  public float getSlowTime() {
    return effects.slow().duration();
  }

  public float getSpeedBoost() {
    return effects.speed().amount();
  }

  public float getDuration() {
    return effects.speed().duration();
  }

  public float getPoisonDamagePerSecond() {
    return effects.poison().amount();
  }

  public float getPoisonDuration() {
    return effects.poison().duration();
  }

  private record ItemStats(
      int damage, float range, float cooldown, int healAmount, boolean consumeAmmo) {}

  private record TimedEffect(float amount, float duration) {
    private static TimedEffect none() {
      return new TimedEffect(0f, 0f);
    }
  }

  private record StatusEffects(
      TimedEffect burn, TimedEffect slow, TimedEffect speed, TimedEffect poison) {
    private static StatusEffects none() {
      return new StatusEffects(
          TimedEffect.none(), TimedEffect.none(), TimedEffect.none(), TimedEffect.none());
    }
  }
}
