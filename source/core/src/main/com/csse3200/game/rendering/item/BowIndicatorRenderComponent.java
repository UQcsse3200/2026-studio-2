package com.csse3200.game.rendering.item;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.factories.ProjectileFactory;

/**
 * Arc preview for the bow: shown while a shot is being drawn, for whichever arrow type was loaded.
 * A longer draw fires faster, so the arc visibly stretches out and flattens the longer you hold,
 * and disappears the instant you let go and the arrow leaves.
 */
public class BowIndicatorRenderComponent extends ArcIndicatorRenderComponent {

  private BowComponent bow;
  private KeyboardPlayerInputComponent input;

  @Override
  public void create() {
    super.create();
    bow = entity.getComponent(BowComponent.class);
    input = entity.getComponent(KeyboardPlayerInputComponent.class);
  }

  @Override
  protected boolean isActive() {
    return bow != null && bow.isCharging() && input != null;
  }

  @Override
  protected Vector2 aimOffset() {
    return input.getMouseAimDirection();
  }

  @Override
  protected Vector2 launchPoint(Vector2 aim) {
    return entity.getCenterPosition().mulAdd(aim, entity.getScale().x * BowComponent.SPAWN_OFFSET);
  }

  @Override
  protected float launchSpeed() {
    return ProjectileFactory.getArrowSpeed(bow.getArrowType()) * bow.currentSpeedMultiplier();
  }

  @Override
  protected float maxRange() {
    return ProjectileFactory.getArrowRange(bow.getArrowType());
  }
}
