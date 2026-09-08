package com.csse3200.game.components.item.weapons;

import com.badlogic.gdx.math.Vector2;

/** Interface for weapons assigned to the primary weapon slot. */
public interface PrimaryWeapon extends AttackBehaviour {
    /**
     * Executes the weapon's primary attack logic in the given direction.
     *
     * @param direction Aim trajectory vector.
     */
    void attack(Vector2 direction);

    /**
     * Checks whether the weapon is ready to strike or off cooldown.
     *
     * @return true if ready to attack, false otherwise.
     */
    boolean isReady();

    /**
     * Gets remaining weapon cooldown time.
     *
     * @return Cooldown time remaining in seconds.
     */
    float getCooldownRemaining();
}