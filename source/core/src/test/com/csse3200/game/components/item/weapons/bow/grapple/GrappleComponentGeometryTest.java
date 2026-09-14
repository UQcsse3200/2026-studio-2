package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleComponentGeometryTest {

  @Test
  void shouldUseWallDimensionsWhenChoosingTopContact() {
    World world = new World(Vector2.Zero, true);
    try {
      Fixture wall = rectangle(world, new Vector2(5f, 0f), 2f, 4f, 0f);

      Vector2 contact =
          GrappleComponent.findTopContact(wall, new Vector2(0f, 0f), new Vector2(10f, 3f));

      // The shorter taut route crosses the wall's actual upper-right corner, not its centre.
      assertEquals(6f, contact.x, 0.001f);
      assertEquals(2.025f, contact.y, 0.001f);
    } finally {
      world.dispose();
    }
  }

  @Test
  void shouldTransformRotatedFixtureVerticesIntoWorldSpace() {
    World world = new World(Vector2.Zero, true);
    try {
      Fixture wall = rectangle(world, new Vector2(3f, 4f), 2f, 4f, 90f);

      Vector2 contact =
          GrappleComponent.findTopContact(wall, new Vector2(-2f, 0f), new Vector2(8f, 8f));

      // Rotating the 2x4 box makes its highest world vertices y=5 around x=1 and x=5.
      assertEquals(5.025f, contact.y, 0.001f);
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
    shape.dispose();
    return fixture;
  }
}
