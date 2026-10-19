package com.csse3200.game.components.level;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;

/**
 * The end of a level. The player has to walk into the entity's sensor and press F to use it. That
 * triggers a "levelEndActivated" event, so the entity can light up, and then runs {@link
 * #activate()} once, a moment later so the player sees it light up first.
 */
public abstract class LevelEndComponent extends InputComponent {
  private static final int INPUT_PRIORITY = 9;
  private static final float ACTIVATION_DELAY = 0.6f;

  private boolean playerInRange;
  private boolean activated;
  private boolean finished;
  private float activationTimer;

  protected LevelEndComponent() {
    super(INPUT_PRIORITY);
  }

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
  }

  /** Runs once, shortly after the player presses F while standing in range. */
  protected abstract void activate();

  /**
   * @return true while the player is standing close enough to use the level end
   */
  public boolean isPlayerInRange() {
    return playerInRange;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode != Input.Keys.F
        || !playerInRange
        || activated
        || ServiceLocator.getEntityService().getPaused()) {
      return false;
    }
    activated = true;
    entity.getEvents().trigger("levelEndActivated");
    return true;
  }

  @Override
  public void update() {
    if (!activated || finished) {
      return;
    }
    activationTimer += ServiceLocator.getTimeSource().getDeltaTime();
    if (activationTimer >= ACTIVATION_DELAY) {
      finished = true;
      activate();
    }
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (isPlayer(other)) {
      playerInRange = true;
    }
  }

  private void onCollisionEnd(Fixture me, Fixture other) {
    if (isPlayer(other)) {
      playerInRange = false;
    }
  }

  private static boolean isPlayer(Fixture fixture) {
    return fixture != null
        && fixture.getBody() != null
        && fixture.getBody().getUserData() instanceof BodyUserData data
        && data.entity != null
        && data.entity.getComponent(PlayerActions.class) != null;
  }
}
