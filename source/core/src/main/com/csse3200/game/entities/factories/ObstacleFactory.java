package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.level.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.*;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TiledRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory to create obstacle entities.
 *
 * <p>Each obstacle entity type should have a creation method that returns a corresponding entity.
 */
public class ObstacleFactory {

  /**
   * Creates a tree entity.
   *
   * @return tree entity
   */
  public static Entity createTree() {
    Entity tree =
        new Entity()
            .addComponent(new TextureRenderComponent("images/tree.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE));

    tree.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    tree.getComponent(TextureRenderComponent.class).scaleEntity();
    tree.scaleHeight(2.5f);
    PhysicsUtils.setScaledCollider(tree, 0.5f, 0.2f);

    return tree;
  }

  /**
   * Creates the normal platform used by the other levels.
   *
   * <p>Level 2's tiled ground uses TiledRenderComponent so the tile texture repeats instead of
   * stretching across the entire floor.
   *
   * @param config configuration object for this platform
   * @return platform entity
   */
  public static Entity createPlatform(PlatformConfig config) {

    Entity platform = new Entity();

    /*
     * Use the tiled renderer only for the Level 2 ground texture.
     * All normal platforms continue to use TextureRenderComponent.
     */
    if ("images/terrain/Level_2/level_2_tile.png".equals(config.textureFilepath)) {

      platform.addComponent(
          new TiledRenderComponent("images/terrain/Level_2/level_2_tile.png", 0.75f));

    } else {

      platform.addComponent(new TextureRenderComponent(config.textureFilepath));
    }

    platform
        .addComponent(new PhysicsComponent().setBodyType(BodyType.KinematicBody))
        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND))
        .addComponent(new PlatformGrappleComponent(config.grappleSides));

    return platform;
  }

  /**
   * Creates a normal moving platform.
   *
   * @param config configuration object for this platform
   * @return moving platform entity
   */
  public static Entity createMovingPlatform(MovingPlatformConfig config) {

    PhysicsComponent physicsComponent = new PhysicsComponent();
    ColliderComponent colliderComponent = new ColliderComponent();

    Entity movingPlatform =
        new Entity()
            .addComponent(new TextureRenderComponent(config.textureFilepath))
            .addComponent(physicsComponent)
            .addComponent(new PhysicsMovementComponent())
            .addComponent(colliderComponent.setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(
                new MovingPlatformComponent(
                    config.grappleSides,
                    config.getFirstTarget(),
                    config.getSecondTarget(),
                    config.getSpeed()))
            .addComponent(new PlatformGrappleComponent(config.grappleSides))
            .addComponent(new ActivatableComponent(config.activateIds));

    // Grapple edge visuals disabled while the grapple_tile texture is removed.

    physicsComponent.getBody().setGravityScale(0f);
    physicsComponent.setBodyType(BodyType.KinematicBody);
    colliderComponent.setFriction(1.5f);

    return movingPlatform;
  }

  /**
   * Creates a normal crumbling platform.
   *
   * @param config configuration object for this platform
   * @return crumbling platform entity
   */
  public static Entity createCrumblingPlatform(CrumblingPlatformConfig config) {
    Entity platform =
        new Entity()
            .addComponent(new DynamicTextureRenderComponent(config.textureFilepath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(
                new CrumblingPlatformComponent(
                    config.grappleSides,
                    config.getTimeBeforeCrumble(),
                    config.getCrumbleTime(),
                    config.getRespawnTime()));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return platform;
  }

  /**
   * Creates a normal triggerable platform.
   *
   * @param config configuration object for this platform
   * @return triggerable platform entity
   */
  public static Entity createTriggerablePlatform(TriggerablePlatformConfig config) {
    Entity platform =
        new Entity()
            .addComponent(new DynamicTextureRenderComponent(config.textureFilepath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new PlatformGrappleComponent(config.grappleSides))
            .addComponent(new ActivatableComponent(config.getInitialState(), config.getIds()))
            .addComponent(new TriggerablePlatformComponent());

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return platform;
  }

  /**
   * Creates a new trigger button entity.
   *
   * @param config configuration to use for the creation of this button
   * @return button entity
   */
  public static Entity createButton(TriggerButtonConfig config) {
    RotatableAnimationRenderComponent animator =
        new RotatableAnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/ui/in_level_button.atlas", TextureAtlas.class));

    animator.addAnimation("default", 1f, Animation.PlayMode.LOOP);
    animator.addAnimation("pressed", 0.075f, Animation.PlayMode.NORMAL);
    animator.startAnimation("default");

    Entity button =
        new Entity()
            .addComponent(animator)
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.DEFAULT))
            .addComponent(new ActivatableComponent(config.getIds()))
            .addComponent(new TriggerButtonComponent())
            .addComponent(new RotatableMapComponent(config.getRotation()));

    // If attach is requested, add component.
    if (config.getAttached()) {
      button.addComponent(new AttachableMapComponent());
    }

    button.getComponent(PhysicsComponent.class).setBodyType(BodyType.KinematicBody);

    return button;
  }

  public static Entity createSlipperyPlatform(SlipperyPlatformConfig config) {
    Entity platform =
        new Entity()
            .addComponent(new DynamicTextureRenderComponent(config.textureFilepath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new PlatformGrappleComponent(config.grappleSides))
            .addComponent(
                new SlipperyPlatformComponent(
                    config.getSlipperiness(), config.getMaxGrappleTime()));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return platform;
  }

  /**
   * Creates a win condition entity.
   *
   * @return win condition entity
   */
  public static Entity createWinConEntity() {
    ColliderComponent collider = new ColliderComponent();
    collider.setLayer(PhysicsLayer.NPC);
    collider.setSensor(true);

    Entity winCon =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(collider)
            .addComponent(new WinConditionComponent());

    winCon.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    winCon.getComponent(ColliderComponent.class).setAsBox(new Vector2(2f, 2f));

    return winCon;
  }

  /**
   * Creates a new sensor entity that detects a collision from the player and triggers a level swap
   *
   * @param nextLevelName a String representing the name of the level this component should force
   *     the game screen to swap to
   * @return a level trigger entity
   */
  public static Entity createNextLevelTriggerEntity(String nextLevelName) {
    ColliderComponent collider = new ColliderComponent();
    collider.setLayer(PhysicsLayer.NPC);
    collider.setSensor(true);

    Entity trigger =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(collider)
            .addComponent(new LevelTriggerComponent(nextLevelName));

    trigger.getComponent(ColliderComponent.class).setAsBox(new Vector2(2f, 3f));

    return trigger;
  }

  /**
   * Creates the normal floor used by the other levels.
   *
   * @param config configuration object for the floor
   * @return floor entity
   */
  public static Entity createFloor(PlatformConfig config) {
    Entity floor =
        new Entity()
            .addComponent(new TiledRenderComponent(config.textureFilepath, 0.75f))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new PlatformGrappleComponent(config.grappleSides));

    // Grapple edge visuals are disabled while the grapple_tile texture is removed. The grapple
    // logic itself (PlatformGrappleComponent) still applies.
    floor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return floor;
  }

  /**
   * Creates an invisible physics wall.
   *
   * @param width wall width in world units
   * @param height wall height in world units
   * @return wall entity
   */
  public static Entity createWall(float width, float height) {
    Entity wall =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
    //// .addComponent(new ColliderComponent().setLayer(PhysicsLayer.WALL));
    wall.setScale(width, height);

    return wall;
  }

  /**
   * Creates a ledge entity.
   *
   * @param config configuration object for this ledge
   * @return ledge entity
   */
  public static Entity createLedge(PlatformConfig config) {
    Entity ledge =
        new Entity()
            .addComponent(new TextureRenderComponent(config.textureFilepath))
            .addComponent(new LedgeComponent())
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    return ledge;
  }

  /**
   * Creates a statue that is able to be stood in front of.
   *
   * @return statue entity
   */
  public static Entity createStatue() {
    return new Entity()
        .addComponent(new TextureRenderComponent("images/Greek Statues Pack I/Brute.png"))
        .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE));
  }

  /**
   * Creates a default upward-facing spike hazard entity.
   *
   * @param config configuration object for the spike
   * @return spike entity
   */
  public static Entity createSpike(SpikeClusterConfig config) {
    return createSpike(config, "images/terrain/Level_1/Level_1_Spike.png");
  }

  /**
   * Creates an upward-facing spike hazard entity that uses the given texture, so each level can
   * have its own spike design.
   *
   * @param config configuration object for the spike
   * @param texturePath texture to draw the spike with
   * @return spike entity
   */
  public static Entity createSpike(SpikeClusterConfig config, String texturePath) {
    Entity spike =
        new Entity()
            .addComponent(new DynamicTextureRenderComponent(texturePath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new CombatStatsComponent(100, 2))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER))
            .addComponent(new RotatableMapComponent(config.getRotation()));

    // If attachment is requested, add component.
    if (config.getAttached()) {
      spike.addComponent(new AttachableMapComponent());
    }

    spike.getComponent(PhysicsComponent.class).setBodyType(BodyType.KinematicBody);

    // Scale slightly larger to close grid gaps
    spike.setScale(1.25f, 1.25f);

    PhysicsUtils.setScaledCollider(spike, 0.8f, 0.5f);

    return spike;
  }

  /**
   * Creates a spiky ball trap that determines the shoot direction for its children spiky balls
   * based on rotation
   *
   * @param config the SpikyBallTrapConfig file that sets settings for this instance of the trap
   * @return a spiky ball trap entity ready to be registered
   */
  public static Entity createSpikyBallTrap(SpikyBallTrapConfig config) {
    // calculate which direction this trap's balls should move based on the rotation configured
    float rotation = config.getRotation();
    Vector2 direction =
        switch ((int) rotation) {
          case 0 -> new Vector2(0, 1);
          case 90 -> new Vector2(-1, 0);
          case 180 -> new Vector2(0, -1);
          case 270 -> new Vector2(1, 0);
          default -> new Vector2(0, 0);
        };

    Entity trap =
        new Entity()
            .addComponent(new DynamicTextureRenderComponent("images/traps/spiky_ball_trap.png"))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ActivatableComponent(config.getIds()))
            .addComponent(
                new SpawnerComponent(
                    new ArrayList<>(List.of(() -> ObstacleFactory.createSpikyBall(direction))),
                    config.getSpawnInterval(),
                    -1,
                    config.getInitialState(),
                    config.getMode()))
            .addComponent(new RotatableMapComponent(config.getRotation()));

    return trap;
  }

  /**
   * Creates a child spiky ball to be emitted from a spiky ball trap entity
   *
   * @param moveDirection the direction to move the ball in
   * @return a child spiky ball entity that handles its own movement
   */
  public static Entity createSpikyBall(Vector2 moveDirection) {
    Entity spikyBall =
        new Entity()
            .addComponent(new TextureRenderComponent("images/traps/spiky_ball.png"))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.DynamicBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new CombatStatsComponent(100, 4))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER))
            .addComponent(new SpikyBallComponent(moveDirection));

    spikyBall.setScale(0.75f, 0.75f);

    spikyBall.getComponent(PhysicsComponent.class).getBody().setGravityScale(0f);

    return spikyBall;
  }

  /**
   * Creates two entities - the water itself that contains the visual aspect and handles all the
   * movement, and the hitbox for the water. They are required to be two separate entities as the
   * way water grows (scaling the entity) does not scale the hitbox, meaning the hitbox desyncs from
   * the top of the water.
   *
   * @param speed the default speed the water should rise at when spawning
   * @param initialHeight the initial height of the water when spawning into the level
   * @return the water entity with the hitbox attached to it in the RisingWaterComponent ready to be
   *     registered. Note: the RisingWaterComponent handles the registration of the hitbox entity,
   *     so only the returned entity from this method requires registration
   */
  public static Entity createRisingWaterEntity(float speed, float initialHeight) {
    Entity waterHitbox =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.KinematicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE));

    // ensure the hitbox always tries to collide with the player
    waterHitbox.getComponent(PhysicsComponent.class).getBody().setSleepingAllowed(false);

    Entity water =
        new Entity()
            .addComponent(new TiledRenderComponent("images/terrain/Level_3/water tile.png", 1f))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.KinematicBody))
            .addComponent(new ActivatableComponent(true, new String[] {"waterStop", "waterStart"}))
            .addComponent(new RisingWaterComponent(speed, initialHeight, waterHitbox));

    return water;
  }

  /**
   * Creates an entity capable of spawning enemies in the level
   *
   * @param config the EnemySpawnerConfig that determines what enemies should be spawned and how
   *     often etc.
   * @return the created spawner entity ready to be registered in the level
   */
  public static Entity createEnemySpawnerEntity(EnemySpawnerConfig config) {
    SpawnerConfig sData = config.spawnData;
    SpawnerComponent spawnerComp =
        new SpawnerComponent(
            sData.spawns, sData.spawnInterval, sData.maxSpawns, sData.active, sData.mode);
    spawnerComp.setIds(config.completionIds);

    Entity spawner =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(spawnerComp)
            .addComponent(new ActivatableComponent(sData.active, config.ids));

    return spawner;
  }

  /**
   * Creates a trigger entity that when collided with, emits signals to all ids
   *
   * @param c the config file to specify settings for this trigger
   * @return a collider trigger Entity object to register with the entity service
   */
  public static Entity createTriggerEntity(TriggerConfig c) {
    ColliderComponent collider = new ColliderComponent();
    collider.setLayer(PhysicsLayer.NPC);
    collider.setSensor(true);

    Entity trigger =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(collider)
            .addComponent(new TriggerComponent(c.ids, c.oneTimeActivation));

    return trigger;
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
