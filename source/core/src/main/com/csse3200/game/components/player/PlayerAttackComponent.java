package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;
import java.util.function.BiFunction;

/** Handles player ranged-attack events and spawns standard arrows. */
public class PlayerAttackComponent extends Component {
  private static final String ATTACK_SOUND = "sounds/Impact4.ogg";

  private final BiFunction<Vector2, Vector2, Entity> projectileFactory;

  /** Creates a player attack component which fires standard arrows. */
  public PlayerAttackComponent() {
    this(ProjectileFactory::createPlayerArrow);
  }

  PlayerAttackComponent(BiFunction<Vector2, Vector2, Entity> projectileFactory) {
    this.projectileFactory = projectileFactory;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("fireArrow", this::fireArrow);
  }

  private void fireArrow(Vector2 direction) {
    if (direction == null || direction.isZero()) {
      return;
    }

    Vector2 normalizedDirection = direction.cpy().nor();
    Vector2 spawnPosition =
        entity.getCenterPosition().mulAdd(normalizedDirection, entity.getScale().x * 0.6f);
    Entity projectile = projectileFactory.apply(spawnPosition, normalizedDirection);
    ServiceLocator.getEntityService().register(projectile);

    Sound attackSound = ServiceLocator.getResourceService().getAsset(ATTACK_SOUND, Sound.class);
    attackSound.play();
    entity.getEvents().trigger("attackAnimation", normalizedDirection.cpy());
  }
}
