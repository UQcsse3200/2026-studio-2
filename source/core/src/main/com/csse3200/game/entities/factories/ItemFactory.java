package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/** Factory to create item entities that sit in the world for the player to find or pick up. */
public class ItemFactory {
  private static final float ITEM_HEIGHT = 0.85f;

  /**
   * Creates a world entity for an item using the texture from its {@code ItemType}.
   *
   * @param item item this entity represents
   * @return item entity
   */
  public static Entity createItem(Item item) {
    Entity itemEntity =
        new Entity()
            .addComponent(new TextureRenderComponent(item.getItemType().getTexturePath()))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.DEFAULT))
            .addComponent(new ItemComponent(item));

    itemEntity.getComponent(TextureRenderComponent.class).scaleEntity();
    itemEntity.scaleHeight(ITEM_HEIGHT);
    return itemEntity;
  }

  public static Entity createRopeArrow() {
    return createRopeArrow(1);
  }

  public static Entity createRopeArrow(int quantity) {
    return createItem(new Arrow(ItemType.ROPE_ARROW, quantity));
  }

  public static Entity createStandardArrow(int quantity) {
    return createItem(new Arrow(ItemType.STANDARD_ARROW, quantity));
  }

  public static Entity createFireArrow(int quantity) {
    return createItem(new Arrow(ItemType.FIRE_ARROW, quantity));
  }

  public static Entity createColdArrow(int quantity) {
    return createItem(new Arrow(ItemType.COLD_ARROW, quantity));
  }

  public static Entity createHealthPotion(int quantity) {
    return createItem(new HealthPotion(quantity));
  }

  private ItemFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
