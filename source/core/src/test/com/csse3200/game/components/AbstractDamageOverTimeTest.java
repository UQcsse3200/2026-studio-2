package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * What burning and poison have in common: damage once a second for a set time, refreshed rather
 * than stacked when applied again.
 */
@ExtendWith(GameExtension.class)
abstract class AbstractDamageOverTimeTest {
  static final int START_HEALTH = 100;

  final AtomicLong now = new AtomicLong(0L);

  /** The effect under test, as the component to put on the entity. */
  abstract Component createEffect();

  /** Applies the effect the way the game does, through the entity's events. */
  abstract void apply(Entity entity, float damagePerSecond, float seconds);

  /** Whether the effect is currently active on the entity. */
  abstract boolean isActive(Entity entity);

  @BeforeEach
  void setUpTime() {
    GameTime time = mock(GameTime.class);
    when(time.getTime()).thenAnswer(call -> now.get());
    ServiceLocator.registerTimeSource(time);
  }

  Entity victim() {
    Entity entity =
        new Entity()
            .addComponent(new CombatStatsComponent(START_HEALTH, 1))
            .addComponent(createEffect());
    entity.create();
    return entity;
  }

  static int healthOf(Entity entity) {
    return entity.getComponent(CombatStatsComponent.class).getHealth();
  }

  void tickTo(Entity entity, long time) {
    now.set(time);
    entity.update();
  }

  @Test
  void shouldDoNothingUntilApplied() {
    Entity entity = victim();

    tickTo(entity, 5000);

    assertEquals(START_HEALTH, healthOf(entity));
    assertFalse(isActive(entity));
  }

  @Test
  void shouldBecomeActiveWhenApplied() {
    Entity entity = victim();

    apply(entity, 3f, 5f);

    assertTrue(isActive(entity));
  }

  @Test
  void shouldNotHurtBeforeTheFirstSecondIsUp() {
    Entity entity = victim();
    apply(entity, 3f, 5f);

    tickTo(entity, 999);

    assertEquals(START_HEALTH, healthOf(entity));
  }

  @Test
  void shouldHurtOnceEachSecond() {
    Entity entity = victim();
    apply(entity, 3f, 5f);

    tickTo(entity, 1000);
    assertEquals(START_HEALTH - 3, healthOf(entity));

    tickTo(entity, 1999);
    assertEquals(START_HEALTH - 3, healthOf(entity), "still the same second");

    tickTo(entity, 2000);
    assertEquals(START_HEALTH - 6, healthOf(entity));
  }

  @Test
  void shouldHurtOnceForEachSecondOfItsDurationIncludingTheLast() {
    Entity entity = victim();
    apply(entity, 3f, 5f);

    for (long time = 1000; time <= 5000; time += 1000) {
      tickTo(entity, time);
    }

    assertEquals(START_HEALTH - 5 * 3, healthOf(entity));
  }

  @Test
  void shouldNotHurtAnyMoreOnceItHasRunItsCourse() {
    Entity entity = victim();
    apply(entity, 3f, 5f);
    tickTo(entity, 5000);
    int afterTheLastTick = healthOf(entity);

    tickTo(entity, 6000);
    tickTo(entity, 20_000);

    assertEquals(afterTheLastTick, healthOf(entity));
    assertFalse(isActive(entity));
  }

  @Test
  void shouldMakeUpEveryMissedTickWhenUpdatedLate() {
    Entity entity = victim();
    apply(entity, 3f, 5f);

    tickTo(entity, 3500); // three seconds' worth in one go

    assertEquals(START_HEALTH - 3 * 3, healthOf(entity));
  }

  @Test
  void shouldNeverDealMoreThanTheDurationAllowsEvenWhenUpdatedVeryLate() {
    Entity entity = victim();
    apply(entity, 3f, 5f);

    tickTo(entity, 1_000_000);

    assertEquals(START_HEALTH - 5 * 3, healthOf(entity));
  }

  @Test
  void shouldStayActiveUntilTheEndTimeIsReached() {
    Entity entity = victim();
    apply(entity, 3f, 5f);

    tickTo(entity, 4999);
    assertTrue(isActive(entity));

    tickTo(entity, 5000);
    assertFalse(isActive(entity));
  }

  @Test
  void shouldRestartTheDurationWhenAppliedAgainRatherThanStackingTheDamage() {
    Entity entity = victim();
    apply(entity, 3f, 5f);
    tickTo(entity, 2500); // two ticks so far: 94
    assertEquals(START_HEALTH - 6, healthOf(entity));

    apply(entity, 3f, 5f); // refreshed at 2500: ends at 7500, next tick at 3500
    tickTo(entity, 3499);
    assertEquals(START_HEALTH - 6, healthOf(entity), "the clock restarted");
    tickTo(entity, 3500);
    assertEquals(START_HEALTH - 9, healthOf(entity), "still 3 a tick, not doubled");

    tickTo(entity, 7500);
    assertEquals(START_HEALTH - 6 - 5 * 3, healthOf(entity), "five more ticks in the new window");
    assertFalse(isActive(entity));
  }

  @Test
  void shouldUseTheLatestDamageWhenAppliedAgain() {
    Entity entity = victim();
    apply(entity, 3f, 5f);
    apply(entity, 1f, 5f);

    tickTo(entity, 1000);

    assertEquals(START_HEALTH - 1, healthOf(entity));
  }

  @Test
  void shouldRoundFractionalDamageToTheNearestWholeNumber() {
    Entity entity = victim();
    apply(entity, 2.5f, 3f);

    tickTo(entity, 1000);

    assertEquals(START_HEALTH - 3, healthOf(entity), "2.5 rounds up to 3");
  }

  @Test
  void shouldIgnoreZeroOrNegativeDamage() {
    Entity entity = victim();

    apply(entity, 0f, 5f);
    apply(entity, -2f, 5f);
    tickTo(entity, 3000);

    assertEquals(START_HEALTH, healthOf(entity));
    assertFalse(isActive(entity));
  }

  @Test
  void shouldIgnoreZeroOrNegativeDuration() {
    Entity entity = victim();

    apply(entity, 3f, 0f);
    apply(entity, 3f, -1f);
    tickTo(entity, 3000);

    assertEquals(START_HEALTH, healthOf(entity));
    assertFalse(isActive(entity));
  }

  @Test
  void shouldNotLetABadRequestCancelAnEffectAlreadyRunning() {
    Entity entity = victim();
    apply(entity, 3f, 5f);

    apply(entity, 0f, 5f); // ignored
    tickTo(entity, 1000);

    assertEquals(START_HEALTH - 3, healthOf(entity));
  }

  @Test
  void shouldNeverTickForAnEffectShorterThanASecond() {
    // The first tick is a second after it is applied, which is after a half second effect ends
    Entity entity = victim();
    apply(entity, 3f, 0.5f);

    tickTo(entity, 1000);
    tickTo(entity, 2000);

    assertEquals(START_HEALTH, healthOf(entity));
  }

  @Test
  void shouldKillAnEnemyWhoseHealthRunsOut() {
    Entity entity =
        new Entity().addComponent(new CombatStatsComponent(5, 1)).addComponent(createEffect());
    entity.create();
    AtomicInteger deaths = new AtomicInteger();
    entity.getEvents().addListener("death", deaths::incrementAndGet);
    apply(entity, 3f, 5f);

    tickTo(entity, 5000); // would be 15 damage

    assertEquals(0, healthOf(entity), "health never goes below zero");
    assertEquals(1, deaths.get(), "and the death is announced once");
  }

  @Test
  void shouldDoNothingWithNoCombatStatsToHurt() {
    Entity entity = new Entity().addComponent(createEffect());
    entity.create();
    apply(entity, 3f, 5f);

    assertDoesNotThrow(() -> tickTo(entity, 3000));
  }

  @Test
  void shouldIgnoreBeingAppliedWithoutATimeSource() {
    Entity entity = victim();
    ServiceLocator.registerTimeSource(null);

    apply(entity, 3f, 5f);

    assertFalse(isActive(entity));
  }

  @Test
  void shouldNotCrashWhenUpdatedWithoutATimeSourceMidEffect() {
    Entity entity = victim();
    apply(entity, 3f, 5f);
    ServiceLocator.registerTimeSource(null);

    entity.update(); // nothing to read the time from: it just skips

    assertEquals(START_HEALTH, healthOf(entity));
  }
}
