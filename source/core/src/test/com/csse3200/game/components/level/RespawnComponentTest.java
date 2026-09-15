package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
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
}
