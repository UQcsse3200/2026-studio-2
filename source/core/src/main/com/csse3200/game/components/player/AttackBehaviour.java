package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;

/** Behaviour implemented by a weapon or other player attack type. */
public interface AttackBehaviour {
  /**
   * Performs an attack in the requested direction.
   *
   * @param direction world-space attack direction
   */
  void attack(Vector2 direction);
}
