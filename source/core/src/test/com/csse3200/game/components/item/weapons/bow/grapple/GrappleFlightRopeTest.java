package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleFlightRopeTest {
  private PhysicsService physics;
  private EntityService entities;
  private Entity player;
  private GrappleComponent grapple;

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.016f);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    grapple = new GrappleComponent();
    player = new Entity().addComponent(new PhysicsComponent()).addComponent(grapple);
    player.setScale(1f, 1f);
    player.setPosition(-0.5f, -0.5f);
    entities.register(player);
    player.getComponent(PhysicsComponent.class).getBody().setGravityScale(0f);
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  private Entity fire() {
    grapple.fire(new Vector2(1f, 1f));
    return entities.getEntities().get(1);
  }

  private Vector2 arrowCenter(Entity arrow) {
    return arrow.getComponent(ArrowProjectileComponent.class).getWorldCenter();
  }

  private Fixture box(float x, float y, float halfWidth, float halfHeight) {
    BodyDef def = new BodyDef();
    def.position.set(x, y);
    Body body = physics.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(halfWidth, halfHeight);
    Fixture fixture = body.createFixture(shape, 0f);
    shape.dispose();
    var filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.OBSTACLE;
    fixture.setFilterData(filter);
    return fixture;
  }

  private void moveArrow(Entity arrow, float x, float y) {
    // Control only the arrow endpoint so wrapping history can be tested independently of aim.
    arrow.setPosition(x - 0.3f, y - 0.15f);
    grapple.update();
  }

  private Entity curveOverObstacle() {
    box(5f, 0f, 1f, 2f);
    Entity arrow = fire();
    moveArrow(arrow, 3f, 4f);
    moveArrow(arrow, 6.5f, 4f);
    moveArrow(arrow, 8f, 3f);
    moveArrow(arrow, 8f, 1f);
    moveArrow(arrow, 8f, -4f);
    return arrow;
  }

  @Test
  void ropeShouldExistAsSoonAsTheArrowFiresAndFollowItsLiveCenter() {
    Entity arrow = fire();
    assertEquals(2, grapple.getRopePath().size(), "rope should be visible on the launch frame");
    assertEquals(arrowCenter(arrow), grapple.getRopePath().getLast());
    assertFalse(grapple.isAttached(), "flying rope must not put the player into swing mode");
    assertEquals(0, physics.getPhysics().getWorld().getJointCount());
    moveArrow(arrow, 3f, 4f);
    assertEquals(arrowCenter(arrow), grapple.getRopePath().getLast());
    assertEquals(2f, player.getComponent(PhysicsComponent.class).getBody().getLinearDamping());
    grapple.getRopePath().getLast().setZero();
    assertEquals(new Vector2(3f, 4f), grapple.getRopePath().getLast());
  }

  @Test
  void realGravityArcShouldLeaveTheAnchoredRopeOverTheObstacle() {
    box(5f, 0f, 1f, 2f);
    box(30.5f, 0f, 0.5f, 20f);
    Entity arrow = fire();
    boolean bentDuringFlight = false;
    float peakY = arrowCenter(arrow).y;
    for (int i = 0; i < 200 && !grapple.isAttached(); i++) {
      physics.getPhysics().update();
      if (entities.getEntities().contains(arrow, true)) {
        peakY = Math.max(peakY, arrowCenter(arrow).y);
      }
      entities.update();
      if (!grapple.isAttached() && grapple.getRopePath().size() > 2) {
        bentDuringFlight = true;
      }
    }

    assertTrue(peakY > 5f, "arrow should arc over the obstacle under real gravity");
    assertTrue(bentDuringFlight, "bends must form while the arrow is flying");
    assertTrue(grapple.isAttached(), "arrow should eventually anchor on the far wall");
    List<Vector2> path = grapple.getRopePath();
    assertEquals(4, path.size(), "rope should retain both top corners: " + path);
    assertTrue(path.get(1).x < 4f && path.get(1).y > 2f);
    assertTrue(path.get(2).x > 6f && path.get(2).y > 2f);
    assertEquals(30f, path.getLast().x, 0.05f);
    assertTrue(path.getLast().y < -4f, "arrow should land below the obstacle");
    float routedLength = 0f;
    for (int i = 1; i < path.size(); i++) {
      routedLength += path.get(i - 1).dst(path.get(i));
    }
    assertEquals(routedLength, grapple.getInitialRopeLength(), 0.001f);
  }

  @Test
  void cancelledArrowShouldNotAttachEvenIfItsCollisionArrivesBeforeDisposal() {
    Entity arrow = fire();
    moveArrow(arrow, 8f, -4f);
    Fixture target = box(8.5f, -4f, 0.5f, 1f);
    grapple.release();
    arrow
        .getEvents()
        .trigger("collisionStart", arrow.getComponent(HitboxComponent.class).getFixture(), target);
    entities.update();
    assertFalse(grapple.isAttached(), "cancelled shot must not attach a new rope");
    assertTrue(grapple.getRopePath().isEmpty());
  }

  @Test
  void flyingRopeShouldRememberGoingOverAnObstacleEvenWhenBottomRouteIsShorter() {
    Entity arrow = curveOverObstacle();
    List<Vector2> path = grapple.getRopePath();
    assertEquals(4, path.size(), "both top corners should remain in the path");
    assertTrue(path.get(1).x < 4f && path.get(1).y > 2f, "player-side top corner");
    assertTrue(path.get(2).x > 6f && path.get(2).y > 2f, "arrow-side top corner");
    assertEquals(arrowCenter(arrow), path.getLast());
    assertFalse(grapple.isAttached());
    assertEquals(0, physics.getPhysics().getWorld().getJointCount());
  }

  @Test
  void impactShouldPreserveFlightBendsAndUseTheWholePathAsRopeLength() {
    Entity arrow = curveOverObstacle();
    List<Vector2> flightPath = grapple.getRopePath();
    assertEquals(4, flightPath.size());
    Fixture target = box(8.5f, -4f, 0.5f, 1f);
    arrow
        .getEvents()
        .trigger("collisionStart", arrow.getComponent(HitboxComponent.class).getFixture(), target);
    entities.update();

    assertTrue(grapple.isAttached());
    List<Vector2> anchoredPath = grapple.getRopePath();
    assertEquals(flightPath, anchoredPath, "anchoring on x=8 should preserve the flight path");
    float pathLength = 0f;
    for (int i = 1; i < anchoredPath.size(); i++) {
      pathLength += anchoredPath.get(i - 1).dst(anchoredPath.get(i));
    }
    assertEquals(pathLength, grapple.getRopeLength(), 0.001f);
    assertTrue(grapple.getRopeLength() > new Vector2(8f, -4f).len() + 2f);
    assertEquals(1, physics.getPhysics().getWorld().getJointCount());
    assertEquals(anchoredPath.get(1), grapple.getAnchorPoint(), "swing pivot is nearest bend");
  }

  @Test
  void releaseShouldCancelFlyingArrowAndItsRope() {
    Entity arrow = curveOverObstacle();
    grapple.release();
    entities.update();
    assertTrue(grapple.getRopePath().isEmpty());
    assertFalse(entities.getEntities().contains(arrow, true));
    assertFalse(grapple.isAttached());
    assertEquals(0, physics.getPhysics().getWorld().getJointCount());
  }

  @Test
  void releaseShouldCancelImpactQueuedDuringThePhysicsStep() {
    Entity arrow = fire();
    moveArrow(arrow, 8f, -4f);
    Fixture target = box(8.5f, -4f, 0.5f, 1f);
    arrow
        .getEvents()
        .trigger("collisionStart", arrow.getComponent(HitboxComponent.class).getFixture(), target);
    grapple.release();
    entities.update();
    assertFalse(grapple.isAttached());
    assertTrue(grapple.getRopePath().isEmpty());
    assertEquals(0, physics.getPhysics().getWorld().getJointCount());
  }

  @Test
  void clearChordShouldNotCutAcrossTheInsideOfAnExistingWrapAroundADiamond() {
    BodyDef def = new BodyDef();
    def.position.set(5f, 0f);
    Body obstacle = physics.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.set(
        new Vector2[] {
          new Vector2(-1f, 0f), new Vector2(0f, -2f),
          new Vector2(1f, 0f), new Vector2(0f, 2f)
        });
    Fixture fixture = obstacle.createFixture(shape, 0f);
    shape.dispose();
    var filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.OBSTACLE;
    fixture.setFilterData(filter);

    Entity arrow = fire();
    moveArrow(arrow, 8f, 4f);
    moveArrow(arrow, 8f, 3f);
    moveArrow(arrow, 8f, 1f);
    assertTrue(
        grapple.getRopePath().stream().anyMatch(point -> point.y > 2f && point.x < 6f),
        "rope should wrap over the top apex before the endpoints descend");
    player.setPosition(-0.5f, -4.5f);
    moveArrow(arrow, 8f, -4f);

    List<Vector2> path = grapple.getRopePath();
    assertTrue(path.size() > 2, "a clear chord below the diamond must not cut through its wrap");
    assertTrue(
        path.stream().anyMatch(point -> point.y > 2f), "rope should stay over the apex: " + path);
  }

  @Test
  void flyingRopeShouldUnwrapWhenTheArrowReturnsAboveTheObstacle() {
    Entity arrow = curveOverObstacle();
    assertEquals(4, grapple.getRopePath().size());
    moveArrow(arrow, 8f, 5f);
    moveArrow(arrow, 3f, 5f);
    assertEquals(2, grapple.getRopePath().size(), "clear return path should remove old bends");
    assertEquals(arrowCenter(arrow), grapple.getRopePath().getLast());
  }

  @Test
  void replacingAMissedShotShouldRetireItsArrowWithoutClearingTheNewRope() {
    Entity oldArrow = curveOverObstacle();
    for (int i = 0; i < 126; i++) {
      grapple.update();
    }
    assertFalse(grapple.isOnCooldown());
    grapple.fire(Vector2.Y);
    Entity newArrow = entities.getEntities().get(2);
    Fixture target = box(8.5f, -4f, 0.5f, 1f);
    oldArrow
        .getEvents()
        .trigger(
            "collisionStart", oldArrow.getComponent(HitboxComponent.class).getFixture(), target);
    entities.update();
    assertFalse(entities.getEntities().contains(oldArrow, true));
    assertFalse(grapple.isAttached(), "old arrow must not anchor the replacement rope");
    assertEquals(2, grapple.getRopePath().size(), "replacement should start without old bends");
    assertEquals(arrowCenter(newArrow), grapple.getRopePath().getLast());
  }

  @Test
  void expiredArrowShouldRemoveItsFlyingRopeAndLeaveThePlayerFree() {
    Entity arrow = curveOverObstacle();
    assertEquals(4, grapple.getRopePath().size(), "expiry should clear an existing bent rope");
    moveArrow(arrow, 60f, 0f);
    entities.update();
    assertFalse(entities.getEntities().contains(arrow, true));
    assertTrue(grapple.getRopePath().isEmpty());
    assertFalse(grapple.isAttached());
    assertEquals(2f, player.getComponent(PhysicsComponent.class).getBody().getLinearDamping());
  }

  @Test
  void respawnShouldCancelFlightAndClearAllBends() {
    Entity arrow = curveOverObstacle();
    player.getEvents().trigger("respawnAtCheckpoint");
    entities.update();
    assertFalse(entities.getEntities().contains(arrow, true));
    assertTrue(grapple.getRopePath().isEmpty());
    assertFalse(grapple.isOnCooldown());
    fire();
    assertEquals(2, grapple.getRopePath().size(), "next shot should start without old bends");
  }
}
