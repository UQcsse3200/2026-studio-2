package com.csse3200.game.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.csse3200.game.components.level.LedgeComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Covers the S-key route through player actions into one-way platform contact handling. */
@ExtendWith(GameExtension.class)
class PlayerLedgeDropIntegrationTest {
  private PhysicsEngine engine;
  private KeyboardPlayerInputComponent input;
  private PlayerActions actions;
  private Fixture playerFixture;
  private Fixture ledgeFixture;
  private PhysicsContactListener listener;

  @BeforeEach
  void setUp() {
    engine = new PhysicsEngine();
    ServiceLocator.registerPhysicsService(new PhysicsService(engine));
    ServiceLocator.registerTimeSource(mock(GameTime.class));
    ServiceLocator.registerInputService(mock(InputService.class));
    input = new KeyboardPlayerInputComponent();
    actions = new PlayerActions();
    Entity player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(actions)
            .addComponent(input);
    player.create();
    playerFixture = player.getComponent(ColliderComponent.class).getFixture();
    ledgeFixture = createPlatform(true);
    listener = new PhysicsContactListener();
  }

  @AfterEach
  void tearDown() {
    engine.dispose();
  }

  private Fixture createPlatform(boolean ledge) {
    Entity platform =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new ColliderComponent());
    if (ledge) {
      platform.addComponent(new LedgeComponent());
    }
    platform.create();
    return platform.getComponent(ColliderComponent.class).getFixture();
  }

  private Contact contact(Fixture first, Fixture second) {
    Contact contact = mock(Contact.class);
    when(contact.getFixtureA()).thenReturn(first);
    when(contact.getFixtureB()).thenReturn(second);
    return contact;
  }

  @Test
  void shouldWakeSleepingPlayerWhenSRequestsLedgeDrop() {
    playerFixture.getBody().setAwake(false);

    input.keyDown(Keys.S);

    assertTrue(actions.droppingFromLedge);
    assertTrue(playerFixture.getBody().isAwake());
  }

  @Test
  void shouldDisableLedgeContactAfterSPressWithPlayerFixtureFirst() {
    assertDropDisablesContact(false);
  }

  @Test
  void shouldDisableLedgeContactAfterSPressWithLedgeFixtureFirst() {
    assertDropDisablesContact(true);
  }

  private void assertDropDisablesContact(boolean ledgeFirst) {
    Contact contact =
        ledgeFirst ? contact(ledgeFixture, playerFixture) : contact(playerFixture, ledgeFixture);
    playerFixture.getBody().setLinearVelocity(0f, -1f);

    input.keyDown(Keys.S);
    listener.preSolve(contact, mock(Manifold.class));

    verify(contact).setEnabled(false);
  }

  @Test
  void shouldContinueDropAfterKeyReleaseUntilContactEnds() {
    Contact contact = contact(playerFixture, ledgeFixture);
    input.keyDown(Keys.S);
    input.keyUp(Keys.S);

    listener.preSolve(contact, mock(Manifold.class));

    assertTrue(actions.droppingFromLedge);
    verify(contact).setEnabled(false);
  }

  @Test
  void shouldRestoreCollisionForNextLedgeAfterCompletingDrop() {
    Contact firstContact = contact(playerFixture, ledgeFixture);
    input.keyDown(Keys.S);
    listener.preSolve(firstContact, mock(Manifold.class));
    verify(firstContact).setEnabled(false);

    listener.endContact(firstContact);

    assertFalse(actions.droppingFromLedge);
    Contact nextContact = contact(playerFixture, createPlatform(true));
    playerFixture.getBody().setLinearVelocity(0f, -1f);
    listener.preSolve(nextContact, mock(Manifold.class));
    verify(nextContact, never()).setEnabled(false);
  }

  @Test
  void shouldNotCancelLedgeDropWhenUnrelatedContactEnds() {
    Contact ledgeContact = contact(playerFixture, ledgeFixture);
    input.keyDown(Keys.S);
    listener.preSolve(ledgeContact, mock(Manifold.class));

    listener.endContact(contact(playerFixture, createPlatform(false)));

    assertTrue(actions.droppingFromLedge);
    listener.endContact(ledgeContact);
    assertFalse(actions.droppingFromLedge);
  }

  @Test
  void shouldNotDisableSolidPlatformCollisionWhenSIsPressed() {
    Contact solidContact = contact(playerFixture, createPlatform(false));
    input.keyDown(Keys.S);

    listener.preSolve(solidContact, mock(Manifold.class));

    verify(solidContact, never()).setEnabled(false);
  }
}
