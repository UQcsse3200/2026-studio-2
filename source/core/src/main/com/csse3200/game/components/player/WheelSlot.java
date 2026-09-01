package com.csse3200.game.components.player;

/**
 * The four categories arranged around a selection wheel.
 *
 * <p>Each constant also describes where its wedge sits on screen, so the backend and the UI agree
 * on the layout without passing angles between them.
 */
public enum WheelSlot {
  /** Top wedge. */
  HEAVY,
  /** Left wedge. */
  LIGHT,
  /** Right wedge. */
  MELEE,
  /** Bottom wedge. */
  SIDE
}
