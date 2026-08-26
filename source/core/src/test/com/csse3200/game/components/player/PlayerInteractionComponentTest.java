package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.RopeArr;
import com.csse3200.game.components.item.StandardArr;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerInteractionComponentTest {
  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    // Dropping an item spawns a world entity via ItemFactory, which loads a texture. Mock the
    // resource service so drop tests don't depend on real asset loading in a headless test.
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);
  }

  @Test
  void shouldFindItemInRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity item = spawnWorldItem(new RopeArr(), new Vector2(0.5f, 0f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertEquals(item, interaction.findNearestItem());
  }

  @Test
  void shouldNotFindItemOutOfRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnWorldItem(new RopeArr(), new Vector2(10f, 10f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertNull(interaction.findNearestItem());
  }

  @Test
  void shouldIgnoreNonItemEntities() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity notAnItem = new Entity();
    ServiceLocator.getEntityService().register(notAnItem);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertNull(interaction.findNearestItem());
  }

  @Test
  void shouldPickUpItemInRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity item = spawnWorldItem(new StandardArr(3), new Vector2(0.5f, 0f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertTrue(interaction.pickup(item));
    assertEquals(3, player.getComponent(InventoryComponent.class).getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldTriggerItemPickedUpEvent() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity item = spawnWorldItem(new RopeArr(), new Vector2(0.5f, 0f));

    boolean[] triggered = {false};
    player.getEvents().addListener("itemPickedUp", (Item picked) -> triggered[0] = true);

    player.getComponent(PlayerInteractionComponent.class).pickup(item);

    assertTrue(triggered[0]);
  }

  @Test
  void shouldRejectPickupWhenOutOfRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity item = spawnWorldItem(new RopeArr(), new Vector2(10f, 10f));

    boolean[] failed = {false};
    player.getEvents().addListener("interactionFailed", () -> failed[0] = true);

    assertFalse(player.getComponent(PlayerInteractionComponent.class).pickup(item));
    assertTrue(failed[0]);
  }

  @Test
  void shouldRejectPickupWhenInventoryFull() {
    Entity player = createPlayer(new InventoryComponent(0, 1));
    player.getComponent(InventoryComponent.class).addItem(ItemType.RopeArrow, 1);

    Entity item = spawnWorldItem(new StandardArr(1), new Vector2(0.5f, 0f));

    boolean[] blocked = {false};
    player.getEvents().addListener("itemPickupBlocked", (Item rejected) -> blocked[0] = true);

    assertFalse(player.getComponent(PlayerInteractionComponent.class).pickup(item));
    assertTrue(blocked[0]);
  }

  @Test
  void shouldRejectPickupOfNullEntity() {
    Entity player = createPlayer(new InventoryComponent(0));

    assertFalse(player.getComponent(PlayerInteractionComponent.class).pickup(null));
  }

  @Test
  void shouldDropSelectedItem() {
    Entity player = createPlayer(new InventoryComponent(0));
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ARROW, 4);

    assertTrue(player.getComponent(PlayerInteractionComponent.class).dropItem());
    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldRejectDropWhenInventoryEmpty() {
    Entity player = createPlayer(new InventoryComponent(0));

    assertFalse(player.getComponent(PlayerInteractionComponent.class).dropItem());
  }

  @Test
  void shouldDeleteSelectedItem() {
    Entity player = createPlayer(new InventoryComponent(0));
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.RopeArrow, 1);

    assertTrue(player.getComponent(PlayerInteractionComponent.class).deleteItem());
    assertEquals(0, inventory.getItemCount(ItemType.RopeArrow));
  }

  @Test
  void shouldRejectDeleteWhenInventoryEmpty() {
    Entity player = createPlayer(new InventoryComponent(0));

    assertFalse(player.getComponent(PlayerInteractionComponent.class).deleteItem());
  }

  @Test
  void shouldSwitchSelectedItem() {
    Entity player = createPlayer(new InventoryComponent(0));
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    ItemType initial = inventory.getSelectedItem();
    ItemType other = initial == ItemType.ARROW ? ItemType.RopeArrow : ItemType.ARROW;

    interaction.switchItem(1);
    assertEquals(other, inventory.getSelectedItem());

    interaction.switchItem(-1);
    assertEquals(initial, inventory.getSelectedItem());
  }

  Entity createPlayer(InventoryComponent inventory) {
    Entity player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent())
            .addComponent(inventory)
            .addComponent(new PlayerInteractionComponent());
    player.create();
    return player;
  }

  Entity spawnWorldItem(Item item, Vector2 position) {
    Entity itemEntity =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent())
            .addComponent(new ItemComponent(item));
    itemEntity.setPosition(position);
    ServiceLocator.getEntityService().register(itemEntity);
    return itemEntity;
  }
}
