package com.csse3200.game.components.item;

import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.factories.ItemFactory;
import java.util.Arrays;
import java.util.stream.Stream;

/** Texture dependencies shared by inventory icons, world pickups, and their projectiles. */
public final class ItemAssets {
  private ItemAssets() {}

  /**
   * Returns a fresh, deduplicated list for the owning screen to load and unload. Keeping a screen
   * reference prevents area transitions from unloading held items' textures.
   *
   * @return item textures, including projectile animations and status effects
   */
  public static String[] getTextures() {
    return Stream.of(
            Arrays.stream(ItemType.values())
                .flatMap(item -> Stream.of(item.getTexturePath(), item.getProjectileTexturePath())),
            Arrays.stream(ArrowType.values()).map(ArrowType::getTexturePath),
            Stream.of(
                ItemFactory.GOLD_TEXTURE,
                "images/items/effects/fire_status_effect.png",
                "images/items/effects/ice_status_effect.png"))
        .flatMap(stream -> stream)
        .distinct()
        .toArray(String[]::new);
  }
}
