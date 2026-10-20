package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Ice: a temporary cut to an enemy's movement speed. */
@ExtendWith(GameExtension.class)
class SlowStatsComponentTest {
  private final AtomicLong now = new AtomicLong(0L);
  private PhysicsMovementComponent movement;
  private Entity enemy;

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getTime()).thenAnswer(call -> now.get());
    ServiceLocator.registerTimeSource(time);
    movement = mock(PhysicsMovementComponent.class);
    enemy = new Entity().addComponent(movement).addComponent(new SlowStatsComponent());
    enemy.create();
  }

  private SlowStatsComponent slow() {
    return enemy.getComponent(SlowStatsComponent.class);
  }

  private void tickTo(long time) {
    now.set(time);
    enemy.update();
  }

  @Test
  void shouldNotBeSlowedToBeginWith() {
    assertFalse(slow().isSlowed());
  }

  @Test
  void shouldCutTheEnemysSpeedByTheMultiplierWhenSlowed() {
    enemy.getEvents().trigger("applySlow", 0.5f, 4f);

    verify(movement).setSpeedMultiplier(0.5f);
    assertTrue(slow().isSlowed());
  }

  @Test
  void shouldStayAsSlowAsAppliedUntilTheEndTime() {
    enemy.getEvents().trigger("applySlow", 0.5f, 4f);

    tickTo(3999);

    assertTrue(slow().isSlowed());
    verify(movement, never()).setSpeedMultiplier(1f);
  }

  @Test
  void shouldRestoreFullSpeedWhenTheSlowEnds() {
    enemy.getEvents().trigger("applySlow", 0.5f, 4f);

    tickTo(4000);

    assertFalse(slow().isSlowed());
    verify(movement).setSpeedMultiplier(1f);
  }

  @Test
  void shouldOnlyRestoreSpeedOnceTheSlowIsOver() {
    enemy.getEvents().trigger("applySlow", 0.5f, 4f);
    tickTo(4000);

    tickTo(5000);
    tickTo(9000);

    verify(movement, times(1)).setSpeedMultiplier(1f);
  }

  @Test
  void shouldTakeTheNewMultiplierAndRestartTheTimerWhenSlowedAgain() {
    enemy.getEvents().trigger("applySlow", 0.5f, 4f);
    now.set(3000);
    enemy.getEvents().trigger("applySlow", 0.25f, 4f); // ends at 7000 now

    verify(movement).setSpeedMultiplier(0.25f);
    tickTo(6999);
    assertTrue(slow().isSlowed(), "the original end time of 4000 no longer applies");
    tickTo(7000);
    assertFalse(slow().isSlowed());
  }

  @Test
  void shouldIgnoreAMultiplierThatDoesNotSlowAnything() {
    enemy.getEvents().trigger("applySlow", 1f, 4f);
    enemy.getEvents().trigger("applySlow", 1.5f, 4f);

    assertFalse(slow().isSlowed());
    verify(movement, never()).setSpeedMultiplier(anyFloat());
  }

  @Test
  void shouldIgnoreAMultiplierThatWouldStopTheEnemyDeadOrReverseIt() {
    enemy.getEvents().trigger("applySlow", 0f, 4f);
    enemy.getEvents().trigger("applySlow", -0.5f, 4f);

    assertFalse(slow().isSlowed());
    verify(movement, never()).setSpeedMultiplier(anyFloat());
  }

  @Test
  void shouldIgnoreADurationOfZeroOrLess() {
    enemy.getEvents().trigger("applySlow", 0.5f, 0f);
    enemy.getEvents().trigger("applySlow", 0.5f, -3f);

    assertFalse(slow().isSlowed());
    verify(movement, never()).setSpeedMultiplier(anyFloat());
  }

  @Test
  void shouldNotLetABadRequestCancelASlowAlreadyApplied() {
    enemy.getEvents().trigger("applySlow", 0.5f, 4f);

    enemy.getEvents().trigger("applySlow", 1f, 4f); // ignored

    assertTrue(slow().isSlowed());
  }

  @Test
  void shouldDoNothingToAnEnemyThatCannotMove() {
    Entity statue = new Entity().addComponent(new SlowStatsComponent());
    statue.create();

    statue.getEvents().trigger("applySlow", 0.5f, 4f);
    statue.update();

    assertFalse(statue.getComponent(SlowStatsComponent.class).isSlowed());
  }

  @Test
  void shouldIgnoreBeingSlowedWithoutATimeSource() {
    ServiceLocator.registerTimeSource(null);

    enemy.getEvents().trigger("applySlow", 0.5f, 4f);

    assertFalse(slow().isSlowed());
    verify(movement, never()).setSpeedMultiplier(anyFloat());
  }

  @Test
  void shouldNotCrashWhenUpdatedWithoutATimeSourceMidSlow() {
    enemy.getEvents().trigger("applySlow", 0.5f, 4f);
    ServiceLocator.registerTimeSource(null);

    enemy.update();

    assertTrue(slow().isSlowed(), "it cannot tell time, so it stays slowed");
  }

  @Test
  void shouldNotTouchTheSpeedOfAnEnemyThatWasNeverSlowed() {
    tickTo(10_000);

    verify(movement, never()).setSpeedMultiplier(anyFloat());
  }
}
