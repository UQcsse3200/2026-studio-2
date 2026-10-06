package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.rendering.RotatableAnimationRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TriggerButtonComponentTest {

  private GameTime timeSource;

  @BeforeEach
  void setUp() {
    timeSource = mock(GameTime.class);
    ServiceLocator.registerTimeSource(timeSource);
  }

  @Test
  void shouldAccumulateActivationTime() {
    TriggerButtonComponent component = new TriggerButtonComponent();

    Entity entity = new Entity().addComponent(component);
    entity.create();

    when(timeSource.getDeltaTime()).thenReturn(0.2f);

    component.update();

    assertEquals(0.2f, component.lastActivation, 0.001f);
  }

  @Test
  void shouldReturnToDefaultAnimationWhenFinished() {
    RotatableAnimationRenderComponent animator = mock(RotatableAnimationRenderComponent.class);

    when(animator.isFinished()).thenReturn(true);

    TriggerButtonComponent component = new TriggerButtonComponent();

    Entity entity = new Entity().addComponent(animator).addComponent(component);

    entity.create();

    when(timeSource.getDeltaTime()).thenReturn(0.1f);

    component.update();

    verify(animator).startAnimation("default");
  }

  @Test
  void shouldNotRestartAnimationWhenNotFinished() {
    RotatableAnimationRenderComponent animator = mock(RotatableAnimationRenderComponent.class);

    when(animator.isFinished()).thenReturn(false);

    TriggerButtonComponent component = new TriggerButtonComponent();

    Entity entity = new Entity().addComponent(animator).addComponent(component);

    entity.create();

    when(timeSource.getDeltaTime()).thenReturn(0.1f);

    component.update();

    verify(animator, never()).startAnimation("default");
  }

  @Test
  void shouldNotActivateForNonArrowCollision() {
    RotatableAnimationRenderComponent animator = mock(RotatableAnimationRenderComponent.class);

    ActivatableComponent activatable = mock(ActivatableComponent.class);

    TriggerButtonComponent component = new TriggerButtonComponent();

    Entity button =
        new Entity().addComponent(animator).addComponent(activatable).addComponent(component);

    button.create();

    Entity otherEntity = new Entity();

    BodyUserData data = new BodyUserData();
    data.entity = otherEntity;

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    button.getEvents().trigger("collisionStart", null, other);

    verify(animator, never()).startAnimation("pressed");
  }

  @Test
  void shouldNotActivateArrowBeforeCooldown() {
    RotatableAnimationRenderComponent animator = mock(RotatableAnimationRenderComponent.class);

    ActivatableComponent activatable = mock(ActivatableComponent.class);

    TriggerButtonComponent component = new TriggerButtonComponent();

    Entity button =
        new Entity().addComponent(animator).addComponent(activatable).addComponent(component);

    button.create();

    Entity arrow = new Entity().addComponent(mock(ArrowProjectileComponent.class));

    BodyUserData data = new BodyUserData();
    data.entity = arrow;

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    component.lastActivation = 0.2f;

    button.getEvents().trigger("collisionStart", null, other);

    verify(animator, never()).startAnimation("pressed");
    assertEquals(0.2f, component.lastActivation, 0.001f);
  }

  @Test
  void shouldActivateWhenArrowHitsAfterCooldown() {
    RotatableAnimationRenderComponent animator = mock(RotatableAnimationRenderComponent.class);

    ActivatableComponent activatable = mock(ActivatableComponent.class);

    when(activatable.getIds()).thenReturn(new String[] {"door1", "platform1"});

    TriggerButtonComponent component = new TriggerButtonComponent();

    Entity button =
        new Entity().addComponent(animator).addComponent(activatable).addComponent(component);

    button.create();

    final int[] activations = {0};

    button.getEvents().addListener("activateByKey", (String id) -> activations[0]++);

    Entity arrow = new Entity().addComponent(mock(ArrowProjectileComponent.class));

    BodyUserData data = new BodyUserData();
    data.entity = arrow;

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    component.lastActivation = 0.3f;

    button.getEvents().trigger("collisionStart", null, other);

    assertEquals(2, activations[0]);
    verify(animator).startAnimation("pressed");
    assertEquals(0f, component.lastActivation, 0.001f);
  }
}
