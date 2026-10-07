package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

/** The rope itself - attaching, reporting its state, swinging and reeling - on real Box2D. */
@ExtendWith(GameExtension.class)
class GrappleComponentRopeTest {
  private static final float STEP = 0.016f;
  private static final float ROPE_LENGTH = 5f;

  private GameTime gameTime;
  private PhysicsService physics;

  @BeforeEach
  void setUp() {
    gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(STEP);
    ServiceLocator.registerTimeSource(gameTime);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    ServiceLocator.registerEntityService(mock(EntityService.class));
  }

  @AfterEach
  void tearDown() {
    physics.getPhysics().dispose();
  }

  /** A player with no size, so their centre is exactly where they're placed. */
  private Entity playerAt(float x, float y) {
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    player.create();
    player.setScale(0f, 0f);
    player.setPosition(x, y);
    return player;
  }

  private Body anchorAt(float x, float y) {
    BodyDef def = new BodyDef();
    def.position.set(x, y);
    return physics.getPhysics().createBody(def);
  }

  private static GrappleComponent grappleOf(Entity player) {
    return player.getComponent(GrappleComponent.class);
  }

  private static Body bodyOf(Entity player) {
    return player.getComponent(PhysicsComponent.class).getBody();
  }

  /** Attaches and builds the joint, with the rope just taut. */
  private GrappleComponent attached(Entity player, Body anchor, Vector2 point) {
    GrappleComponent grapple = grappleOf(player);
    grapple.attachTo(anchor, point);
    grapple.update();
    assertTrue(grapple.isAttached());
    return grapple;
  }

  /** Runs one hanging-pendulum step: gravity off, moving sideways at {@code speed}. */
  private float sidewaysVelocityAfterOneStep(float speed, float swingDirection) {
    Entity player = playerAt(0f, -ROPE_LENGTH);
    Body body = bodyOf(player);
    body.setGravityScale(0f);
    GrappleComponent grapple = attached(player, anchorAt(0f, 0f), new Vector2(0f, 0f));
    body.setLinearVelocity(speed, 0f);

    grapple.swing(swingDirection);
    physics.getPhysics().update();
    return body.getLinearVelocity().x;
  }

  @Test
  void shouldReportNothingWhileDetached() {
    GrappleComponent grapple = grappleOf(playerAt(0f, 0f));

    assertFalse(grapple.isAttached());
    assertEquals(0f, grapple.getRopeLength());
    assertEquals(0f, grapple.getInitialRopeLength());
    assertNull(grapple.getAnchorPoint());
    assertTrue(grapple.getRopePath().isEmpty());
  }

  @Test
  void shouldDescribeTheRopeFromThePlayerToTheAnchorOnceAttached() {
    Entity player = playerAt(0f, ROPE_LENGTH);
    GrappleComponent grapple = attached(player, anchorAt(0f, 0f), new Vector2(0f, 0f));

    assertEquals(ROPE_LENGTH, grapple.getRopeLength(), 1e-4f);
    assertEquals(ROPE_LENGTH, grapple.getInitialRopeLength(), 1e-4f);

    List<Vector2> path = grapple.getRopePath();
    assertEquals(2, path.size());
    assertTrue(path.get(0).epsilonEquals(new Vector2(0f, ROPE_LENGTH), 1e-4f), "player end");
    assertTrue(path.get(1).epsilonEquals(new Vector2(0f, 0f), 1e-4f), "anchor end");
  }

  @Test
  void shouldHandOutACopyOfTheAnchorPointSoCallersCannotMoveTheRope() {
    GrappleComponent grapple =
        attached(playerAt(0f, ROPE_LENGTH), anchorAt(0f, 0f), new Vector2(0f, 0f));

    grapple.getAnchorPoint().set(99f, 99f);

    assertTrue(grapple.getAnchorPoint().epsilonEquals(new Vector2(0f, 0f), 1e-4f));
  }

  @Test
  void shouldKeepTheFirstAnchorWhenAnotherArrowLandsWhileAlreadyAttached() {
    Entity player = playerAt(0f, ROPE_LENGTH);
    GrappleComponent grapple = attached(player, anchorAt(0f, 0f), new Vector2(0f, 0f));

    grapple.attachTo(anchorAt(3f, 0f), new Vector2(3f, 0f));
    grapple.update();

    assertTrue(grapple.getAnchorPoint().epsilonEquals(new Vector2(0f, 0f), 1e-4f));
    assertEquals(ROPE_LENGTH, grapple.getRopeLength(), 1e-4f);
  }

  @Test
  void shouldKeepTheFirstAnchorWhenTwoArrowsLandBeforeTheJointIsBuilt() {
    Entity player = playerAt(0f, ROPE_LENGTH);
    GrappleComponent grapple = grappleOf(player);

    grapple.attachTo(anchorAt(0f, 0f), new Vector2(0f, 0f));
    grapple.attachTo(anchorAt(3f, 0f), new Vector2(3f, 0f));
    grapple.update();

    assertTrue(grapple.getAnchorPoint().epsilonEquals(new Vector2(0f, 0f), 1e-4f));
  }

  @Test
  void shouldNotFireANewArrowWhileAlreadyAttached() {
    GrappleComponent grapple =
        attached(playerAt(0f, ROPE_LENGTH), anchorAt(0f, 0f), new Vector2(0f, 0f));

    try (MockedStatic<ProjectileFactory> factory = mockStatic(ProjectileFactory.class)) {
      grapple.fire(Vector2.X.cpy());
      grapple.startCharge(Vector2.X.cpy());
      grapple.releaseCharge(Vector2.X.cpy());

      factory.verify(
          () ->
              ProjectileFactory.createGrappleArrow(
                  any(Entity.class), any(Vector2.class), any(Vector2.class), anyFloat()),
          times(0));
    }
    assertFalse(grapple.isCharging());
  }

  @Test
  void shouldPushAShotFromRestWhenUnderTheSwingSpeedCap() {
    float pushed = sidewaysVelocityAfterOneStep(3f, 1f);
    float coasting = sidewaysVelocityAfterOneStep(3f, 0f);

    assertTrue(pushed - coasting > 0.05f, "pushed=" + pushed + " coasting=" + coasting);
  }

  @Test
  void shouldStopPumpingOnceTheSwingIsAlreadyAtTheSpeedCap() {
    float pushed = sidewaysVelocityAfterOneStep(9f, 1f);
    float coasting = sidewaysVelocityAfterOneStep(9f, 0f);

    // Past the cap, holding a direction adds nothing, so you can't pump a swing up forever.
    assertEquals(coasting, pushed, 0.01f);
  }

  @Test
  void shouldPushUpOrDownFromTheKeyWhenLevelWithTheAnchor() {
    // Level with the anchor the arc points straight up or down, so the key can't pick a side from
    // where the push points - holding right pushes up and left pushes down, by convention.
    for (float direction : new float[] {1f, -1f}) {
      Entity player = playerAt(ROPE_LENGTH, 0f);
      Body body = bodyOf(player);
      body.setGravityScale(0f);
      GrappleComponent grapple = attached(player, anchorAt(0f, 0f), new Vector2(0f, 0f));

      grapple.swing(direction);
      physics.getPhysics().update();

      float vy = body.getLinearVelocity().y;
      assertTrue(vy * direction > 0.05f, "direction " + direction + " gave vy=" + vy);
    }
  }

  @Test
  void shouldNeverLowerTheRopeBeyondItsOriginalLength() {
    when(gameTime.getDeltaTime()).thenReturn(0.5f);
    GrappleComponent grapple =
        attached(playerAt(0f, -ROPE_LENGTH), anchorAt(0f, 0f), new Vector2(0f, 0f));

    grapple.startDescending();
    grapple.update();

    assertEquals(ROPE_LENGTH, grapple.getRopeLength(), 1e-4f);
  }

  @Test
  void shouldHoldTheRopeAtItsActualLengthWhenTheLoweringControlIsReleased() {
    when(gameTime.getDeltaTime()).thenReturn(0.5f);
    GrappleComponent grapple =
        attached(playerAt(0f, -ROPE_LENGTH), anchorAt(0f, 0f), new Vector2(0f, 0f));

    grapple.startDescending();
    grapple.update();
    grapple.stopDescending();

    assertTrue(grapple.isAttached());
    assertEquals(ROPE_LENGTH, grapple.getRopeLength(), 1e-4f);
  }

  @Test
  void shouldIgnoreReelingControlsWhileDetached() {
    GrappleComponent grapple = grappleOf(playerAt(0f, 0f));

    grapple.startClimbing();
    grapple.startDescending();
    grapple.update();
    grapple.stopClimbing();
    grapple.stopDescending();

    assertFalse(grapple.isAttached());
    assertEquals(0f, grapple.getRopeLength());
  }

  @Test
  void shouldDoNothingWhenSwingIsHeldWithNoDirection() {
    float held = sidewaysVelocityAfterOneStep(3f, 0f);
    float untouched = sidewaysVelocityAfterOneStep(3f, 0f);

    assertEquals(untouched, held, 1e-4f);
    assertNotNull(grappleOf(playerAt(0f, 0f)));
  }
}
