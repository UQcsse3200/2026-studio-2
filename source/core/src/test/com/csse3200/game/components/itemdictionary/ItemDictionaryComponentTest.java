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
    assertTrue(dictionary.unlockItem(ItemType.FireArrow));
    assertTrue(dictionary.isDiscovered(ItemType.FireArrow));
    assertEquals(1, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldNotUnlockSameItemTwice() {
    assertTrue(dictionary.unlockItem(ItemType.FireArrow));
    assertFalse(dictionary.unlockItem(ItemType.FireArrow));

    assertEquals(1, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldTrackMultipleItems() {
    dictionary.unlockItem(ItemType.FireArrow);
    dictionary.unlockItem(ItemType.ColdArrow);

    assertTrue(dictionary.isDiscovered(ItemType.FireArrow));
    assertTrue(dictionary.isDiscovered(ItemType.ColdArrow));
    assertEquals(2, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldReturnFalseForUndiscoveredItem() {
    assertFalse(dictionary.isDiscovered(ItemType.FireArrow));
  }

  @Test
  void shouldHandleNullSafely() {
    assertFalse(dictionary.unlockItem(null));
    assertFalse(dictionary.isDiscovered(null));
    assertEquals(0, dictionary.getDiscoveredCount());
  }

  @Test
  void shouldTrackDifferentItemTypes() {
    dictionary.unlockItem(ItemType.ARROW);
    dictionary.unlockItem(ItemType.RopeArrow);
    dictionary.unlockItem(ItemType.CONSUMABLE);

    assertTrue(dictionary.isDiscovered(ItemType.ARROW));
    assertTrue(dictionary.isDiscovered(ItemType.RopeArrow));
    assertTrue(dictionary.isDiscovered(ItemType.CONSUMABLE));
    assertEquals(3, dictionary.getDiscoveredCount());
  }
}
