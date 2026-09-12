package com.csse3200.game.components.item;

/**
 * Catalog of item kinds. Static attributes (id, name, combat stats, texture) live here so inventory
 * UI and item use do not keep a second copy.
 */
public enum ItemType {
  ARROW(
      1,
      "Standard Arrow",
      "A basic arrow used as ammunition.",
      "images/arrow.png",
      10,
      15f,
      0f,
      0,
      true),
  RopeArrow(
      2,
      "Rope Arrow",
      "An arrow used for grappling.",
      "images/rope_arrow.png",
      0,
      15f,
      5f,
      0,
      false),
  CONSUMABLE(
      3,
      "Health Potion",
      "Restores a small amount of health.",
      "images/heart.png",
      0,
      0f,
      0f,
      25,
      true),
  FireArrow(
      4,
      "Fire Arrow",
      "An arrow that burns enemies over time.",
      "images/fire_arrow.png",
      5,
      16f,
      0f,
      0,
      true,
      3f,
      5f,
      0f,
      0f),
  ColdArrow(
      5,
      "Cold Arrow",
      "An arrow that slows enemies.",
      "images/cold_arrow.png",
      8,
      16f,
      0f,
      0,
      true,
      0f,
      0f,
      0.75f,
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
      float slowTime) {
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
}
