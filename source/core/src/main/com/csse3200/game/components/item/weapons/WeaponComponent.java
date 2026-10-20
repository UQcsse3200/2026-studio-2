package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.Component;

/** Component that manages equipped primary weapons for player or non-player entities. */
public class WeaponComponent extends Component {

  private PrimaryWeapon primaryWeapon;

  public WeaponComponent() {}

  public WeaponComponent(PrimaryWeapon initialPrimary) {
    this.primaryWeapon = initialPrimary;
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
}
