package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

class PlatformGrappleComponentTest {
  @Test
  void invalidGrappleSides() {
    PlatformGrappleComponent comp = new PlatformGrappleComponent(23);

    assertEquals(15, comp.getGrappleSides());
  }

  @Test
  void shouldUpdateGrappleSides() {
    PlatformGrappleComponent comp = new PlatformGrappleComponent(0);

    comp.updateGrappleSides(8);
    assertEquals(8, comp.getGrappleSides());

    comp.updateGrappleSides(29);
    assertEquals(15, comp.getGrappleSides());
  }

  @Test
  void checkInvalidEntitySideHitFails() {
    PlatformGrappleComponent comp = new PlatformGrappleComponent(0);
    Entity entity = mock(Entity.class);

    assertEquals(0, comp.checkSideHit(entity, new Vector2(0f, 0f)));
  }

  @Test
  void checkCorrectSideHit() {
    PlatformGrappleComponent comp = new PlatformGrappleComponent(0);
    Entity entity = spy(Entity.class);
    entity.addComponent(comp);
    entity.setPosition(0.5f, 0.5f);

    // check expected cases
    assertEquals(8, comp.checkSideHit(entity, new Vector2(0.5f, 0.55f)));
    assertEquals(4, comp.checkSideHit(entity, new Vector2(0.55f, 1.5f)));
    assertEquals(2, comp.checkSideHit(entity, new Vector2(1.5f, 0.55f)));
    assertEquals(1, comp.checkSideHit(entity, new Vector2(0.55f, 0.5f)));
    assertEquals(0, comp.checkSideHit(entity, new Vector2(1.51f, 1.51f)));

    // check corner interactions prefer y axis (left/right side)
    assertEquals(8, comp.checkSideHit(entity, new Vector2(0.5f, 0.5f)));
    assertEquals(8, comp.checkSideHit(entity, new Vector2(0.5f, 1.5f)));
    assertEquals(2, comp.checkSideHit(entity, new Vector2(1.5f, 0.5f)));
    assertEquals(2, comp.checkSideHit(entity, new Vector2(1.5f, 1.5f)));
  }

  @Test
  void checkCorrectPlatformBounds() {
    PlatformGrappleComponent comp = new PlatformGrappleComponent(0);
    Entity entity = spy(Entity.class);
    entity.addComponent(comp);
    entity.setPosition(0.5f, 0.5f);

    Vector2[] result = comp.calculatePlatformBounds(entity);
    assertEquals(new Vector2(0.5f, 0.5f), result[0]);
    assertEquals(new Vector2(1.5f, 1.5f), result[1]);

    entity.setScale(2f, 3f);
    result = comp.calculatePlatformBounds(entity);
    assertEquals(new Vector2(0.5f, 0.5f), result[0]);
    assertEquals(new Vector2(2.5f, 3.5f), result[1]);
  }

  @Test
  void checkSuccessfulGrapples() {
    PlatformGrappleComponent comp = new PlatformGrappleComponent(0);
    comp.updateGrappleSides(15);
    assertTrue(comp.successfulGrapple(1));
    assertTrue(comp.successfulGrapple(2));
    assertTrue(comp.successfulGrapple(4));
    assertTrue(comp.successfulGrapple(8));

    comp.updateGrappleSides(8);
    assertFalse(comp.successfulGrapple(1));
    assertFalse(comp.successfulGrapple(2));
    assertFalse(comp.successfulGrapple(4));
    assertTrue(comp.successfulGrapple(8));

    comp.updateGrappleSides(4);
    assertFalse(comp.successfulGrapple(1));
    assertFalse(comp.successfulGrapple(2));
    assertTrue(comp.successfulGrapple(4));
    assertFalse(comp.successfulGrapple(8));

    comp.updateGrappleSides(10);
    assertFalse(comp.successfulGrapple(1));
    assertTrue(comp.successfulGrapple(2));
    assertFalse(comp.successfulGrapple(4));
    assertTrue(comp.successfulGrapple(8));
  }
}
