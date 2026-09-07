package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.level.PlatformGrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameAreaTest {
  @Test
  void shouldSpawnEntities() {
    TerrainFactory factory = mock(TerrainFactory.class);

    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {}
        };

    ServiceLocator.registerEntityService(new EntityService());
    Entity entity = mock(Entity.class);

    gameArea.spawnEntity(entity);
    verify(entity).create();

    gameArea.dispose();
    verify(entity).dispose();
  }

  @Test
  void shouldSavePlatformReference() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {}
        };

    ServiceLocator.registerEntityService(new EntityService());

    Entity p1 = spy(Entity.class);
    p1.addComponent(new PlatformGrappleComponent(0));
    gameArea.spawnEntity(p1);
    verify(p1).create();

    assertFalse(gameArea.platforms.contains(p1));

    Entity p2 = spy(Entity.class);
    p2.addComponent(new PlatformGrappleComponent(8));
    gameArea.spawnEntity(p2);
    verify(p2).create();

    assertTrue(gameArea.platforms.contains(p2));

    gameArea.dispose();
    verify(p1).dispose();
    verify(p2).dispose();
  }

  @Test
  void findsBestPlatformCandidate() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {}
        };

    ServiceLocator.registerEntityService(new EntityService());
    PlatformGrappleComponent comp = new PlatformGrappleComponent(15);

    Entity p1 = spy(Entity.class);
    p1.setPosition(0.5f, 5.5f);
    p1.addComponent(comp);

    Entity p2 = spy(Entity.class);
    p2.setPosition(1.5f, 0.5f);
    p2.addComponent(comp);

    Entity p3 = spy(Entity.class);
    p3.setPosition(7.5f, 4.5f);
    p3.addComponent(comp);

    gameArea.spawnEntity(p1);
    gameArea.spawnEntity(p2);
    gameArea.spawnEntity(p3);

    assertEquals(p1, gameArea.findTargetedPlatform(new Vector2(-15, 5))); // test negatives
    assertEquals(p2, gameArea.findTargetedPlatform(new Vector2(2, 1))); // test exact center
    assertEquals(p3, gameArea.findTargetedPlatform(new Vector2(15, 10))); // test far
    assertEquals(p1, gameArea.findTargetedPlatform(new Vector2(1.5f, 3.5f))); // test equidistant
  }
}
