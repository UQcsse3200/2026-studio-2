package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;
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
  private static final String ITEM_USE_FAILED = "itemUseFailed";
  private static final String ITEM_USED = "itemUsed";
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

  /**
   * Starts using the selected arrow item on shoot-button-down. Consumables are ignored here so
   * holding or clicking the shoot button never accidentally drinks a potion.
   *
   * <p>Every arrow, including the rope arrow, is held as a charge and only actually fires when
   * {@link #stopShootSelectedArrow()} releases the button, with power (and for the grapple, reach)
   * scaling based on how long it was held. The rope arrow fires on its own dedicated event rather
   * than the bow's "chargeStart"/"chargeRelease", so the two weapons never cross-trigger each other
   * off the same shared broadcast, and doesn't spend ammo or lock in a bow arrow type, since
   * GrappleComponent, not BowComponent, is what actually fires it.
   *
   * @param direction Input direction from mouse aim or controller
   */
  void shootSelectedArrow(Vector2 direction) {
    if (inventory == null) {
      return;
    }
    ItemType selected = inventory.getSelectedItem();
    if (selected == null || !selected.isArrow() || !inventory.hasItem(selected)) {
      return;
    }

    if (direction == null || direction.isZero()) {
      entity.getEvents().trigger(ITEM_USE_FAILED, selected);
      return;
    }

    if (selected == ItemType.ROPE_ARROW) {
      entity.getEvents().trigger(ITEM_USED, selected);
      entity.getEvents().trigger("grappleDrawStart", direction);
      return;
    }

    // Reject before reserving ammo. Readiness also covers a bow that already has a paid arrow
    // charging, so repeat clicks and the alternate attack input cannot consume another arrow.
    if (!isPrimaryWeaponReady()) {
      entity.getEvents().trigger(ITEM_USE_FAILED, selected);
      return;
    }

    entity.getEvents().trigger("setArrowType", selected.toArrowType());
    if (selected.consumesAmmo()) {
      inventory.removeItem(selected, 1);
    }
    entity.getEvents().trigger(ITEM_USED, selected);
    entity.getEvents().trigger("chargeStart", direction);
  }

  /**
   * Detaches an active swing and releases any charging shot - bow or grapple - when the shoot
   * button comes back up.
   *
   * <p>All three events are signalled unconditionally rather than picking one based on the current
   * selection: spending the last arrow in a slot auto-advances the inventory to the next occupied
   * slot, so the item selected on release is not necessarily the one that started the draw.
   * GrappleComponent ignores a release when no rope is attached and a charge release when it isn't
   * charging, and BowComponent ignores a release when nothing is charging, so signalling all three
   * is safe and guarantees a charge can never be left hanging.
   */
  void stopShootSelectedArrow() {
    if (inventory == null) {
      return;
    }
    if (inventory.getSelectedItem() == ItemType.ROPE_ARROW) {
      entity.getEvents().trigger("grappleRelease");
    }
    entity.getEvents().trigger("grappleDrawRelease", getAimDirection());
    entity.getEvents().trigger("chargeRelease", getAimDirection());
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
      entity.getEvents().trigger(ITEM_USE_FAILED, arrowItem);
      return false;
    }

    if (arrowItem == ItemType.ROPE_ARROW) {
      entity.getEvents().trigger("grappleFire", direction);
    } else {
      // Dispatch is synchronous, so check readiness before publishing the attack or using ammo.
      WeaponComponent weapons = entity.getComponent(WeaponComponent.class);
      PrimaryWeapon primary = weapons == null ? null : weapons.getPrimaryWeapon();
      if (primary == null || !primary.isReady()) {
        entity.getEvents().trigger(ITEM_USE_FAILED, arrowItem);
        return false;
      }

      // Configure bow variant and trigger primary weapon execution via WeaponComponent
      entity.getEvents().trigger("setArrowType", arrowItem.toArrowType());
      entity.getEvents().trigger("primaryAttack", direction);

      if (arrowItem.consumesAmmo()) {
        inventory.removeItem(arrowItem, 1);
      }
    }

    entity.getEvents().trigger(ITEM_USED, arrowItem);
    return true;
  }

  private boolean useMeleeWeapon(ItemType weaponType) {
    if (!inventory.hasItem(weaponType)) {
      logger.debug("No {} available to use", weaponType);
      entity.getEvents().trigger(ITEM_USE_FAILED, weaponType);
      return false;
    }

    entity
        .getEvents()
        .trigger("meleeAttack", getAimDirection(), weaponType.getDamage(), weaponType.getRange());
    entity.getEvents().trigger(ITEM_USED, weaponType);
    return true;
  }

  private boolean useHealthPotion() {
    if (combatStats == null || combatStats.isHealthFull()) {
      logger.debug("Cannot use health potion: health is already full or stats missing.");
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.HEALTH_POTION);
      return false;
    }

    if (!inventory.removeItem(ItemType.HEALTH_POTION, 1)) {
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.HEALTH_POTION);
      return false;
    }

    combatStats.addHealth(ItemType.HEALTH_POTION.getHealAmount());
    entity.getEvents().trigger(ITEM_USED, ItemType.HEALTH_POTION);
    return true;
  }

  private boolean useSpeedPotion() {
    if (!inventory.hasItem(ItemType.SpeedPotion)) {
      logger.debug("No speed potion available to use");
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.SpeedPotion);
      return false;
    }

    PlayerActions playerActions = entity.getComponent(PlayerActions.class);
    if (playerActions != null && playerActions.isSpeedPotionActive()) {
      logger.debug("Speed potion buff is already active");
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.SpeedPotion);
      return false;
    }

    if (!inventory.removeItem(ItemType.SpeedPotion, 1)) {
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.SpeedPotion);
      return false;
    }

    entity
        .getEvents()
        .trigger(
            "speedPotionUsed",
            ItemType.SpeedPotion.getSpeedBoost(),
            ItemType.SpeedPotion.getDuration());
    entity.getEvents().trigger(ITEM_USED, ItemType.SpeedPotion);
    return true;
  }

  private boolean usePoisonPotion() {
    if (!inventory.hasItem(ItemType.PoisonPotion)) {
      logger.debug("No poison potion available to use");
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.PoisonPotion);
      return false;
    }

    Vector2 direction = getAimDirection();
    if (direction == null
        || direction.isZero()
        || ServiceLocator.getEntityService() == null
        || ServiceLocator.getPhysicsService() == null) {
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.PoisonPotion);
      return false;
    }

    if (!inventory.removeItem(ItemType.PoisonPotion, 1)) {
      entity.getEvents().trigger(ITEM_USE_FAILED, ItemType.PoisonPotion);
      return false;
    }

    Vector2 throwDirection = direction.cpy().nor();
    Vector2 spawnPosition =
        entity.getCenterPosition().mulAdd(throwDirection, entity.getScale().x * 0.8f);
    ServiceLocator.getEntityService()
        .register(
            ProjectileFactory.createThrownPoisonPotion(entity, spawnPosition, throwDirection));
    entity.getEvents().trigger(ITEM_USED, ItemType.PoisonPotion);
    return true;
  }

  /**
   * @return true when an equipped primary weapon is ready to attack
   */
  private boolean isPrimaryWeaponReady() {
    WeaponComponent weapon = entity.getComponent(WeaponComponent.class);
    return weapon != null
        && weapon.getPrimaryWeapon() != null
        && weapon.getPrimaryWeapon().isReady();
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
