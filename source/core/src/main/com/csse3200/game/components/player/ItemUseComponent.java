package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.weapons.ColdArr;
import com.csse3200.game.components.item.weapons.FireArr;
import com.csse3200.game.components.item.weapons.RopeArr;
import com.csse3200.game.components.item.weapons.StandardArr;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the currently selected inventory item when the player attacks.
 *
 * <p>Standard arrows consume ammunition and notify combat systems via {@code arrowFired}. Rope
 * arrows are unlimited but gated by a cooldown and notify grappling via {@code grappleFire}.
 * Consumables heal the player and are removed from the inventory.
 *
 * <p>Requires InventoryComponent. CombatStatsComponent is required to use consumables.
 */
public class ItemUseComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(ItemUseComponent.class);
  private static final String ATTACK_SOUND = "sounds/Impact4.ogg";

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
    if (selected == null) {
      playAttackSound();
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
    StandardArr arrow = new StandardArr(1);
    if (!inventory.removeItem(ItemType.ARROW, 1)) {
      logger.debug("No standard arrows left to fire");
      entity.getEvents().trigger("itemUseFailed", ItemType.ARROW);
      return false;
    }

    playAttackSound();
    entity.getEvents().trigger("arrowFired", arrow.getDamage(), arrow.getRange());
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

    GameTime time = ServiceLocator.getTimeSource();
    RopeArr rope = new RopeArr();
    long cooldownMs = (long) (rope.getCooldown() * 1000f);
    if (time != null) {
      ropeReadyTimeMs = time.getTime() + cooldownMs;
    }

    playAttackSound();
    entity.getEvents().trigger("grappleFire");
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

    HealthPotion potion = new HealthPotion(1);
    if (!inventory.removeItem(ItemType.CONSUMABLE, 1)) {
      entity.getEvents().trigger("itemUseFailed", ItemType.CONSUMABLE);
      return false;
    }

    combatStats.addHealth(potion.getTreatment());
    entity.getEvents().trigger("itemUsed", ItemType.CONSUMABLE);
    return true;
  }

  private boolean useFireArrow() {
    FireArr arrow = new FireArr(1);

    if (!inventory.removeItem(ItemType.FireArrow, 1)) {
      logger.debug("No fire arrows left to fire");
      entity.getEvents().trigger("itemUseFailed", ItemType.FireArrow);
      return false;
    }

    playAttackSound();
    entity.getEvents().trigger("fireArrFired", arrow);
    entity.getEvents().trigger("itemUsed", ItemType.FireArrow);
    return true;
  }

  private boolean useColdArrow() {
    ColdArr arrow = new ColdArr(1);

    if (!inventory.removeItem(ItemType.ColdArrow, 1)) {
      logger.debug("No fire arrows left to fire");
      entity.getEvents().trigger("itemUseFailed", ItemType.ColdArrow);
      return false;
    }

    playAttackSound();
    entity.getEvents().trigger("coldArrFired", arrow);
    entity.getEvents().trigger("itemUsed", ItemType.FireArrow);
    return true;
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
