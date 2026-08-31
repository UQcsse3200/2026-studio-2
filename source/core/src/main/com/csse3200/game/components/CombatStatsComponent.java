package com.csse3200.game.components;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store information related to combat such as health, attack, etc. Any entities
 * which engage it combat should have an instance of this class registered. This class can be
 * extended for more specific combat needs.
 */
public class CombatStatsComponent extends Component {

  private static final Logger logger = LoggerFactory.getLogger(CombatStatsComponent.class);
  private int health;
  private int maxHealth;
  private int baseAttack;
  private final long invulnerabilityDuration;
  private long invulnerableUntil;

  public CombatStatsComponent(int health, int baseAttack) {
    this(health, baseAttack, 0);
  }

  /**
   * Creates combat stats with an invulnerability window after each successful hit.
   *
   * @param health initial health
   * @param baseAttack base attack damage
   * @param invulnerabilityDuration invulnerability duration in milliseconds
   */
  public CombatStatsComponent(int health, int baseAttack, long invulnerabilityDuration) {
    this.invulnerabilityDuration = Math.max(0, invulnerabilityDuration);
    this.maxHealth = Math.max(health, 0);
    setHealth(health);
    setBaseAttack(baseAttack);
  }

  @Override
  public void create() {
    entity.getEvents().addListener("takeDamage", this::hit);
  }

  /**
   * Returns true if the entity's has 0 health, otherwise false.
   *
   * @return is player dead
   */
  public Boolean isDead() {
    return health == 0;
  }

  /**
   * Returns the entity's health.
   *
   * @return entity's health
   */
  public int getHealth() {
    return health;
  }

  /**
   * Returns the entity's maximum health.
   *
   * @return entity's maximum health
   */
  public int getMaxHealth() {
    return maxHealth;
  }

  /**
   * Increases the entity's maximum health by the given amount, and heals by the same amount.
   * Amounts less than or equal to zero are ignored.
   *
   * @param amount amount to increase maximum health by
   */
  public void addMaxHealth(int amount) {
    if (amount <= 0) {
      return;
    }
    maxHealth += amount;
    addHealth(amount);
  }

  /**
   * Sets the entity's health. Health has a minimum bound of 0.
   *
   * @param health health
   */
  public void setHealth(int health) {
    boolean wasAlive = this.health > 0;
    if (health >= 0) {
      this.health = health;
    } else {
      this.health = 0;
    }
    if (entity != null) {
      entity.getEvents().trigger("updateHealth", this.health);
      if (wasAlive && isDead()) {
        entity.getEvents().trigger("death");
      }
    }
  }

  /**
   * Adds to the player's health. The amount added can be negative.
   *
   * @param health health to add
   */
  public void addHealth(int health) {
    setHealth(this.health + health);
  }

  /**
   * Returns the entity's base attack damage.
   *
   * @return base attack damage
   */
  public int getBaseAttack() {
    return baseAttack;
  }

  /**
   * Sets the entity's attack damage. Attack damage has a minimum bound of 0.
   *
   * @param attack Attack damage
   */
  public void setBaseAttack(int attack) {
    if (attack >= 0) {
      this.baseAttack = attack;
    } else {
      logger.error("Can not set base attack to a negative attack value");
    }
  }

  public void hit(CombatStatsComponent attacker) {
    GameTime timeSource = ServiceLocator.getTimeSource();
    long currentTime = timeSource == null ? 0 : timeSource.getTime();
    if (currentTime < invulnerableUntil) {
      return;
    }

    int newHealth = getHealth() - attacker.getBaseAttack();
    setHealth(newHealth);
    invulnerableUntil = currentTime + invulnerabilityDuration;
  }
}
