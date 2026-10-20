package com.csse3200.game.physics.raycast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class AllHitCallbackTest {
  private static Fixture fixtureOn(short layer) {
    Filter filter = new Filter();
    filter.categoryBits = layer;
    Fixture fixture = mock(Fixture.class);
    when(fixture.getFilterData()).thenReturn(filter);
    return fixture;
  }

  @Test
  void returnsHitsAsARaycastHitArray() {
    AllHitCallback callback = new AllHitCallback();
    callback.layerMask = PhysicsLayer.NPC;
    Fixture npc = fixtureOn(PhysicsLayer.NPC);

    callback.reportRayFixture(npc, new Vector2(1f, 2f), new Vector2(0f, 1f), 0.5f);
    RaycastHit[] hits = callback.getHitsAndClear();

    assertEquals(1, hits.length);
    assertSame(npc, hits[0].fixture);
  }

  @Test
  void ignoresFixturesOutsideTheLayerMaskAndClearsAfterReading() {
    AllHitCallback callback = new AllHitCallback();
    callback.layerMask = PhysicsLayer.NPC;

    callback.reportRayFixture(
        fixtureOn(PhysicsLayer.PLAYER), new Vector2(), new Vector2(0f, 1f), 0.5f);
    assertEquals(0, callback.getHitsAndClear().length);

    callback.reportRayFixture(
        fixtureOn(PhysicsLayer.NPC), new Vector2(), new Vector2(0f, 1f), 0.5f);
    assertEquals(1, callback.getHitsAndClear().length);
    assertEquals(0, callback.getHitsAndClear().length);
  }
}
