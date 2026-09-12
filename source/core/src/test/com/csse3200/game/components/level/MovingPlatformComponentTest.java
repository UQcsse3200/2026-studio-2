package com.csse3200.game.components.level;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovingPlatformComponentTest {
  Vector2 firstTarget;
  Vector2 secondTarget;
  Vector2 speed;
  PhysicsMovementComponent moveComp;
  PhysicsComponent comp;
  MovingPlatformComponent platform;
  Body body;

  @BeforeEach
  void beforeEach() {
    firstTarget = new Vector2(-10f, 0f);
    secondTarget = new Vector2(10f, 0f);
    speed = new Vector2(5f, 0f);
    platform = new MovingPlatformComponent(0, firstTarget, secondTarget, speed);

    moveComp = mock(PhysicsMovementComponent.class);
    comp = mock(PhysicsComponent.class);
    body = mock(Body.class);
    Entity entity = spy(Entity.class);

    entity.addComponent(comp);
    entity.addComponent(moveComp);
    entity.addComponent(platform);
  }

  @Test
  void shouldSetCorrectFirstTarget() {
    when(moveComp.getMoving()).thenReturn(true);
    when(moveComp.getTarget()).thenReturn(firstTarget);
    when(comp.getBody()).thenReturn(body);
    when(body.getPosition()).thenReturn(new Vector2(-10f, 0f));
    shouldSetCorrectTarget(platform, moveComp, secondTarget);
  }

  @Test
  void shouldSetCorrectSecondTarget() {
    when(moveComp.getMoving()).thenReturn(true);
    when(moveComp.getTarget()).thenReturn(secondTarget);
    when(comp.getBody()).thenReturn(body);
    when(body.getPosition()).thenReturn(new Vector2(10f, 0f));
    shouldSetCorrectTarget(platform, moveComp, firstTarget);
  }

  @Test
  void shouldSetCorrectFirstHorizontalSpeed() {
    when(moveComp.getMoving()).thenReturn(true);
    when(moveComp.getTarget()).thenReturn(firstTarget).thenReturn(secondTarget);
    when(comp.getBody()).thenReturn(body);
    when(body.getPosition()).thenReturn(firstTarget);
    shouldSetCorrectSpeed(platform, new Vector2(5f, 0f));
  }

  @Test
  void shouldSetCorrectSecondHorizontalSpeed() {
    when(moveComp.getMoving()).thenReturn(true);
    when(moveComp.getTarget()).thenReturn(secondTarget).thenReturn(firstTarget);
    when(comp.getBody()).thenReturn(body);
    when(body.getPosition()).thenReturn(secondTarget);
    shouldSetCorrectSpeed(platform, new Vector2(-5f, 0f));
  }

  @Test
  void shouldSetCorrectFirstVerticalSpeed() {
    firstTarget = new Vector2(0f, -10f);
    secondTarget = new Vector2(0f, 10f);
    speed = new Vector2(0f, 5f);
    platform = new MovingPlatformComponent(0, firstTarget, secondTarget, speed);

    moveComp = mock(PhysicsMovementComponent.class);
    comp = mock(PhysicsComponent.class);
    body = mock(Body.class);
    Entity entity = spy(Entity.class);

    entity.addComponent(comp);
    entity.addComponent(moveComp);
    entity.addComponent(platform);

    when(moveComp.getMoving()).thenReturn(true);
    when(moveComp.getTarget()).thenReturn(firstTarget).thenReturn(secondTarget);
    when(comp.getBody()).thenReturn(body);
    when(body.getPosition()).thenReturn(firstTarget);
    shouldSetCorrectSpeed(platform, new Vector2(0f, 5f));
  }

  @Test
  void shouldSetCorrectSecondVerticalSpeed() {
    firstTarget = new Vector2(0f, -10f);
    secondTarget = new Vector2(0f, 10f);
    speed = new Vector2(0f, 5f);
    platform = new MovingPlatformComponent(0, firstTarget, secondTarget, speed);

    moveComp = mock(PhysicsMovementComponent.class);
    comp = mock(PhysicsComponent.class);
    body = mock(Body.class);
    Entity entity = spy(Entity.class);

    entity.addComponent(comp);
    entity.addComponent(moveComp);
    entity.addComponent(platform);

    when(moveComp.getMoving()).thenReturn(true);
    when(moveComp.getTarget()).thenReturn(secondTarget).thenReturn(firstTarget);
    when(comp.getBody()).thenReturn(body);
    when(body.getPosition()).thenReturn(secondTarget);
    shouldSetCorrectSpeed(platform, new Vector2(0f, -5f));
  }

  void shouldSetCorrectTarget(
      MovingPlatformComponent platform, PhysicsMovementComponent moveComp, Vector2 target) {
    platform.create();
    clearInvocations(moveComp);
    platform.update();
    verify(moveComp).setTarget(target);
  }

  void shouldSetCorrectSpeed(MovingPlatformComponent platform, Vector2 speed) {
    platform.create();
    platform.update();
    verify(body).setLinearVelocity(speed);
  }
}
