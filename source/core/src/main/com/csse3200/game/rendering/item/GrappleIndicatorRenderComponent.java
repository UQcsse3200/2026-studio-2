package com.csse3200.game.rendering.item;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.factories.ProjectileFactory;

/**
 * Arc preview for the grapple: shown while the shoot button is held with the rope arrow selected.
 * It charges like the bow does - a tap lobs the arrow a short way and a longer hold flings it
 * further, so the arc stretches out as the draw builds - and it's gone the instant the button is
 * released and the shot fires.
 */
public class GrappleIndicatorRenderComponent extends ArcIndicatorRenderComponent {

  private GrappleComponent grapple;
  private InventoryComponent inventory;
  private KeyboardPlayerInputComponent input;

  @Override
  public void create() {
    super.create();
    grapple = entity.getComponent(GrappleComponent.class);
    inventory = entity.getComponent(InventoryComponent.class);
    input = entity.getComponent(KeyboardPlayerInputComponent.class);
  }

  @Override
  protected boolean isActive() {
    // isCharging() alone already implies not attached and not on cooldown.
    return grapple != null
        && grapple.isCharging()
        && inventory != null
        && inventory.getSelectedItem() == ItemType.ROPE_ARROW
        && input != null;
  }

  @Override
  protected Vector2 aimOffset() {
    return input.getMouseAimDirection();
  }

  @Override
  protected Vector2 launchPoint(Vector2 aim) {
    return entity
        .getCenterPosition()
        .mulAdd(aim, entity.getScale().x * GrappleComponent.SPAWN_OFFSET);
  }

  @Override
  protected float launchSpeed() {
    return ProjectileFactory.GRAPPLE_ARROW_SPEED * grapple.currentSpeedMultiplier();
  }

  @Override
  protected float maxRange() {
    return ProjectileFactory.GRAPPLE_ARROW_RANGE;
  }
}
