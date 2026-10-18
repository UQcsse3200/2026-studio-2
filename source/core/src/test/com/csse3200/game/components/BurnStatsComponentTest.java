package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

/** Fire: burning damage over time, applied by event or directly. */
class BurnStatsComponentTest extends AbstractDamageOverTimeTest {
  @Override
  Component createEffect() {
    return new BurnStatsComponent();
  }

  @Override
  void apply(Entity entity, float damagePerSecond, float seconds) {
    entity.getEvents().trigger("applyBurn", damagePerSecond, seconds);
  }

  @Override
  boolean isActive(Entity entity) {
    return entity.getComponent(BurnStatsComponent.class).isBurning();
  }

  @Test
  void shouldBurnWhenAppliedDirectlyToTheComponentToo() {
    Entity entity = victim();

    entity.getComponent(BurnStatsComponent.class).applyBurn(4f, 2f);
    tickTo(entity, 2000);

    assertEquals(START_HEALTH - 2 * 4, healthOf(entity));
  }

  @Test
  void shouldNotBeBurningToBeginWith() {
    assertFalse(victim().getComponent(BurnStatsComponent.class).isBurning());
  }

  @Test
  void shouldBeBurningOnceSetAlight() {
    Entity entity = victim();

    entity.getComponent(BurnStatsComponent.class).applyBurn(4f, 2f);

    assertTrue(entity.getComponent(BurnStatsComponent.class).isBurning());
  }
}
