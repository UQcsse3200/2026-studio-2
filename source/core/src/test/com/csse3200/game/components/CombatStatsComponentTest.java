package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class CombatStatsComponentTest {
  @Test
  void shouldSetGetHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertEquals(100, combat.getHealth());

    combat.setHealth(150);
    assertEquals(150, combat.getHealth());

    combat.setHealth(-50);
    assertEquals(0, combat.getHealth());
  }

  @Test
  void shouldCheckIsDead() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertFalse(combat.isDead());

    combat.setHealth(0);
    assertTrue(combat.isDead());
  }

  @Test
  void shouldAddHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    combat.addHealth(-500);
    assertEquals(0, combat.getHealth());

    combat.addHealth(100);
    combat.addHealth(-20);
    assertEquals(80, combat.getHealth());
  }

  @Test
  void shouldSetGetBaseAttack() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertEquals(20, combat.getBaseAttack());

    combat.setBaseAttack(150);
    assertEquals(150, combat.getBaseAttack());

    combat.setBaseAttack(-50);
    assertEquals(150, combat.getBaseAttack());
  }

  @Test
  void shouldIgnoreHitsDuringInvulnerabilityWindow() {
    GameTime gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    CombatStatsComponent target = new CombatStatsComponent(100, 0, 1000);
    CombatStatsComponent attacker = new CombatStatsComponent(100, 10);

    when(gameTime.getTime()).thenReturn(100L, 500L, 1100L);

    target.hit(attacker);
    target.hit(attacker);
    assertEquals(90, target.getHealth());

    target.hit(attacker);
    assertEquals(80, target.getHealth());
  }

  @Test
  void shouldTriggerDeathWhenHealthReachesZero() {
    CombatStatsComponent combat = new CombatStatsComponent(10, 0);
    Entity entity = new Entity().addComponent(combat);
    AtomicInteger deaths = new AtomicInteger();
    entity.getEvents().addListener("death", deaths::incrementAndGet);
    entity.create();

    combat.setHealth(0);
    combat.setHealth(0);

    assertEquals(1, deaths.get());
  }
}
