package com.csse3200.game.components.npc;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import java.util.List;

/** Validates local, gold-funded purchases before changing the player's inventory. */
public class MerchantComponent extends Component {
  public record Offer(ItemType item, int quantity, int price) {}

  public enum Result {
    PURCHASED,
    OUT_OF_RANGE,
    NOT_ENOUGH_GOLD,
    INVENTORY_FULL,
    INVALID_OFFER
  }

  public static final List<Offer> OFFERS =
      List.of(
          new Offer(ItemType.STANDARD_ARROW, 10, 5),
          new Offer(ItemType.HEALTH_POTION, 1, 10),
          new Offer(ItemType.FIRE_ARROW, 5, 15),
          new Offer(ItemType.COLD_ARROW, 5, 15));
  private final Entity player;

  public MerchantComponent(Entity player) {
    this.player = player;
  }

  public boolean canTrade() {
    CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
    return (stats == null || stats.getHealth() > 0)
        && entity.getCenterPosition().dst2(player.getCenterPosition()) <= 3.5f * 3.5f;
  }

  public int getGold() {
    return player.getComponent(InventoryComponent.class).getGold();
  }

  public Result buy(int index) {
    if (index < 0 || index >= OFFERS.size()) return Result.INVALID_OFFER;
    if (!canTrade()) return Result.OUT_OF_RANGE;
    Offer offer = OFFERS.get(index);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    if (!inventory.hasGold(offer.price())) return Result.NOT_ENOUGH_GOLD;
    // addItem is atomic on failure: a full backpack must never cost gold.
    if (!inventory.addItem(offer.item(), offer.quantity())) return Result.INVENTORY_FULL;
    inventory.addGold(-offer.price());
    return Result.PURCHASED;
  }
}
