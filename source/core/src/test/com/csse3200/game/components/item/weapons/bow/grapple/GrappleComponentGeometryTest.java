package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.extensions.GameExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleComponentGeometryTest {

  @Test
  void shouldDiscoverPlatformEdgesFromAnchorTowardPlayer() {
    World world = new World(Vector2.Zero, true);
    try {
      Fixture wall = rectangle(world, new Vector2(5f, 0f), 2f, 4f, 0f);

      List<GrappleComponent.RopeContact> contacts =
          GrappleComponent.traceContacts(world, new Vector2(10f, 3f), new Vector2(0f, 0f));

      assertEquals(2, contacts.size());
      // Walking down from the anchor discovers the far edge before the player-side edge.
      assertTrue(contacts.get(0).getWorldPoint().x > 6f);
      assertTrue(contacts.get(1).getWorldPoint().x < 4f);
      assertTrue(contacts.get(0).getWorldPoint().y > 2f);
      assertTrue(contacts.get(1).getWorldPoint().y > 2f);
    } finally {
      world.dispose();
    }
  }

  @Test
  void shouldTransformRotatedFixtureVerticesIntoWorldSpace() {
    World world = new World(Vector2.Zero, true);
    try {
      Fixture wall = rectangle(world, new Vector2(3f, 4f), 2f, 4f, 90f);

      List<GrappleComponent.RopeContact> contacts =
          GrappleComponent.traceContacts(world, new Vector2(8f, 8f), new Vector2(-2f, 0f));

      assertTrue(!contacts.isEmpty());
      // Contacts are calculated from transformed vertices, not unrotated local coordinates.
      assertTrue(contacts.get(0).getWorldPoint().y > 5f);
    } finally {
      world.dispose();
    }
  }

  @Test
  void shouldKeepFindingMovingPlatformBehindNearerWall() {
    World world = new World(Vector2.Zero, true);
    try {
      rectangle(world, new Vector2(3f, 0f), 1f, 2f, 0f);
      Fixture movingPlatform = rectangle(world, new Vector2(6f, 0f), 2f, 2f, 0f);
      Vector2 player = new Vector2(0f, 0f);
      Vector2 anchor = new Vector2(10f, 0f);

      List<GrappleComponent.RopeContact> contacts =
          GrappleComponent.traceContacts(world, anchor, player);
      assertTrue(contacts.stream().anyMatch(contact -> contact.getFixture() == movingPlatform));

      movingPlatform.getBody().setTransform(7f, 0f, 0f);
      contacts = GrappleComponent.traceContacts(world, anchor, player);
      assertTrue(contacts.stream().anyMatch(contact -> contact.getFixture() == movingPlatform));

      movingPlatform.getBody().setTransform(7f, 4f, 0f);
      contacts = GrappleComponent.traceContacts(world, anchor, player);
      assertTrue(contacts.stream().noneMatch(contact -> contact.getFixture() == movingPlatform));
    } finally {
      world.dispose();
    }
  }

  @Test
  void shouldRetainInitialWindingSidePerContact() {
    World world = new World(Vector2.Zero, true);
    try {
      Fixture fixture = rectangle(world, new Vector2(5f, 5f), 1f, 1f, 0f);
      Vector2 before = new Vector2(0f, 0f);
      Vector2 after = new Vector2(10f, 0f);
      Vector2 bend = new Vector2(5f, 5f);
      GrappleComponent.RopeContact contact =
          new GrappleComponent.RopeContact(
              fixture, bend, GrappleComponent.sideOf(before, after, bend));

      assertEquals(1, contact.getInitialSide());
      assertFalse(contact.hasCrossedSide(before, after));

      fixture.getBody().setTransform(5f, -5f, 0f);
      assertTrue(contact.hasCrossedSide(before, after));
    } finally {
      world.dispose();
    }
  }

  @Test
  void shouldReallocateLengthWhenAnchorToBendPathMoves() {
    World world = new World(Vector2.Zero, true);
    try {
      Fixture firstBody = rectangle(world, new Vector2(3f, 2f), 1f, 1f, 0f);
      Fixture lastBody = rectangle(world, new Vector2(6f, 2f), 1f, 1f, 0f);
      GrappleComponent.RopeContact first =
          new GrappleComponent.RopeContact(firstBody, new Vector2(3f, 2f), 1);
      GrappleComponent.RopeContact last =
          new GrappleComponent.RopeContact(lastBody, new Vector2(6f, 2f), 1);
      List<GrappleComponent.RopeContact> contacts = List.of(first, last);
      Vector2 anchor = new Vector2(0f, 2f);

      assertEquals(6f, GrappleComponent.fixedPathLength(anchor, contacts), 0.001f);

      firstBody.getBody().setTransform(4f, 2f, 0f);
      assertEquals(6f, GrappleComponent.fixedPathLength(anchor, contacts), 0.001f);

      lastBody.getBody().setTransform(8f, 2f, 0f);
      assertEquals(8f, GrappleComponent.fixedPathLength(anchor, contacts), 0.001f);
    } finally {
      world.dispose();
    }
  }

  private Fixture rectangle(
      World world, Vector2 position, float width, float height, float angleDegrees) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.position.set(position);
    bodyDef.angle = angleDegrees * MathUtils.degreesToRadians;
    Body body = world.createBody(bodyDef);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(width / 2f, height / 2f);
    Fixture fixture = body.createFixture(shape, 0f);
    Filter filter = fixture.getFilterData();
    filter.categoryBits = com.csse3200.game.physics.PhysicsLayer.SOLID;
    fixture.setFilterData(filter);
    shape.dispose();
    return fixture;
  }
}
