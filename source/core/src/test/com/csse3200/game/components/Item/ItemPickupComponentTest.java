package com.csse3200.game.components.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemPickupComponentTest {
  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerEntityService(new EntityService());
  }

  @Test
  void shouldAddItemToCollectorInventory() {
    Entity item = createItem(new StandardArr(3));
    Entity collector = createCollector(new InventoryComponent(0));

    collide(item, collector);

    assertEquals(3, collector.getComponent(InventoryComponent.class).getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldTriggerPickedUpEventOnCollector() {
    Entity item = createItem(new RopeArr());
    Entity collector = createCollector(new InventoryComponent(0));

    boolean[] triggered = {false};
    collector.getEvents().addListener("itemPickedUp", (Item collected) -> triggered[0] = true);

    collide(item, collector);

    assertTrue(triggered[0]);
  }

  @Test
  void shouldIgnoreCollectorOnOtherLayer() {
    Entity item = createItem(new RopeArr());
    Entity collector = createCollector(new InventoryComponent(0), PhysicsLayer.NPC);

    collide(item, collector);

    assertFalse(item.getComponent(ItemPickupComponent.class).isCollected());
    assertEquals(0, collector.getComponent(InventoryComponent.class).getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldIgnoreCollectorWithoutInventory() {
    Entity item = createItem(new RopeArr());
    Entity collector =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER));
    collector.create();

    // Should not throw, and the item should stay on the ground.
    collide(item, collector);

    assertFalse(item.getComponent(ItemPickupComponent.class).isCollected());
  }

  @Test
  void shouldLeaveItemOnGroundWhenInventoryIsFull() {
    Entity item = createItem(new RopeArr());
    InventoryComponent inventory = new InventoryComponent(0, 1);
    Entity collector = createCollector(inventory);
    inventory.addItem(ItemType.RopeArrow, 1);

    collide(item, collector);

    assertFalse(item.getComponent(ItemPickupComponent.class).isCollected());
    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldTriggerBlockedEventWhenInventoryIsFull() {
    Entity item = createItem(new RopeArr());
    InventoryComponent inventory = new InventoryComponent(0, 1);
    Entity collector = createCollector(inventory);
    inventory.addItem(ItemType.RopeArrow, 1);

    boolean[] blocked = {false};
    collector.getEvents().addListener("itemPickupBlocked", (Item rejected) -> blocked[0] = true);

    collide(item, collector);

    assertTrue(blocked[0]);
  }

  @Test
  void shouldRemoveItemFromWorldAfterCollection() {
    Entity item = buildItem(new RopeArr());
    ServiceLocator.getEntityService().register(item);
    Entity collector = createCollector(new InventoryComponent(0));

    collide(item, collector);

    // Removal happens on update, not in the collision callback.
    ServiceLocator.getEntityService().update();
    // A second pass must not dispose the item a second time.
    ServiceLocator.getEntityService().update();

    assertTrue(item.getComponent(ItemPickupComponent.class).isCollected());
  }

  @Test
  void shouldOnlyCollectOnce() {
    Entity item = createItem(new StandardArr(2));
    Entity collector = createCollector(new InventoryComponent(0));

    collide(item, collector);
    collide(item, collector);

    assertEquals(2, collector.getComponent(InventoryComponent.class).getItemCount(ItemType.ARROW));
  }

  private void collide(Entity item, Entity collector) {
    Fixture itemFixture = item.getComponent(HitboxComponent.class).getFixture();
    Fixture collectorFixture = collector.getComponent(HitboxComponent.class).getFixture();
    item.getEvents().trigger("collisionStart", itemFixture, collectorFixture);
  }

  private Entity buildItem(Item item) {
    return new Entity()
        .addComponent(new PhysicsComponent())
        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.DEFAULT))
        .addComponent(new ItemComponent(item))
        .addComponent(new ItemPickupComponent(PhysicsLayer.PLAYER));
  }

  private Entity createItem(Item item) {
    Entity itemEntity = buildItem(item);
    itemEntity.create();
    return itemEntity;
  }

  private Entity createCollector(InventoryComponent inventory) {
    return createCollector(inventory, PhysicsLayer.PLAYER);
  }

  private Entity createCollector(InventoryComponent inventory, short layer) {
    Entity collector =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(layer))
            .addComponent(inventory);
    collector.create();
    return collector;
  }
}
