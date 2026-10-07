package com.csse3200.game.components.player;

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
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
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
    when(fixture.getBody()).thenReturn(mock(Body.class));
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

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));
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
}
