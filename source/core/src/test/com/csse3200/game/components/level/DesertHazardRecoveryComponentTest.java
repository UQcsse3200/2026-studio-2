package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DesertHazardRecoveryComponentTest {
  private Entity player;
  private CombatStatsComponent stats;
  private DesertHazardRecoveryComponent recovery;

  @BeforeEach
  void setup() {
    stats = new CombatStatsComponent(10, 0, 1000L);
    recovery = new DesertHazardRecoveryComponent();
    player = new Entity().addComponent(stats).addComponent(recovery);
    stats.create();
  }

  @AfterEach
  void cleanup() {
    ServiceLocator.clear();
  }

  @Test
  void spikeFailureCostsOneHitAndReturnsToNearbySafeShelf() {
    player.setPosition(61, 20.1f);
    recovery.update();
    assertEquals(new Vector2(53, 21.1f), player.getPosition());
    assertEquals(8, stats.getHealth());
    recovery.update();
    assertEquals(8, stats.getHealth());
  }

  @Test
  void priorSpikeContactDoesNotChargeAgainDuringRecovery() {
    stats.hit(new CombatStatsComponent(1, 2));
    player.setPosition(61, 20.1f);
    recovery.update();
    assertEquals(8, stats.getHealth());
  }

  @Test
  void originalLowerRouteAndOptionalRoomsDoNotTriggerRecovery() {
    for (Vector2 p : new Vector2[] {new Vector2(61, 10), new Vector2(72, 2), new Vector2(115, 2)}) {
      player.setPosition(p);
      recovery.update();
      assertEquals(p, player.getPosition());
    }
    assertEquals(10, stats.getHealth());
  }

  @Test
  void fallingOutsideMapUsesCollectedCheckpoint() {
    player.getEvents().addListener("respawnAtCheckpoint", () -> player.setPosition(47, 22));
    player.setPosition(5, -5);
    recovery.update();
    assertEquals(new Vector2(47, 22), player.getPosition());
  }
}
