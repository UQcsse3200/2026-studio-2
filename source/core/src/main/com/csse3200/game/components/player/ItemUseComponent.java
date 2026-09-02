package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.ColdArr;
import com.csse3200.game.components.item.weapons.FireArr;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the currently selected inventory item when the player attacks.
 *
 * <p>An item must be owned and selected before it can be used. Standard arrows fire through {@code
 * primaryAttack} before ammunition is removed. Rope arrows fire through {@code grappleFire} before
 * the cooldown starts. Consumables heal the player and are removed from the inventory.
 *
 * <p>Requires InventoryComponent. CombatStatsComponent is required to use consumables.
 */
public class ItemUseComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(ItemUseComponent.class);
  private static final String ATTACK_SOUND = "sounds/Impact4.ogg";
  private static final Vector2 DEFAULT_AIM = new Vector2(1f, 0f);

  private InventoryComponent inventory;
  private CombatStatsComponent combatStats;
  private long ropeReadyTimeMs;

  @Override
  public void create() {
    inventory = entity.getComponent(InventoryComponent.class);
    combatStats = entity.getComponent(CombatStatsComponent.class);
    entity.getEvents().addListener("attack", this::useSelectedItem);
    entity.getEvents().addListener("useItem", this::useSelectedItem);
  }

  /**
   * @return remaining rope-arrow cooldown in seconds, or 0 if it can be used now
   */
  public float getRopeCooldownRemaining() {
    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return 0f;
    }
    long remainingMs = ropeReadyTimeMs - time.getTime();
    return remainingMs <= 0 ? 0f : remainingMs / 1000f;
  }

  /**
   * @return true if a rope arrow can be fired without waiting
   */
  public boolean isRopeReady() {
    return getRopeCooldownRemaining() <= 0f;
  }

  /**
   * Uses the currently selected item.
   *
   * @return true if the item was used
   */
  boolean useSelectedItem() {
    if (inventory == null) {
      return false;
    }

    ItemType selected = inventory.getSelectedItem();
    if (selected == null || !inventory.hasItem(selected)) {
      return false;
    }

    return switch (selected) {
      case ARROW -> useStandardArrow();
      case RopeArrow -> useRopeArrow();
      case CONSUMABLE -> useConsumable();
      case FireArrow -> useFireArrow();
      case ColdArrow -> useColdArrow();
    };
  }

  private boolean useStandardArrow() {
    Vector2 direction = getAimDirection();
    if (direction.isZero()) {
      entity.getEvents().trigger("itemUseFailed", ItemType.ARROW);
      return false;
    }

    if (!inventory.hasItem(ItemType.ARROW)) {
      logger.debug("No standard arrows left to fire");
      entity.getEvents().trigger("itemUseFailed", ItemType.ARROW);
      return false;
    }

    entity.getEvents().trigger("primaryAttack", direction);
    inventory.removeItem(ItemType.ARROW, 1);
    entity.getEvents().trigger("itemUsed", ItemType.ARROW);
    return true;
  }

  private boolean useRopeArrow() {
    if (!inventory.hasItem(ItemType.RopeArrow)) {
      entity.getEvents().trigger("itemUseFailed", ItemType.RopeArrow);
      return false;
    }

    if (!isRopeReady()) {
      logger.debug("Rope arrow on cooldown for {}s", getRopeCooldownRemaining());
      entity.getEvents().trigger("itemUseFailed", ItemType.RopeArrow);
      return false;
    }

    Vector2 direction = getAimDirection();
    if (direction.isZero()) {
      entity.getEvents().trigger("itemUseFailed", ItemType.RopeArrow);
      return false;
    }

    entity.getEvents().trigger("grappleFire", direction);

    GrappleComponent grapple = entity.getComponent(GrappleComponent.class);
    if (grapple != null && !grapple.isAttached()) {
      entity.getEvents().trigger("itemUseFailed", ItemType.RopeArrow);
      return false;
    }

    GameTime time = ServiceLocator.getTimeSource();
    long cooldownMs = (long) (ItemType.RopeArrow.getCooldown() * 1000f);
    if (time != null) {
      ropeReadyTimeMs = time.getTime() + cooldownMs;
    }

    entity.getEvents().trigger("itemUsed", ItemType.RopeArrow);
    return true;
  }

  private boolean useConsumable() {
    if (combatStats == null) {
      entity.getEvents().trigger("itemUseFailed", ItemType.CONSUMABLE);
      return false;
    }

    if (combatStats.isHealthFull()) {
      logger.debug("Cannot use a health potion at full health");
      entity.getEvents().trigger("itemUseFailed", ItemType.CONSUMABLE);
      return false;
    }

    if (!inventory.removeItem(ItemType.CONSUMABLE, 1)) {
      entity.getEvents().trigger("itemUseFailed", ItemType.CONSUMABLE);
      return false;
    }

    combatStats.addHealth(ItemType.CONSUMABLE.getHealAmount());
    entity.getEvents().trigger("itemUsed", ItemType.CONSUMABLE);
    return true;
  }

  private boolean useFireArrow() {
    if (!inventory.hasItem(ItemType.FireArrow)) {
      logger.debug("No fire arrows left to fire");
      entity.getEvents().trigger("itemUseFailed", ItemType.FireArrow);
      return false;
    }

    FireArr arrow = new FireArr(1);
    playAttackSound();
    entity.getEvents().trigger("fireArrFired", arrow);
    inventory.removeItem(ItemType.FireArrow, 1);
    entity.getEvents().trigger("itemUsed", ItemType.FireArrow);
    return true;
  }

  private boolean useColdArrow() {
    if (!inventory.hasItem(ItemType.ColdArrow)) {
      logger.debug("No cold arrows left to fire");
      entity.getEvents().trigger("itemUseFailed", ItemType.ColdArrow);
      return false;
    }

    ColdArr arrow = new ColdArr(1);
    playAttackSound();
    entity.getEvents().trigger("coldArrFired", arrow);
    inventory.removeItem(ItemType.ColdArrow, 1);
    entity.getEvents().trigger("itemUsed", ItemType.ColdArrow);
    return true;
  }

  private Vector2 getAimDirection() {
    KeyboardPlayerInputComponent input = entity.getComponent(KeyboardPlayerInputComponent.class);
    if (input != null) {
      try {
        Vector2 aim = input.getMouseAimDirection();
        if (aim != null && !aim.isZero()) {
          return aim;
        }
      } catch (Exception e) {
        logger.debug("Aim direction unavailable, using default");
      }
    }
    return DEFAULT_AIM.cpy();
  }

  private void playAttackSound() {
    if (ServiceLocator.getResourceService() == null) {
      return;
    }
    try {
      Sound attackSound = ServiceLocator.getResourceService().getAsset(ATTACK_SOUND, Sound.class);
      if (attackSound != null) {
        attackSound.play();
      }
    } catch (Exception e) {
      logger.debug("Attack sound not loaded");
    }
  }
}
