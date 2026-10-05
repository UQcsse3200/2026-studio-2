package com.csse3200.game.components.itemdictionary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.UiTestSupport;
import org.junit.jupiter.api.Test;

class ItemDictionaryDisplayTest extends UiTestSupport {
  @Test
  void shouldShowSpeedAndPoisonPotionStatistics() {
    ItemDictionaryComponent dictionary = new ItemDictionaryComponent();
    ItemDictionaryDisplay display = new ItemDictionaryDisplay();
    register(new Entity().addComponent(dictionary).addComponent(display));
    dictionary.unlockItem(ItemType.SpeedPotion);
    dictionary.unlockItem(ItemType.PoisonPotion);
    display.showDictionary();
    Table content = field(display, "contentTable", Table.class);
    click(content.getChildren().get(ItemType.SpeedPotion.ordinal() + 1));
    assertTrue(labels(content).contains("Speed Boost: 70%"));
    assertTrue(labels(content).contains("Duration: 3.0s"));
    click(content.getChildren().peek());
    click(content.getChildren().get(ItemType.PoisonPotion.ordinal() + 1));
    assertTrue(labels(content).contains("Poison Damage: 3.0/s"));
    assertTrue(labels(content).contains("Poison Duration: 3.0s"));
  }

  @Test
  void shouldKeepLockedItemsHiddenAndAllowDiscoveredItemWithoutIcon() {
    ItemDictionaryComponent dictionary = new ItemDictionaryComponent();
    ItemDictionaryDisplay display = new ItemDictionaryDisplay();
    Entity player = register(new Entity().addComponent(dictionary).addComponent(display));
    display.showDictionary();
    Table content = field(display, "contentTable", Table.class);
    click(content.getChildren().get(ItemType.FIRE_ARROW.ordinal() + 1));
    assertTrue(labels(content).contains("ITEM DICTIONARY"));
    when(resources.containsAsset(ItemType.FIRE_ARROW.getTexturePath(), Texture.class))
        .thenReturn(false);
    dictionary.unlockItem(ItemType.FIRE_ARROW);
    click(content.getChildren().get(ItemType.FIRE_ARROW.ordinal() + 1));
    assertTrue(labels(content).contains("Fire Arrow"));
    assertTrue(labels(content).contains("?"));
    assertTrue(labels(content).contains("Burn Damage: 10.0"));
    display.hideDictionary();
    assertFalse(display.isDictionaryVisible());
    player.dispose();
    assertEquals(0, stage.getActors().size);
  }
}
