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

  private Entity arrowFiredBy(Entity shooter) {
    Entity arrow = new Entity().addComponent(new GrappleArrowComponent(shooter));
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
  void shouldIgnoreNonTerrainHits() {
    GrappleComponent grapple = spy(new GrappleComponent());
    Entity arrow = arrowFiredBy(new Entity().addComponent(grapple));

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.NPC));

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
  void shouldNotCrashWhenShooterHasNoGrapple() {
    Entity arrow = arrowFiredBy(new Entity());

    arrow
        .getEvents()
        .trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(PhysicsLayer.GROUND));
  }
}
