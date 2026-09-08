package com.csse3200.game.components.item.weapons;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

/** Component that manages equipped primary weapons for player or non-player entities. */
public class WeaponComponent extends Component {

    private PrimaryWeapon primaryWeapon;

    public WeaponComponent() {}

    public WeaponComponent(PrimaryWeapon initialPrimary) {
        this.primaryWeapon = initialPrimary;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("primaryAttack", this::attackPrimary);
    }

    /**
     * Swaps or equips the active primary weapon.
     *
     * @param weapon New weapon implementing PrimaryWeapon.
     */
    public void setPrimaryWeapon(PrimaryWeapon weapon) {
        this.primaryWeapon = weapon;
        if (entity != null) {
            entity.getEvents().trigger("weaponChanged", weapon);
        }
    }

    public PrimaryWeapon getPrimaryWeapon() {
        return primaryWeapon;
    }

    /**
     * Triggers the active primary weapon attack if equipped and ready.
     *
     * @param direction Target attack direction vector.
     */
    public void attackPrimary(Vector2 direction) {
        if (primaryWeapon != null && primaryWeapon.isReady()) {
            primaryWeapon.attack(direction);
        }
    }
}