package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoomDoorComponentTest {
  private Entity player;
  private CameraComponent camera;
  private EntityService entities;
  private RoomDoorComponent door;

  @BeforeEach
  void setup() {
    player = new Entity().addComponent(new CombatStatsComponent(10, 0));
    player.setPosition(16, 5);
    camera = mock(CameraComponent.class);
    entities = mock(EntityService.class);
    ServiceLocator.registerEntityService(entities);
    door =
        new RoomDoorComponent(
            player, camera, new Vector2(115, 2.1f), new Rectangle(110, 0, 20, 11.25f), "Refuge");
    Entity doorway = new Entity().addComponent(door);
    doorway.setPosition(16, 5);
    doorway.setScale(2.5f, 3.34f);
  }

  @AfterEach
  void cleanup() {
    ServiceLocator.clear();
  }

  @Test
  void entersOnlyOnInteractionAndUpdatesCamera() {
    assertFalse(door.keyDown(Input.Keys.SPACE));
    assertEquals(new Vector2(16, 5), player.getPosition());
    assertTrue(door.keyDown(Input.Keys.F));
    assertEquals(new Vector2(115, 2.1f), player.getPosition());
    verify(camera).setRoomBounds(110, 0, 130, 11.25f);
    verify(camera).setTarget(player);
    assertFalse(door.keyDown(Input.Keys.F));
  }

  @Test
  void cannotEnterFromAnotherPlatformOrWhileDead() {
    player.setPosition(16, 8);
    assertFalse(door.keyDown(Input.Keys.F));
    player.setPosition(16, 5);
    player.getComponent(CombatStatsComponent.class).setHealth(0);
    assertFalse(door.keyDown(Input.Keys.F));
    verifyNoInteractions(camera);
  }

  @Test
  void pausedGameDoesNotEnterRoom() {
    when(entities.getPaused()).thenReturn(true);
    assertFalse(door.keyDown(Input.Keys.F));
    verifyNoInteractions(camera);
  }
}
