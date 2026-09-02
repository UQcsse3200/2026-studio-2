package com.csse3200.game.components.minigames.spinthewheel;

/**
 * A single item on the wheel
 *
 * @param name the label on the segment
 * @param value the amount awarded
 */
public record WheelItem(String name, int value) {}
