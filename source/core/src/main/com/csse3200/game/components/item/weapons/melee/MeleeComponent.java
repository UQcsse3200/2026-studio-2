package com.csse3200.game.components.item.weapons.melee;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Close-range attack that damages the first enemy directly in front of the entity. */
public class MeleeComponent extends Component implements PrimaryWeapon {

    private static final float RANGE = 1f;
    private static final float SWORD_COOLDOWN = 0.3f;

    private float cooldownTimer = 0f;

    @Override
    public void create() {
        entity.getEvents().addListener("attack", this::attack);
        entity.getEvents().addListener("melee", this::attack);
    }

    @Override
    public void update() {
        if (cooldownTimer > 0f) {
            cooldownTimer -= ServiceLocator.getTimeSource().getDeltaTime();
        }
    }

    @Override
    public void attack(Vector2 direction) {
        if (direction == null || direction.isZero() || !isReady()) {
            return;
        }

        cooldownTimer = SWORD_COOLDOWN;

        // Cast a short ray out from the entity and stop at the first NPC it touches
        Vector2 origin = entity.getCenterPosition();
        Vector2 reach = origin.cpy().mulAdd(direction.cpy().nor(), RANGE);
        RaycastHit hit = new RaycastHit();

        if (!ServiceLocator.getPhysicsService()
                .getPhysics()
                .raycast(origin, reach, PhysicsLayer.NPC, hit)) {
            return;
        }

        Object userData = hit.fixture.getBody().getUserData();
        if (!(userData instanceof BodyUserData)) {
            return;
        }

        Entity target = ((BodyUserData) userData).entity;
        if (target == null) {
            return;
        }

        // Damage comes from the entity's own combat stats
        CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            stats.hit(entity.getComponent(CombatStatsComponent.class));
        }

        entity.getEvents().trigger("attackAnimation", direction.cpy().nor());
    }

    @Override
    public boolean isReady() {
        return cooldownTimer <= 0f;
    }

    @Override
    public float getCooldownRemaining() {
        return Math.max(0f, cooldownTimer);
    }
}