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

    Entity platform = spy(Entity.class);
    platform.addComponent(new PlatformGrappleComponent(0));
    gameArea.spawnEntity(platform);
    verify(platform).create();

    assertTrue(gameArea.platforms.contains(platform));

    gameArea.dispose();
    verify(platform).dispose();
  }

  @Test
  void findsBestPlatformCandidate() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {}
        };

    ServiceLocator.registerEntityService(new EntityService());
    PlatformGrappleComponent comp = new PlatformGrappleComponent(0);

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

  @Test
  void checkSuccessfulGrapples() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {}
        };

    ServiceLocator.registerEntityService(new EntityService());
    PlatformGrappleComponent comp = new PlatformGrappleComponent(8);

    Entity p1 = spy(Entity.class);
    p1.setPosition(0.5f, 5.5f); // center is 1f, 6f
    p1.addComponent(comp);

    gameArea.spawnEntity(p1);

    assertTrue(gameArea.checkSuccessfulGrapple(new Vector2(0.501f, 5.634f))); // normal valid
    assertTrue(gameArea.checkSuccessfulGrapple(new Vector2(0.505f, 5.51f))); // edge success
    assertFalse(gameArea.checkSuccessfulGrapple(new Vector2(0.494f, 5.51f))); // edge fail
    assertFalse(gameArea.checkSuccessfulGrapple(new Vector2(5f, 10f))); // normal invalid
  }
}
