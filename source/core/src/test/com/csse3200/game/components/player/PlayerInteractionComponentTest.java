package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.GoldPickupComponent;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.WheelTokenPickupComponent;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.npc.ShopNpcComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class PlayerInteractionComponentTest {
  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    LightingEngine lightingEngine = mock(LightingEngine.class);
    box2dLight.RayHandler rayHandler = mock(box2dLight.RayHandler.class);
    when(lightingEngine.getRayHandler()).thenReturn(rayHandler);
    LightingService lightingService = mock(LightingService.class);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerLightingService(lightingService);

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
    Entity item = spawnWorldItem(new Arrow(ItemType.ROPE_ARROW, 1), new Vector2(0.5f, 0f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertEquals(item, interaction.findNearestItem());
  }

  @Test
  void shouldNotFindItemOutOfRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnWorldItem(new Arrow(ItemType.ROPE_ARROW, 1), new Vector2(10f, 10f));

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
    Entity item = spawnWorldItem(new Arrow(ItemType.STANDARD_ARROW, 3), new Vector2(0.5f, 0f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertTrue(interaction.pickup(item));
    assertEquals(
        3, player.getComponent(InventoryComponent.class).getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldTriggerItemPickedUpEvent() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity item = spawnWorldItem(new Arrow(ItemType.ROPE_ARROW, 1), new Vector2(0.5f, 0f));

    boolean[] triggered = {false};
    player.getEvents().addListener("itemPickedUp", (Item picked) -> triggered[0] = true);

    player.getComponent(PlayerInteractionComponent.class).pickup(item);

    assertTrue(triggered[0]);
  }

  @Test
  void shouldRejectPickupWhenOutOfRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity item = spawnWorldItem(new Arrow(ItemType.ROPE_ARROW, 1), new Vector2(10f, 10f));

    boolean[] failed = {false};
    player.getEvents().addListener("interactionFailed", () -> failed[0] = true);

    assertFalse(player.getComponent(PlayerInteractionComponent.class).pickup(item));
    assertTrue(failed[0]);
  }

  @Test
  void shouldRejectPickupWhenInventoryFull() {
    Entity player = createPlayer(new InventoryComponent(0, 1));
    player.getComponent(InventoryComponent.class).addItem(ItemType.ROPE_ARROW, 1);

    Entity item = spawnWorldItem(new Arrow(ItemType.STANDARD_ARROW, 1), new Vector2(0.5f, 0f));

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
    inventory.addItem(ItemType.STANDARD_ARROW, 4);

    try (MockedConstruction<PointLightComponent> ignored =
        mockConstruction(PointLightComponent.class)) {
      assertTrue(player.getComponent(PlayerInteractionComponent.class).dropItem());
    }
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldDropRopeArrowStackWithFullQuantity() {
    Entity player = createPlayer(new InventoryComponent(0));
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ROPE_ARROW, 3);

    try (MockedConstruction<PointLightComponent> ignored =
        mockConstruction(PointLightComponent.class)) {
      assertTrue(player.getComponent(PlayerInteractionComponent.class).dropItem());
    }
    assertEquals(0, inventory.getItemCount(ItemType.ROPE_ARROW));

    int droppedQuantity = 0;
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      ItemComponent itemComponent = entity.getComponent(ItemComponent.class);
      if (itemComponent != null && itemComponent.getItem().getItemType() == ItemType.ROPE_ARROW) {
        droppedQuantity = itemComponent.getItem().getQuantity();
        break;
      }
    }
    assertEquals(3, droppedQuantity);
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
    inventory.addItem(ItemType.ROPE_ARROW, 1);

    assertTrue(player.getComponent(PlayerInteractionComponent.class).deleteItem());
    assertEquals(0, inventory.getItemCount(ItemType.ROPE_ARROW));
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
    inventory.addItem(ItemType.STANDARD_ARROW, 1);
    inventory.addItem(ItemType.ROPE_ARROW, 1);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    ItemType initial = inventory.getSelectedItem();
    ItemType other =
        initial == ItemType.STANDARD_ARROW ? ItemType.ROPE_ARROW : ItemType.STANDARD_ARROW;

    interaction.switchItem(1);
    assertEquals(other, inventory.getSelectedItem());

    interaction.switchItem(-1);
    assertEquals(initial, inventory.getSelectedItem());
  }

  @Test
  void shouldFindShopNpcInRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity shopNpc = spawnShopNpc(new Vector2(0.5f, 0f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertEquals(shopNpc, interaction.findNearestShopNpc());
  }

  @Test
  void shouldNotFindShopNpcOutOfRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnShopNpc(new Vector2(10f, 10f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertNull(interaction.findNearestShopNpc());
  }

  @Test
  void shouldOpenShopWhenInteractingWithShopNpc() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnShopNpc(new Vector2(0.5f, 0f));

    boolean[] opened = {false};
    player.getEvents().addListener("openShop", () -> opened[0] = true);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertTrue(interaction.interact());
    assertTrue(opened[0]);
    assertTrue(interaction.isShopOpen());
  }

  @Test
  void shouldPreferShopNpcOverItem() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnWorldItem(new Arrow(ItemType.ROPE_ARROW, 1), new Vector2(0.5f, 0f));
    spawnShopNpc(new Vector2(0.5f, 0f));

    boolean[] opened = {false};
    player.getEvents().addListener("openShop", () -> opened[0] = true);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertTrue(interaction.interact());
    assertTrue(opened[0]);
    assertEquals(
        0, player.getComponent(InventoryComponent.class).getItemCount(ItemType.ROPE_ARROW));
  }

  @Test
  void shouldCloseShopOnSecondInteract() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnShopNpc(new Vector2(0.5f, 0f));

    boolean[] closed = {false};
    player.getEvents().addListener("closeShop", () -> closed[0] = true);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    interaction.interact();
    assertTrue(interaction.isShopOpen());

    assertTrue(interaction.interact());
    assertTrue(closed[0]);
    assertFalse(interaction.isShopOpen());
  }

  @Test
  void shouldFindGoldInRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    Entity gold = spawnGold(new Vector2(0.5f, 0f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertEquals(gold, interaction.findNearestGold());
  }

  @Test
  void shouldNotFindGoldOutOfRange() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnGold(new Vector2(10f, 10f));

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertNull(interaction.findNearestGold());
  }

  @Test
  void shouldPickUpGoldAndAddTenGold() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnGold(new Vector2(0.5f, 0f));

    int[] collected = {0};
    player.getEvents().addListener("goldPickedUp", (Integer amount) -> collected[0] = amount);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertTrue(interaction.interact());
    assertEquals(GoldPickupComponent.DEFAULT_AMOUNT, collected[0]);
    assertEquals(
        GoldPickupComponent.DEFAULT_AMOUNT,
        player.getComponent(InventoryComponent.class).getGold());
  }

  @Test
  void shouldPickUpWheelTokenAndAskForTheWheel() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnWheelToken(new Vector2(0.5f, 0f));

    boolean[] picked = {false};
    player.getEvents().addListener("wheelTokenPickedUp", () -> picked[0] = true);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertTrue(interaction.interact());
    assertTrue(picked[0]);
    assertNull(interaction.findNearestWheelToken());
  }

  @Test
  void shouldPreferShopNpcOverGold() {
    Entity player = createPlayer(new InventoryComponent(0));
    spawnGold(new Vector2(0.5f, 0f));
    spawnShopNpc(new Vector2(0.5f, 0f));

    boolean[] opened = {false};
    player.getEvents().addListener("openShop", () -> opened[0] = true);

    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);

    assertTrue(interaction.interact());
    assertTrue(opened[0]);
    assertEquals(0, player.getComponent(InventoryComponent.class).getGold());
  }

  @Test
  void shouldReportFailureWhenNoTargetIsNearby() {
    Entity player = createPlayer(new InventoryComponent(0));
    int[] failures = {0};
    player.getEvents().addListener("interactionFailed", () -> failures[0]++);
    assertFalse(player.getComponent(PlayerInteractionComponent.class).interact());
    assertEquals(1, failures[0]);
  }

  @Test
  void shouldRejectPickupsWithoutRequiredComponentsOrContents() {
    InventoryComponent inventory = new InventoryComponent(0);
    Entity player = createPlayer(inventory);
    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    int[] failures = {0};
    java.util.List<Item> blocked = new java.util.ArrayList<>();
    player.getEvents().addListener("interactionFailed", () -> failures[0]++);
    player.getEvents().<Item>addListener("itemPickupBlocked", blocked::add);
    Entity empty = new Entity();
    assertFalse(interaction.pickup(empty));
    assertFalse(interaction.pickupGold(empty));
    assertFalse(interaction.pickupWheelToken(empty));
    assertFalse(interaction.pickupGold(null));
    assertFalse(interaction.pickupWheelToken(null));
    assertFalse(interaction.isInRange(null));
    Entity distantGold = spawnGold(new Vector2(10, 0));
    Entity distantToken = spawnWheelToken(new Vector2(10, 0));
    assertFalse(interaction.pickupGold(distantGold));
    assertFalse(interaction.pickupWheelToken(distantToken));
    assertEquals(7, failures[0]);
    Entity missingItem = spawnWorldItem(null, new Vector2(0, 0));
    assertFalse(interaction.pickup(missingItem));
    assertEquals(1, blocked.size());
    assertNull(blocked.getFirst());
    assertEquals(0, inventory.getGold());
    assertTrue(ServiceLocator.getEntityService().getEntities().contains(missingItem, true));
  }

  @Test
  void shouldPickTheNearestTargetAcrossItemGoldAndTokenKinds() {
    InventoryComponent inventory = new InventoryComponent(0);
    Entity player = createPlayer(inventory);
    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    spawnGold(new Vector2(1f, 0));
    Entity item = spawnWorldItem(new Arrow(ItemType.FIRE_ARROW, 2), new Vector2(0.5f, 0));
    spawnWheelToken(new Vector2(1.25f, 0));
    assertTrue(interaction.interact());
    assertEquals(2, interaction.getInventory().getItemCount(ItemType.FIRE_ARROW));
    assertEquals(0, inventory.getGold());
    assertFalse(ServiceLocator.getEntityService().getEntities().contains(item, true));
    assertTrue(interaction.interact());
    assertEquals(10, inventory.getGold());
    org.junit.jupiter.api.Assertions.assertNotNull(interaction.findNearestWheelToken());
  }

  @Test
  void shouldPreferGoldOnDistanceTieAndTokenWhenItIsCloser() {
    Entity player = createPlayer(new InventoryComponent(0));
    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    spawnGold(new Vector2(1f, 0));
    spawnWorldItem(new Arrow(ItemType.ICE_ARROW, 1), new Vector2(1f, 0));
    spawnWheelToken(new Vector2(0.25f, 0));
    assertTrue(interaction.interact());
    assertNull(interaction.findNearestWheelToken());
    assertEquals(0, interaction.getInventory().getGold());
    assertTrue(interaction.interact());
    assertEquals(10, interaction.getInventory().getGold());
    assertEquals(0, interaction.getInventory().getItemCount(ItemType.ICE_ARROW));
  }

  @Test
  void shouldKeepNearestItemWhenLaterCandidatesAreFartherAndIncludeRangeBoundary() {
    Entity player = createPlayer(new InventoryComponent(0));
    ServiceLocator.getEntityService().register(player);
    Entity near = spawnWorldItem(new Arrow(ItemType.ICE_ARROW, 1), new Vector2(1.5f, 0));
    spawnWorldItem(new Arrow(ItemType.FIRE_ARROW, 1), new Vector2(1.6f, 0));
    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    assertTrue(interaction.isInRange(near));
    assertEquals(near, interaction.findNearestItem());
    Entity nearer = spawnWorldItem(new Arrow(ItemType.STANDARD_ARROW, 1), new Vector2(0.2f, 0));
    assertEquals(nearer, interaction.findNearestItem());
    spawnWorldItem(new Arrow(ItemType.ROPE_ARROW, 1), new Vector2(0.7f, 0));
    assertEquals(nearer, interaction.findNearestItem());
  }

  @Test
  void shouldLeaveWorldAndInventoryUnchangedWhenRemovalFails() {
    InventoryComponent inventory = spy(new InventoryComponent(0));
    inventory.addItem(ItemType.FIRE_ARROW, 3);
    doReturn(false).when(inventory).removeItem(ItemType.FIRE_ARROW, 3);
    Entity player = createPlayer(inventory);
    PlayerInteractionComponent interaction = player.getComponent(PlayerInteractionComponent.class);
    int[] failures = {0};
    int[] successes = {0};
    player.getEvents().addListener("interactionFailed", () -> failures[0]++);
    player.getEvents().<ItemType>addListener("itemDropped", ignored -> successes[0]++);
    player.getEvents().<ItemType>addListener("itemDeleted", ignored -> successes[0]++);
    assertFalse(interaction.dropItem());
    assertFalse(interaction.deleteItem());
    assertEquals(2, failures[0]);
    assertEquals(0, successes[0]);
    assertEquals(3, inventory.getItemCount(ItemType.FIRE_ARROW));
    assertEquals(0, ServiceLocator.getEntityService().getEntities().size);
  }

  @Test
  void shouldTreatNullSwitchDirectionAsNextItem() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.FIRE_ARROW, 1);
    inventory.addItem(ItemType.ICE_ARROW, 1);
    Entity player = createPlayer(inventory);
    player.getComponent(PlayerInteractionComponent.class).switchItem(null);
    assertEquals(ItemType.ICE_ARROW, inventory.getSelectedItem());
  }

  @Test
  void shouldDropEveryItemKindWithItsQuantityAndPlayerPosition() {
    for (ItemType type : ItemType.values()) {
      ServiceLocator.registerEntityService(new EntityService());
      InventoryComponent inventory = new InventoryComponent(0);
      inventory.addItem(type, 3);
      Entity player = createPlayer(inventory);
      player.setPosition(4, 7);
      try (MockedConstruction<PointLightComponent> ignored =
          mockConstruction(PointLightComponent.class)) {
        assertTrue(player.getComponent(PlayerInteractionComponent.class).dropItem());
      }
      assertEquals(0, inventory.getItemCount(type));
      assertEquals(1, ServiceLocator.getEntityService().getEntities().size);
      Entity dropped = ServiceLocator.getEntityService().getEntities().first();
      assertEquals(type, dropped.getComponent(ItemComponent.class).getItem().getItemType());
      assertEquals(3, dropped.getComponent(ItemComponent.class).getItem().getQuantity());
      assertEquals(new Vector2(4, 7), dropped.getPosition());
    }
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

  Entity spawnShopNpc(Vector2 position) {
    Entity shopNpc = new Entity().addComponent(new ShopNpcComponent());
    shopNpc.setPosition(position);
    ServiceLocator.getEntityService().register(shopNpc);
    return shopNpc;
  }

  Entity spawnGold(Vector2 position) {
    Entity gold =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent())
            .addComponent(new GoldPickupComponent());
    gold.setPosition(position);
    ServiceLocator.getEntityService().register(gold);
    return gold;
  }

  Entity spawnWheelToken(Vector2 position) {
    Entity token =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent())
            .addComponent(new WheelTokenPickupComponent());
    token.setPosition(position);
    ServiceLocator.getEntityService().register(token);
    return token;
  }
}
