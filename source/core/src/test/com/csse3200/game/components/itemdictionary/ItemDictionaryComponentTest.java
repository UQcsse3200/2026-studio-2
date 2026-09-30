package com.csse3200.game.components.itemdictionary;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.components.item.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ItemDictionaryComponentTest {
  private ItemDictionaryComponent dictionary;

  @BeforeEach
  void setUp() {
    dictionary = new ItemDictionaryComponent();
  }

  @Test
  void shouldUnlockNewItem() {
    assertTrue(dictionary.unlockItem(ItemType.FIRE_ARROW));
    assertTrue(dictionary.isDiscovered(ItemType.FIRE_ARROW));
    assertEquals(1, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldNotUnlockSameItemTwice() {
    assertTrue(dictionary.unlockItem(ItemType.FIRE_ARROW));
    assertFalse(dictionary.unlockItem(ItemType.FIRE_ARROW));

    assertEquals(1, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldTrackMultipleItems() {
    dictionary.unlockItem(ItemType.FIRE_ARROW);
    dictionary.unlockItem(ItemType.ICE_ARROW);

    assertTrue(dictionary.isDiscovered(ItemType.FIRE_ARROW));
    assertTrue(dictionary.isDiscovered(ItemType.ICE_ARROW));
    assertEquals(2, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldReturnFalseForUndiscoveredItem() {
    assertFalse(dictionary.isDiscovered(ItemType.FIRE_ARROW));
  }

  @Test
  void shouldHandleNullSafely() {
    assertFalse(dictionary.unlockItem(null));
    assertFalse(dictionary.isDiscovered(null));
    assertEquals(0, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldTrackDifferentItemTypes() {
    dictionary.unlockItem(ItemType.STANDARD_ARROW);
    dictionary.unlockItem(ItemType.ROPE_ARROW);
    dictionary.unlockItem(ItemType.HEALTH_POTION);

    assertTrue(dictionary.isDiscovered(ItemType.STANDARD_ARROW));
    assertTrue(dictionary.isDiscovered(ItemType.ROPE_ARROW));
    assertTrue(dictionary.isDiscovered(ItemType.HEALTH_POTION));
    assertEquals(3, dictionary.getDiscoveredCount());
  }
}
