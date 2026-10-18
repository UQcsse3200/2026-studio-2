package com.csse3200.game.components;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** How a projectile's life ends: out of time, at its target, or against something it hits. */
@ExtendWith(GameExtension.class)
class ProjectileComponentTest {
  /** A projectile cannot hit or expire at its target during its first 0.15 seconds. */
  private static final float ARM_TIME = 0.15f;

  private EntityService entityService;
  private GameTime time;
  private PhysicsMovementComponent movement;

  @BeforeEach
  void setUp() {
    entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    movement = mock(PhysicsMovementComponent.class);
  }

  private Entity projectile(float lifetime) {
    Entity projectile =
        new Entity().addComponent(new ProjectileComponent(lifetime)).addComponent(movement);
    projectile.setPosition(0f, 0f);
    projectile.create();
    return projectile;
  }

  private void step(Entity projectile, float seconds) {
    when(time.getDeltaTime()).thenReturn(seconds);
    projectile.update();
  }

  private void verifyDisposedTimes(Entity projectile, int times) {
    verify(entityService, times(times)).scheduleForDisposal(projectile);
  }

  private static Fixture fixtureOnLayer(short layer) {
    Filter filter = new Filter();
    filter.categoryBits = layer;
    Fixture fixture = mock(Fixture.class);
    when(fixture.getFilterData()).thenReturn(filter);
    return fixture;
  }

  private void collide(Entity projectile, short layerHit) {
    projectile.getEvents().trigger("collisionStart", mock(Fixture.class), fixtureOnLayer(layerHit));
  }

  // --- lifetime ---

  @Test
  void shouldLiveUntilItsLifetimeIsUp() {
    Entity projectile = projectile(1f);

    step(projectile, 0.4f);
    step(projectile, 0.4f);

    verify(entityService, never()).scheduleForDisposal(any());
  }

  @Test
  void shouldDespawnOnceItsLifetimeRunsOut() {
    Entity projectile = projectile(1f);
    step(projectile, 0.4f);
    step(projectile, 0.4f);

    step(projectile, 0.4f);

    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldDespawnAtExactlyTheEndOfItsLifetime() {
    Entity projectile = projectile(0.5f);

    step(projectile, 0.5f);

    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldNotChaseATargetThatIsNotThere() {
    when(movement.getTarget()).thenReturn(null);
    Entity projectile = projectile(10f);

    step(projectile, 0.2f);
    step(projectile, 0.2f);

    verify(entityService, never()).scheduleForDisposal(any());
  }

  @Test
  void shouldFlyOnWithNoMovementComponentUntilItsLifetimeEnds() {
    Entity projectile = new Entity().addComponent(new ProjectileComponent(1f));
    projectile.create();

    step(projectile, 0.5f);
    verify(entityService, never()).scheduleForDisposal(any());

    step(projectile, 0.5f);
    verifyDisposedTimes(projectile, 1);
  }

  // --- reaching or passing its target ---

  @Test
  void shouldDespawnOnReachingItsTarget() {
    when(movement.getTarget()).thenReturn(new Vector2(0.5f, 0.5f)); // the projectile's centre
    Entity projectile = projectile(10f);

    step(projectile, ARM_TIME + 0.05f);

    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldNotDespawnAtItsTargetBeforeItIsArmed() {
    when(movement.getTarget()).thenReturn(new Vector2(0.5f, 0.5f));
    Entity projectile = projectile(10f);

    step(projectile, ARM_TIME - 0.05f);
    verify(entityService, never()).scheduleForDisposal(any());

    step(projectile, 0.1f);
    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldNotDespawnWhileStillClosingOnTheTarget() {
    when(movement.getTarget()).thenReturn(new Vector2(10.5f, 0.5f));
    Entity projectile = projectile(10f);

    for (int i = 1; i <= 8; i++) {
      projectile.setPosition(i * 1f, 0f);
      step(projectile, 0.2f);
    }

    verify(entityService, never()).scheduleForDisposal(any());
  }

  @Test
  void shouldNotDespawnJustOutsideThePickupDistance() {
    when(movement.getTarget()).thenReturn(new Vector2(0.75f, 0.5f)); // exactly 0.25 away
    Entity projectile = projectile(10f);

    step(projectile, 0.2f);

    verify(entityService, never()).scheduleForDisposal(any());
  }

  @Test
  void shouldDespawnOnceItHasFlownPastItsTarget() {
    when(movement.getTarget()).thenReturn(new Vector2(5.5f, 0.5f));
    Entity projectile = projectile(10f);
    projectile.setPosition(3f, 0f);
    step(projectile, 0.2f); // 2 away
    projectile.setPosition(4f, 0f);
    step(projectile, 0.2f); // 1 away: closing in, fine
    verify(entityService, never()).scheduleForDisposal(any());

    projectile.setPosition(7f, 0f); // now 1.5 away and getting further
    step(projectile, 0.2f);

    verifyDisposedTimes(projectile, 1);
  }

  // --- collisions ---

  @Test
  void shouldIgnoreCollisionsBeforeItIsArmed() {
    Entity projectile = projectile(10f);
    step(projectile, ARM_TIME - 0.05f);

    collide(projectile, PhysicsLayer.PLAYER);

    verify(entityService, never()).scheduleForDisposal(any());
  }

  @Test
  void shouldDespawnWhenItHitsThePlayerOnceArmed() {
    Entity projectile = projectile(10f);
    step(projectile, ARM_TIME + 0.05f);

    collide(projectile, PhysicsLayer.PLAYER);

    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldDespawnWhenItHitsGroundOnceArmed() {
    Entity projectile = projectile(10f);
    step(projectile, ARM_TIME + 0.05f);

    collide(projectile, PhysicsLayer.GROUND);

    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldDespawnWhenItHitsAnObstacleOnceArmed() {
    Entity projectile = projectile(10f);
    step(projectile, ARM_TIME + 0.05f);

    collide(projectile, PhysicsLayer.OBSTACLE);

    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldDespawnWhenItHitsAnNpcOnceArmed() {
    Entity projectile = projectile(10f);
    step(projectile, ARM_TIME + 0.05f);

    collide(projectile, PhysicsLayer.NPC);

    verifyDisposedTimes(projectile, 1);
  }

  @Test
  void shouldPassThroughLayersThatDoNotStopIt() {
    Entity projectile = projectile(10f);
    step(projectile, ARM_TIME + 0.05f);

    collide(projectile, PhysicsLayer.PLAYER_PROJECTILE);
    collide(projectile, PhysicsLayer.ENEMY_PROJECTILE);
    collide(projectile, PhysicsLayer.DEFAULT);
    collide(projectile, PhysicsLayer.NONE);

    verify(entityService, never()).scheduleForDisposal(any());
  }
}
