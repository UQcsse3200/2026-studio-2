package com.csse3200.game.components.item;

import com.csse3200.game.components.Component;

/**
 * Marks a world entity as a gold coin the player can pick up.
 *
 * <p>Gold is added to the player's currency, not stored as an inventory item.
 */
public class GoldPickupComponent extends Component {
  /** Gold granted by a standard coin pickup. */
  public static final int DEFAULT_AMOUNT = 10;

  private final int amount;

  public GoldPickupComponent() {
    this(DEFAULT_AMOUNT);
  }

  /**
   * @param amount gold granted when this coin is collected
   */
  public GoldPickupComponent(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Gold pickups require a positive amount");
    }
    this.amount = amount;
  }

  /**
   * @return gold granted when this coin is collected
   */
  public int getAmount() {
    return amount;
  }
}
