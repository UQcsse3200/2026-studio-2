package com.csse3200.game.physics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.level.LedgeComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhysicsContactResolutionTest {
  @Test
  void absentPlayerActionsLeaveDescendingLedgeContactEnabled() throws Exception {
    var resolve =
        PhysicsContactListener.class.getDeclaredMethod(
            "resolveLedgeContact",
            com.badlogic.gdx.physics.box2d.Contact.class,
            com.csse3200.game.components.player.PlayerActions.class,
            com.badlogic.gdx.physics.box2d.Body.class,
            com.badlogic.gdx.physics.box2d.Fixture.class);
    resolve.setAccessible(true);
    var body = mock(com.badlogic.gdx.physics.box2d.Body.class);
    when(body.getLinearVelocity()).thenReturn(new Vector2(0f, -1f));
    var contact = mock(com.badlogic.gdx.physics.box2d.Contact.class);
    var ledge = mock(com.badlogic.gdx.physics.box2d.Fixture.class);
    resolve.invoke(new PhysicsContactListener(), contact, null, body, ledge);
    verify(contact, never()).setEnabled(false);
  }

  @Test
  void correctsFallingPlayerAtEitherPlatformEdge() {
    assertCorrection(-1f, -0.9f);
    assertCorrection(1f, 0.9f);
  }

  private void assertCorrection(float playerX, float correctedX) {
    Entity player = player();
    Entity ground = new Entity().addComponent(new ColliderComponent());
    List<Vector2> requests = new ArrayList<>();
    player.getEvents().<Vector2>addListener("physicsPositionRequest", requests::add);
    Contact contact = contact(fixture(player, playerX, 0.5f, -1f), fixture(ground, 0f, 0f, 0f));

    new PhysicsContactListener().preSolve(contact, null);

    assertEquals(1, requests.size());
    assertEquals(correctedX, requests.getFirst().x, 0.001f);
    assertEquals(1.1f, requests.getFirst().y, 0.001f);
  }

  @Test
  void dropThroughWorksWithPlayerAsSecondFixtureAndClearsAtContactEnd() {
    Entity player = player();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    actions.droppingFromLedge = true;
    Entity ledge =
        new Entity().addComponent(new ColliderComponent()).addComponent(new LedgeComponent());
    Contact contact = contact(fixture(ledge, 0f, 0f, 0f), fixture(player, 0f, 0f, -1f));
    PhysicsContactListener listener = new PhysicsContactListener();

    listener.preSolve(contact, null);
    verify(contact).setEnabled(false);
    assertTrue(actions.droppingFromLedge);
    listener.endContact(contact);
    assertFalse(actions.droppingFromLedge);
  }

  @Test
  void solidPairDoesNotApplyPlatformCorrection() {
    Entity player = player();
    player.getComponent(ColliderComponent.class).setLayer(PhysicsLayer.SOLID);
    Entity ground = new Entity().addComponent(new ColliderComponent().setLayer(PhysicsLayer.SOLID));
    List<Vector2> requests = new ArrayList<>();
    player.getEvents().<Vector2>addListener("physicsPositionRequest", requests::add);
    Contact contact = contact(fixture(player, -1f, 0.5f, -1f), fixture(ground, 0f, 0f, 0f));

    new PhysicsContactListener().preSolve(contact, null);

    assertTrue(requests.isEmpty());
    verify(contact, never()).setEnabled(false);
  }

  @Test
  void invalidHeightOrVelocityDoesNotRequestPositionCorrection() {
    assertNoCorrection(Float.NaN, -1f);
    assertNoCorrection(0.5f, Float.NaN);
  }

  private void assertNoCorrection(float playerY, float velocityY) {
    Entity player = player();
    Entity ground = new Entity().addComponent(new ColliderComponent());
    List<Vector2> requests = new ArrayList<>();
    player.getEvents().<Vector2>addListener("physicsPositionRequest", requests::add);
    Contact contact =
        contact(fixture(player, -1f, playerY, velocityY), fixture(ground, 0f, 0f, 0f));

    new PhysicsContactListener().preSolve(contact, null);

    assertTrue(requests.isEmpty());
  }

  @Test
  void ignoresContactsMissingEitherCollider() {
    Entity player = player();
    Entity withoutCollider = new Entity();
    Fixture playerFixture = fixture(player, -1f, 0.5f, -1f);
    Fixture otherFixture = fixture(withoutCollider, 0f, 0f, 0f);
    List<Vector2> requests = new ArrayList<>();
    player.getEvents().<Vector2>addListener("physicsPositionRequest", requests::add);
    Contact firstMissing = contact(otherFixture, playerFixture);
    Contact secondMissing = contact(playerFixture, otherFixture);
    PhysicsContactListener listener = new PhysicsContactListener();

    listener.preSolve(firstMissing, null);
    listener.preSolve(secondMissing, null);

    assertTrue(requests.isEmpty());
    verify(firstMissing, never()).setEnabled(false);
    verify(secondMissing, never()).setEnabled(false);
  }

  @Test
  void correctsPlayerWhenSolidGroundIsFirstFixture() {
    Entity player = player();
    player.getComponent(ColliderComponent.class).setLayer(PhysicsLayer.PLAYER);
    Entity ground =
        new Entity().addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND));
    List<Vector2> requests = new ArrayList<>();
    player.getEvents().<Vector2>addListener("physicsPositionRequest", requests::add);
    Contact contact = contact(fixture(ground, 0f, 0f, 0f), fixture(player, -1f, 0.5f, -1f));

    new PhysicsContactListener().preSolve(contact, null);

    assertEquals(1, requests.size());
    assertEquals(-0.9f, requests.getFirst().x, 0.001f);
  }

  @Test
  void doesNotLiftPlayerWhenTooFarBelowPlatformTop() {
    assertNoCorrection(-2f, -1f);
  }

  private Entity player() {
    return new Entity().addComponent(new ColliderComponent()).addComponent(new PlayerActions());
  }

  private Fixture fixture(Entity entity, float x, float y, float velocityY) {
    Body body = mock(Body.class);
    BodyUserData data = new BodyUserData();
    data.entity = entity;
    when(body.getUserData()).thenReturn(data);
    when(body.getPosition()).thenReturn(new Vector2(x, y));
    when(body.getLinearVelocity()).thenReturn(new Vector2(0f, velocityY));
    Fixture fixture = mock(Fixture.class);
    when(fixture.getBody()).thenReturn(body);
    return fixture;
  }

  private Contact contact(Fixture first, Fixture second) {
    Contact contact = mock(Contact.class);
    when(contact.getFixtureA()).thenReturn(first);
    when(contact.getFixtureB()).thenReturn(second);
    return contact;
  }
}
