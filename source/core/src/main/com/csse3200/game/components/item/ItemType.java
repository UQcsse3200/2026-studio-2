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
      "images/arrow.png",
      10,
      15f,
      0f,
      0,
      true),
  ROPE_ARROW(
      2,
      "Rope Arrow",
      "An arrow used for grappling.",
      "images/rope_arrow.png",
      0,
      15f,
      5f,
      0,
      false),
  HEALTH_POTION(
      3,
      "Health Potion",
      "Restores a small amount of health.",
      "images/red_heart.png",
      0,
      0f,
      0f,
      25,
      false),
  FIRE_ARROW(
      4,
      "Fire Arrow",
      "Deals 12 direct damage and burns enemies for 5 damage per second for 3 seconds.",
      "images/fire_arrow.png",
      5,
      16f,
      0f,
      0,
      true,
      10f,
      5f,
      0f,
      0f,
      0f,
      0f,
      0f,
      0f),
  ICE_ARROW(
      5,
      "Ice Arrow",
      "Slows enemies for 3 seconds.",
      "images/cold_arrow.png",
      8,
      16f,
      0f,
      0,
      true,
      0f,
      0f,
      0.5f,
      5f,
      0f,
      0f,
      0f,
      0f),

  Sword(
      6,
      "Great Sword",
      "A heavy sword with high damage.",
      "images/sword.png",
      20,
      5f,
      0f,
      0,
      false),

  Spear(7, "Spear", "A long spear with extended range.", "images/spear.png", 12, 8f, 0f, 0, false),

  SpeedPotion(
      8,
      "Speed Potion",
      "Increases movement speed by 70% for 3 seconds.",
      "images/speed_potion.png",
      0,
      0f,
      0f,
      0,
      true,
      0f,
      0f,
      0f,
      0f,
      0.7f,
      3f,
      0f,
      0f),

  PoisonPotion(
      9,
      "Poison Potion",
      "Applies poison damage over time.",
      "images/poison_potion.png",
      0,
      0f,
      0f,
      0,
      true,
      0f,
      0f,
      0f,
      0f,
      0f,
      0f,
      5f,
      5f);

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

  ItemType(
      int id,
      String displayName,
      String description,
      String texturePath,
      int damage,
      float range,
      float cooldown,
      int healAmount,
      boolean consumeAmmo) {
    this(
        id,
        displayName,
        description,
        texturePath,
        damage,
        range,
        cooldown,
        healAmount,
        consumeAmmo,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f);
  }

  ItemType(
      int id,
      String displayName,
      String description,
      String texturePath,
      int damage,
      float range,
      float cooldown,
      int healAmount,
      boolean consumeAmmo,
      float burnDamagePerSecond,
      float burnTime,
      float slowSpeed,
      float slowTime,
      float speedBoost,
      float speedDuration,
      float poisonDamagePerSecond,
      float poisonDuration) {
    this.id = id;
    this.displayName = displayName;
    this.description = description;
    this.texturePath = texturePath;
    this.damage = damage;
    this.range = range;
    this.cooldown = cooldown;
    this.healAmount = healAmount;
    this.consumeAmmo = consumeAmmo;
    this.burnDamagePerSecond = burnDamagePerSecond;
    this.burnTime = burnTime;
    this.slowSpeed = slowSpeed;
    this.slowTime = slowTime;
    this.speedBoost = speedBoost;
    this.speedDuration = speedDuration;
    this.poisonDamagePerSecond = poisonDamagePerSecond;
    this.poisonDuration = poisonDuration;
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
      case FIRE_ARROW -> "images/fireArr_animation.png";
      case ICE_ARROW -> "images/coldArr_animation.png";
      default -> "images/arrow.png";
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
      case HEALTH_POTION, Sword, Spear, SpeedPotion, PoisonPotion -> false;
    };
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
