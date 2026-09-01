package com.csse3200.game.components.player;

/**
 * Which of the player's two selection wheels is being shown.
 *
 * <p>Both wheels are driven by the same {@link SelectionWheelComponent}, so this distinguishes
 * which set of slots an open wheel is reading from.
 */
public enum WheelType {
  /** Weapon wheel, opened with the wheel key. */
  WEAPON,
  /** Consumable wheel, opened with the consumables key. */
  CONSUMABLE
}
