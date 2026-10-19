package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
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
 * Fires the real grapple at the floor. A hook that cannot catch should stay where it landed for a
 * moment and then be reeled back along the rope to the player, rather than just vanishing.
 */
@ExtendWith(GameExtension.class)
class GrappleMissReelInIntegrationTest {
  private static final float STEP = 0.016f;
  private static final int MAX_STEPS = 600;

  private PhysicsService physics;
  private EntityService entities;
  private Entity player;
  private GrappleComponent grapple;
  private final List<Vector2> failures = new ArrayList<>();

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(STEP);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerRenderService(mock(RenderService.class));

    grapple = new GrappleComponent();
    player = new Entity().addComponent(new PhysicsComponent()).addComponent(grapple);
    player.setScale(1f, 1f);
    player.setPosition(0f, 0f); // standing on the floor: their feet are at y = 0
    entities.register(player);
    player.getComponent(PhysicsComponent.class).getBody().setGravityScale(0f);
    player.getEvents().addListener("grappleFailed", (Vector2 point) -> failures.add(point));

    // A long floor whose top surface is at y = 0.
    BodyDef def = new BodyDef();
    def.position.set(0f, -1f);
    Body floor = physics.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(100f, 1f);
    Fixture fixture = floor.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.GROUND;
    fixture.setFilterData(filter);
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  private void step() {
    physics.getPhysics().update();
    entities.update();
  }

  private Vector2 centreOf(Entity arrow) {
    return arrow.getComponent(ArrowProjectileComponent.class).getWorldCenter();
  }

  private boolean isAlive(Entity arrow) {
    return entities.getEntities().contains(arrow, true);
  }

  /** Fires out across the floor from far enough up that it lands several units away. */
  private Entity fireAtTheFloorUntilItLands() {
    return fireAtTheFloorUntilItLands(new Vector2(1f, -0.1f));
  }

  /** Fires at the floor, and steps until the hook has landed and failed. */
  private Entity fireAtTheFloorUntilItLands(Vector2 aim) {
    grapple.fire(aim);
    Entity arrow = entities.getEntities().get(1);
    for (int i = 0; i < MAX_STEPS && failures.isEmpty(); i++) {
      step();
    }
    assertEquals(1, failures.size(), "the hook should have landed on the floor and failed");
    return arrow;
  }

  @Test
  void shouldKeepAMissedHookWhereItLandedForAMoment() {
    Entity arrow = fireAtTheFloorUntilItLands();
    Vector2 landed = centreOf(arrow);

    for (int i = 0; i < 10; i++) { // about 0.16 seconds
      step();
      assertTrue(isAlive(arrow), "it should still be there");
      assertTrue(centreOf(arrow).epsilonEquals(landed, 1e-3f), "and not yet moving");
    }
  }

  @Test
  void shouldThenReelItBackToThePlayerAndRemoveItOnArrival() {
    Entity arrow = fireAtTheFloorUntilItLands();
    float lastDistance = centreOf(arrow).dst(player.getCenterPosition());
    boolean startedMoving = false;

    int steps = 0;
    while (isAlive(arrow) && steps++ < MAX_STEPS) {
      step();
      if (!isAlive(arrow)) {
        break;
      }
      float distance = centreOf(arrow).dst(player.getCenterPosition());
      assertTrue(distance <= lastDistance + 1e-3f, "it should only ever come closer");
      startedMoving |= distance < lastDistance - 1e-3f;
      lastDistance = distance;
    }

    assertFalse(isAlive(arrow), "it should be gone once it has been reeled in");
    assertTrue(startedMoving, "it should have been reeled in, not just removed");
    assertTrue(lastDistance < 1.5f, "and it vanished beside the player, not out on the floor");
    assertTrue(steps * STEP < 2f, "reeling in should be quick, but took " + steps * STEP);
  }

  @Test
  void shouldAlsoHoldThenRemoveAHookThatLandsRightAtThePlayersFeet() {
    Entity arrow = fireAtTheFloorUntilItLands(new Vector2(0f, -1f));
    assertTrue(centreOf(arrow).dst(player.getCenterPosition()) < 1.5f, "it landed at their feet");

    for (int i = 0; i < 8; i++) {
      step();
    }
    assertTrue(isAlive(arrow), "it is held for a moment even this close");

    for (int i = 0; i < MAX_STEPS && isAlive(arrow); i++) {
      step();
    }
    assertFalse(isAlive(arrow));
    assertTrue(grapple.getRopePath().isEmpty());
  }

  @Test
  void shouldKeepTheRopeRunningToTheHookAllTheWayBack() {
    Entity arrow = fireAtTheFloorUntilItLands();

    int checked = 0;
    for (int i = 0; i < MAX_STEPS && isAlive(arrow); i++) {
      List<Vector2> path = grapple.getRopePath();
      assertTrue(path.size() >= 2, "there should be a rope while the hook is out");
      assertTrue(path.getLast().epsilonEquals(centreOf(arrow), 1e-3f), "ending at the hook");
      assertTrue(
          path.getFirst()
              .epsilonEquals(
                  player.getComponent(PhysicsComponent.class).getBody().getWorldCenter(), 1e-3f),
          "starting at the player");
      checked++;
      step();
    }

    assertTrue(checked > 15, "the rope should have been there through the hold and the reel");
    assertTrue(grapple.getRopePath().isEmpty(), "and be gone once the hook is");
  }

  @Test
  void shouldNeverHookAnythingWhileItIsBeingReeledIn() {
    Entity arrow = fireAtTheFloorUntilItLands();

    for (int i = 0; i < MAX_STEPS && isAlive(arrow); i++) {
      step();
      assertFalse(grapple.isAttached());
    }

    assertEquals(0, physics.getPhysics().getWorld().getJointCount());
    assertEquals(1, failures.size(), "reeling past the floor must not report a second failure");
  }

  @Test
  void shouldDropAMissedHookAtOnceWhenTheShooterReleases() {
    Entity arrow = fireAtTheFloorUntilItLands();
    Vector2 landed = centreOf(arrow);
    for (int i = 0; i < MAX_STEPS && centreOf(arrow).epsilonEquals(landed, 1e-3f); i++) {
      step(); // wait out the hold, until it starts to be reeled in
    }
    assertTrue(isAlive(arrow), "it should be mid-reel");
    assertFalse(centreOf(arrow).epsilonEquals(landed, 1e-3f), "it should have started moving");

    player.getEvents().trigger("grappleRelease");
    step();

    assertFalse(isAlive(arrow));
    assertTrue(grapple.getRopePath().isEmpty());
  }

  @Test
  void shouldGiveUpOnAHookThePlayerOutrunsRatherThanChasingForever() {
    Entity arrow = fireAtTheFloorUntilItLands();

    // Running away much faster than the rope can reel the hook in.
    for (int i = 0; i < MAX_STEPS && isAlive(arrow); i++) {
      player.setPosition(player.getPosition().x - 0.9f, 0f);
      step();
    }

    assertFalse(isAlive(arrow), "it should have been dropped, not left chasing the player");
  }

  @Test
  void shouldLetThePlayerFireAgainOnceTheMissHasFinishedAndTheCooldownEnded() {
    Entity arrow = fireAtTheFloorUntilItLands();
    for (int i = 0; i < MAX_STEPS && isAlive(arrow); i++) {
      step();
    }
    assertFalse(isAlive(arrow));
    for (int i = 0; i < 140; i++) { // past the two second cooldown
      step();
    }
    int before = entities.getEntities().size;

    grapple.fire(new Vector2(-1f, 1f));

    assertEquals(before + 1, entities.getEntities().size, "a fresh arrow should be in flight");
    assertTrue(grapple.getRopePath().size() >= 2);
  }
}
