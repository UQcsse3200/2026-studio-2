package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerAttackComponentTest {
  private EntityService entityService;
  private Sound attackSound;

  @BeforeEach
  void setUp() {
    entityService = mock(EntityService.class);
    ResourceService resourceService = mock(ResourceService.class);
    attackSound = mock(Sound.class);
    when(resourceService.getAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(attackSound);
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerResourceService(resourceService);
  }

  @Test
  void shouldSpawnArrowAndPublishAnimationDirection() {
    Entity projectile = mock(Entity.class);
    AtomicReference<Vector2> spawnPosition = new AtomicReference<>();
    AtomicReference<Vector2> projectileDirection = new AtomicReference<>();
    PlayerAttackComponent component =
        new PlayerAttackComponent(
            (position, direction) -> {
              spawnPosition.set(position);
              projectileDirection.set(direction);
              return projectile;
            });
    Entity player = new Entity().addComponent(component);
    player.setPosition(1f, 2f);
    player.setScale(2f, 2f);
    AtomicReference<Vector2> animationDirection = new AtomicReference<>();
    player
        .getEvents()
        .addListener("attackAnimation", (Vector2 direction) -> animationDirection.set(direction));
    player.create();

    player.getEvents().trigger("fireArrow", new Vector2(3f, 4f));

    Vector2 expectedDirection = new Vector2(0.6f, 0.8f);
    assertTrue(projectileDirection.get().epsilonEquals(expectedDirection));
    assertTrue(spawnPosition.get().epsilonEquals(new Vector2(2.72f, 3.96f)));
    assertTrue(animationDirection.get().epsilonEquals(expectedDirection));
    verify(entityService).register(projectile);
    verify(attackSound).play();
  }

  @Test
  void shouldIgnoreZeroDirection() {
    Entity projectile = mock(Entity.class);
    PlayerAttackComponent component =
        new PlayerAttackComponent((position, direction) -> projectile);
    Entity player = new Entity().addComponent(component);
    player.create();

    player.getEvents().trigger("fireArrow", Vector2.Zero.cpy());

    verify(entityService, never()).register(projectile);
    verify(attackSound, never()).play();
  }
}
