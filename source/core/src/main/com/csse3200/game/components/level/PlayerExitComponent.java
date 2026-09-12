package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Completes an unlocked exit while the player overlaps it, outside the physics step. */
public class PlayerExitComponent extends Component {
  private final Entity player;
  private final BooleanSupplier unlocked;
  private final Runnable onExit;
  private final Set<Fixture> contacts = new HashSet<>();
  private boolean completed;

  public PlayerExitComponent(Entity player, BooleanSupplier unlocked, Runnable onExit) {
    this.player = player;
    this.unlocked = unlocked;
    this.onExit = onExit;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onStart);
    entity.getEvents().addListener("collisionEnd", this::onEnd);
  }

  private void onStart(Fixture me, Fixture other) {
    if (other.getBody().getUserData() instanceof BodyUserData data && data.entity == player) {
      contacts.add(other);
    }
  }

  private void onEnd(Fixture me, Fixture other) {
    contacts.remove(other);
  }

  @Override
  public void update() {
    if (!completed && !contacts.isEmpty() && unlocked.getAsBoolean()
        && !ServiceLocator.getEntityService().getPaused()) {
      completed = true;
      onExit.run();
    }
  }
}
