package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
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
    assertItemTexturesSurviveAreaUnload(LevelsGameScreen.class);
  }

  @Test
  void shouldKeepMainGameItemTexturesLoadedAfterAreaUnload() throws Exception {
    assertItemTexturesSurviveAreaUnload(MainGameScreen.class);
  }

  @Test
  void shouldKeepLevelTwoItemTexturesLoadedAfterAreaUnload() throws Exception {
    assertItemTexturesSurviveAreaUnload(Level2GameScreen.class);
  }

  private void assertItemTexturesSurviveAreaUnload(Class<?> screen) throws Exception {
    ResourceService resources = new ResourceService();
    String[] screenTextures = itemTextures(screen, "mainGameTextures");
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

  @Test
  void shouldOnlyQueueExistingLevelOnePotionTextures() throws Exception {
    Field field = Level1GameArea.class.getDeclaredField("forestTextures");
    field.setAccessible(true);
    for (String path : (String[]) field.get(null)) {
      if (path.contains("potion") || path.startsWith("images/items/consumables/")) {
        assertTrue(Gdx.files.internal(path).exists(), "Missing Level 1 potion texture: " + path);
      }
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
