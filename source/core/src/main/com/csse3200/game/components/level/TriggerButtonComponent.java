package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.RotatableAnimationRenderComponent;

public class TriggerButtonComponent extends Component {
  RotatableAnimationRenderComponent animator;

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    animator = entity.getComponent(RotatableAnimationRenderComponent.class);
  }

  @Override
  public void update() {
    if (animator != null && animator.isFinished()) {
      animator.startAnimation("default");
    }
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    ActivatableComponent activeComponent = entity.getComponent(ActivatableComponent.class);
    entity.getEvents().trigger("activateByKey", activeComponent.getId());

    animator = entity.getComponent(RotatableAnimationRenderComponent.class);
    animator.startAnimation("pressed");
  }
}
