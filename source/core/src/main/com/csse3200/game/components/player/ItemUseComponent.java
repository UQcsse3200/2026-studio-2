package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges input events and inventory selection with active combat, health, and grapple logic.
 *
 * Listens for player input events ("attack", "useItem", "shoot", "stopShoot") and routes
 * execution based on the currently selected item in the player's inventory.
 */
public class ItemUseComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(ItemUseComponent.class);
    private static final Vector2 DEFAULT_AIM = new Vector2(1f, 0f);

    private InventoryComponent inventory;
    private CombatStatsComponent combatStats;

    @Override
    public void create() {
        inventory = entity.getComponent(InventoryComponent.class);
        combatStats = entity.getComponent(CombatStatsComponent.class);

        entity.getEvents().addListener("attack", this::useSelectedItem);
        entity.getEvents().addListener("useItem", this::useSelectedItem);
        entity.getEvents().addListener("shoot", this::shootSelectedArrow);
        entity.getEvents().addListener("stopShoot", this::stopShootSelectedArrow);
    }

    /**
     * Fires the selected arrow item. Consumables are ignored here so holding or clicking
     * the shoot button never accidentally drinks a potion.
     *
     * @param direction Input direction from mouse aim or controller
     */
    void shootSelectedArrow(Vector2 direction) {
        if (inventory == null) {
            return;
        }
        ItemType selected = inventory.getSelectedItem();
        if (selected != null && selected.isArrow()) {
            useSelectedItem();
        }
    }

    /** Releases the grapple when the shoot button is released while a rope arrow is equipped. */
    void stopShootSelectedArrow() {
        if (inventory != null && inventory.getSelectedItem() == ItemType.ROPE_ARROW) {
            entity.getEvents().trigger("grappleRelease");
        }
    }

    /**
     * Attempts to use the currently selected item stack in the inventory.
     *
     * @return true if the item was successfully used or fired
     */
    public boolean useSelectedItem() {
        if (inventory == null) {
            return false;
        }

        ItemType selected = inventory.getSelectedItem();
        if (selected == null || !inventory.hasItem(selected)) {
            return false;
        }

        if (selected.isArrow()) {
            return useArrow(selected);
        } else if (selected == ItemType.HEALTH_POTION) {
            return usePotion();
        }

        return false;
    }

    private boolean useArrow(ItemType arrowItem) {
        Vector2 direction = getAimDirection();
        if (direction.isZero()) {
            entity.getEvents().trigger("itemUseFailed", arrowItem);
            return false;
        }

        if (arrowItem == ItemType.ROPE_ARROW) {
            // GrappleComponent handles its own cooldown timer internally upon receiving "grappleFire"
            entity.getEvents().trigger("grappleFire", direction);
        } else {
            // Configure bow variant and trigger primary weapon execution via WeaponComponent
            entity.getEvents().trigger("setArrowType", arrowItem.toArrowType());
            entity.getEvents().trigger("primaryAttack", direction);

            if (arrowItem.consumesAmmo()) {
                inventory.removeItem(arrowItem, 1);
            }
        }

        entity.getEvents().trigger("itemUsed", arrowItem);
        return true;
    }

    private boolean usePotion() {
        if (combatStats == null || combatStats.isHealthFull()) {
            logger.debug("Cannot use health potion: health is already full or stats missing.");
            entity.getEvents().trigger("itemUseFailed", ItemType.HEALTH_POTION);
            return false;
        }

        if (!inventory.removeItem(ItemType.HEALTH_POTION, 1)) {
            entity.getEvents().trigger("itemUseFailed", ItemType.HEALTH_POTION);
            return false;
        }

        combatStats.addHealth(ItemType.HEALTH_POTION.getHealAmount());
        entity.getEvents().trigger("itemUsed", ItemType.HEALTH_POTION);
        return true;
    }

    private Vector2 getAimDirection() {
        KeyboardPlayerInputComponent input = entity.getComponent(KeyboardPlayerInputComponent.class);
        if (input != null) {
            try {
                Vector2 aim = input.getMouseAimDirection();
                if (aim != null && !aim.isZero()) {
                    return aim;
                }
            } catch (Exception e) {
                logger.debug("Mouse aim direction unavailable, defaulting to standard vector.");
            }
        }
        return DEFAULT_AIM.cpy();
    }
}