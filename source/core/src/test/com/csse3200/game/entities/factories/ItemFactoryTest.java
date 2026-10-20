package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.csse3200.game.components.item.GoldPickupComponent;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.WheelTokenPickupComponent;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.consumables.PoisonPotion;
import com.csse3200.game.components.item.consumables.SpeedPotion;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.MockServices;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemFactoryTest {
  private PhysicsService physicsService;

  @BeforeEach
  void setUp() {
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);

    MockServices.registerLighting();
    MockServices.registerTextures();
  }

  @AfterEach
  void tearDown() {
    physicsService.getPhysics().dispose();
  }

  @Test
  void shouldCreateConcreteItemForEveryItemType() {
    assertItemMapping(ItemType.STANDARD_ARROW, Arrow.class);
    assertItemMapping(ItemType.ROPE_ARROW, Arrow.class);
    assertItemMapping(ItemType.HEALTH_POTION, HealthPotion.class);
    assertItemMapping(ItemType.FIRE_ARROW, Arrow.class);
    assertItemMapping(ItemType.ICE_ARROW, Arrow.class);
    assertItemMapping(ItemType.SPEED_POTION, SpeedPotion.class);
    assertItemMapping(ItemType.POISON_POTION, PoisonPotion.class);
  }

  @Test
  void shouldCreateGoldPickupWorthTenGold() {
    Entity entity = ItemFactory.createGold();
    GoldPickupComponent gold = entity.getComponent(GoldPickupComponent.class);

    assertEquals(GoldPickupComponent.DEFAULT_AMOUNT, gold.getAmount());
  }

  @Test
  void shouldCreateGoldPickupWithCustomAmount() {
    Entity entity = ItemFactory.createGold(25);

    assertEquals(25, entity.getComponent(GoldPickupComponent.class).getAmount());
  }

  @Test
  void shouldSupportColdArrowCatalogAlias() {
    Item item = ItemFactory.createItem("coldArrow", 2).getComponent(ItemComponent.class).getItem();

    assertEquals(ItemType.ICE_ARROW, item.getItemType());
    assertEquals(2, item.getQuantity());
  }

  @Test
  void potionCatalogNamesStillCreateTheSameItems() {
    Item speed =
        ItemFactory.createItem("speedPotion", 2).getComponent(ItemComponent.class).getItem();
    Item poison =
        ItemFactory.createItem("poisonPotion", 3).getComponent(ItemComponent.class).getItem();

    assertInstanceOf(SpeedPotion.class, speed);
    assertEquals(8, speed.getItemType().getId());
    assertEquals(2, speed.getQuantity());
    assertInstanceOf(PoisonPotion.class, poison);
    assertEquals(9, poison.getItemType().getId());
    assertEquals(3, poison.getQuantity());
  }

  @Test
  void shouldCreateWheelTokenPickup() {
    Entity entity = ItemFactory.createWheelToken();

    assertNotNull(entity.getComponent(WheelTokenPickupComponent.class));
  }

  @Test
  void shouldCreateWheelTokenWithGlow() {
    Entity entity = ItemFactory.createWheelToken();

    PointLightComponent light = entity.getComponent(PointLightComponent.class);
    assertNotNull(light);
    assertEquals(LightingDefaults.RAYS, light.getRayCount());
    assertEquals(2f, light.getDistance());
  }

  @Test
  void itemAndGoldLightsUseTheConfiguredRayCount() {
    PointLightComponent itemLight =
        ItemFactory.createStandardArrow(1).getComponent(PointLightComponent.class);
    PointLightComponent goldLight =
        ItemFactory.createGold().getComponent(PointLightComponent.class);

    assertEquals(100, itemLight.getRayCount());
    assertEquals(100, goldLight.getRayCount());
    assertEquals(1f, itemLight.getBaseRed());
  }

  private static void assertItemMapping(ItemType type, Class<? extends Item> expectedClass) {
    Entity entity = ItemFactory.createItem(type, 3);
    Item item = entity.getComponent(ItemComponent.class).getItem();

    assertInstanceOf(expectedClass, item);
    assertEquals(type, item.getItemType());
    assertEquals(3, item.getQuantity());
  }
}
