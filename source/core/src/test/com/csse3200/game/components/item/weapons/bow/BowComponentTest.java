package com.csse3200.game.components.item.weapons.bow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BowComponentTest {
  // Must match BowComponent's private MIN_CHARGE_SPEED_FACTOR.
  private static final float MIN_CHARGE_SPEED_FACTOR = 0.2f;

  private EntityService entityService;
  private Sound attackSound;
  private GameTime gameTime;

  @BeforeEach
  void setUp() {
    entityService = mock(EntityService.class);
    ResourceService resourceService = mock(ResourceService.class);
    attackSound = mock(Sound.class);
    gameTime = mock(GameTime.class);

    when(resourceService.containsAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(true);
    when(resourceService.getAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(attackSound);

    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerResourceService(resourceService);
    ServiceLocator.registerTimeSource(gameTime);
  }

  @Test
  void shouldSpawnArrowAndPublishAnimationDirection() {
    Entity projectile = mock(Entity.class);
    AtomicReference<Entity> shooterRef = new AtomicReference<>();
    AtomicReference<Vector2> spawnPosition = new AtomicReference<>();
    AtomicReference<Vector2> projectileDirection = new AtomicReference<>();
    AtomicReference<Float> speedMultiplierRef = new AtomicReference<>();

    BowComponent component =
        new BowComponent(
            (shooter, position, direction, speedMultiplier) -> {
              shooterRef.set(shooter);
              spawnPosition.set(position);
              projectileDirection.set(direction);
              speedMultiplierRef.set(speedMultiplier);
              return projectile;
            });

    Entity player = new Entity().addComponent(component);
    player.setPosition(1f, 2f);
    player.setScale(2f, 2f);

    AtomicReference<Vector2> animationDirection = new AtomicReference<>();
    player
        .getEvents()
        .addListener("attackAnimation", (Vector2 direction) -> animationDirection.set(direction));

    component.attack(new Vector2(3f, 4f));

    Vector2 expectedDirection = new Vector2(0.6f, 0.8f);
    assertEquals(player, shooterRef.get());
    assertTrue(projectileDirection.get().epsilonEquals(expectedDirection));
    assertTrue(spawnPosition.get().epsilonEquals(new Vector2(2.96f, 4.28f)));
    assertTrue(animationDirection.get().epsilonEquals(expectedDirection));
    assertEquals(1f, speedMultiplierRef.get());
    verify(entityService).register(projectile);
    verify(attackSound).play();
  }

  @Test
  void shouldIgnoreZeroDirection() {
    Entity projectile = mock(Entity.class);
    BowComponent component =
        new BowComponent((shooter, position, direction, speedMultiplier) -> projectile);
    new Entity().addComponent(component);

    component.attack(Vector2.Zero.cpy());

    verify(entityService, never()).register(projectile);
    verify(attackSound, never()).play();
  }

  @Test
  void shouldSwapArrowTypesCorrectly() {
    BowComponent component = new BowComponent();
    assertEquals(ArrowType.STANDARD, component.getArrowType());

    component.setArrowType(ArrowType.FIRE);
    assertEquals(ArrowType.FIRE, component.getArrowType());

    component.setArrowType(null);
    assertEquals(ArrowType.STANDARD, component.getArrowType());
  }

  private BowComponent createChargeComponent(AtomicReference<Float> speedMultiplierRef) {
    Entity projectile = mock(Entity.class);
    BowComponent component =
        new BowComponent(
            (shooter, position, direction, speedMultiplier) -> {
              speedMultiplierRef.set(speedMultiplier);
              return projectile;
            });
    new Entity().addComponent(component);
    return component;
  }

  @Test
  void shouldFireAtMinSpeedFactorOnImmediateRelease() {
    AtomicReference<Float> speedMultiplierRef = new AtomicReference<>();
    BowComponent component = createChargeComponent(speedMultiplierRef);
    when(gameTime.getTime()).thenReturn(0L, 0L);

    component.startCharge(new Vector2(1f, 0f));
    component.releaseCharge(new Vector2(1f, 0f));

    assertEquals(MIN_CHARGE_SPEED_FACTOR, speedMultiplierRef.get());
  }

  @Test
  void shouldFireAtFullSpeedAfterMaxCharge() {
    AtomicReference<Float> speedMultiplierRef = new AtomicReference<>();
    BowComponent component = createChargeComponent(speedMultiplierRef);
    when(gameTime.getTime()).thenReturn(0L, 2000L);

    component.startCharge(new Vector2(1f, 0f));
    component.releaseCharge(new Vector2(1f, 0f));

    assertEquals(1f, speedMultiplierRef.get());
  }

  @Test
  void shouldClampSpeedMultiplierBeyondMaxCharge() {
    AtomicReference<Float> speedMultiplierRef = new AtomicReference<>();
    BowComponent component = createChargeComponent(speedMultiplierRef);
    when(gameTime.getTime()).thenReturn(0L, 5000L);

    component.startCharge(new Vector2(1f, 0f));
    component.releaseCharge(new Vector2(1f, 0f));

    assertEquals(1f, speedMultiplierRef.get());
  }

  @Test
  void shouldScaleLinearlyAtPartialCharge() {
    AtomicReference<Float> speedMultiplierRef = new AtomicReference<>();
    BowComponent component = createChargeComponent(speedMultiplierRef);
    when(gameTime.getTime()).thenReturn(0L, 1000L); // 1s of the 2s max

    component.startCharge(new Vector2(1f, 0f));
    component.releaseCharge(new Vector2(1f, 0f));

    float expected = MIN_CHARGE_SPEED_FACTOR + (1f - MIN_CHARGE_SPEED_FACTOR) * 0.5f;
    assertEquals(expected, speedMultiplierRef.get(), 1e-5f);
  }

  @Test
  void shouldNoOpReleaseWithoutCharge() {
    Entity projectile = mock(Entity.class);
    BowComponent component =
        new BowComponent((shooter, position, direction, speedMultiplier) -> projectile);
    new Entity().addComponent(component);

    component.releaseCharge(new Vector2(1f, 0f));

    verify(entityService, never()).register(projectile);
  }

  @Test
  void shouldNotStartChargeWhileOnCooldown() {
    AtomicReference<Float> speedMultiplierRef = new AtomicReference<>();
    BowComponent component = createChargeComponent(speedMultiplierRef);
    when(gameTime.getTime()).thenReturn(0L);

    component.attack(new Vector2(1f, 0f)); // puts the bow on cooldown
    component.startCharge(new Vector2(1f, 0f));
    component.releaseCharge(new Vector2(1f, 0f));

    // Only the initial attack() should have fired - startCharge was rejected by the cooldown.
    assertEquals(1f, speedMultiplierRef.get());
  }

  @Test
  void shouldApplyCooldownOnReleaseNotOnStart() {
    AtomicReference<Float> speedMultiplierRef = new AtomicReference<>();
    BowComponent component = createChargeComponent(speedMultiplierRef);
    when(gameTime.getTime()).thenReturn(0L, 0L);

    component.startCharge(new Vector2(1f, 0f));
    assertTrue(component.isReady());

    component.releaseCharge(new Vector2(1f, 0f));
    assertFalse(component.isReady());
    assertTrue(component.getCooldownRemaining() > 0f);
  }
}
