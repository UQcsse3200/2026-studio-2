package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RespawnComponentTest {
  /**
   * These tests are entirely dependent on respawn activation height being -10 in RespawnComponent
   */
  RespawnComponent respawnComponent;

  boolean respawnCalled;
  Entity player;

  void respawn() {
    respawnCalled = true;
  }

  @BeforeEach
  void beforeEach() {
    respawnComponent = new RespawnComponent();
    respawnCalled = false;
    player = spy(Entity.class);
    player.addComponent(new CombatStatsComponent(10, 0));
  }

  @Test
  void shouldTriggerAtCorrectHeight() {
    player.addComponent(respawnComponent);
    player.getEvents().addListener("respawnAtCheckpoint", this::respawn);
    player.setPosition(0, -11);
    respawnComponent.update();
    assertTrue(respawnCalled);
  }

  @Test
  void shouldNotTriggerAtIncorrectPosition() {
    player.addComponent(respawnComponent);
    player.getEvents().addListener("respawnAtCheckpoint", this::respawn);
    player.setPosition(0, -9);
    respawnComponent.update();
    assertFalse(respawnCalled);
    player.setPosition(0, 100);
    respawnComponent.update();
    assertFalse(respawnCalled);
    player.setPosition(-11, 0);
    respawnComponent.update();
    assertFalse(respawnCalled);
    player.setPosition(11, 0);
    respawnComponent.update();
    assertFalse(respawnCalled);
  }

  @Test
  void shouldCallRespawn() {
    GameArea gameArea = mock(GameArea.class);
    player.addComponent(respawnComponent);
    player.getEvents().addListener("respawnAtCheckpoint", gameArea::respawn);
    player.setPosition(0, -11);
    respawnComponent.update();
    verify(gameArea).respawn();
  }

  @Test
  void shouldApplyHealthPenaltyOnFall() {
    player.addComponent(respawnComponent);
    player.setPosition(0, -11);
    respawnComponent.update();
    assertEquals(
        10 - RespawnComponent.RESPAWN_HEALTH_PENALTY,
        player.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldReviveDeadPlayerWithPenalty() {
    player.getComponent(CombatStatsComponent.class).setHealth(0);
    RespawnComponent.reviveWithPenalty(player);
    assertEquals(
        player.getComponent(CombatStatsComponent.class).getMaxHealth()
            - RespawnComponent.RESPAWN_HEALTH_PENALTY,
        player.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldReduceHealthByTwoWhenBelowRespawnHeight() {
    player.addComponent(respawnComponent);
    player.setPosition(0, -11);

    respawnComponent.update();

    assertEquals(8, player.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldNotReduceHealthWhenAboveRespawnHeight() {
    player.addComponent(respawnComponent);
    player.setPosition(0, -9);

    respawnComponent.update();

    assertEquals(10, player.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldNotRespawnAtExactBoundary() {
    player.addComponent(respawnComponent);
    player.getEvents().addListener("respawnAtCheckpoint", this::respawn);
    player.setPosition(0, -10);

    respawnComponent.update();

    assertFalse(respawnCalled);
    assertEquals(10, player.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldMovePlayerToQueuedRespawnCoordinates() {
    player.addComponent(respawnComponent);
    player.setPosition(0, 0);

    respawnComponent.queueRespawn(new Vector2(12, 8));
    respawnComponent.update();

    assertEquals(12f, player.getPosition().x, 0.001f);
    assertEquals(8f, player.getPosition().y, 0.001f);
  }

  @Test
  void shouldDiscardPendingPhysicsRepositionWhenTeleporting() throws Exception {
    PlayerActions actions = new PlayerActions();
    player.addComponent(actions);
    actions.create(); // registers listeners; this test player has no physics component
    player.addComponent(respawnComponent);
    player.setPosition(0, 0);

    player.getEvents().trigger("physicsPositionRequest", new Vector2(5f, 5f));
    assertTrue(pendingPhysicsReposition(actions), "request should be armed before the teleport");

    respawnComponent.queueRespawn(new Vector2(12f, 8f));
    respawnComponent.update();

    assertEquals(12f, player.getPosition().x, 0.001f);
    // The stale reposition must be gone: consumed next frame it would drag the player back
    // to where they died.
    assertFalse(pendingPhysicsReposition(actions));
  }

  private static boolean pendingPhysicsReposition(PlayerActions actions) throws Exception {
    Field field = PlayerActions.class.getDeclaredField("physicsHelperRequestedPositionChange");
    field.setAccessible(true);
    return (boolean) field.get(actions);
  }

  @Test
  void shouldReleaseGrappleWhenRespawning() {
    player.addComponent(respawnComponent);
    boolean[] grappleReleased = {false};

    player.getEvents().addListener("grappleRelease", () -> grappleReleased[0] = true);

    respawnComponent.queueRespawn(new Vector2(5, 6));
    respawnComponent.update();

    assertTrue(grappleReleased[0]);
  }

  @Test
  void shouldOnlyProcessQueuedRespawnOnce() {
    player.addComponent(respawnComponent);
    int[] grappleReleaseCount = {0};

    player.getEvents().addListener("grappleRelease", () -> grappleReleaseCount[0]++);

    respawnComponent.queueRespawn(new Vector2(5, 6));

    respawnComponent.update();
    assertEquals(1, grappleReleaseCount[0]);

    respawnComponent.update();
    assertEquals(1, grappleReleaseCount[0]);
  }

  @Test
  void shouldNotApplyHealthPenaltyInGodMode() {
    CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
    stats.setGodMode(true);

    RespawnComponent.applyHealthPenalty(player);

    assertEquals(10, stats.getHealth());
  }

  @Test
  void shouldNotApplyFallPenaltyInGodMode() {
    player.addComponent(respawnComponent);
    player.getComponent(CombatStatsComponent.class).setGodMode(true);
    player.setPosition(0, -11);

    respawnComponent.update();

    assertEquals(10, player.getComponent(CombatStatsComponent.class).getHealth());
  }
}
