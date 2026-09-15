package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges input events and inventory selection with active combat, health, and grapple logic.
 *
 * <p>Listens for player input events ("attack", "useItem", "shoot", "stopShoot") and routes
 * execution based on the currently selected item in the player's inventory.
 */
public class ItemUseComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(ItemUseComponent.class);
  private static final Vector2 DEFAULT_AIM = new Vector2(1f, 0f);

  private InventoryComponent inventory;
  private CombatStatsComponent combatStats;

  @Override
  public void create() {
    inventory = entity.getComponent(InventoryComponent.class);
    combatStats = entity.getComponent(CombatStatsComponent.class);

    entity.getEvents().addListener("attack", this::useSelectedItem);
    entity.getEvents().addListener("useItem", this::useSelectedItem);
    entity.getEvents().addListener("shoot", this::shootSelectedArrow);
    entity.getEvents().addListener("stopShoot", this::stopShootSelectedArrow);
  }

  void shootSelectedArrow(Vector2 direction) {
    if (inventory == null) {
      return;
    }
    ItemType selected = inventory.getSelectedItem();
    if (selected != null && selected.isArrow()) {
      useArrow(selected, direction != null ? direction : getAimDirection());
    }
  }

  /** Releases the grapple when the shoot button is released while a rope arrow is equipped. */
  void stopShootSelectedArrow() {
    if (inventory != null && inventory.getSelectedItem() == ItemType.ROPE_ARROW) {
      entity.getEvents().trigger("grappleRelease");
    }
  }

  /**
   * Attempts to use the currently selected item stack in the inventory.
   *
   * @return true if the item was successfully used or fired
   */
  public boolean useSelectedItem() {
    if (inventory == null) {
      return false;
    }

    ItemType selected = inventory.getSelectedItem();
    if (selected == null || !inventory.hasItem(selected)) {
      return false;
    }

    return switch (selected) {
      case STANDARD_ARROW, FIRE_ARROW, ICE_ARROW, ROPE_ARROW ->
          useArrow(selected, getAimDirection());
      case HEALTH_POTION -> useHealthPotion();
      case Sword -> useMeleeWeapon(ItemType.Sword);
      case Spear -> useMeleeWeapon(ItemType.Spear);
      case SpeedPotion -> useSpeedPotion();
      case PoisonPotion -> usePoisonPotion();
    };
  }

  private boolean useArrow(ItemType arrowItem, Vector2 direction) {
    if (direction == null || direction.isZero() || !inventory.hasItem(arrowItem)) {
      entity.getEvents().trigger("itemUseFailed", arrowItem);
      return false;
    }

    if (arrowItem == ItemType.ROPE_ARROW) {
      entity.getEvents().trigger("grappleFire", direction);
    } else {
      entity.getEvents().trigger("setArrowType", arrowItem.toArrowType());
      entity.getEvents().trigger("primaryAttack", direction);

      if (arrowItem.consumesAmmo()) {
        inventory.removeItem(arrowItem, 1);
      }
    }

    entity.getEvents().trigger("itemUsed", arrowItem);
    return true;
  }

  private boolean useMeleeWeapon(ItemType weaponType) {
    if (!inventory.hasItem(weaponType)) {
      logger.debug("No {} available to use", weaponType);
      entity.getEvents().trigger("itemUseFailed", weaponType);
      return false;
    }

    entity
        .getEvents()
        .trigger("meleeAttack", getAimDirection(), weaponType.getDamage(), weaponType.getRange());
    entity.getEvents().trigger("itemUsed", weaponType);
    return true;
  }

  private boolean useHealthPotion() {
    if (combatStats == null || combatStats.isHealthFull()) {
      logger.debug("Cannot use health potion: health is already full or stats missing.");
      entity.getEvents().trigger("itemUseFailed", ItemType.HEALTH_POTION);
      return false;
    }

    if (!inventory.removeItem(ItemType.HEALTH_POTION, 1)) {
      entity.getEvents().trigger("itemUseFailed", ItemType.HEALTH_POTION);
      return false;
    }

    combatStats.addHealth(ItemType.HEALTH_POTION.getHealAmount());
    entity.getEvents().trigger("itemUsed", ItemType.HEALTH_POTION);
    return true;
  }

  private boolean useSpeedPotion() {
    if (!inventory.hasItem(ItemType.SpeedPotion)) {
      logger.debug("No speed potion available to use");
      entity.getEvents().trigger("itemUseFailed", ItemType.SpeedPotion);
      return false;
    }

    PlayerActions playerActions = entity.getComponent(PlayerActions.class);
    if (playerActions != null && playerActions.isSpeedPotionActive()) {
      logger.debug("Speed potion buff is already active");
      entity.getEvents().trigger("itemUseFailed", ItemType.SpeedPotion);
      return false;
    }

    if (!inventory.removeItem(ItemType.SpeedPotion, 1)) {
      entity.getEvents().trigger("itemUseFailed", ItemType.SpeedPotion);
      return false;
    }

    entity
        .getEvents()
        .trigger(
            "speedPotionUsed",
            ItemType.SpeedPotion.getSpeedBoost(),
            ItemType.SpeedPotion.getDuration());
    entity.getEvents().trigger("itemUsed", ItemType.SpeedPotion);
    return true;
  }

  private boolean usePoisonPotion() {
    if (!inventory.hasItem(ItemType.PoisonPotion)) {
      logger.debug("No poison potion available to use");
      entity.getEvents().trigger("itemUseFailed", ItemType.PoisonPotion);
      return false;
    }

    PoisonBuff poisonBuff = entity.getComponent(PoisonBuff.class);
    if (poisonBuff != null && poisonBuff.isActive()) {
      logger.debug("Poison potion buff is already active");
      entity.getEvents().trigger("itemUseFailed", ItemType.PoisonPotion);
      return false;
    }

    if (!inventory.removeItem(ItemType.PoisonPotion, 1)) {
      entity.getEvents().trigger("itemUseFailed", ItemType.PoisonPotion);
      return false;
    }

    entity
        .getEvents()
        .trigger(
            "poisonPotionUsed",
            ItemType.PoisonPotion.getPoisonDamagePerSecond(),
            ItemType.PoisonPotion.getPoisonDuration());
    entity.getEvents().trigger("itemUsed", ItemType.PoisonPotion);
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
        logger.debug("Mouse aim direction unavailable, defaulting to standard vector.");
      }
    }
    return DEFAULT_AIM.cpy();
  }
}
