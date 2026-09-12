package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class TriggerButtonComponent extends Component {
  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    ActivatableComponent activeComponent = entity.getComponent(ActivatableComponent.class);
    entity.getEvents().trigger("activateByKey", activeComponent.getId());

    AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
    animator.startAnimation("pressed");
  }
}
