package com.csse3200.game.components.item.weapons.bow;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;

/** Ranged attack behaviour that fires player arrow variants (Standard, Cold, Fire, Grapple). */
public class BowComponent extends Component implements PrimaryWeapon {

    private static final String ATTACK_SOUND = "sounds/Impact4.ogg";
    private static final float BOW_COOLDOWN = 0.4f;

    @FunctionalInterface
    public interface ProjectileCreator {
        Entity create(Entity shooter, Vector2 position, Vector2 direction);
    }

    private ProjectileCreator projectileCreator;
    private ArrowType currentArrowType = ArrowType.STANDARD;
    private float cooldownTimer = 0f;

    public BowComponent() {
        this(ArrowType.STANDARD);
    }

    public BowComponent(ArrowType arrowType) {
        setArrowType(arrowType);
    }

    public BowComponent(ProjectileCreator projectileCreator) {
        this.projectileCreator = projectileCreator;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("attack", this::attack);
        entity.getEvents().addListener("setArrowType", this::setArrowType);
    }

    @Override
    public void update() {
        if (cooldownTimer > 0f) {
            cooldownTimer -= ServiceLocator.getTimeSource().getDeltaTime();
        }
    }

    /** Swaps the active arrow type fired by the bow. */
    public void setArrowType(ArrowType arrowType) {
        if (arrowType == null) {
            arrowType = ArrowType.STANDARD;
        }
        this.currentArrowType = arrowType;
        switch (arrowType) {
            case COLD:
                this.projectileCreator = ProjectileFactory::createColdArrow;
                break;
            case FIRE:
                this.projectileCreator = ProjectileFactory::createFireArrow;
                break;
            case GRAPPLE:
                this.projectileCreator = ProjectileFactory::createGrappleArrow;
                break;
            case STANDARD:
            default:
                this.projectileCreator = ProjectileFactory::createPlayerArrow;
                break;
        }
    }

    public ArrowType getArrowType() {
        return currentArrowType;
    }

    @Override
    public void attack(Vector2 direction) {
        if (direction == null || direction.isZero() || !isReady()) {
            return;
        }

        cooldownTimer = BOW_COOLDOWN;
        Vector2 normalizedDirection = direction.cpy().nor();
        Vector2 spawnPosition =
                entity.getCenterPosition().mulAdd(normalizedDirection, entity.getScale().x * 0.8f);

        Entity projectile = projectileCreator.create(entity, spawnPosition, normalizedDirection);
        ServiceLocator.getEntityService().register(projectile);

        if (ServiceLocator.getResourceService() != null
                && ServiceLocator.getResourceService().containsAsset(ATTACK_SOUND, Sound.class)) {
            Sound attackSound = ServiceLocator.getResourceService().getAsset(ATTACK_SOUND, Sound.class);
            if (attackSound != null) {
                attackSound.play();
            }
        }

        entity.getEvents().trigger("attackAnimation", normalizedDirection.cpy());
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