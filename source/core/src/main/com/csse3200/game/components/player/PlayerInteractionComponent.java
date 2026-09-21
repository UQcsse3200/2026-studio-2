package com.csse3200.game.components.player;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.GoldPickupComponent;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.npc.ShopNpcComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects interactable entities within range and performs pickup, shop, drop, delete and switch
 * actions on behalf of the player.
 *
 * <p>Requires an InventoryComponent on this entity.
 */
public class PlayerInteractionComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(PlayerInteractionComponent.class);

  /** Maximum distance, in metres, at which the player can interact with an item or NPC. */
  public static final float INTERACTION_RANGE = 1.5f;

  private InventoryComponent inventory;
  private boolean shopOpen;

  @Override
  public void create() {
    inventory = entity.getComponent(InventoryComponent.class);
    entity.getEvents().addListener("interact", this::interact);
    entity.getEvents().addListener("dropItem", this::dropItem);
    entity.getEvents().addListener("deleteItem", this::deleteItem);
    entity.getEvents().addListener("switchItem", this::switchItem);
    entity.getEvents().addListener("closeShop", this::onShopClosed);
  }

  /**
   * Interacts with the nearest shopkeeper, gold coin, or item. Pressing interact again while the
   * shop is open closes it.
   *
   * @return true if a shop was opened or closed, or an item was picked up
   */
  boolean interact() {
    if (shopOpen) {
      entity.getEvents().trigger("closeShop");
      return true;
    }

    Entity shopNpc = findNearestShopNpc();
    if (shopNpc != null) {
      shopOpen = true;
      entity.getEvents().trigger("openShop");
      return true;
    }

    Entity gold = findNearestGold();
    Entity item = findNearestItem();
    Entity target = nearer(gold, item);
    if (target == null) {
      logger.debug("No interactable entity in range of {}", entity);
      entity.getEvents().trigger("interactionFailed");
      return false;
    }
    if (target.getComponent(GoldPickupComponent.class) != null) {
      return pickupGold(target);
    }
    return pickup(target);
  }

  /**
   * @return true if the shop page is currently open
   */
  public boolean isShopOpen() {
    return shopOpen;
  }

  private void onShopClosed() {
    shopOpen = false;
  }

  /**
   * Attempts to pick up the given item entity. The item's availability and the player's range to it
   * are both re-validated at the time of interaction.
   *
   * @param itemEntity item entity to pick up
   * @return true if the item was picked up
   */
  boolean pickup(Entity itemEntity) {
    if (itemEntity == null || !isInRange(itemEntity)) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    ItemComponent itemComponent = itemEntity.getComponent(ItemComponent.class);
    if (itemComponent == null) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    Item item = itemComponent.getItem();
    if (item == null || !inventory.addItem(item.getItemType(), item.getQuantity())) {
      logger.debug("Pickup blocked for {}", itemEntity);
      entity.getEvents().trigger("itemPickupBlocked", item);
      return false;
    }

    itemEntity.dispose();
    entity.getEvents().trigger("itemPickedUp", item);
    return true;
  }

  /**
   * Collects a gold coin, adding its value to the player's gold and removing it from the world.
   *
   * @param goldEntity gold pickup entity
   * @return true if gold was collected
   */
  boolean pickupGold(Entity goldEntity) {
    if (goldEntity == null || !isInRange(goldEntity)) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    GoldPickupComponent goldPickup = goldEntity.getComponent(GoldPickupComponent.class);
    if (goldPickup == null) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    int amount = goldPickup.getAmount();
    inventory.addGold(amount);
    goldEntity.dispose();
    entity.getEvents().trigger("goldPickedUp", amount);
    return true;
  }

  /**
   * Drops the currently selected item stack into the world at the player's position.
   *
   * @return true if an item was dropped
   */
  boolean dropItem() {
    ItemType selected = inventory.getSelectedItem();
    if (selected == null) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    int quantity = inventory.getItemCount(selected);

    if (!inventory.removeItem(selected, quantity)) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    Entity dropped = createItemEntity(selected, quantity);
    dropped.setPosition(entity.getPosition());
    ServiceLocator.getEntityService().register(dropped);

    entity.getEvents().trigger("itemDropped", selected);
    return true;
  }

  /**
   * Permanently deletes the currently selected item stack from the inventory.
   *
   * @return true if an item was deleted
   */
  boolean deleteItem() {
    ItemType selected = inventory.getSelectedItem();
    if (selected == null) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    int quantity = inventory.getItemCount(selected);

    if (!inventory.removeItem(selected, quantity)) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    entity.getEvents().trigger("itemDeleted", selected);
    return true;
  }

  /**
   * Switches the selected inventory item.
   *
   * @param direction positive to select next item, negative to select previous item
   */
  void switchItem(Integer direction) {
    if (direction != null && direction < 0) {
      inventory.selectPrevious();
    } else {
      inventory.selectNext();
    }
  }

  /**
   * Checks whether another entity is within the player's interaction range.
   *
   * @param other entity to check
   * @return true if the entity is within range
   */
  boolean isInRange(Entity other) {
    if (other == null) {
      return false;
    }
    float distance = entity.getCenterPosition().dst(other.getCenterPosition());
    return distance <= INTERACTION_RANGE;
  }

  /**
   * Finds the nearest item-bearing entity within interaction range.
   *
   * @return nearest interactable item entity, or null if none are in range
   */
  Entity findNearestItem() {
    return findNearest(ItemComponent.class);
  }

  /**
   * Finds the nearest gold coin within interaction range.
   *
   * @return nearest gold pickup, or null if none are in range
   */
  Entity findNearestGold() {
    return findNearest(GoldPickupComponent.class);
  }

  private Entity nearer(Entity first, Entity second) {
    if (first == null) {
      return second;
    }
    if (second == null) {
      return first;
    }
    float firstDistance = entity.getCenterPosition().dst(first.getCenterPosition());
    float secondDistance = entity.getCenterPosition().dst(second.getCenterPosition());
    return firstDistance <= secondDistance ? first : second;
  }

  /**
   * Finds the nearest shopkeeper within interaction range.
   *
   * @return nearest shop NPC, or null if none are in range
   */
  Entity findNearestShopNpc() {
    return findNearest(ShopNpcComponent.class);
  }

  /**
   * Finds the nearest entity that has the given component and is within interaction range.
   *
   * @param type component that marks an interactable entity
   * @return nearest matching entity, or null if none are in range
   */
  private Entity findNearest(Class<? extends Component> type) {
    Entity nearest = null;
    float nearestDistance = Float.MAX_VALUE;

    Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
    for (Entity candidate : entities) {
      if (candidate.equals(entity) || candidate.getComponent(type) == null) {
        continue;
      }

      float distance = entity.getCenterPosition().dst(candidate.getCenterPosition());
      if (distance <= INTERACTION_RANGE && distance < nearestDistance) {
        nearest = candidate;
        nearestDistance = distance;
      }
    }

    return nearest;
  }

  /**
   * Creates a world item entity for a dropped inventory stack.
   *
   * @param type item type being dropped
   * @param quantity quantity being dropped
   * @return item entity
   */
  private Entity createItemEntity(ItemType type, int quantity) {
    return switch (type) {
      case STANDARD_ARROW -> ItemFactory.createStandardArrow(quantity);
      case ROPE_ARROW -> ItemFactory.createRopeArrow(quantity);
      case FIRE_ARROW -> ItemFactory.createFireArrow(quantity);
      case ICE_ARROW -> ItemFactory.createIceArrow(quantity);
      case HEALTH_POTION -> ItemFactory.createHealthPotion(quantity);
      case Sword -> ItemFactory.createSword(quantity);
      case Spear -> ItemFactory.createSpear(quantity);
      case SpeedPotion -> ItemFactory.createSpeedPotion(quantity);
      case PoisonPotion -> ItemFactory.createPoisonPotion(quantity);
    };
  }
}
