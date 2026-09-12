package com.csse3200.game.components.player;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleArrowComponent;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleArrowComponentTest {

  private Fixture fixtureOnLayer(short layer) {
    Filter filter = new Filter();
    filter.categoryBits = layer;
    Fixture fixture = mock(Fixture.class);
    when(fixture.getFilterData()).thenReturn(filter);
    when(fixture.getBody()).thenReturn(mock(Body.class));
    return fixture;
  }

  /** An arrow already well clear of the shooter, so the "just left the player" guard is satisfied. */
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
          PhysicsLayer.PLAYER,
          PhysicsLayer.PLAYER_PROJECTILE,
          PhysicsLayer.WALL,
          PhysicsLayer.NPC
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

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));
  }

  @Test
  void shouldNotAttachIfButtonWasReleasedBeforeLanding() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity shooter = new Entity().addComponent(grapple).addComponent(new KeyboardPlayerInputComponent());
    // Right click was never held down (or was already released), so isRightMouseHeld() is false
    Entity arrow = arrowFiredBy(shooter);

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));

    verify(grapple, never()).attachTo(any(Body.class), any(Vector2.class));
  }

  @Test
  void shouldAttachIfButtonIsStillHeldWhenLanding() {
    GrappleComponent grapple = spy(new GrappleComponent());
    KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();
    Entity shooter = new Entity().addComponent(grapple).addComponent(input);
    input.touchDown(0, 0, 0, com.badlogic.gdx.Input.Buttons.RIGHT);
    Entity arrow = arrowFiredBy(shooter);

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));

    verify(grapple).attachTo(any(Body.class), any(Vector2.class));
  }
}
