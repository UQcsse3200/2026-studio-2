package com.csse3200.game.components.player;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects items within the player's interaction range and validates and performs pickup, drop,
 * delete and switch interactions on behalf of the player.
 *
 * <p>Requires an InventoryComponent on this entity.
 */
public class PlayerInteractionComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(PlayerInteractionComponent.class);

  /** Maximum distance, in metres, at which the player can interact with an item. */
  public static final float INTERACTION_RANGE = 1.5f;

  private InventoryComponent inventory;

  @Override
  public void create() {
    inventory = entity.getComponent(InventoryComponent.class);
    entity.getEvents().addListener("interact", this::interact);
    entity.getEvents().addListener("dropItem", this::dropItem);
    entity.getEvents().addListener("deleteItem", this::deleteItem);
    entity.getEvents().addListener("switchItem", this::switchItem);
  }

  /**
   * Finds the nearest interactable item within range and attempts to pick it up.
   *
   * @return true if an item was picked up
   */
  boolean interact() {
    Entity target = findNearestItem();
    if (target == null) {
      logger.debug("No interactable item in range of {}", entity);
      entity.getEvents().trigger("interactionFailed");
      return false;
    }
    return pickup(target);
  }

  /**
   * Attempts to pick up the given item entity. The item's availability and the player's range to it
   * are both re-validated at the time of interaction, since either may have changed since the item
   * was first detected.
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
      // Not an interactable item.
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
   * Drops the currently selected item stack into the world at the player's position.
   *
   * @return true if an item was dropped
   */
  boolean dropItem() {
    ItemType selected = inventory.getSelectedItem();
    int quantity = inventory.getItemCount(selected);

    if (selected == null || !inventory.removeItem(selected, quantity)) {
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
    int quantity = inventory.getItemCount(selected);

    if (selected == null || !inventory.removeItem(selected, quantity)) {
      entity.getEvents().trigger("interactionFailed");
      return false;
    }

    entity.getEvents().trigger("itemDeleted", selected);
    return true;
  }

  /**
   * Switches the selected inventory item.
   *
   * @param direction positive to select the next item, negative to select the previous item
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
    Entity nearest = null;
    float nearestDistance = Float.MAX_VALUE;

    Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
    for (Entity candidate : entities) {
      if (candidate.equals(entity) || candidate.getComponent(ItemComponent.class) == null) {
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
      case ARROW -> ItemFactory.createStandardArrow(quantity);
      case RopeArrow -> ItemFactory.createRopeArrow();
      case FireArrow -> ItemFactory.createFireArrow(quantity);
      case ColdArrow -> ItemFactory.createColdArrow(quantity);
      case CONSUMABLE -> ItemFactory.createHealthPotion(quantity);
    };
  }
}
