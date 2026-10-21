package com.csse3200.game.components.npc;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** A skeleton idles, walks, turns to face the player, and plays its sweep when it attacks. */
@ExtendWith(GameExtension.class)
class EnemyAnimationControllerTest {
  private AnimationRenderComponent animator;
  private Body body;
  private Entity player;
  private Entity enemy;

  @BeforeEach
  void setUp() {
    animator = mock(AnimationRenderComponent.class);
    when(animator.hasAnimation("walk")).thenReturn(true);
    when(animator.hasAnimation("sweep")).thenReturn(true);
    body = mock(Body.class);
    when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));
    PhysicsComponent physics = mock(PhysicsComponent.class);
    when(physics.getBody()).thenReturn(body);
    player = new Entity();
    player.setPosition(10f, 0f);
    enemy = build(player, physics);
  }

  private Entity build(Entity target, PhysicsComponent physics) {
    Entity built =
        new Entity().addComponent(animator).addComponent(new EnemyAnimationController(target));
    if (physics != null) {
      built.addComponent(physics);
    }
    built.setPosition(0f, 0f);
    built.create();
    return built;
  }

  private void movingAtSpeed(float speed) {
    when(body.getLinearVelocity()).thenReturn(new Vector2(speed, 0f));
  }

  // --- idle and walk ---

  @Test
  void shouldIdleFromTheStart() {
    verify(animator).startAnimation("idle");
  }

  @Test
  void shouldNotRestartIdleWhenAlreadyIdling() {
    when(animator.getCurrentAnimation()).thenReturn("idle");

    enemy.update();
    enemy.update();

    verify(animator, times(1)).startAnimation("idle"); // only the one from creation
  }

  @Test
  void shouldWalkWhenItMovesFasterThanTheThreshold() {
    movingAtSpeed(0.5f);

    enemy.update();

    verify(animator).startAnimation("walk");
  }

  @Test
  void shouldWalkWhenMovingLeftAsWell() {
    movingAtSpeed(-0.5f);

    enemy.update();

    verify(animator).startAnimation("walk");
  }

  @Test
  void shouldStayIdleWhenMovingNoFasterThanTheThreshold() {
    movingAtSpeed(0.1f); // exactly the threshold does not count

    enemy.update();

    verify(animator, never()).startAnimation("walk");
  }

  @Test
  void shouldNotRestartWalkWhileAlreadyWalking() {
    movingAtSpeed(0.5f);
    when(animator.getCurrentAnimation()).thenReturn("walk");

    enemy.update();
    enemy.update();

    verify(animator, never()).startAnimation("walk");
  }

  @Test
  void shouldGoBackToIdleWhenItStopsWalking() {
    movingAtSpeed(0.5f);
    when(animator.getCurrentAnimation()).thenReturn("walk");
    enemy.update();

    movingAtSpeed(0f);
    enemy.update();

    verify(animator, times(2)).startAnimation("idle"); // at creation, and now
  }

  @Test
  void shouldIdleInsteadWhenItHasNoWalkAnimation() {
    when(animator.hasAnimation("walk")).thenReturn(false);
    movingAtSpeed(0.5f);

    enemy.update();

    verify(animator, never()).startAnimation("walk");
    verify(animator, times(2)).startAnimation("idle");
  }

  @Test
  void shouldCountAsStandingStillWithNoPhysicsBody() {
    Entity bodiless = build(player, null);
    movingAtSpeed(5f); // irrelevant: it has no body to be moving with

    bodiless.update();

    verify(animator, never()).startAnimation("walk");
  }

  // --- facing ---

  @Test
  void shouldFaceRightWhenThePlayerIsOnTheRight() {
    player.setPosition(10f, 0f);

    enemy.update();

    verify(animator).setFlipX(false);
  }

  @Test
  void shouldFaceLeftWhenThePlayerIsOnTheLeft() {
    player.setPosition(-10f, 0f);

    enemy.update();

    verify(animator).setFlipX(true);
  }

  @Test
  void shouldKeepItsFacingWhenThePlayerIsAlmostDirectlyAbove() {
    player.setPosition(0.04f, 5f); // within the 0.05 deadzone sideways

    enemy.update();

    verify(animator, never()).setFlipX(anyBoolean());
  }

  @Test
  void shouldTurnOnceThePlayerIsJustOutsideTheDeadzone() {
    player.setPosition(-0.06f, 5f);

    enemy.update();

    verify(animator).setFlipX(true);
  }

  @Test
  void shouldNotCrashOrTurnWithNoTarget() {
    Entity noTarget = build(null, mock(PhysicsComponent.class));

    noTarget.update();

    verify(animator, never()).setFlipX(anyBoolean());
  }

  // --- attacking ---

  @Test
  void shouldPlaySweepWhenItStartsAnAttack() {
    enemy.getEvents().trigger("attackStart");

    verify(animator).startAnimation("sweep");
  }

  @Test
  void shouldIgnoreAnAttackWithNoSweepAnimation() {
    when(animator.hasAnimation("sweep")).thenReturn(false);

    enemy.getEvents().trigger("attackStart");
    enemy.update();

    verify(animator, never()).startAnimation("sweep");
  }

  @Test
  void shouldNotTurnOrWalkMidSwing() {
    when(animator.isFinished()).thenReturn(false);
    enemy.getEvents().trigger("attackStart");
    movingAtSpeed(2f);
    player.setPosition(-10f, 0f);

    enemy.update();

    verify(animator, never()).startAnimation("walk");
    verify(animator, never()).setFlipX(anyBoolean());
    verify(animator, times(1)).startAnimation("idle"); // from creation, and not again mid-swing
    verify(animator, times(1)).startAnimation("sweep");
  }

  @Test
  void shouldGoBackToIdleOnceTheSwingFinishes() {
    enemy.getEvents().trigger("attackStart");
    when(animator.isFinished()).thenReturn(true);
    when(animator.getCurrentAnimation()).thenReturn("sweep");

    enemy.update();

    verify(animator, times(2)).startAnimation("idle");
  }

  @Test
  void shouldCarryOnAsNormalAfterTheSwing() {
    enemy.getEvents().trigger("attackStart");
    when(animator.isFinished()).thenReturn(true);
    enemy.update(); // the swing is over

    movingAtSpeed(0.5f);
    enemy.update();

    verify(animator).startAnimation("walk");
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void shouldPlayLaserOnlyWhenTheAnimationExists(boolean available) {
    when(animator.hasAnimation("laser")).thenReturn(available);

    enemy.getEvents().trigger("laserStart");

    verify(animator, times(available ? 1 : 0)).startAnimation("laser");
  }

  @ParameterizedTest
  @CsvSource({
    "laser, false, false",
    "laser, true, true",
    "idle, false, true",
    "hurt, false, false",
    "hurt, true, true"
  })
  void shouldRespectUnfinishedLaserAndHurtAnimations(
      String currentAnimation, boolean finished, boolean shouldPlayHurt) {
    when(animator.hasAnimation("hurt")).thenReturn(true);
    when(animator.getCurrentAnimation()).thenReturn(currentAnimation);
    when(animator.isFinished()).thenReturn(finished);

    enemy.getEvents().trigger("hurt");

    verify(animator, times(shouldPlayHurt ? 1 : 0)).startAnimation("hurt");
  }

  @Test
  void shouldIgnoreHurtWhenTheAnimationIsMissing() {
    enemy.getEvents().trigger("hurt");

    verify(animator, never()).startAnimation("hurt");
  }
}
