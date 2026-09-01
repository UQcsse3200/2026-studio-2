package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.weapons.ColdArr;
import com.csse3200.game.components.item.weapons.FireArr;
import com.csse3200.game.components.item.weapons.RopeArr;
import com.csse3200.game.components.item.weapons.StandardArr;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory to create item entities that sit in the world for the player to find.
 *
 * <p>Each entity carries the item it represents and a sensor hitbox, so interaction logic can
 * detect it. Collecting the item is handled separately.
 *
 * <p>Each item type should have a creation method that returns a corresponding entity.
 */
public class ItemFactory {
  private static final String STANDARD_ARROW_TEXTURE = "images/arrow.png";
  private static final String ROPE_ARROW_TEXTURE = "images/rope_arrow.png";
  private static final String CONSUMABLE_TEXTURE = "images/heart.png";
  private static final String FIRE_ARROW_TEXTURE = "images/fire_arrow.png";
  private static final String COLD_ARROW_TEXTURE = "images/cold_arrow.png";

  private static final float ITEM_HEIGHT = 0.5f;

  /**
   * Creates a world entity for an item.
   *
   * @param item item this entity represents
   * @param texturePath internal path of the texture to draw
   * @return entity
   */
  public static Entity createItem(Item item, String texturePath) {
    Entity itemEntity =
        new Entity()
            .addComponent(new TextureRenderComponent(texturePath))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.DEFAULT))
            .addComponent(new ItemComponent(item));

    itemEntity.getComponent(TextureRenderComponent.class).scaleEntity();
    itemEntity.scaleHeight(ITEM_HEIGHT);
    return itemEntity;
  }

  /**
   * Creates a single rope arrow lying in the world.
   *
   * @return entity
   */
  public static Entity createRopeArrow() {
    return createRopeArrow(1);
  }

  /**
   * Creates a stack of rope arrows lying in the world.
   *
   * @param quantity number of rope arrows in the stack
   * @return entity
   */
  public static Entity createRopeArrow(int quantity) {
    return createItem(new RopeArr(quantity), ROPE_ARROW_TEXTURE);
  }

  /**
   * Creates a stack of standard arrows lying in the world.
   *
   * @param quantity number of arrows in the stack
   * @return entity
   */
  public static Entity createStandardArrow(int quantity) {
    return createItem(new StandardArr(quantity), STANDARD_ARROW_TEXTURE);
  }

  /**
   * Creates a stack of health potions lying in the world.
   *
   * @param quantity number of potions in the stack
   * @return entity
   */
  public static Entity createHealthPotion(int quantity) {
    return createItem(new HealthPotion(quantity), CONSUMABLE_TEXTURE);
  }

  public static Entity createFireArrow(int quantity) {
    return createItem(new FireArr(quantity), FIRE_ARROW_TEXTURE);
  }

  public static Entity createColdArrow(int quantity) {
    return createItem(new ColdArr(quantity), COLD_ARROW_TEXTURE);
  }

  private ItemFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
