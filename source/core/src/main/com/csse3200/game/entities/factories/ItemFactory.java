package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.item.GoldPickupComponent;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.consumables.PoisonPotion;
import com.csse3200.game.components.item.consumables.SpeedPotion;
import com.csse3200.game.components.item.weapons.Spear;
import com.csse3200.game.components.item.weapons.Sword;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/** Factory to create item entities that sit in the world for the player to find or pick up. */
public class ItemFactory {
  private static final float ITEM_HEIGHT = 0.85f;
  public static final String GOLD_TEXTURE = "images/gold_coin.png";

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

  public static Entity createItem(ItemType type, int quantity) {
    return switch (type) {
      case STANDARD_ARROW -> createStandardArrow(quantity);
      case ROPE_ARROW -> createRopeArrow(quantity);
      case HEALTH_POTION -> createHealthPotion(quantity);
      case FIRE_ARROW -> createFireArrow(quantity);
      case ICE_ARROW -> createIceArrow(quantity);
      case Sword -> createSword(quantity);
      case Spear -> createSpear(quantity);
      case SpeedPotion -> createSpeedPotion(quantity);
      case PoisonPotion -> createPoisonPotion(quantity);
    };
  }

  /**
   * Creates a world entity containing the concrete item represented by the supplied type.
   *
   * @param type item type to create
   * @param quantity number of items in the stack
   * @return corresponding world item entity
   */
  public static Entity createItem(String itemName, int quantity) {
    return switch (itemName) {
      case "standardArrow" -> createItem(ItemType.STANDARD_ARROW, quantity);
      case "ropeArrow" -> createItem(ItemType.ROPE_ARROW, quantity);
      case "healthPotion" -> createItem(ItemType.HEALTH_POTION, quantity);
      case "fireArrow" -> createItem(ItemType.FIRE_ARROW, quantity);
      case "iceArrow", "coldArrow" -> createItem(ItemType.ICE_ARROW, quantity);
      case "sword" -> createItem(ItemType.Sword, quantity);
      case "spear" -> createItem(ItemType.Spear, quantity);
      case "speedPotion" -> createItem(ItemType.SpeedPotion, quantity);
      case "poisonPotion" -> createItem(ItemType.PoisonPotion, quantity);
      default -> throw new IllegalArgumentException("Unknown item type: " + itemName);
    };
  }

  public static Entity createStandardArrow(int quantity) {
    return createItem(new Arrow(ItemType.STANDARD_ARROW, quantity));
  }

  public static Entity createRopeArrow() {
    return createRopeArrow(1);
  }

  public static Entity createRopeArrow(int quantity) {
    return createItem(new Arrow(ItemType.ROPE_ARROW, quantity));
  }

  public static Entity createFireArrow(int quantity) {
    return createItem(new Arrow(ItemType.FIRE_ARROW, quantity));
  }

  public static Entity createIceArrow(int quantity) {
    return createItem(new Arrow(ItemType.ICE_ARROW, quantity));
  }

  public static Entity createHealthPotion(int quantity) {
    return createItem(new HealthPotion(quantity));
  }

  public static Entity createSword(int quantity) {
    return createItem(new Sword(quantity));
  }

  public static Entity createSpear(int quantity) {
    return createItem(new Spear(quantity));
  }

  public static Entity createSpeedPotion(int quantity) {
    return createItem(new SpeedPotion(quantity));
  }

  public static Entity createPoisonPotion(int quantity) {
    return createItem(new PoisonPotion(quantity));
  }

  /**
   * Creates a world gold coin worth {@link GoldPickupComponent#DEFAULT_AMOUNT} gold.
   *
   * @return gold pickup entity
   */
  public static Entity createGold() {
    return createGold(GoldPickupComponent.DEFAULT_AMOUNT);
  }

  /**
   * Creates a world gold coin worth the given amount.
   *
   * @param amount gold granted on pickup
   * @return gold pickup entity
   */
  public static Entity createGold(int amount) {
    Entity gold =
        new Entity()
            .addComponent(new TextureRenderComponent(GOLD_TEXTURE))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.DEFAULT))
            .addComponent(new GoldPickupComponent(amount));

    gold.getComponent(TextureRenderComponent.class).scaleEntity();
    gold.scaleHeight(ITEM_HEIGHT);
    return gold;
  }

  private ItemFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
