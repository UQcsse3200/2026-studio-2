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
    killPlayer(fixtureB);
    ServiceLocator.getEntityService().scheduleForDisposal(entity);
  }

  /**
   * Kills the player if the other fixture belongs to them. The ball is disposed on its first
   * contact, before TouchAttackComponent's delayed hit can land, so the kill is applied here.
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
    if (stats == null || stats.isDead()) {
      return;
    }

    // Same events as a killing enemy hit in CombatStatsComponent.hit: updateHealth and death, then
    // hurt.
    stats.setHealth(0);
    data.entity.getEvents().trigger("hurt");
  }
}
