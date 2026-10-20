package com.csse3200.game.components.item.weapons;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class WeaponComponentTest {

  @Test
  void shouldReturnConfiguredPrimaryWeapon() {
    PrimaryWeapon bow = mock(PrimaryWeapon.class);
    WeaponComponent component = new WeaponComponent(bow);

    assertSame(bow, component.getPrimaryWeapon());
  }

  @Test
  void shouldSwapWeaponAndPublishWeaponChanged() {
    PrimaryWeapon first = mock(PrimaryWeapon.class);
    PrimaryWeapon second = mock(PrimaryWeapon.class);

    WeaponComponent component = new WeaponComponent(first);
    Entity player = new Entity().addComponent(component);

    AtomicReference<PrimaryWeapon> changedTo = new AtomicReference<>();
    player.getEvents().addListener("weaponChanged", changedTo::set);

    component.setPrimaryWeapon(second);

    assertSame(second, component.getPrimaryWeapon());
    assertSame(second, changedTo.get());
  }
}
