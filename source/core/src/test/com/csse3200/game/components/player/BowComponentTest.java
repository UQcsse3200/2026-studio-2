package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BowComponentTest {
  private static final float MIN_CHARGE_SPEED = 3f;
  private static final float MAX_CHARGE_SPEED = 15f; // ProjectileFactory.STANDARD_ARROW_SPEED

  private EntityService entityService;
  private Sound attackSound;
  private GameTime gameTime;

  @BeforeEach
  void setUp() {
    entityService = mock(EntityService.class);
    ResourceService resourceService = mock(ResourceService.class);
    attackSound = mock(Sound.class);
    when(resourceService.getAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(attackSound);
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerResourceService(resourceService);

    gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
  }

  /** Builds a bow whose spawns are captured, wired up on a fresh player entity. */
  private BowComponent buildBow(
      AtomicReference<Vector2> spawnPosition,
      AtomicReference<Vector2> projectileDirection,
      AtomicReference<Float> spawnSpeed,
      AtomicInteger spawnCount,
      Entity projectile) {
    BowComponent component =
        new BowComponent(
            (position, direction, speed) -> {
              spawnPosition.set(position);
              projectileDirection.set(direction);
              spawnSpeed.set(speed);
              spawnCount.incrementAndGet();
              return projectile;
            });
    Entity player = new Entity().addComponent(component);
    player.setPosition(1f, 2f);
    player.setScale(2f, 2f);
    component.create();
    return component;
  }

  @Test
  void shouldSpawnArrowAndPublishAnimationDirection() {
    Entity projectile = mock(Entity.class);
    AtomicReference<Vector2> spawnPosition = new AtomicReference<>();
    AtomicReference<Vector2> projectileDirection = new AtomicReference<>();
    AtomicReference<Float> spawnSpeed = new AtomicReference<>();
    AtomicInteger spawnCount = new AtomicInteger();
    BowComponent component =
        buildBow(spawnPosition, projectileDirection, spawnSpeed, spawnCount, projectile);
    AtomicReference<Vector2> animationDirection = new AtomicReference<>();
    component
        .getEntity()
        .getEvents()
        .addListener("attackAnimation", (Vector2 direction) -> animationDirection.set(direction));

    component.attack(new Vector2(3f, 4f));

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
    BowComponent component =
        buildBow(
            new AtomicReference<>(),
            new AtomicReference<>(),
            new AtomicReference<>(),
            new AtomicInteger(),
            projectile);

    component.attack(Vector2.Zero.cpy());

    verify(entityService, never()).register(any());
    verify(attackSound, never()).play();
  }

  @Test
  void shouldFireAtMinimumSpeedWithNoCharge() {
    AtomicReference<Float> spawnSpeed = new AtomicReference<>();
    BowComponent component =
        buildBow(
            new AtomicReference<>(),
            new AtomicReference<>(),
            spawnSpeed,
            new AtomicInteger(),
            mock(Entity.class));

    // No "attackChargeStart" event and no update() calls - chargeSeconds stays at 0.
    component.attack(new Vector2(1f, 0f));

    assertEquals(MIN_CHARGE_SPEED, spawnSpeed.get(), 0.01f);
  }

  @Test
  void shouldFireAtMaxSpeedAfterFullCharge() {
    AtomicReference<Float> spawnSpeed = new AtomicReference<>();
    BowComponent component =
        buildBow(
            new AtomicReference<>(),
            new AtomicReference<>(),
            spawnSpeed,
            new AtomicInteger(),
            mock(Entity.class));

    when(gameTime.getDeltaTime()).thenReturn(2.5f); // beyond the 2s cap in one step
    component.getEntity().getEvents().trigger("attackChargeStart");
    component.update();
    component.attack(new Vector2(1f, 0f));

    assertEquals(MAX_CHARGE_SPEED, spawnSpeed.get(), 0.01f);
  }

  @Test
  void shouldScaleSpeedLinearlyWithPartialCharge() {
    AtomicReference<Float> spawnSpeed = new AtomicReference<>();
    BowComponent component =
        buildBow(
            new AtomicReference<>(),
            new AtomicReference<>(),
            spawnSpeed,
            new AtomicInteger(),
            mock(Entity.class));

    when(gameTime.getDeltaTime()).thenReturn(1f); // half of the 2s cap
    component.getEntity().getEvents().trigger("attackChargeStart");
    component.update();
    component.attack(new Vector2(1f, 0f));

    float expectedSpeed = MIN_CHARGE_SPEED + (MAX_CHARGE_SPEED - MIN_CHARGE_SPEED) * 0.5f;
    assertEquals(expectedSpeed, spawnSpeed.get(), 0.01f);
  }

  @Test
  void shouldStopAccumulatingChargeAfterRelease() {
    AtomicReference<Float> spawnSpeed = new AtomicReference<>();
    AtomicInteger spawnCount = new AtomicInteger();
    BowComponent component =
        buildBow(
            new AtomicReference<>(),
            new AtomicReference<>(),
            spawnSpeed,
            spawnCount,
            mock(Entity.class));

    when(gameTime.getDeltaTime()).thenReturn(2.5f);
    when(gameTime.getTimeSince(anyLong())).thenReturn(1_000L);
    component.getEntity().getEvents().trigger("attackChargeStart");
    component.update();
    component.attack(new Vector2(1f, 0f)); // releases, resets charge to 0

    // Further update() calls without a new "attackChargeStart" should not accumulate charge.
    component.update();
    component.attack(new Vector2(1f, 0f)); // second shot, past cooldown

    assertEquals(2, spawnCount.get());
    assertEquals(MIN_CHARGE_SPEED, spawnSpeed.get(), 0.01f);
  }

  @Test
  void shouldDropShotFiredDuringCooldown() {
    AtomicInteger spawnCount = new AtomicInteger();
    BowComponent component =
        buildBow(
            new AtomicReference<>(),
            new AtomicReference<>(),
            new AtomicReference<>(),
            spawnCount,
            mock(Entity.class));

    when(gameTime.getTime()).thenReturn(1000L);
    component.attack(new Vector2(1f, 0f)); // first shot succeeds, records lastShotTimeMs = 1000

    when(gameTime.getTimeSince(1000L)).thenReturn(50L); // only 50ms later, still on cooldown
    component.attack(new Vector2(1f, 0f));

    assertEquals(1, spawnCount.get());
    verify(entityService, times(1)).register(any());
  }

  @Test
  void shouldFireAgainOnceCooldownElapses() {
    AtomicInteger spawnCount = new AtomicInteger();
    BowComponent component =
        buildBow(
            new AtomicReference<>(),
            new AtomicReference<>(),
            new AtomicReference<>(),
            spawnCount,
            mock(Entity.class));

    when(gameTime.getTime()).thenReturn(1000L);
    component.attack(new Vector2(1f, 0f));

    when(gameTime.getTimeSince(1000L)).thenReturn(250L); // past the 200ms cooldown
    component.attack(new Vector2(1f, 0f));

    assertEquals(2, spawnCount.get());
    verify(entityService, times(2)).register(any());
  }
}
