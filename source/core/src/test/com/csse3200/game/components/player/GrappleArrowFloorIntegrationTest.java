package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleArrowComponent;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Fires real grapple arrows at a real floor and walls. An arrow that lands below the shooter - the
 * floor they are standing on, say - must not hook, and must break there rather than carry on
 * through the platform.
 */
@ExtendWith(GameExtension.class)
class GrappleArrowFloorIntegrationTest {
  private static final float STEP = 0.016f;
  private static final int MAX_STEPS = 400;

  private PhysicsService physicsService;
  private EntityService entityService;
  private GrappleComponent grapple;
  private Entity shooter;
  private final List<Vector2> failures = new ArrayList<>();

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(STEP);
    ServiceLocator.registerTimeSource(time);
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerRenderService(mock(RenderService.class));

    grapple = spy(new GrappleComponent());
    shooter = new Entity().addComponent(grapple);
    shooter.setPosition(0f, 0f); // standing on the floor: their feet are at y = 0
    shooter.getEvents().<Vector2>addListener("grappleFailed", failures::add);

    // A long floor whose top surface is at y = 0, and a tall wall whose right face is at x = -10.
    solid(0f, -1f, 100f, 1f);
    solid(-12f, 0f, 2f, 60f);
  }

  @AfterEach
  void tearDown() {
    physicsService.getPhysics().dispose();
  }

  /** A static block on the ground layer, centred at (x, y) with the given half sizes. */
  private void solid(float x, float y, float halfWidth, float halfHeight) {
    BodyDef def = new BodyDef();
    def.position.set(x, y);
    Body body = physicsService.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(halfWidth, halfHeight);
    Fixture fixture = body.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.GROUND;
    fixture.setFilterData(filter);
  }

  /** What happened to one arrow. */
  private record Flight(boolean gone, float lowestPoint, float highestPoint, float furthestRight) {}

  /** Fires from beside the shooter and steps the world until the arrow is gone or gives up. */
  private Flight fire(Vector2 aim) {
    return fire(aim, 1f);
  }

  private Flight fire(Vector2 aim, float speedMultiplier) {
    Vector2 spawn = shooter.getCenterPosition().mulAdd(aim.cpy().nor(), 0.6f);
    Entity arrow = ProjectileFactory.createGrappleArrow(shooter, spawn, aim, speedMultiplier);
    arrow.addComponent(new GrappleArrowComponent(shooter));
    entityService.register(arrow);

    float lowest = arrow.getCenterPosition().y;
    float highest = lowest;
    float furthestRight = arrow.getCenterPosition().x;
    for (int step = 0; step < MAX_STEPS; step++) {
      physicsService.getPhysics().update();
      entityService.update();
      lowest = Math.min(lowest, arrow.getCenterPosition().y);
      highest = Math.max(highest, arrow.getCenterPosition().y);
      furthestRight = Math.max(furthestRight, arrow.getCenterPosition().x);
      if (!entityService.getEntities().contains(arrow, true)) {
        return new Flight(true, lowest, highest, furthestRight);
      }
    }
    return new Flight(false, lowest, highest, furthestRight);
  }

  private void assertDidNotHook() {
    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldShowAFailureOnTheFloorWhenShotStraightDownAtIt() {
    fire(new Vector2(0f, -1f));

    assertEquals(1, failures.size());
    assertEquals(0f, failures.get(0).y, 0.15f, "the marker belongs on the floor's surface");
  }

  @Test
  void shouldShowTheSameFailureWhenItLandsOnTheFloorFurtherAway() {
    fire(new Vector2(1f, -0.2f));

    assertEquals(1, failures.size());
    assertEquals(0f, failures.get(0).y, 0.15f);
    assertTrue(failures.get(0).x > 1.5f, "it landed some way off, not under their feet");
  }

  @Test
  void shouldShowAFailureForAWallTooCloseToHook() {
    solid(3.0f, 0f, 1.5f, 60f);

    fire(new Vector2(1f, 0.3f));

    assertEquals(1, failures.size());
    assertEquals(1.5f, failures.get(0).x, 0.1f, "on the face of the wall");
  }

  @Test
  void shouldNotShowAFailureWhenTheArrowHooksAWall() {
    fire(new Vector2(-1f, 0.6f));

    assertEquals(0, failures.size());
  }

  @Test
  void shouldShowTheMarkerWhereTheArrowRanOutOfRange() {
    // Straight up, with nothing there to hit: the arrow gives up in mid-air.
    Flight flight = fire(new Vector2(0f, 1f), 1.5f);

    assertTrue(flight.gone());
    assertEquals(1, failures.size(), "an arrow that flew its range should also show the X");
    assertEquals(0f, failures.get(0).x, 0.5f);
    assertEquals(
        ProjectileFactory.GRAPPLE_ARROW_RANGE,
        failures.get(0).y,
        0.8f,
        "at the point it gave up, " + ProjectileFactory.GRAPPLE_ARROW_RANGE + " from the shooter");
    assertDidNotHook();
  }

  @Test
  void shouldShowOnlyTheFloorsMarkerForAWeakShotThatFallsBackDown() {
    // A weak shot straight up falls back down well short of the range, and lands on the floor: that
    // is the floor's marker, and there must be only the one.
    fire(new Vector2(0f, 1f), 0.3f);

    assertEquals(1, failures.size());
    assertEquals(0f, failures.get(0).y, 0.15f, "on the floor, not in mid-air");
  }

  @Test
  void shouldGiveUpOnceItHasFlownTheGrappleRange() {
    // A fully charged shot straight up would climb to about 28 units before turning round. The
    // range cuts it off a few units short of that, as it passes 25 from the shooter.
    Flight flight = fire(new Vector2(0f, 1f), 1.5f);

    assertTrue(flight.gone(), "the arrow should have been removed");
    assertDidNotHook();
    assertTrue(
        flight.highestPoint() < ProjectileFactory.GRAPPLE_ARROW_RANGE + 2f,
        "it climbed to " + flight.highestPoint());
    assertTrue(
        flight.highestPoint() > ProjectileFactory.GRAPPLE_ARROW_RANGE - 2f,
        "it gave up too early, at " + flight.highestPoint());
  }

  @Test
  void shouldNotHookOrPassThroughTheFloorWhenShotStraightDownAtIt() {
    Flight flight = fire(new Vector2(0f, -1f));

    assertDidNotHook();
    assertTrue(flight.gone(), "the arrow should have broken on the floor");
    assertTrue(flight.lowestPoint() > -1.5f, "it went " + flight.lowestPoint() + " into the floor");
  }

  @Test
  void shouldNotHookOrPassThroughTheFloorWhenShotDownAtAnAngleNearby() {
    Flight flight = fire(new Vector2(1f, -1f));

    assertDidNotHook();
    assertTrue(flight.gone());
    assertTrue(flight.lowestPoint() > -1.5f);
  }

  @Test
  void shouldNotHookTheFloorFurtherAwayEither() {
    // A shallow downward shot lands well over a unit away, so it used to hook the floor.
    Flight flight = fire(new Vector2(1f, -0.2f));

    assertDidNotHook();
    assertTrue(flight.gone());
    assertTrue(flight.lowestPoint() > -1.5f);
  }

  @Test
  void shouldNotHookTheFloorBelowALedgeTheShooterIsStandingOn() {
    shooter.setPosition(0f, 6f);

    Flight flight = fire(new Vector2(1f, -0.4f));

    assertDidNotHook();
    assertTrue(flight.gone(), "it should break on the floor six units below");
    assertTrue(flight.lowestPoint() > -1.5f);
  }

  @Test
  void shouldStillHookAWallAboveTheShootersFeet() {
    Flight flight = fire(new Vector2(-1f, 0.6f));

    verify(grapple, times(1)).attachTo(any(Body.class), any(Vector2.class));
    assertTrue(flight.gone(), "the arrow is used up once it has hooked");
  }

  @Test
  void shouldNotHookOrPassThroughAWallRightNextToTheShooter() {
    // A wall whose face is just beside the shooter: too close to hook, but still a wall.
    solid(3.0f, 0f, 1.5f, 60f);

    Flight flight = fire(new Vector2(1f, 0.3f));

    assertDidNotHook();
    assertTrue(flight.gone());
    assertFalse(
        flight.furthestRight() > 5f, "it went through the wall to " + flight.furthestRight());
  }
}
