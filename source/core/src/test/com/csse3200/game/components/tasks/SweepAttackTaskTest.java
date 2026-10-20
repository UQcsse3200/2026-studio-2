package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.csse3200.game.ai.tasks.Task.Status;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Calypso's telegraphed melee sweep: wind up, hit whatever is in front of her at that instant, and
 * then wait out a cooldown.
 */
@ExtendWith(GameExtension.class)
class SweepAttackTaskTest {
  private static final int PRIORITY = 22;
  private static final float RANGE = 2f;
  private static final float HEIGHT = 1.5f;
  private static final int ATTACK = 5;
  private static final int PLAYER_HEALTH = 20;

  private TaskTestWorld world;
  private Entity player;
  private Entity boss;
  private final AtomicInteger attackStarts = new AtomicInteger();
  private final AtomicInteger hits = new AtomicInteger();
  private final AtomicInteger sweepHits = new AtomicInteger();

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld();
    boss = world.fighterAt(0f, 0f, 300, ATTACK);
    player = world.fighterAt(1.5f, 0f, PLAYER_HEALTH, 0); // 1.5 in front of her
    boss.getEvents().addListener("attackStart", attackStarts::incrementAndGet);
    boss.getEvents().addListener("hitPlayer", hits::incrementAndGet);
    boss.getEvents().addListener("sweepHit", sweepHits::incrementAndGet);
  }

  /** A sweep that winds up for 0.5s and lasts 1s, with a 2s cooldown. */
  private SweepAttackTask task() {
    return task(0.5f, 1f, 2f);
  }

  private SweepAttackTask task(float windUp, float duration, float cooldown) {
    SweepAttackTask task =
        new SweepAttackTask(player, PRIORITY, RANGE, HEIGHT, windUp, duration, cooldown);
    task.create(() -> boss);
    return task;
  }

  private int playerHealth() {
    return player.getComponent(CombatStatsComponent.class).getHealth();
  }

  // --- deciding to attack ---

  @Test
  void shouldWantToSweepAPlayerInFront() {
    assertEquals(PRIORITY, task().getPriority());
  }

  @Test
  void shouldWantToSweepAPlayerBehindToo() {
    player.setPosition(-1.5f, 0f); // it turns to face whoever it sweeps

    assertEquals(PRIORITY, task().getPriority());
  }

  @Test
  void shouldIgnoreAPlayerBeyondReach() {
    player.setPosition(10f, 0f);

    assertEquals(-1, task().getPriority());
  }

  @Test
  void shouldReachAsFarAsTheRangePlusHalfThePlayersWidth() {
    // Both are one unit wide, standing level. The boss's centre is at x = 0.5, so a player whose
    // centre is RANGE + 0.5 further on (half the player's width) is right at the edge of the zone.
    SweepAttackTask task = task();
    player.setPosition(RANGE + 0.5f, 0f);

    assertEquals(PRIORITY, task.getPriority(), "right at the edge still counts");

    player.setPosition(RANGE + 0.5f + 0.05f, 0f);
    assertEquals(-1, task.getPriority(), "a little further does not");
  }

  @Test
  void shouldIgnoreAPlayerTooFarAboveOrBelow() {
    SweepAttackTask task = task();

    player.setPosition(1.5f, HEIGHT / 2f + 0.5f + 0.1f); // just past half height + half the player
    assertEquals(-1, task.getPriority());

    player.setPosition(1.5f, HEIGHT / 2f + 0.5f - 0.1f);
    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldIgnoreAPlayerBelowAsWellAsAbove() {
    player.setPosition(1.5f, -(HEIGHT / 2f + 0.5f + 0.1f));

    assertEquals(-1, task().getPriority());
  }

  // --- the swing ---

  @Test
  void shouldAnnounceTheSwingWhenItStarts() {
    SweepAttackTask task = task();

    task.start();

    assertEquals(1, attackStarts.get());
    assertEquals(Status.ACTIVE, task.getStatus());
  }

  @Test
  void shouldPlantItsFeetForTheSwing() {
    PhysicsMovementComponent movement = TaskTestWorld.movementOf(boss);
    movement.setMoving(true);
    SweepAttackTask task = task();

    task.start();

    assertFalse(movement.getMoving());
  }

  @Test
  void shouldNotHurtBeforeTheWindUpIsOver() {
    SweepAttackTask task = task();
    task.start();

    world.advance(499);
    task.update();

    assertEquals(PLAYER_HEALTH, playerHealth());
  }

  @Test
  void shouldHurtOnceTheWindUpIsOver() {
    SweepAttackTask task = task();
    task.start();

    world.advance(500);
    task.update();

    assertEquals(PLAYER_HEALTH - ATTACK, playerHealth());
  }

  @Test
  void shouldHurtOnlyOncePerSwing() {
    SweepAttackTask task = task();
    task.start();
    world.advance(500);
    task.update();

    world.advance(100);
    task.update();
    world.advance(100);
    task.update();

    assertEquals(PLAYER_HEALTH - ATTACK, playerHealth());
  }

  @Test
  void shouldKeepSwingingUntilTheDurationIsUp() {
    SweepAttackTask task = task();
    task.start();

    world.advance(999);
    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
  }

  @Test
  void shouldFinishWhenTheDurationIsUp() {
    SweepAttackTask task = task();
    task.start();

    world.advance(1000);
    task.update();

    assertEquals(Status.FINISHED, task.getStatus());
  }

  @Test
  void shouldAnnounceAHitOnlyWhenTheSwingEnds() {
    SweepAttackTask task = task();
    task.start();
    world.advance(500);
    task.update(); // the blow lands

    assertEquals(0, hits.get(), "nothing announced yet, the swing is still going");

    world.advance(500);
    task.update(); // the swing ends

    assertEquals(1, hits.get());
    assertEquals(1, sweepHits.get());
  }

  @Test
  void shouldMissAPlayerWhoHasMovedOutOfTheWayByTheTimeItLands() {
    SweepAttackTask task = task();
    task.start();
    player.setPosition(10f, 0f); // stepped well out of range during the wind-up

    world.advance(500);
    task.update();
    world.advance(500);
    task.update();

    assertEquals(PLAYER_HEALTH, playerHealth());
    assertEquals(0, hits.get(), "a miss announces nothing");
    assertEquals(0, sweepHits.get());
  }

  @Test
  void shouldSwingTheWayItWasFacingEvenIfThePlayerSlipsBehind() {
    SweepAttackTask task = task();
    task.start(); // faces right: the player is on the right
    player.setPosition(-1.5f, 0f); // and they dash round behind her

    world.advance(500);
    task.update();

    assertEquals(PLAYER_HEALTH, playerHealth(), "it does not turn mid-swing");
  }

  @Test
  void shouldFaceLeftWhenThePlayerIsOnTheLeftWhenItStarts() {
    player.setPosition(-1.5f, 0f);
    SweepAttackTask task = task();
    task.start();

    world.advance(500);
    task.update();

    assertEquals(PLAYER_HEALTH - ATTACK, playerHealth());
  }

  @Test
  void shouldFaceRightWhenThePlayerIsExactlyLevelWithHer() {
    player.setPosition(0f, 0f); // same centre
    SweepAttackTask task = task();
    task.start();

    world.advance(500);
    task.update();

    assertEquals(PLAYER_HEALTH - ATTACK, playerHealth(), "level counts as in front");
  }

  @Test
  void shouldTreatAHitOnAThingWithNothingToHurtAsAHitAnyway() {
    Entity scenery = new Entity();
    scenery.setPosition(1.5f, 0f);
    SweepAttackTask task = new SweepAttackTask(scenery, PRIORITY, RANGE, HEIGHT, 0.5f, 1f, 2f);
    task.create(() -> boss);
    task.start();
    world.advance(1000);

    assertDoesNotThrow(task::update);

    assertEquals(1, hits.get());
  }

  // --- timing edge cases ---

  @Test
  void shouldHitStraightAwayWithNoWindUp() {
    SweepAttackTask task = task(0f, 1f, 2f);
    task.start();

    task.update();

    assertEquals(PLAYER_HEALTH - ATTACK, playerHealth());
  }

  @Test
  void shouldTreatANegativeWindUpAsNone() {
    SweepAttackTask task = task(-3f, 1f, 2f);
    task.start();

    task.update();

    assertEquals(PLAYER_HEALTH - ATTACK, playerHealth());
  }

  @Test
  void shouldNeverEndTheSwingBeforeTheBlowLands() {
    SweepAttackTask task = task(0.5f, 0.1f, 2f); // a duration shorter than the wind-up

    task.start();
    world.advance(499);
    task.update();
    assertEquals(
        Status.ACTIVE, task.getStatus(), "the swing lasts at least as long as the wind-up");

    world.advance(1);
    task.update();
    assertEquals(PLAYER_HEALTH - ATTACK, playerHealth(), "the blow lands...");
    assertEquals(Status.FINISHED, task.getStatus(), "...as the swing ends");
  }

  @Test
  void shouldStartASwingByItselfIfUpdatedWhileNotActive() {
    SweepAttackTask task = task();

    task.update();

    assertEquals(Status.ACTIVE, task.getStatus());
    assertEquals(1, attackStarts.get());
  }

  @Test
  void shouldStartAFreshSwingWhenUpdatedAfterFinishing() {
    SweepAttackTask task = task();
    task.start();
    world.advance(1000);
    task.update(); // finished

    task.update(); // not active, so it begins another

    assertEquals(2, attackStarts.get());
    assertEquals(Status.ACTIVE, task.getStatus());
  }

  // --- cooldown ---

  @Test
  void shouldHaveNoCooldownBeforeItsFirstSwing() {
    assertEquals(PRIORITY, task().getPriority());
  }

  @Test
  void shouldRestAfterASwingFinishes() {
    SweepAttackTask task = task();
    task.start();
    world.advance(1000);
    task.update();

    assertEquals(-1, task.getPriority(), "the player is right there, but it is on cooldown");
  }

  @Test
  void shouldBeReadyAgainOnceTheCooldownIsOver() {
    SweepAttackTask task = task();
    task.start();
    world.advance(1000);
    task.update(); // finished at t = 1000, cooldown 2000

    world.advance(1999);
    assertEquals(-1, task.getPriority());

    world.advance(1);
    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldAlsoRestWhenASwingIsCutShort() {
    SweepAttackTask task = task();
    task.start();
    world.advance(200);

    task.stop(); // interrupted mid-swing

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldNotRestForStoppingWithoutHavingSwung() {
    SweepAttackTask task = task();

    task.stop();

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldHaveNoCooldownWhenConfiguredWithNone() {
    SweepAttackTask task = task(0.5f, 1f, 0f);
    task.start();
    world.advance(1000);
    task.update();

    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldKeepItsPriorityWhileSwingingEvenWithThePlayerOutOfReach() {
    SweepAttackTask task = task();
    task.start();
    player.setPosition(10f, 0f);

    assertEquals(PRIORITY, task.getPriority(), "it commits to the swing");
  }
}
