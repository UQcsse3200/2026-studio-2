package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleContactLimitTest {
  @Test
  void shouldPreserveFullContactListWithoutInsertingAnotherBend() throws Exception {
    try (WorldFixture setup = new WorldFixture(16)) {
      List<GrappleComponent.RopeContact> original = new ArrayList<>(setup.contacts);
      setup.wall(0.5f, 0f);
      setup.insertMissingContacts();
      assertEquals(original, setup.contacts);
    }
  }

  @Test
  void shouldStopAfterSixteenSearchesEvenWhenLastSearchAddsContact() throws Exception {
    try (WorldFixture setup = new WorldFixture(15)) {
      List<GrappleComponent.RopeContact> original = new ArrayList<>(setup.contacts);
      Fixture obstacle = setup.wall(17.5f, 0f);
      setup.insertMissingContacts();
      assertEquals(16, setup.contacts.size());
      assertEquals(original, setup.contacts.subList(0, 15));
      assertSame(obstacle, setup.contacts.getLast().getFixture());
      assertTrue(setup.contacts.getLast().getWorldPoint().x < 17.5f);
    }
  }

  private static class WorldFixture implements AutoCloseable {
    private final World world = new World(Vector2.Zero, true);
    private final GrappleComponent grapple = new GrappleComponent();
    private final List<GrappleComponent.RopeContact> contacts;

    @SuppressWarnings("unchecked")
    WorldFixture(int count) throws Exception {
      // Seed persistent contacts directly to isolate the two search-budget boundaries from
      // wrapping/unwrapping history. Raycasts and polygon geometry still use real Box2D.
      var contactsField = GrappleComponent.class.getDeclaredField("ropeContacts");
      contactsField.setAccessible(true);
      contacts = (List<GrappleComponent.RopeContact>) contactsField.get(grapple);
      Fixture support = wall(0f, 50f);
      Vector2 point = new Vector2(1f, 0f);
      for (int i = 0; i < count; i++) {
        contacts.add(new GrappleComponent.RopeContact(support, point, 1));
        point.x += 1f;
      }
      var lengthField = GrappleComponent.class.getDeclaredField("totalRopeLength");
      lengthField.setAccessible(true);
      lengthField.setFloat(grapple, 100f);
    }

    Fixture wall(float x, float y) {
      BodyDef definition = new BodyDef();
      definition.position.set(x, y);
      var body = world.createBody(definition);
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.25f, 1f);
      Fixture fixture = body.createFixture(shape, 0f);
      shape.dispose();
      var filter = fixture.getFilterData();
      filter.categoryBits = PhysicsLayer.SOLID;
      fixture.setFilterData(filter);
      return fixture;
    }

    void insertMissingContacts() throws Exception {
      var method =
          GrappleComponent.class.getDeclaredMethod(
              "insertMissingContacts", World.class, Vector2.class, Vector2.class);
      method.setAccessible(true);
      method.invoke(grapple, world, Vector2.Zero.cpy(), new Vector2(20f, 0f));
    }

    @Override
    public void close() {
      world.dispose();
    }
  }
}
