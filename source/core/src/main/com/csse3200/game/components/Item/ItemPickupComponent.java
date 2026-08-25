package com.csse3200.game.components.item;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * When a valid collector touches this entity, transfer its item into their inventory and remove the
 * entity from the world.
 *
 * <p>Requires ItemComponent and HitboxComponent on this entity. Collectors without an
 * InventoryComponent are ignored, as are collectors whose inventory has no room, so the item stays
 * on the ground until it can actually be stored.
 */
public class ItemPickupComponent extends Component {
  private final short targetLayer;
  private ItemComponent itemComponent;
  private HitboxComponent hitboxComponent;
  private boolean collected = false;
  private boolean removed = false;

  /**
   * @param targetLayer physics layer of entities allowed to collect this item
   */
  public ItemPickupComponent(short targetLayer) {
    this.targetLayer = targetLayer;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    itemComponent = entity.getComponent(ItemComponent.class);
    hitboxComponent = entity.getComponent(HitboxComponent.class);
  }

  /**
   * @return true once this item has been collected and is waiting to be removed
   */
  public boolean isCollected() {
    return collected;
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (collected || hitboxComponent.getFixture() != me) {
      return;
    }

    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) {
      return;
    }

    Entity collector = ((BodyUserData) other.getBody().getUserData()).entity;
    InventoryComponent inventory = collector.getComponent(InventoryComponent.class);
    if (inventory == null) {
      return;
    }

    Item item = itemComponent.getItem();
    if (!inventory.addItem(item.itemType, item.quantity)) {
      collector.getEvents().trigger("itemPickupBlocked", item);
      return;
    }

    collected = true;
    collector.getEvents().trigger("itemPickedUp", item);
  }

  @Override
  public void update() {
    // Box2D forbids destroying bodies during a world step, which is when collisionStart fires.
    if (collected && !removed) {
      removed = true;
      removeFromWorld();
    }
  }

  /**
   * Tears the item down component by component rather than calling {@link Entity#dispose()}, which
   * walks the same component list that {@link Entity#update()} is already iterating. libGDX arrays
   * reuse their iterators, so that nesting invalidates the outer loop.
   */
  private void removeFromWorld() {
    ServiceLocator.getEntityService().unregister(entity);

    TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
    if (render != null) {
      render.dispose();
    }

    // The collider holds a fixture on the body, so it has to go first.
    hitboxComponent.dispose();
    entity.getComponent(PhysicsComponent.class).dispose();
  }
}
