package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemAssetLifecycleTest {
  @Test
  void shouldKeepInventoryAndProjectileTexturesLoadedAfterLeavingLevelOne() throws Exception {
    ResourceService resources = new ResourceService();
    String[] screenTextures = itemTextures(LevelsGameScreen.class, "mainGameTextures");
    String[] areaTextures = itemTextures(Level1GameArea.class, "forestTextures");
    try {
      resources.loadTextures(screenTextures);
      resources.loadAll();
      resources.loadTextures(areaTextures);
      resources.loadAll();
      resources.unloadAssets(areaTextures);

      for (ItemType item : ItemType.values()) {
        assertNotNull(resources.getAsset(item.getTexturePath(), Texture.class));
        assertNotNull(resources.getAsset(item.getProjectileTexturePath(), Texture.class));
      }
      for (ArrowType arrow : ArrowType.values()) {
        assertNotNull(resources.getAsset(arrow.getTexturePath(), Texture.class));
      }
      assertNotNull(
          resources.getAsset("images/items/effects/fire_status_effect.png", Texture.class));
      assertNotNull(
          resources.getAsset("images/items/effects/ice_status_effect.png", Texture.class));
      assertNotNull(resources.getAsset("images/items/currency/gold_coin.png", Texture.class));
    } finally {
      resources.dispose();
    }
  }

  private static String[] itemTextures(Class<?> owner, String fieldName) throws Exception {
    Field field = owner.getDeclaredField(fieldName);
    field.setAccessible(true);
    return Arrays.stream((String[]) field.get(null))
        .filter(path -> path.startsWith("images/items/"))
        .toArray(String[]::new);
  }
}
