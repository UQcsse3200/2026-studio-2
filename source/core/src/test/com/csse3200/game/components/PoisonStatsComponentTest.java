package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

/** Poison: damage over time, applied by event. */
class PoisonStatsComponentTest extends AbstractDamageOverTimeTest {
  @Override
  Component createEffect() {
    return new PoisonStatsComponent();
  }

  @Override
  void apply(Entity entity, float damagePerSecond, float seconds) {
    entity.getEvents().trigger("applyPoison", damagePerSecond, seconds);
  }

  @Override
  boolean isActive(Entity entity) {
    return entity.getComponent(PoisonStatsComponent.class).isPoisoned();
  }

  @Test
  void shouldNotBePoisonedToBeginWith() {
    assertFalse(victim().getComponent(PoisonStatsComponent.class).isPoisoned());
  }

  @Test
  void shouldNotMistakeFireForPoison() {
    Entity entity = victim();

    entity.getEvents().trigger("applyBurn", 3f, 5f);

    assertFalse(entity.getComponent(PoisonStatsComponent.class).isPoisoned());
  }
}
