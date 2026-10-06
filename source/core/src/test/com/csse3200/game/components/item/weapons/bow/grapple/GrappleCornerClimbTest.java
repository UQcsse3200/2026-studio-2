package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
    platformBody = platform(direction, 0f, 1f, 0.5f).getBody();
    grapple = new GrappleComponent();
    player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setDensity(1.5f).setLayer(PhysicsLayer.PLAYER))
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
}
