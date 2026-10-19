package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.components.item.weapons.Weapons;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerAuxiliaryContractsTest {
  @Test
  void goldBalanceClampsDebtAndChecksAffordabilityAtTheBoundary() {
    InventoryComponent inventory = new InventoryComponent(-5);
    assertEquals(0, inventory.getGold());
    inventory.addGold(20);
    assertTrue(inventory.hasGold(20));
    assertFalse(inventory.hasGold(21));
    inventory.addGold(-25);
    assertEquals(0, inventory.getGold());
  }

  @Test
  void touchControlsReportDirectionsStopsAndAttackWithoutConsumingUnknownKeys() {
    TouchPlayerInputComponent input = new TouchPlayerInputComponent();
    Entity player = new Entity().addComponent(input);
    List<Vector2> directions = new ArrayList<>();
    AtomicInteger stops = new AtomicInteger();
    AtomicInteger attacks = new AtomicInteger();
    player.getEvents().<Vector2>addListener("walk", direction -> directions.add(direction.cpy()));
    player.getEvents().addListener("walkStop", stops::incrementAndGet);
    player.getEvents().addListener("attack", attacks::incrementAndGet);
    int[] keys = {Keys.UP, Keys.LEFT, Keys.DOWN, Keys.RIGHT};
    List<Vector2> expected =
        List.of(new Vector2(0, 1), new Vector2(-1, 0), new Vector2(0, -1), new Vector2(1, 0));
    for (int key : keys) {
      assertTrue(input.keyDown(key));
      assertTrue(input.keyUp(key));
    }
    assertEquals(expected, directions);
    assertEquals(4, stops.get());
    assertFalse(input.keyDown(Keys.F12));
    assertFalse(input.keyUp(Keys.F12));
    assertTrue(input.touchDown(20, 30, 0, 0));
    assertEquals(1, attacks.get());
  }

  @Test
  void poisonBuffExpiresAtItsDeadlineWithoutTruncatingLargeTimestamps() {
    GameTime time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    when(time.getTime()).thenReturn(5_000_000_000L);
    PoisonBuff buff = new PoisonBuff();
    Entity player = new Entity().addComponent(buff);
    player.create();
    assertFalse(buff.isActive());
    assertEquals(0f, buff.getPoisonDamagePerSecond());
    assertEquals(0f, buff.getPoisonDuration());
    player.getEvents().trigger("poisonPotionUsed", 3f, 2f);
    when(time.getTime()).thenReturn(5_000_001_999L);
    assertTrue(buff.isActive());
    assertEquals(3f, buff.getPoisonDamagePerSecond());
    assertEquals(2f, buff.getPoisonDuration());
    when(time.getTime()).thenReturn(5_000_002_000L);
    assertFalse(buff.isActive());
    assertEquals(0f, buff.getPoisonDamagePerSecond());
    assertEquals(0f, buff.getPoisonDuration());
  }

  @Test
  void invalidPoisonApplicationsPreserveAnExistingBuffAndMissingClockDisablesIt() {
    GameTime time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    PoisonBuff buff = new PoisonBuff();
    Entity player = new Entity().addComponent(buff);
    player.create();
    player.getEvents().trigger("poisonPotionUsed", 3f, 2f);
    player.getEvents().trigger("poisonPotionUsed", -1f, 8f);
    player.getEvents().trigger("poisonPotionUsed", 9f, -1f);
    assertEquals(3f, buff.getPoisonDamagePerSecond());
    assertEquals(2f, buff.getPoisonDuration());
    ServiceLocator.registerTimeSource(null);
    player.getEvents().trigger("poisonPotionUsed", 7f, 8f);
    assertFalse(buff.isActive());
    ServiceLocator.registerTimeSource(time);
    assertEquals(3f, buff.getPoisonDamagePerSecond());
    player.getEvents().trigger("poisonPotionUsed", 0f, 5f);
    assertFalse(buff.isActive());
  }

  @Test
  void weaponCanBeEquippedBeforeAnEntityAndDispatchesItsAttack() {
    WeaponComponent coordinator = new WeaponComponent();
    PrimaryWeapon weapon = mock(PrimaryWeapon.class);
    when(weapon.isReady()).thenReturn(true);
    coordinator.setPrimaryWeapon(weapon);
    coordinator.attackPrimary(Vector2.Y);
    verify(weapon).attack(Vector2.Y);
  }

  @Test
  void legacyWeaponStatsFollowTheSelectedItemKind() {
    Weapons fire = new Weapons(ItemType.FIRE_ARROW, 2) {};
    Weapons ice = new Weapons(ItemType.ICE_ARROW, 1) {};
    assertEquals(5, fire.getDamage());
    assertEquals(16f, fire.getRange());
    assertEquals(8, ice.getDamage());
    assertEquals(16f, ice.getRange());
  }
}
