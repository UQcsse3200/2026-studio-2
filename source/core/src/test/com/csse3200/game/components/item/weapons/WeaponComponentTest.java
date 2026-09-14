package com.csse3200.game.components.item.weapons;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
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
  void shouldDelegateAttackWhenWeaponIsReady() {
    PrimaryWeapon bow = mock(PrimaryWeapon.class);
    when(bow.isReady()).thenReturn(true);
    WeaponComponent component = new WeaponComponent(bow);
    Vector2 direction = new Vector2(3f, 4f);

    component.attackPrimary(direction);

    verify(bow).attack(direction);
  }

  @Test
  void shouldNotAttackWhenWeaponIsOnCooldown() {
    PrimaryWeapon bow = mock(PrimaryWeapon.class);
    when(bow.isReady()).thenReturn(false);
    WeaponComponent component = new WeaponComponent(bow);

    component.attackPrimary(Vector2.X.cpy());

    verify(bow, never()).attack(any());
  }

  @Test
  void shouldIgnoreAttackWithNoWeaponEquipped() {
    WeaponComponent component = new WeaponComponent();

    assertDoesNotThrow(() -> component.attackPrimary(Vector2.X.cpy()));
  }

  @Test
  void shouldFireEquippedWeaponOnPrimaryAttackEvent() {
    PrimaryWeapon bow = mock(PrimaryWeapon.class);
    when(bow.isReady()).thenReturn(true);
    Entity player = new Entity().addComponent(new WeaponComponent(bow));
    player.create();
    Vector2 direction = new Vector2(2f, 1f);

    player.getEvents().trigger("primaryAttack", direction);

    verify(bow).attack(direction);
  }

  @Test
  void shouldSwapWeaponAndPublishWeaponChanged() {
    PrimaryWeapon first = mock(PrimaryWeapon.class);
    PrimaryWeapon second = mock(PrimaryWeapon.class);
    when(second.isReady()).thenReturn(true);

    WeaponComponent component = new WeaponComponent(first);
    Entity player = new Entity().addComponent(component);

    AtomicReference<PrimaryWeapon> changedTo = new AtomicReference<>();
    player.getEvents().addListener("weaponChanged", (PrimaryWeapon w) -> changedTo.set(w));

    component.setPrimaryWeapon(second);

    assertSame(second, component.getPrimaryWeapon());
    assertSame(second, changedTo.get());

    Vector2 direction = new Vector2(1f, 0f);
    component.attackPrimary(direction);
    verify(second).attack(direction);
  }
}
