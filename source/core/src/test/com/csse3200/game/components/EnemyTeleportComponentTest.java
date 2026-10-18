package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** An enemy that blinks between set spots when it hits, when it is hurt, and every so often. */
@ExtendWith(GameExtension.class)
class EnemyTeleportComponentTest {
  private static final long DAMAGED_DELAY = 300L;
  private static final long FREQUENCY = 1000L;
  private static final Vector2 START = new Vector2(0f, 0f);
  private static final List<Vector2> SPOTS =
      List.of(new Vector2(10f, 0f), new Vector2(20f, 0f), new Vector2(30f, 5f));

  private final AtomicLong now = new AtomicLong(0L);

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getTime()).thenAnswer(call -> now.get());
    ServiceLocator.registerTimeSource(time);
  }

  private Entity enemy(List<Vector2> spots, int health) {
    Entity enemy =
        new Entity()
            .addComponent(new CombatStatsComponent(health, 1))
            .addComponent(new EnemyTeleportComponent(spots, DAMAGED_DELAY, FREQUENCY));
    enemy.setPosition(START);
    enemy.create();
    return enemy;
  }

  private static boolean isAtOneOf(Entity enemy, List<Vector2> spots) {
    return spots.stream().anyMatch(spot -> spot.equals(enemy.getPosition()));
  }

  private void tickTo(Entity enemy, long time) {
    now.set(time);
    enemy.update();
  }

  @Test
  void shouldStayWhereItIsUntilSomethingMakesItTeleport() {
    Entity enemy = enemy(SPOTS, 10);

    tickTo(enemy, FREQUENCY - 1);

    assertEquals(START, enemy.getPosition());
  }

  @Test
  void shouldTeleportAtOnceWhenItHitsThePlayer() {
    Entity enemy = enemy(SPOTS, 10);

    enemy.getEvents().trigger("hitPlayer");

    assertTrue(isAtOneOf(enemy, SPOTS), "now at " + enemy.getPosition());
  }

  @Test
  void shouldTeleportPeriodicallyOnceTheFrequencyIsUp() {
    Entity enemy = enemy(SPOTS, 10);

    tickTo(enemy, FREQUENCY);

    assertTrue(isAtOneOf(enemy, SPOTS));
  }

  @Test
  void shouldWaitAnotherFullPeriodAfterEachPeriodicTeleport() {
    Entity enemy = enemy(SPOTS, 10);
    tickTo(enemy, FREQUENCY);
    Vector2 first = enemy.getPosition().cpy();

    tickTo(enemy, 2 * FREQUENCY - 1);
    assertEquals(first, enemy.getPosition(), "the next one is not due yet");

    tickTo(enemy, 2 * FREQUENCY);
    assertNotEquals(first, enemy.getPosition(), "now it is, and it never repeats a spot");
  }

  @Test
  void shouldTeleportAfterTheDelayWhenHurt() {
    Entity enemy = enemy(SPOTS, 10);
    now.set(100);
    enemy.getEvents().trigger("updateHealth", 6);

    tickTo(enemy, 100 + DAMAGED_DELAY - 1);
    assertEquals(START, enemy.getPosition(), "not yet: still within the delay");

    tickTo(enemy, 100 + DAMAGED_DELAY);
    assertTrue(isAtOneOf(enemy, SPOTS));
  }

  @Test
  void shouldTeleportOnlyOncePerInjury() {
    Entity enemy = enemy(SPOTS, 10);
    now.set(100);
    enemy.getEvents().trigger("updateHealth", 6);
    tickTo(enemy, 100 + DAMAGED_DELAY);
    Vector2 afterInjury = enemy.getPosition().cpy();

    tickTo(enemy, 100 + DAMAGED_DELAY + 50);

    assertEquals(afterInjury, enemy.getPosition());
  }

  @Test
  void shouldRestartThePeriodicClockAfterAnInjuryTeleport() {
    Entity enemy = enemy(SPOTS, 10);
    now.set(100);
    enemy.getEvents().trigger("updateHealth", 6);
    tickTo(enemy, 400); // the injury teleport
    Vector2 afterInjury = enemy.getPosition().cpy();

    tickTo(enemy, FREQUENCY); // would have been the original periodic teleport
    assertEquals(afterInjury, enemy.getPosition(), "the clock restarted at the injury teleport");

    tickTo(enemy, 400 + FREQUENCY);
    assertNotEquals(afterInjury, enemy.getPosition());
  }

  @Test
  void shouldNotAlsoTeleportPeriodicallyInTheSameUpdateAsAnInjuryTeleport() {
    Entity enemy = enemy(SPOTS, 10);
    now.set(FREQUENCY - 100); // the injury is just before the periodic teleport is due
    enemy.getEvents().trigger("updateHealth", 6);
    List<Vector2> seen = new java.util.ArrayList<>();

    now.set(FREQUENCY + DAMAGED_DELAY); // both are overdue
    enemy.update();
    seen.add(enemy.getPosition().cpy());
    enemy.update(); // a second update at the same moment must not have moved it again
    seen.add(enemy.getPosition().cpy());

    assertEquals(seen.get(0), seen.get(1), "one teleport for both, not two");
  }

  @Test
  void shouldIgnoreHealthThatGoesUpOrStaysTheSame() {
    Entity enemy = enemy(SPOTS, 10);
    now.set(100);

    enemy.getEvents().trigger("updateHealth", 10);
    enemy.getEvents().trigger("updateHealth", 12);
    tickTo(enemy, 100 + DAMAGED_DELAY);

    assertEquals(START, enemy.getPosition());
  }

  @Test
  void shouldReactToEachNewInjuryRelativeToTheLatestHealth() {
    Entity enemy = enemy(SPOTS, 10);
    now.set(0);
    enemy.getEvents().trigger("updateHealth", 6);
    tickTo(enemy, DAMAGED_DELAY);
    Vector2 first = enemy.getPosition().cpy();

    enemy.getEvents().trigger("updateHealth", 6); // no further injury
    tickTo(enemy, 2 * DAMAGED_DELAY);
    assertEquals(first, enemy.getPosition());

    enemy.getEvents().trigger("updateHealth", 3); // a fresh injury
    tickTo(enemy, 3 * DAMAGED_DELAY);
    assertNotEquals(first, enemy.getPosition());
  }

  @Test
  void shouldNeverTeleportToTheSpotItIsAlreadyAt() {
    Entity enemy = enemy(SPOTS, 10);
    Vector2 previous = null;
    Set<Vector2> visited = new HashSet<>();

    for (int i = 0; i < 300; i++) {
      enemy.getEvents().trigger("hitPlayer");
      Vector2 now = enemy.getPosition().cpy();
      assertTrue(isAtOneOf(enemy, SPOTS));
      if (previous != null) {
        assertNotEquals(previous, now, "teleported nowhere on attempt " + i);
      }
      visited.add(now);
      previous = now;
    }

    assertEquals(new HashSet<>(SPOTS), visited, "every spot gets used eventually");
  }

  @Test
  void shouldDoNothingWithNoSpotsToGoTo() {
    Entity enemy = enemy(List.of(), 10);

    assertDoesNotThrow(() -> enemy.getEvents().trigger("hitPlayer"));
    tickTo(enemy, FREQUENCY);

    assertEquals(START, enemy.getPosition());
  }

  @Test
  void shouldNotCrashWhenThereIsOnlyOneSpot() {
    Vector2 only = new Vector2(10f, 0f);
    Entity enemy = enemy(List.of(only), 10);

    enemy.getEvents().trigger("hitPlayer"); // goes there
    assertEquals(only, enemy.getPosition());

    assertDoesNotThrow(
        () -> enemy.getEvents().trigger("hitPlayer"), "a second teleport, with nowhere else to go");
    assertEquals(only, enemy.getPosition(), "it simply stays");
  }
}
