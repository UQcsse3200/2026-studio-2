package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * Firing, cooldown, charge events and sound, with the arrow factory, entity service, resources and
 * clock all mocked so each rule is checked on its own.
 */
@ExtendWith(GameExtension.class)
class GrappleComponentFiringTest {
  private static final String RELEASE_SOUND = "sounds/Arrow_release.wav";

  private GameTime gameTime;
  private PhysicsService physics;
  private EntityService entities;
  private ResourceService resources;
  private Entity player;
  private GrappleComponent grapple;

  @BeforeEach
  void setUp() {
    gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entities = mock(EntityService.class);
    ServiceLocator.registerEntityService(entities);
    resources = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resources);

    player = new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    player.create();
    grapple = player.getComponent(GrappleComponent.class);
  }

  @AfterEach
  void tearDown() {
    physics.getPhysics().dispose();
  }

  private static MockedStatic<ProjectileFactory> arrowFactoryReturning(Entity arrow) {
    MockedStatic<ProjectileFactory> factory = mockStatic(ProjectileFactory.class);
    factory
        .when(
            () ->
                ProjectileFactory.createGrappleArrow(
                    any(Entity.class), any(Vector2.class), any(Vector2.class), anyFloat()))
        .thenReturn(arrow);
    return factory;
  }

  private static void verifyArrowsCreated(MockedStatic<ProjectileFactory> factory, int times) {
    factory.verify(
        () ->
            ProjectileFactory.createGrappleArrow(
                any(Entity.class), any(Vector2.class), any(Vector2.class), anyFloat()),
        times(times));
  }

  @Test
  void shouldFireAFullSpeedArrowWhenTheGrappleFireEventIsTriggered() {
    Entity arrow = new Entity();
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(arrow)) {
      // This is the instant path (the attack key), which skips the charge entirely.
      player.getEvents().trigger("grappleFire", new Vector2(1f, 0f));

      factory.verify(
          () ->
              ProjectileFactory.createGrappleArrow(
                  eq(player), any(Vector2.class), any(Vector2.class), eq(1f)));
    }
    verify(entities).register(arrow);
    assertNotNull(arrow.getComponent(GrappleArrowComponent.class));
    assertTrue(grapple.isOnCooldown());
  }

  @Test
  void shouldSpawnTheArrowJustAheadOfThePlayerAlongTheNormalisedAim() {
    ArgumentCaptor<Vector2> spawn = ArgumentCaptor.forClass(Vector2.class);
    ArgumentCaptor<Vector2> aim = ArgumentCaptor.forClass(Vector2.class);
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      grapple.fire(new Vector2(0f, 5f));

      factory.verify(
          () ->
              ProjectileFactory.createGrappleArrow(
                  eq(player), spawn.capture(), aim.capture(), eq(1f)));
    }

    // The player is 1x1 at the origin, so their centre is (0.5, 0.5).
    assertEquals(0f, aim.getValue().x, 1e-5f);
    assertEquals(1f, aim.getValue().y, 1e-5f);
    assertEquals(0.5f, spawn.getValue().x, 1e-5f);
    assertEquals(0.5f + GrappleComponent.SPAWN_OFFSET, spawn.getValue().y, 1e-5f);
  }

  @Test
  void shouldPlayTheReleaseSoundWhenFiring() {
    Sound sound = mock(Sound.class);
    when(resources.getAsset(RELEASE_SOUND, Sound.class)).thenReturn(sound);

    try (MockedStatic<ProjectileFactory> ignored = arrowFactoryReturning(new Entity())) {
      grapple.fire(Vector2.X.cpy());
    }

    verify(sound).play(0.4f);
  }

  @Test
  void shouldStillFireWhenTheSoundCannotBeLoaded() {
    when(resources.getAsset(RELEASE_SOUND, Sound.class)).thenThrow(new RuntimeException("missing"));

    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      grapple.fire(Vector2.X.cpy());

      verifyArrowsCreated(factory, 1);
    }
  }

  @Test
  void shouldNotFireWhileOnCooldownThenFireAgainOnceItsElapsed() {
    when(gameTime.getDeltaTime()).thenReturn(1f);
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      grapple.fire(Vector2.X.cpy());
      grapple.fire(Vector2.X.cpy());
      verifyArrowsCreated(factory, 1);

      // The cooldown is 2 seconds: one second leaves it running, two clears it.
      grapple.update();
      assertTrue(grapple.isOnCooldown());
      grapple.fire(Vector2.X.cpy());
      verifyArrowsCreated(factory, 1);

      grapple.update();
      assertFalse(grapple.isOnCooldown());
      grapple.fire(Vector2.X.cpy());
      verifyArrowsCreated(factory, 2);
    }
  }

  @Test
  void shouldNotFireWithoutAUsableAim() {
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      grapple.fire(null);
      grapple.fire(Vector2.Zero.cpy());

      verifyArrowsCreated(factory, 0);
    }
    assertFalse(grapple.isOnCooldown());
  }

  @Test
  void shouldOnlyBroadcastTheDrawAnimationEventsForPressesItActuallyAccepts() {
    AtomicInteger drawStarts = new AtomicInteger();
    AtomicInteger drawFires = new AtomicInteger();
    player
        .getEvents()
        .addListener("grappleChargeStart", (Vector2 d) -> drawStarts.incrementAndGet());
    player.getEvents().addListener("grappleChargeFire", (Vector2 d) -> drawFires.incrementAndGet());

    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      grapple.startCharge(Vector2.X.cpy());
      assertEquals(1, drawStarts.get());

      // Already drawing: pressing again is ignored and must not restart the animation.
      grapple.startCharge(Vector2.X.cpy());
      assertEquals(1, drawStarts.get());

      grapple.releaseCharge(Vector2.X.cpy());
      assertEquals(1, drawFires.get());
      verifyArrowsCreated(factory, 1);

      // Now on cooldown: a press does nothing at all, so no draw animation may play for it.
      grapple.startCharge(Vector2.X.cpy());
      assertEquals(1, drawStarts.get());
      assertFalse(grapple.isCharging());

      // And a release with nothing drawn fires nothing and plays nothing.
      grapple.releaseCharge(Vector2.X.cpy());
      assertEquals(1, drawFires.get());
      verifyArrowsCreated(factory, 1);
    }
  }

  @Test
  void shouldDropTheChargeWithoutFiringWhenAnOverlayCancelsIt() {
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      grapple.startCharge(Vector2.X.cpy());
      assertTrue(grapple.isCharging());

      // e.g. the shop opens and swallows the mouse-up
      player.getEvents().trigger("chargeCancel");

      assertFalse(grapple.isCharging());
      grapple.releaseCharge(Vector2.X.cpy());
      verifyArrowsCreated(factory, 0);
    }
    assertFalse(grapple.isOnCooldown());
  }

  @Test
  void shouldListenOnItsOwnEventsAndNotTheBowsChargeEvents() {
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      // The bow's hold-and-release events must not start or fire a grapple charge...
      player.getEvents().trigger("chargeStart", Vector2.X.cpy());
      assertFalse(grapple.isCharging());

      // ...which only its own events do.
      player.getEvents().trigger("grappleDrawStart", Vector2.X.cpy());
      assertTrue(grapple.isCharging());

      player.getEvents().trigger("chargeRelease", Vector2.X.cpy());
      assertTrue(grapple.isCharging());
      verifyArrowsCreated(factory, 0);

      player.getEvents().trigger("grappleDrawRelease", Vector2.X.cpy());
      assertFalse(grapple.isCharging());
      verifyArrowsCreated(factory, 1);
    }
  }

  @Test
  void shouldSlowAShotReleasedEarlyAndNotOneHeldToFullDraw() {
    ArgumentCaptor<Float> tap = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> fullDraw = ArgumentCaptor.forClass(Float.class);
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      when(gameTime.getTime()).thenReturn(0L);
      grapple.startCharge(Vector2.X.cpy());
      grapple.releaseCharge(Vector2.X.cpy());
      factory.verify(
          () ->
              ProjectileFactory.createGrappleArrow(
                  any(Entity.class), any(Vector2.class), any(Vector2.class), tap.capture()));
    }

    // A new grapple, as the first is now on cooldown.
    Entity second =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    second.create();
    try (MockedStatic<ProjectileFactory> factory = arrowFactoryReturning(new Entity())) {
      when(gameTime.getTime()).thenReturn(0L, 10_000L);
      GrappleComponent held = second.getComponent(GrappleComponent.class);
      held.startCharge(Vector2.X.cpy());
      held.releaseCharge(Vector2.X.cpy());
      factory.verify(
          () ->
              ProjectileFactory.createGrappleArrow(
                  any(Entity.class), any(Vector2.class), any(Vector2.class), fullDraw.capture()));
    }

    assertTrue(tap.getValue() < 1f, "a tap should launch below normal speed");
    assertTrue(fullDraw.getValue() > 1f, "a full draw should launch above normal speed");
  }

  @Test
  void shouldNeverStartChargingWithoutAnAim() {
    grapple.startCharge(null);
    grapple.startCharge(Vector2.Zero.cpy());

    assertFalse(grapple.isCharging());
    verify(entities, never()).register(any(Entity.class));
  }
}
