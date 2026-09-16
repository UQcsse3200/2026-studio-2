package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.rendering.RotatableAnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class TriggerButtonComponent extends Component {
  RotatableAnimationRenderComponent animator;
  float lastActivation = 0;

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    animator = entity.getComponent(RotatableAnimationRenderComponent.class);
  }

  @Override
  public void update() {
    lastActivation += ServiceLocator.getTimeSource().getDeltaTime();
    if (animator != null && animator.isFinished()) {
      animator.startAnimation("default");
    }
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    Entity otherEntity = ((BodyUserData) other.getBody().getUserData()).entity;
    if (otherEntity.getComponent(ArrowProjectileComponent.class) == null) {
      return;
    }

    ActivatableComponent activeComponent = entity.getComponent(ActivatableComponent.class);
    String[] ids = activeComponent.getIds();

    if (lastActivation >= 0.3) {
      for (String id : ids) {
        entity.getEvents().trigger("activateByKey", id);
      }
      animator = entity.getComponent(RotatableAnimationRenderComponent.class);
      animator.startAnimation("pressed");
      lastActivation = 0;
    }
  }
}
