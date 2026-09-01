package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class CombatStatsComponentTest {
  @Test
  void shouldSetGetHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertEquals(100, combat.getHealth());

    combat.setHealth(150);
    assertEquals(CombatStatsComponent.MAX_HEALTH, combat.getHealth());

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
  void shouldCapHealthAtMaximum() {
    CombatStatsComponent combat = new CombatStatsComponent(90, 20);
    combat.addHealth(50);
    assertEquals(CombatStatsComponent.MAX_HEALTH, combat.getHealth());
    assertTrue(combat.isHealthFull());
  }

  @Test
  void shouldGetMaxHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertEquals(100, combat.getMaxHealth());

    combat.setHealth(150);
    assertEquals(100, combat.getMaxHealth());
  }

  @Test
  void shouldAddMaxHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(75, 20);
    combat.setHealth(25);

    combat.addMaxHealth(25);
    assertEquals(100, combat.getMaxHealth());
    assertEquals(50, combat.getHealth());

    combat.addMaxHealth(-25);
    combat.addMaxHealth(0);
    assertEquals(100, combat.getMaxHealth());
    assertEquals(50, combat.getHealth());
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
}
