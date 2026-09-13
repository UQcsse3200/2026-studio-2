package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class SpikyBallComponent extends Component {
  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("collisionStart", this::handleDispose);
  }

  @Override
  public void update() {
    super.update();
    Vector2 position = entity.getPosition();
    entity.setPosition(position.x, position.y - 0.1f);
  }

  public void handleDispose(Fixture fixtureA, Fixture fixtureB) {
    ServiceLocator.getEntityService().scheduleForDisposal(entity);
  }
}
