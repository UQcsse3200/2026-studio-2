package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class SpikyBallComponent extends Component {

  public Vector2 direction;

  /**
   * Instantiate a spiky ball component
   *
   * @param direction normalised direction e.g. (1, 0), (0, -1), etc.
   */
  public SpikyBallComponent(Vector2 direction) {
    this.direction = direction;
  }

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("collisionStart", this::handleDispose);
  }

  @Override
  public void update() {
    super.update();
    Vector2 position = entity.getPosition();
    entity.setPosition(position.x + (direction.x / 10), position.y + (direction.y) / 10);
  }

  public void handleDispose(Fixture fixtureA, Fixture fixtureB) {
    ServiceLocator.getEntityService().scheduleForDisposal(entity);
  }
}
