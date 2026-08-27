package com.csse3200.game.components;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;

public class DelayedAttackComponent extends Component {
    short targetLayer;
    boolean attacking = false;
    private HitboxComponent hitboxComponent;

    public DelayedAttackComponent(short targetLayer) {
        this.targetLayer = targetLayer;
    }

    public void create() {
        entity.getEvents().addListener("attackAnimationFinished", this::finishAttack);
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        hitboxComponent = entity.getComponent(HitboxComponent.class);
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

        if (attacking) {
            // Already attacking, ignore
            return;
        }

        attacking = true;
        entity.getEvents().trigger("attack");
    }

    private void finishAttack() {
        attacking = false;
    }
}
