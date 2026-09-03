package com.csse3200.game.components.minigames.spinthewheel;

import com.csse3200.game.components.item.ItemType;
/**
 * A single item on the wheel
 *
 * @param type the item awarded, supplying the name and sprite
 * @param value the amount awarded
 */
public record WheelItem(ItemType type, int value) {}
