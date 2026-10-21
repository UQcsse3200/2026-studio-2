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
      new UsageStats(10, 15f, 0.3f, 0, true)),
  ROPE_ARROW(
      2,
      "Rope Arrow",
      "An arrow used for grappling.",
      "images/projectiles/rope_arrow.png",
      new UsageStats(0, 15f, 5f, 0, false)),
  HEALTH_POTION(
      3,
      "Health Potion",
      "Restores a small amount of health.",
      "images/health/heart_potion.png",
      new UsageStats(0, 0f, 0f, 25, false)),
  FIRE_ARROW(
      4,
      "Fire Arrow",
      "Deals 5 direct damage and burns enemies for 10 damage per second for 5 seconds.",
      "images/projectiles/fire_arrow.png",
      new UsageStats(5, 16f, 0.3f, 0, true),
      new ItemEffects(
          new TimedEffect(10f, 5f), TimedEffect.NONE, TimedEffect.NONE, TimedEffect.NONE)),
  ICE_ARROW(
      5,
      "Ice Arrow",
      "Slows enemies for 5 seconds.",
      "images/projectiles/ice_arrow.png",
      new UsageStats(8, 16f, 0.3f, 0, true),
      new ItemEffects(
          TimedEffect.NONE, new TimedEffect(0.5f, 5f), TimedEffect.NONE, TimedEffect.NONE)),

  SPEED_POTION(
      8,
      "Speed Potion",
      "Increases movement speed by 70% for 3 seconds.",
      "images/items/speed_potion.png",
      new UsageStats(0, 0f, 0f, 0, true),
      new ItemEffects(
          TimedEffect.NONE, TimedEffect.NONE, new TimedEffect(0.7f, 3f), TimedEffect.NONE)),

  POISON_POTION(
      9,
      "Poison Potion",
      "Throws a poison flask that applies poison damage over time.",
      "images/items/poison_potion.png",
      new UsageStats(0, 0f, 0f, 0, true),
      new ItemEffects(
          TimedEffect.NONE, TimedEffect.NONE, TimedEffect.NONE, new TimedEffect(3f, 3f)));

  private final int id;
  private final String displayName;
  private final String description;
  private final String texturePath;
  private final int damage;
  private final float range;
  private final float cooldown;
  private final int healAmount;
  private final boolean consumeAmmo;
  private final float burnDamagePerSecond;
  private final float burnTime;
  private final float slowSpeed;
  private final float slowTime;
  private final float speedBoost;
  private final float speedDuration;
  private final float poisonDamagePerSecond;
  private final float poisonDuration;

  private record UsageStats(
      int damage, float range, float cooldown, int healAmount, boolean consumeAmmo) {}

  private record TimedEffect(float strength, float duration) {
    private static final TimedEffect NONE = new TimedEffect(0f, 0f);
  }

  private record ItemEffects(
      TimedEffect burn, TimedEffect slow, TimedEffect speed, TimedEffect poison) {
    private static final ItemEffects NONE =
        new ItemEffects(TimedEffect.NONE, TimedEffect.NONE, TimedEffect.NONE, TimedEffect.NONE);
  }

  ItemType(int id, String displayName, String description, String texturePath, UsageStats usage) {
    this(id, displayName, description, texturePath, usage, ItemEffects.NONE);
  }

  ItemType(
      int id,
      String displayName,
      String description,
      String texturePath,
      UsageStats usage,
      ItemEffects effects) {
    this.id = id;
    this.displayName = displayName;
    this.description = description;
    this.texturePath = texturePath;
    this.damage = usage.damage();
    this.range = usage.range();
    this.cooldown = usage.cooldown();
    this.healAmount = usage.healAmount();
    this.consumeAmmo = usage.consumeAmmo();
    this.burnDamagePerSecond = effects.burn().strength();
    this.burnTime = effects.burn().duration();
    this.slowSpeed = effects.slow().strength();
    this.slowTime = effects.slow().duration();
    this.speedBoost = effects.speed().strength();
    this.speedDuration = effects.speed().duration();
    this.poisonDamagePerSecond = effects.poison().strength();
    this.poisonDuration = effects.poison().duration();
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
    return damage;
  }

  public float getRange() {
    return range;
  }

  public float getCooldown() {
    return cooldown;
  }

  public int getHealAmount() {
    return healAmount;
  }

  public boolean consumesAmmo() {
    return consumeAmmo;
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
    return burnDamagePerSecond;
  }

  public float getBurnTime() {
    return burnTime;
  }

  public float getSlowSpeed() {
    return slowSpeed;
  }

  public float getSlowTime() {
    return slowTime;
  }

  public float getSpeedBoost() {
    return speedBoost;
  }

  public float getDuration() {
    return speedDuration;
  }

  public float getPoisonDamagePerSecond() {
    return poisonDamagePerSecond;
  }

  public float getPoisonDuration() {
    return poisonDuration;
  }
}
