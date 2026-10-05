package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * When this entity touches a valid enemy's hitbox, deal damage to them and apply a knockback.
 *
 * <p>Requires CombatStatsComponent, HitboxComponent on this entity.
 *
 * <p>Damage is only applied if target entity has a CombatStatsComponent. Knockback is only applied
 * if target entity has a PhysicsComponent.
 */
public class TouchAttackComponent extends Component {
  private short targetLayer;
  private float knockbackForce = 0f;
  private CombatStatsComponent combatStats;
  private HitboxComponent hitboxComponent;
  private boolean hitPlayer = false;

  // Behaviour to ensure continuous collision is punished (not only at start)
  private float touchTimer = 0f;
  private static final float DELAY = 0.25f; // small delay to avoid spam checking each frame
  private Fixture targetFixture;

  /**
   * Create a component which attacks entities on collision, without knockback.
   *
   * @param targetLayer The physics layer of the target's collider.
   */
  public TouchAttackComponent(short targetLayer) {
    this.targetLayer = targetLayer;
  }

  /**
   * Create a component which attacks entities on collision, with knockback.
   *
   * @param targetLayer The physics layer of the target's collider.
   * @param knockback The magnitude of the knockback applied to the entity.
   */
  public TouchAttackComponent(short targetLayer, float knockback) {
    this.targetLayer = targetLayer;
    this.knockbackForce = knockback;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
    combatStats = entity.getComponent(CombatStatsComponent.class);
    hitboxComponent = entity.getComponent(HitboxComponent.class);
  }

  @Override
  public void update() {
    if (hitPlayer) {
      entity.getEvents().trigger("hitPlayer");
      hitPlayer = false;
    }

    if (targetFixture == null) {
      touchTimer = 0f;
      return;
    }

    touchTimer += ServiceLocator.getTimeSource().getDeltaTime();

    if (touchTimer >= DELAY) {
      attack(targetFixture);

      touchTimer = 0f;
    }
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (hitboxComponent.getFixture() != me) {
      // Not triggered by hitbox, ignore
      return;
    }

    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) {
      // Doesn't match our target layer, ignore
      return;
    }

    targetFixture = other;
    touchTimer = 0f;
    attack(other);
  }

  private void onCollisionEnd(Fixture me, Fixture other) {
    if (hitboxComponent.getFixture() != me) {
      // Not triggered by hitbox, ignore
      return;
    }

    if (targetFixture == other) {
      targetFixture = null;
    }
  }

  private void attack(Fixture other) {
    Entity target = ((BodyUserData) other.getBody().getUserData()).entity;
    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
    PhysicsComponent physicsComponent = target.getComponent(PhysicsComponent.class);

    if (targetStats != null) {
      // Apply knockback
      if (physicsComponent != null && knockbackForce > 0f) {
        Body targetBody = physicsComponent.getBody();
        Vector2 direction = target.getCenterPosition().sub(entity.getCenterPosition());
        Vector2 impulse = direction.setLength(knockbackForce);
        targetBody.applyLinearImpulse(impulse, targetBody.getWorldCenter(), true);
      }
      // Try to attack target.
      targetStats.hit(combatStats);
      hitPlayer = true;
    }
  }
}
