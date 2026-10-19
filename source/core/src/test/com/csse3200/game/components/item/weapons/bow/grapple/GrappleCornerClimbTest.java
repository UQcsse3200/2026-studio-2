package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Climbing around terrain with the player's solid collider, gravity and real rope joints. */
@ExtendWith(GameExtension.class)
class GrappleCornerClimbTest {
  private static final float STEP = 0.016f;
  private GameTime time;
  private PhysicsService physics;
  private EntityService entities;
  private Entity player;
  private GrappleComponent grapple;
  private Body playerBody;
  private Body platformBody;
  private Body anchorBody;

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(STEP);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerRenderService(mock(RenderService.class));
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  private Fixture platform(float x, float y, float halfWidth, float halfHeight) {
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

  private void attachOverPlatform(float direction, float width, float height) {
    attachOverPlatform(direction, width, height, 3f);
  }

  private void attachOverPlatform(float direction, float width, float height, float anchorY) {
    attachOverPlatform(
        direction,
        width,
        height,
        anchorY,
        new ColliderComponent().setDensity(1.5f).setLayer(PhysicsLayer.PLAYER));
  }

  private void attachOverPlatform(
      float direction, float width, float height, float anchorY, ColliderComponent collider) {
    platformBody = platform(direction, 0f, 1f, 0.5f).getBody();
    grapple = new GrappleComponent();
    player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(collider)
            .addComponent(grapple);
    player.setScale(width, height);
    player.setPosition(-direction * (width / 2f + 0.025f) - width / 2f, -2f - height / 2f);
    entities.register(player);
    playerBody = player.getComponent(PhysicsComponent.class).getBody();

    // Lay a flight rope over the platform so anchoring preserves its actual routed length.
    grapple.fire(new Vector2(-direction, 1f));
    Entity arrow = entities.getEntities().get(1);
    arrow.setPosition(-direction - 0.3f, 3f - 0.15f);
    grapple.update();
    for (float x = -1f; x <= 4f; x += 0.1f) {
      arrow.setPosition(direction * x - 0.3f, 3f - 0.15f);
      grapple.update();
    }
    for (float y = 3f; y >= anchorY; y -= 0.1f) {
      arrow.setPosition(direction * 4f - 0.3f, y - 0.15f);
      grapple.update();
    }
    BodyDef anchorDef = new BodyDef();
    anchorDef.position.set(direction * 4f, anchorY);
    Body anchor = physics.getPhysics().createBody(anchorDef);
    anchorBody = anchor;
    grapple.attachTo(anchor, anchorDef.position);
    entities.scheduleRemoval(arrow);
    entities.update();
    assertTrue(grapple.isAttached());
    assertTrue(grapple.getRopePath().size() > 2, "rope should start bent over the platform");
    assertTrue(grapple.getRopePath().get(1).y > 0.5f, "nearest bend must be above the platform");
  }

  private void climbPastPlatform(float direction, float width, float height) {
    attachOverPlatform(direction, width, height);
    grapple.startClimbing();
    int frames = (int) Math.ceil(360 * STEP / time.getDeltaTime());
    for (int i = 0; i < frames; i++) {
      Vector2 before = playerBody.getWorldCenter().cpy();
      entities.update();
      physics.getPhysics().update();
      Vector2 center = playerBody.getWorldCenter().cpy();
      assertTrue(
          center.dst(before) < 16f * time.getDeltaTime(),
          "climbing must move through physics, not teleport");
      // Check significant penetration, allowing Box2D's tiny contact tolerance.
      float left = Math.min(0f, direction * 2f);
      float right = Math.max(0f, direction * 2f);
      float overlapX =
          Math.min(center.x + width / 2f, right) - Math.max(center.x - width / 2f, left);
      float overlapY =
          Math.min(center.y + height / 2f, 0.5f) - Math.max(center.y - height / 2f, -0.5f);
      assertFalse(overlapX > 0.03f && overlapY > 0.03f, "player must not pass through terrain");
    }

    Vector2 center = playerBody.getWorldCenter();
    assertTrue(
        direction * center.x > 2.4f,
        "holding W should carry the player over the platform toward the original anchor: "
            + center);
    assertTrue(center.y > 0.5f + height / 2f);
    assertEquals(2, grapple.getRopePath().size(), "passed bends should unwrap");
    assertEquals(1f, grapple.getRopeLength(), 0.05f, "minimum belongs at the final anchor");
  }

  @Test
  void holdingClimbShouldCarryPlayerPastPlatformCornerTowardAnchorOnRight() {
    climbPastPlatform(1f, 0.6f, 1f);
  }

  @Test
  void holdingClimbShouldCarryPlayerPastPlatformCornerTowardAnchorOnLeft() {
    climbPastPlatform(-1f, 0.6f, 1f);
  }

  @Test
  void cornerClimbingShouldAllowClearanceForATallPlayerCollider() {
    climbPastPlatform(1f, 0.8f, 2f);
  }

  @Test
  void cornerClimbingShouldWorkWhenFramesAreShorterThanPhysicsSteps() {
    when(time.getDeltaTime()).thenReturn(STEP / 2f);
    climbPastPlatform(1f, 0.6f, 1f);
  }

  @Test
  void cornerClimbingShouldWorkWhenFramesContainMultiplePhysicsSteps() {
    when(time.getDeltaTime()).thenReturn(STEP * 2f);
    climbPastPlatform(1f, 0.6f, 1f);
  }

  @Test
  void cornerClimbingShouldCompleteAtTwentyFramesPerSecond() {
    when(time.getDeltaTime()).thenReturn(0.05f);
    climbPastPlatform(1f, 0.6f, 1f);
  }

  @Test
  void cornerClimbingShouldCompleteAtTenFramesPerSecond() {
    when(time.getDeltaTime()).thenReturn(0.1f);
    climbPastPlatform(1f, 0.6f, 1f);
  }

  @Test
  void cornerClimbingShouldFollowATranslatingPlatformAndUnwrap() {
    attachOverPlatform(1f, 0.6f, 1f);
    platformBody.setType(BodyDef.BodyType.KinematicBody);
    platformBody.setLinearVelocity(-2f, 0f);
    anchorBody.setType(BodyDef.BodyType.KinematicBody);
    anchorBody.setLinearVelocity(-2f, 0f);
    grapple.startClimbing();
    for (int i = 0; i < 360; i++) {
      if (i == 90) {
        assertEquals(
            2,
            grapple.getRopePath().size(),
            "corner should clear while the platform is still moving");
        platformBody.setLinearVelocity(0f, 0f);
        anchorBody.setLinearVelocity(0f, 0f);
      }
      entities.update();
      physics.getPhysics().update();
    }
    assertEquals(2, grapple.getRopePath().size(), "moving corners must not remain pinned");
    assertTrue(playerBody.getWorldCenter().x > platformBody.getPosition().x + 1.4f);
    assertEquals(1f, grapple.getRopeLength(), 0.05f);
  }

  @Test
  void bendShouldNotMoveThePlayerWithoutClimbInput() {
    attachOverPlatform(1f, 0.6f, 1f);
    player.setPosition(-0.7f, -1.3f);
    playerBody.setGravityScale(0f);
    Vector2 start = playerBody.getWorldCenter().cpy();
    for (int i = 0; i < 30; i++) {
      entities.update();
      physics.getPhysics().update();
    }
    assertTrue(playerBody.getWorldCenter().epsilonEquals(start, 0.001f));
  }

  @Test
  void simultaneousClimbAndDescendShouldNotStartCornerTraversal() {
    attachOverPlatform(1f, 0.6f, 1f);
    player.setPosition(-0.7f, -1.3f);
    playerBody.setGravityScale(0f);
    Vector2 start = playerBody.getWorldCenter().cpy();
    grapple.startClimbing();
    grapple.startDescending();
    for (int i = 0; i < 30; i++) {
      entities.update();
      physics.getPhysics().update();
    }
    assertTrue(playerBody.getWorldCenter().epsilonEquals(start, 0.001f));
  }

  @Test
  void holdingClimbShouldPassSuccessiveBendsOverThePlatform() {
    attachOverPlatform(1f, 0.6f, 1f, -2f);
    assertEquals(4, grapple.getRopePath().size(), "rope should wrap over both top corners");
    grapple.startClimbing();
    for (int i = 0; i < 500; i++) {
      entities.update();
      physics.getPhysics().update();
      Vector2 center = playerBody.getWorldCenter();
      assertFalse(
          center.x > -0.27f && center.x < 2.27f && center.y > -0.97f && center.y < 0.97f,
          "player must not pass through the platform");
    }
    assertTrue(playerBody.getWorldCenter().x > 2.4f);
    assertTrue(playerBody.getWorldCenter().y < -1.5f);
    assertEquals(2, grapple.getRopePath().size());
    assertEquals(1f, grapple.getRopeLength(), 0.05f);
  }

  @Test
  void passedBendShouldUnwrapBeforeThePlayerFinishesClearingTheCorner() {
    attachOverPlatform(1f, 0.6f, 1f, -2f);
    Vector2 passedBend = grapple.getRopePath().get(1).cpy();
    Vector2 nextBend = grapple.getRopePath().get(2).cpy();
    boolean observedCornerClearance = false;
    grapple.startClimbing();
    for (int i = 0; i < 500; i++) {
      entities.update();
      Vector2 center = playerBody.getWorldCenter();
      if (center.x < 0f && center.y > passedBend.y + 0.05f && center.y < 0.95f) {
        observedCornerClearance = true;
        assertTrue(
            grapple.getRopePath().stream().noneMatch(point -> point.epsilonEquals(passedBend)),
            "the rendered rope must not turn backwards through the passed bend");
        assertTrue(
            grapple.getAnchorPoint().epsilonEquals(nextBend),
            "the physical rope must use the same next bend as the rendered rope");
      }
      physics.getPhysics().update();
      center = playerBody.getWorldCenter();
      assertFalse(
          center.x > -0.27f && center.x < 2.27f && center.y > -0.97f && center.y < 0.97f,
          "corner clearance must preserve terrain collisions");
    }
    assertTrue(
        observedCornerClearance, "test must cover the transition before the collider clears");
    assertTrue(
        playerBody.getWorldCenter().x > 2.4f, "climbing must continue after the bend unwraps");
    assertTrue(playerBody.getWorldCenter().y < -1.5f);
    assertEquals(2, grapple.getRopePath().size());
    assertEquals(1f, grapple.getRopeLength(), 0.05f);
  }

  private void climbUntilRopeUnwrapsBeforeColliderClears() {
    grapple.startClimbing();
    for (int i = 0; i < 150; i++) {
      entities.update();
      Vector2 center = playerBody.getWorldCenter();
      if (grapple.getRopePath().size() == 2 && center.x < 0f && center.y < 0.95f) {
        return;
      }
      physics.getPhysics().update();
    }
    fail("rope must unwrap while the independent corner climb is still underway");
  }

  @Test
  void releasingClimbAfterUnwrappingShouldCancelTheIndependentCornerTarget() {
    attachOverPlatform(1f, 0.6f, 1f);
    climbUntilRopeUnwrapsBeforeColliderClears();
    grapple.stopClimbing();
    playerBody.setGravityScale(0f);
    playerBody.setLinearVelocity(0f, 0f);
    Vector2 stopped = playerBody.getWorldCenter().cpy();
    for (int i = 0; i < 20; i++) {
      entities.update();
      physics.getPhysics().update();
    }
    assertTrue(
        playerBody.getWorldCenter().epsilonEquals(stopped, 0.001f),
        "the unwrapped corner must not keep moving the player after W is released");
  }

  @Test
  void removingAnUnwrappedCornerBodyShouldDiscardItsClimbTargetAndKeepTheAnchor() {
    attachOverPlatform(1f, 0.6f, 1f);
    climbUntilRopeUnwrapsBeforeColliderClears();
    physics.getPhysics().destroyBody(platformBody);
    for (int i = 0; i < 300; i++) {
      entities.update();
      physics.getPhysics().update();
    }
    assertTrue(
        grapple.isAttached(), "removing the passed corner must not release the original anchor");
    assertTrue(playerBody.getWorldCenter().x > 2.4f);
    assertEquals(2, grapple.getRopePath().size());
    assertEquals(1f, grapple.getRopeLength(), 0.05f);
  }

  @Test
  void replacementTerrainMustNotInheritTheRemovedCornersClimbTarget() {
    attachOverPlatform(1f, 0.6f, 1f);
    climbUntilRopeUnwrapsBeforeColliderClears();
    physics.getPhysics().destroyBody(platformBody);
    Body replacement = platform(-100f, -100f, 1f, 0.5f).getBody();
    assertSame(platformBody, replacement, "exercise Box2D's pooled body wrapper reuse");
    playerBody.setGravityScale(0f);
    playerBody.setLinearVelocity(0f, 0f);
    float initialX = playerBody.getWorldCenter().x;
    entities.update();
    physics.getPhysics().update();
    assertTrue(
        playerBody.getWorldCenter().x >= initialX - 0.001f,
        "the player must climb toward the original anchor, not the unrelated replacement terrain");
    assertTrue(grapple.isAttached());
    assertEquals(2, grapple.getRopePath().size());
  }

  @Test
  void releasingClimbShouldStopSupportingThePlayerAtTheCorner() {
    attachOverPlatform(1f, 0.6f, 1f);
    grapple.startClimbing();
    for (int i = 0; i < 30; i++) {
      entities.update();
      physics.getPhysics().update();
    }
    assertTrue(playerBody.getLinearVelocity().y > 2f, "corner traversal should be underway");
    grapple.stopClimbing();
    playerBody.setLinearVelocity(0f, 0f);
    float initialHeight = playerBody.getWorldCenter().y;
    entities.update();
    physics.getPhysics().update();
    assertTrue(playerBody.getWorldCenter().y < initialHeight);
    assertTrue(playerBody.getLinearVelocity().y < 0f, "gravity should resume immediately");
  }

  @Test
  void anActualCeilingShouldStillBlockCornerClimbing() {
    attachOverPlatform(1f, 0.6f, 1f);
    platform(-1.5f, 0f, 1.5f, 0.5f);
    grapple.startClimbing();
    for (int i = 0; i < 360; i++) {
      entities.update();
      physics.getPhysics().update();
      assertTrue(
          playerBody.getWorldCenter().y < -0.97f, "climbing must not bypass a solid ceiling");
      assertTrue(grapple.getRopeLength() <= grapple.getInitialRopeLength());
    }
    assertTrue(grapple.isAttached());
  }

  @Test
  void removingOnlyThePassedCornerFixtureShouldDiscardItsClimbTarget() {
    attachOverPlatform(1f, 0.6f, 1f);
    climbUntilRopeUnwrapsBeforeColliderClears();
    platformBody.destroyFixture(platformBody.getFixtureList().first());
    for (int i = 0; i < 300; i++) {
      entities.update();
      physics.getPhysics().update();
    }
    assertTrue(grapple.isAttached());
    assertTrue(playerBody.getWorldCenter().x > 2.4f);
    assertEquals(2, grapple.getRopePath().size());
    assertEquals(1f, grapple.getRopeLength(), 0.05f);
  }

  @Test
  void pausedTimeShouldNotApplyCornerClimbingImpulse() {
    attachOverPlatform(1f, 0.6f, 1f);
    player.setPosition(-0.7f, -1.3f);
    playerBody.setLinearVelocity(0f, 0f);
    grapple.startClimbing();
    when(time.getDeltaTime()).thenReturn(0f);
    Vector2 position = playerBody.getPosition().cpy();
    grapple.update();
    assertEquals(Vector2.Zero, playerBody.getLinearVelocity());
    assertEquals(position, playerBody.getPosition());
    assertTrue(Float.isFinite(grapple.getRopeLength()));
    when(time.getDeltaTime()).thenReturn(STEP);
    grapple.update();
    assertTrue(playerBody.getLinearVelocity().len2() > 0f, "resuming time should resume climbing");
  }

  @Test
  void circularColliderShouldClearTheCornerWhileIgnoringAnOversizedSensor() {
    CircleShape circle = new CircleShape();
    circle.setRadius(0.5f);
    circle.setPosition(new Vector2(0.5f, 0.5f));
    attachOverPlatform(
        1f,
        1f,
        1f,
        3f,
        new ColliderComponent().setShape(circle).setDensity(1.5f).setLayer(PhysicsLayer.PLAYER));
    circle.dispose();
    CircleShape sensor = new CircleShape();
    sensor.setRadius(10f);
    sensor.setPosition(new Vector2(0.5f, 0.5f));
    playerBody.createFixture(sensor, 0f).setSensor(true);
    sensor.dispose();
    grapple.startClimbing();
    for (int i = 0; i < 360; i++) {
      entities.update();
      physics.getPhysics().update();
      Vector2 center = playerBody.getWorldCenter();
      float nearestX = Math.clamp(center.x, 0f, 2f);
      float nearestY = Math.clamp(center.y, -0.5f, 0.5f);
      assertTrue(
          center.dst(nearestX, nearestY) >= 0.47f,
          "the circle must remain outside the platform throughout the climb");
    }
    assertTrue(playerBody.getWorldCenter().x > 2.4f);
    assertTrue(playerBody.getWorldCenter().y > 1f);
    assertEquals(2, grapple.getRopePath().size());
    assertEquals(1f, grapple.getRopeLength(), 0.05f);
  }

  @Test
  void replacingABentPlatformPolygonWithACircleShouldNotApplyPolygonCornerImpulse() {
    attachOverPlatform(1f, 0.6f, 1f);
    Fixture original = platformBody.getFixtureList().first();
    platformBody.destroyFixture(original);
    CircleShape circle = new CircleShape();
    circle.setRadius(0.5f);
    Fixture replacement = platformBody.createFixture(circle, 0f);
    circle.dispose();
    assertSame(original, replacement, "exercise Box2D's pooled fixture wrapper reuse");
    var filter = replacement.getFilterData();
    filter.categoryBits = PhysicsLayer.OBSTACLE;
    replacement.setFilterData(filter);
    playerBody.setLinearVelocity(0f, 0f);
    grapple.startClimbing();
    grapple.update();
    assertEquals(
        Vector2.Zero,
        playerBody.getLinearVelocity(),
        "a circular replacement has no polygon corner to climb around");
    assertTrue(grapple.isAttached());
    assertTrue(Float.isFinite(grapple.getRopeLength()));
  }

  @Test
  void nearlyFlatPolygonCornerShouldNotProduceAnUnboundedClearanceTarget() throws Exception {
    attachOverPlatform(1f, 0.6f, 1f);
    Body body = physics.getPhysics().createBody(new BodyDef());
    PolygonShape polygon = new PolygonShape();
    polygon.set(new float[] {0f, 0f, 10000f, 0f, 20000f, 0.01f, 20000f, 100f, 0f, 100f});
    Fixture fixture = body.createFixture(polygon, 0f);
    polygon.dispose();
    assertEquals(
        5,
        ((PolygonShape) fixture.getShape()).getVertexCount(),
        "the shallow but convex corner must survive Box2D's hull construction");
    GrappleComponent.RopeContact contact =
        new GrappleComponent.RopeContact(fixture, new Vector2(10000f, 0f), 1);
    // Isolate the geometric query with a real convex fixture and contact, without inventing
    // rope history or modifying component state.
    var cornerMethod =
        GrappleComponent.class.getDeclaredMethod(
            "cornerClimbAt", GrappleComponent.RopeContact.class);
    cornerMethod.setAccessible(true);
    Object corner = cornerMethod.invoke(grapple, contact);
    assertNotNull(corner);
    var clearanceMethod =
        GrappleComponent.class.getDeclaredMethod("cornerClearance", corner.getClass());
    clearanceMethod.setAccessible(true);
    assertNull(
        clearanceMethod.invoke(grapple, corner),
        "almost-parallel edge normals must not yield a distant or infinite climb target");
  }

  @Test
  void unsupportedEdgeFixtureShouldNotInflateTheSolidPlayerClearance() throws Exception {
    attachOverPlatform(1f, 0.6f, 1f);
    EdgeShape edge = new EdgeShape();
    edge.set(-20f, 0f, 20f, 0f);
    playerBody.createFixture(edge, 0f);
    edge.dispose();
    var extent = GrappleComponent.class.getDeclaredMethod("playerExtentAlong", Vector2.class);
    extent.setAccessible(true);
    assertEquals(0.3f, (float) extent.invoke(grapple, Vector2.X), 0.0001f);
    assertEquals(0.5f, (float) extent.invoke(grapple, Vector2.Y), 0.0001f);
  }
}
