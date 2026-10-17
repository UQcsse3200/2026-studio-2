package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class GrappleArrowComponentTest {

  private PhysicsService physicsService;

  @AfterEach
  void tearDown() {
    if (physicsService != null) {
      physicsService.getPhysics().dispose();
    }
  }

  private Fixture fixtureOnLayer(short layer) {
    Filter filter = new Filter();
    filter.categoryBits = layer;
    Fixture fixture = mock(Fixture.class);
    when(fixture.getFilterData()).thenReturn(filter);
    Body bodyMock = mock(Body.class);
    when(fixture.getBody()).thenReturn(bodyMock);
    return fixture;
  }

  /**
   * An arrow already well clear of the shooter, so the "just left the player" guard is satisfied.
   */
  private Entity arrowFiredBy(Entity shooter) {
    Entity arrow = new Entity().addComponent(new GrappleArrowComponent(shooter));
    arrow.setPosition(6f, 0f);
    arrow.create();
    return arrow;
  }

  @Test
  void shouldAttachWhenHittingTerrain() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowFiredBy(new Entity().addComponent(grapple));

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.OBSTACLE));

    verify(grapple).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldAttachWhenHittingGround() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowFiredBy(new Entity().addComponent(grapple));

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));

    verify(grapple).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldOnlyLatchOntoSolidTerrain() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowFiredBy(new Entity().addComponent(grapple));

    for (short layer :
        new short[] {
          PhysicsLayer.PLAYER, PhysicsLayer.PLAYER_PROJECTILE, PhysicsLayer.WALL, PhysicsLayer.NPC
        }) {
      arrow.getEvents().trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(layer));
    }

    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldOnlyAttachOnce() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowFiredBy(new Entity().addComponent(grapple));
    Fixture wall = fixtureOnLayer(PhysicsLayer.GROUND);

    arrow.getEvents().trigger("collisionStart", mock(Fixture.class), wall);
    arrow.getEvents().trigger("collisionStart", mock(Fixture.class), wall);

    verify(grapple, times(1)).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldNotLatchWhileStillOnTopOfTheShooter() {
    Entity shooter = new Entity();
    GrappleComponent grapple = spy(new GrappleComponent());
    shooter.addComponent(grapple);

    Entity arrow = new Entity().addComponent(new GrappleArrowComponent(shooter));
    arrow.setPosition(0f, 0f); // spawned right on the player, overlapping their platform
    arrow.create();

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));

    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldNotCrashWhenShooterHasNoGrapple() {
    Entity arrow = arrowFiredBy(new Entity());

    assertDoesNotThrow(
        () ->
            arrow
                .getEvents()
                .trigger(
                    "collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND)));
  }

  @Test
  void shouldAlwaysAttachOnLandingRegardlessOfMouseState() {
    // The shot only fires once the button is released, so by the time the arrow lands the button
    // has already been let go - there's no "was it still held" state left to check.
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowFiredBy(new Entity().addComponent(grapple));

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));

    verify(grapple).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldAttachAtTheSurfaceCrossingPointNotTheArrowsEmbeddedCentre() {
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);

    GrappleComponent grapple = spy(new GrappleComponent());
    Entity shooter = new Entity().addComponent(grapple);
    shooter.setPosition(0f, -3f); // their feet are well below where the hook lands

    BodyDef wallDef = new BodyDef();
    wallDef.position.set(10f, 0f);
    Body wallBody = physicsService.getPhysics().createBody(wallDef);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(0.5f, 5f); // spans x in [9.5, 10.5]
    Fixture wallFixture = wallBody.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = wallFixture.getFilterData();
    filter.categoryBits = PhysicsLayer.GROUND;
    wallFixture.setFilterData(filter);

    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new GrappleArrowComponent(shooter));
    arrow.create();
    // Box2D only reports the collision once the fixtures already overlap, so by the time this
    // fires the arrow's own centre is already 0.3 units inside the wall.
    arrow.setPosition(9.3f, -0.5f);
    arrow.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(20f, 0f);

    arrow.getEvents().trigger("collisionStart", mock(Fixture.class), wallFixture);

    ArgumentCaptor<Vector2> pointCaptor = ArgumentCaptor.forClass(Vector2.class);
    verify(grapple).attachTo(eq(wallBody), pointCaptor.capture());
    // The wall's near face, not the arrow's embedded centre (9.8) or the wall's own centre (10).
    assertEquals(9.5f, pointCaptor.getValue().x, 0.01f);
    assertEquals(0f, pointCaptor.getValue().y, 0.01f);
  }

  /** A shooter whose feet are at the given height, with a real grapple to watch. */
  private Entity shooterWithFeetAt(float feetY, GrappleComponent grapple) {
    Entity shooter = new Entity().addComponent(grapple);
    shooter.setPosition(0f, feetY);
    return shooter;
  }

  /** An arrow whose centre is at the given height, well clear of the shooter horizontally. */
  private Entity arrowCentredAtHeight(Entity shooter, float centreY) {
    Entity arrow = new Entity().addComponent(new GrappleArrowComponent(shooter));
    arrow.setPosition(6f, centreY - 0.5f);
    arrow.create();
    return arrow;
  }

  private void land(Entity arrow) {
    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));
  }

  @Test
  void shouldNotHookSomethingBelowTheShootersFeet() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowCentredAtHeight(shooterWithFeetAt(10f, grapple), 4f);

    land(arrow);

    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldNotHookTheFloorTheShooterIsStandingOn() {
    GrappleComponent grapple = spy(new GrappleComponent());
    // The floor's top surface is exactly at their feet.
    Entity arrow = arrowCentredAtHeight(shooterWithFeetAt(0f, grapple), 0f);

    land(arrow);

    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldHookSomethingLevelWithOrAboveTheShootersMiddle() {
    GrappleComponent grapple = spy(new GrappleComponent());
    land(arrowCentredAtHeight(shooterWithFeetAt(0f, grapple), 0.5f));
    land(arrowCentredAtHeight(shooterWithFeetAt(0f, grapple), 8f));

    verify(grapple, times(2)).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldHookJustAboveTheMarginAndRefuseJustInsideIt() {
    GrappleComponent grapple = spy(new GrappleComponent());

    land(arrowCentredAtHeight(shooterWithFeetAt(0f, grapple), 0.05f));
    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));

    land(arrowCentredAtHeight(shooterWithFeetAt(0f, grapple), 0.2f));
    verify(grapple, times(1)).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldHoldAnArrowThatCannotHookRatherThanRemovingItAtOnce() {
    EntityService entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity below = arrowCentredAtHeight(shooterWithFeetAt(10f, grapple), 4f);
    GrappleComponent closeGrapple = spy(new GrappleComponent());
    Entity tooClose =
        new Entity().addComponent(new GrappleArrowComponent(shooterWithFeetAt(0f, closeGrapple)));
    tooClose.setPosition(0f, 0f); // on top of the shooter
    tooClose.create();

    land(below);
    land(tooClose);

    // It stays where it landed so the rope can reel it back in, instead of vanishing.
    verify(entityService, never()).scheduleRemoval(below);
    verify(entityService, never()).scheduleRemoval(tooClose);
    verify(closeGrapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldRemoveAnArrowThatHooksAtOnceBecauseTheRopeTakesOver() {
    EntityService entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    Entity arrow = arrowCentredAtHeight(shooterWithFeetAt(0f, spy(new GrappleComponent())), 5f);

    land(arrow);

    verify(entityService).scheduleRemoval(arrow);
  }

  @Test
  void shouldRemoveAMissedArrowThatHasNothingToAnimateOnItsNextUpdate() {
    EntityService entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    GameTime time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    Entity arrow = arrowCentredAtHeight(shooterWithFeetAt(10f, spy(new GrappleComponent())), 4f);

    land(arrow);
    arrow.getComponent(GrappleArrowComponent.class).update();

    verify(entityService).scheduleRemoval(arrow);
  }

  @Test
  void shouldDoNothingOnUpdateForAnArrowThatHasNotMissed() {
    EntityService entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    Entity arrow = arrowCentredAtHeight(shooterWithFeetAt(0f, spy(new GrappleComponent())), 5f);

    arrow.getComponent(GrappleArrowComponent.class).update();

    verify(entityService, never()).scheduleRemoval(arrow);
  }

  @Test
  void shouldNotHookOnALaterContactAfterBreakingAgainstTheFloor() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowCentredAtHeight(shooterWithFeetAt(10f, grapple), 4f);

    land(arrow);
    // Still overlapping something higher up as it is removed: it must not now hook that.
    arrow.setPosition(6f, 20f);
    land(arrow);

    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  /** Records every "grappleFailed" the shooter is sent. */
  private List<Vector2> failuresReportedTo(Entity shooter) {
    List<Vector2> failures = new ArrayList<>();
    shooter.getEvents().addListener("grappleFailed", (Vector2 point) -> failures.add(point));
    return failures;
  }

  @Test
  void shouldReportAFailureWhenTheArrowLandsBelowTheShooter() {
    Entity shooter = shooterWithFeetAt(10f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);

    land(arrowCentredAtHeight(shooter, 4f));

    assertEquals(1, failures.size());
    assertEquals(4f, failures.get(0).y, 1e-4f);
  }

  @Test
  void shouldReportAFailureWhenTheArrowLandsRightOnTopOfTheShooter() {
    Entity shooter = shooterWithFeetAt(0f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);
    Entity arrow = new Entity().addComponent(new GrappleArrowComponent(shooter));
    arrow.setPosition(0f, 1f); // above their feet, but a unit from them
    arrow.create();

    land(arrow);

    assertEquals(1, failures.size());
  }

  @Test
  void shouldNotReportAFailureWhenTheArrowHooks() {
    Entity shooter = shooterWithFeetAt(0f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);

    land(arrowCentredAtHeight(shooter, 5f));

    assertEquals(0, failures.size());
  }

  @Test
  void shouldReportEachFailureOnlyOnce() {
    Entity shooter = shooterWithFeetAt(10f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);
    Entity arrow = arrowCentredAtHeight(shooter, 4f);

    land(arrow);
    land(arrow);

    assertEquals(1, failures.size());
  }

  @Test
  void shouldReportAFailureWhereTheArrowRanOutOfRange() {
    Entity shooter = shooterWithFeetAt(0f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);
    Entity arrow = arrowCentredAtHeight(shooter, 5f);

    arrow.getEvents().trigger(ArrowProjectileComponent.RANGE_EXCEEDED_EVENT, new Vector2(7f, 20f));

    assertEquals(1, failures.size());
    assertEquals(new Vector2(7f, 20f), failures.get(0));
  }

  @Test
  void shouldNotReportRunningOutOfRangeForAHookThatAlreadyLanded() {
    Entity shooter = shooterWithFeetAt(10f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);
    Entity arrow = arrowCentredAtHeight(shooter, 4f);

    land(arrow); // missed the floor: that is the one marker for this shot
    arrow.getEvents().trigger(ArrowProjectileComponent.RANGE_EXCEEDED_EVENT, new Vector2(7f, 4f));

    assertEquals(1, failures.size());
  }

  @Test
  void shouldNotReportRunningOutOfRangeForAHookThatCaught() {
    Entity shooter = shooterWithFeetAt(0f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);
    Entity arrow = arrowCentredAtHeight(shooter, 5f);

    land(arrow);
    arrow.getEvents().trigger(ArrowProjectileComponent.RANGE_EXCEEDED_EVENT, new Vector2(7f, 5f));

    assertEquals(0, failures.size());
  }

  @Test
  void shouldNotReportAnythingForLayersThatNeverStopAGrapple() {
    Entity shooter = shooterWithFeetAt(10f, spy(new GrappleComponent()));
    List<Vector2> failures = failuresReportedTo(shooter);
    Entity arrow = arrowCentredAtHeight(shooter, 4f);

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.NPC));

    assertEquals(0, failures.size());
  }

  @Test
  void shouldStillBreakTheArrowOnTerrainWhenTheShooterHasNoGrapple() {
    EntityService entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    Entity arrow = arrowFiredBy(new Entity());

    land(arrow);

    verify(entityService).scheduleRemoval(arrow);
  }
}
