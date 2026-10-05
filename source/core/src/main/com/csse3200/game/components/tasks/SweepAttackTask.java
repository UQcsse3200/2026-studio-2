package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class SweepAttackTask extends DefaultTask implements PriorityTask {
    public static final String ATTACK_START_EVENT = "attackStart";
    public static final String HIT_EVENT = "hitPlayer";
    public static final String SWEEP_HIT_EVENT = "sweepHit";

    private final Entity target;
    private final int priority;
    private final float range;
    private final float height;
    private final long windUpMs;
    private final long durationMs;
    private final long cooldownMs;
    private long swingStartTime;
    private long lastSwingEndTime;
    private boolean hasSwung;
    private boolean damageApplied;
    private boolean connected;
    private float facing = 1f;

    public SweepAttackTask(
            Entity target,
            int priority,
            float range,
            float height,
            float windUp,
            float duration,
            float cooldown) {
        this.target = target;
        this.priority = priority;
        this.range = range;
        this.height = height;
        this.windUpMs = toMillis(Math.max(0f, windUp));
        this.durationMs = Math.max(this.windUpMs, toMillis(Math.max(0f, duration)));
        this.cooldownMs = toMillis(Math.max(0f, cooldown));
    }

    @Override
    public void start() {
        super.start();
        Entity enemy = owner.getEntity();

        facing = target.getCenterPosition().x >= enemy.getCenterPosition().x ? 1f : -1f;
        swingStartTime = now();
        damageApplied = false;
        connected = false;

        PhysicsMovementComponent movement = enemy.getComponent(PhysicsMovementComponent.class);
        if (movement != null) {
            movement.setMoving(false);
        }

        enemy.getEvents().trigger(ATTACK_START_EVENT);
    }

    @Override
    public void update() {
        if (status != Status.ACTIVE) {
            start();
        }

        long elapsed = now() - swingStartTime;

        if (!damageApplied && elapsed >= windUpMs) {
            damageApplied = true;
            connected = applySweepDamage();
        }

        if (elapsed >= durationMs) {
            finishSwing();
        }
    }

    @Override
    public void stop() {
        if (status == Status.ACTIVE) {
            lastSwingEndTime = now();
            hasSwung = true;
        }
        super.stop();
    }

    @Override
    public int getPriority() {
        if (status == Status.ACTIVE) {
            return priority;
        }
        if (isOnCooldown()) {
            return -1;
        }
        return isTargetInSweepZone(directionToTarget()) ? priority : -1;
    }

    private void finishSwing() {
        status = Status.FINISHED;
        lastSwingEndTime = now();
        hasSwung = true;

        if (connected) {
            owner.getEntity().getEvents().trigger(HIT_EVENT);
            owner.getEntity().getEvents().trigger(SWEEP_HIT_EVENT);
        }
    }

    private boolean applySweepDamage() {
        if (!isTargetInSweepZone(facing)) {
            return false;
        }

        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        CombatStatsComponent ownStats = owner.getEntity().getComponent(CombatStatsComponent.class);
        if (targetStats != null && ownStats != null) {
            targetStats.hit(ownStats);
        }
        return true;
    }

    private boolean isTargetInSweepZone(float direction) {
        Vector2 enemyCentre = owner.getEntity().getCenterPosition();
        Vector2 targetCentre = target.getCenterPosition();
        Vector2 targetScale = target.getScale();

        float forward = (targetCentre.x - enemyCentre.x) * direction;
        float vertical = Math.abs(targetCentre.y - enemyCentre.y);
        float halfTargetWidth = targetScale.x / 2f;
        float halfTargetHeight = targetScale.y / 2f;

        return forward >= -halfTargetWidth
                && forward <= range + halfTargetWidth
                && vertical <= height / 2f + halfTargetHeight;
    }

    private float directionToTarget() {
        float dx = target.getCenterPosition().x - owner.getEntity().getCenterPosition().x;
        return dx >= 0f ? 1f : -1f;
    }

    private boolean isOnCooldown() {
        return hasSwung && now() - lastSwingEndTime < cooldownMs;
    }

    private static long toMillis(float seconds) {
        return (long) (seconds * 1000f);
    }

    private static long now() {
        GameTime time = ServiceLocator.getTimeSource();
        return time == null ? 0L : time.getTime();
    }
}