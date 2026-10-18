package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;

public class SpikyBallComponent extends Component {
  public Vector2 direction;
  private static final float SPEED = 5f;

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
        position.x + (direction.x * SPEED * delta), position.y + (direction.y * SPEED * delta));
  }

  public void handleDispose(Fixture fixtureA, Fixture fixtureB) {
    killPlayer(fixtureB);
    ServiceLocator.getEntityService().scheduleForDisposal(entity);
  }

  /**
   * Damages the player if the other fixture belongs to them. The ball is disposed on its first
   * contact, before TouchAttackComponent's delayed hit can land, so the hit is applied here using
   * the ball's own attack value.
   *
   * @param other the fixture this ball collided with
   */
  private void killPlayer(Fixture other) {
    if (other.getBody() == null
        || !(other.getBody().getUserData() instanceof BodyUserData data)
        || data.entity == null
        || data.entity.getComponent(PlayerActions.class) == null) {
      return;
    }

    CombatStatsComponent stats = data.entity.getComponent(CombatStatsComponent.class);
    CombatStatsComponent ballStats = entity.getComponent(CombatStatsComponent.class);
    if (stats == null || ballStats == null || stats.isDead()) {
      return;
    }

    stats.hit(ballStats);
  }
}
