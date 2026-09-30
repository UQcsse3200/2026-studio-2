package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Exercises movement transitions with real Box2D bodies and controlled time/ground detection. */
@ExtendWith(GameExtension.class)
class PlayerMovementRegressionTest {
  private PhysicsEngine engine;
  private GameTime time;
  private Entity player;
  private PlayerActions actions;
  private Body body;
  private GrappleComponent grapple;
  private KeyboardPlayerInputComponent input;

  @BeforeEach
  void setUp() {
    engine = spy(new PhysicsEngine());
    ServiceLocator.registerPhysicsService(new PhysicsService(engine));
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    setGrounded(false);
    grapple = mock(GrappleComponent.class);
    input = mock(KeyboardPlayerInputComponent.class);
    actions = new PlayerActions();
    PhysicsComponent physics = new PhysicsComponent();
    player =
        new Entity()
            .addComponent(physics)
            .addComponent(actions)
            .addComponent(grapple)
            .addComponent(input);
    player.create();
    body = physics.getBody();
    body.setGravityScale(2.5f);
  }

  @AfterEach
  void tearDown() {
    engine.dispose();
  }

  private void setGrounded(boolean grounded) {
    doReturn(grounded)
        .when(engine)
        .raycast(
            any(Vector2.class), any(Vector2.class), eq(PhysicsLayer.SOLID), any(RaycastHit.class));
  }

  private void advance(float seconds) {
    when(time.getDeltaTime()).thenReturn(seconds);
    actions.update();
  }

  @Test
  void shouldDashInLastFacingDirectionAfterWalkingStops() {
    player.getEvents().trigger("walk", new Vector2(-1f, 0f));
    player.getEvents().trigger("walkStop");
    body.setLinearVelocity(0f, -4f);

    player.getEvents().trigger("dash");

    assertTrue(body.getLinearVelocity().x < 0f);
    assertEquals(0f, body.getLinearVelocity().y);
    assertEquals(0f, body.getGravityScale());
  }

  @Test
  void shouldRestoreOriginalGravityWhenDashExpires() {
    player.getEvents().trigger("dash");
    advance(0.1f);
    assertEquals(0f, body.getGravityScale());

    advance(0.06f);

    assertEquals(2.5f, body.getGravityScale());
  }

  @Test
  void shouldRestoreGravityAndStopReassertingDashVelocityWhenHurt() {
    player.getEvents().trigger("dash");
    player.getEvents().trigger("hurt");
    body.setLinearVelocity(-2f, -3f);

    advance(0.01f);

    assertEquals(2.5f, body.getGravityScale());
    assertEquals(-2f, body.getLinearVelocity().x);
    assertEquals(-3f, body.getLinearVelocity().y);
  }

  @Test
  void shouldRejectGroundDashUntilCooldownExpires() {
    setGrounded(true);
    advance(0f);
    EventListener0 started = mock(EventListener0.class);
    player.getEvents().addListener("dashStart", started);
    player.getEvents().trigger("dash");
    advance(0.2f);

    player.getEvents().trigger("dash");
    verify(started).handle();
    assertEquals(2.5f, body.getGravityScale());

    advance(0.81f);
    player.getEvents().trigger("dash");
    verify(started, times(2)).handle();
    assertEquals(0f, body.getGravityScale());
  }

  @Test
  void shouldNotAllowSecondAirDashEvenAfterCooldownExpires() {
    EventListener0 started = mock(EventListener0.class);
    player.getEvents().addListener("airDashStart", started);
    player.getEvents().trigger("dash");
    advance(1.1f);

    player.getEvents().trigger("dash");

    verify(started).handle();
    assertEquals(2.5f, body.getGravityScale());
  }

  @Test
  void shouldRestoreAirDashAfterLandingAndLeavingGround() {
    EventListener0 started = mock(EventListener0.class);
    player.getEvents().addListener("airDashStart", started);
    player.getEvents().trigger("dash");
    advance(0.2f);
    setGrounded(true);
    advance(0.01f);
    setGrounded(false);
    advance(0.01f);

    player.getEvents().trigger("dash");

    verify(started, times(2)).handle();
    assertEquals(0f, body.getGravityScale());
  }

  @Test
  void shouldRejectDashWhilePausedThenAllowItAfterResume() {
    player.getEvents().trigger("togglePaused");
    player.getEvents().trigger("dash");
    assertEquals(2.5f, body.getGravityScale());
    assertEquals(0f, body.getLinearVelocity().x);

    player.getEvents().trigger("togglePaused");
    player.getEvents().trigger("dash");
    assertEquals(0f, body.getGravityScale());
    assertTrue(body.getLinearVelocity().x > 0f);
  }

  @Test
  void shouldBlockDashWhileAttachedToGrapple() {
    when(grapple.isAttached()).thenReturn(true);
    body.setLinearVelocity(2f, -3f);

    player.getEvents().trigger("dash");

    assertEquals(2.5f, body.getGravityScale());
    assertEquals(2f, body.getLinearVelocity().x);
    assertEquals(-3f, body.getLinearVelocity().y);
    verify(grapple, never()).release();
  }

  @Test
  void shouldReleaseGrappleOnJumpWithUpwardBoostAndPreserveHorizontalMomentum() {
    when(grapple.isAttached()).thenReturn(true);
    body.setLinearVelocity(4f, -2f);

    player.getEvents().trigger("jump");

    verify(grapple).release();
    assertEquals(4f, body.getLinearVelocity().x);
    assertTrue(body.getLinearVelocity().y > 0f);
  }

  @Test
  void shouldKeepGrappleWhileMouseHeldAndReleaseWhenLetGo() {
    when(grapple.isAttached()).thenReturn(true);
    when(input.isRightMouseHeld()).thenReturn(true);
    advance(0.02f);
    verify(grapple, never()).release();

    when(input.isRightMouseHeld()).thenReturn(false);
    advance(0.02f);
    verify(grapple).release();
  }

  @Test
  void shouldEndSprintOnceAfterReleaseGraceExpires() {
    setGrounded(true);
    advance(0f);
    player.getEvents().trigger("walk", new Vector2(1f, 0f));
    player.getEvents().trigger("sprint");
    advance(0.2f);
    EventListener0 ended = mock(EventListener0.class);
    player.getEvents().addListener("sprintEnd", ended);
    player.getEvents().trigger("sprintStop");
    advance(0.05f);
    verifyNoInteractions(ended);
    assertTrue(body.getLinearVelocity().x > 5f);

    advance(0.08f);
    advance(0.2f);
    verify(ended).handle();
    assertEquals(5f, body.getLinearVelocity().x, 0.001f);
  }

  @Test
  void shouldKeepSprintingWhenRepressedDuringReleaseGrace() {
    setGrounded(true);
    advance(0f);
    player.getEvents().trigger("walk", new Vector2(1f, 0f));
    player.getEvents().trigger("sprint");
    advance(0.2f);
    EventListener0 ended = mock(EventListener0.class);
    player.getEvents().addListener("sprintEnd", ended);
    player.getEvents().trigger("sprintStop");
    advance(0.05f);
    player.getEvents().trigger("sprint");

    advance(0.2f);

    verifyNoInteractions(ended);
    assertTrue(body.getLinearVelocity().x > 5f);
  }
}
