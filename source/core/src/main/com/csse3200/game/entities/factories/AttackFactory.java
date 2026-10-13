package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.DurationComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

public class AttackFactory {
  public static Entity createNewAttack(
      Vector2 size,
      Vector2 position,
      int damage,
      float duration,
      float knockback,
      Entity ownerEnemy) {

    Entity attack =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.KinematicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC).setAsBox(size, position))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, knockback, ownerEnemy))
            .addComponent(new DurationComponent(duration));

    return attack;
  }
}
