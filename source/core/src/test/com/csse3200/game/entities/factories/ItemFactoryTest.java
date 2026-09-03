package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.weapons.ColdArr;
import com.csse3200.game.components.item.weapons.FireArr;
import com.csse3200.game.components.item.weapons.RopeArr;
import com.csse3200.game.components.item.weapons.StandardArr;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ResourceService;
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

    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);
  }

  @AfterEach
  void tearDown() {
    physicsService.getPhysics().dispose();
  }

  @Test
  void shouldCreateConcreteItemForEveryItemType() {
    assertItemMapping(ItemType.ARROW, StandardArr.class);
    assertItemMapping(ItemType.RopeArrow, RopeArr.class);
    assertItemMapping(ItemType.CONSUMABLE, HealthPotion.class);
    assertItemMapping(ItemType.FireArrow, FireArr.class);
    assertItemMapping(ItemType.ColdArrow, ColdArr.class);
  }

  private static void assertItemMapping(ItemType type, Class<? extends Item> expectedClass) {
    Entity entity = ItemFactory.createItem(type, 3);
    Item item = entity.getComponent(ItemComponent.class).getItem();

    assertInstanceOf(expectedClass, item);
    assertEquals(type, item.getItemType());
    assertEquals(3, item.getQuantity());
  }
}
