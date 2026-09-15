package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class SpikyBallComponent extends Component {
  public Vector2 direction;
  private final float speed = 5f;

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

    float delta = ServiceLocator.getTimeSource().getDeltaTime();

    Vector2 position = entity.getPosition();
    entity.setPosition(
        position.x + (direction.x * speed * delta), position.y + (direction.y * speed * delta));
  }

  public void handleDispose(Fixture fixtureA, Fixture fixtureB) {
    ServiceLocator.getEntityService().scheduleForDisposal(entity);
  }
}
