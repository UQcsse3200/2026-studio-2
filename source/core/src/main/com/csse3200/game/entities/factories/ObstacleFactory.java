package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
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
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create obstacle entities.
 *
 * <p>Each obstacle entity type should have a creation method that returns a corresponding entity.
 */
public class ObstacleFactory {

  /**
   * Creates a tree entity.
   *
   * @return entity
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
   * @param grappleSides number of sides that can be grappled
   * @return platform entity
   */
  public static Entity createPlatform(int grappleSides, String textureFilepath) {
    Entity platform =
        new Entity()
            .addComponent(new TextureRenderComponent(textureFilepath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND))
            .addComponent(new PlatformGrappleComponent(grappleSides));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return platform;
  }

  /**
   * Creates a normal moving platform.
   *
   * @param grappleSides number of sides that can be grappled
   * @param firstTarget first movement target
   * @param secondTarget second movement target
   * @param maxSpeed maximum movement speed
   * @param activateId activation ID
   * @return moving platform entity
   */
  public static Entity createMovingPlatform(
      int grappleSides,
      String textureFilepath,
      Vector2 firstTarget,
      Vector2 secondTarget,
      Vector2 maxSpeed,
      String activateId) {

    PhysicsComponent physicsComponent = new PhysicsComponent();
    ColliderComponent colliderComponent = new ColliderComponent();

    Entity movingPlatform =
        new Entity()
            .addComponent(new TextureRenderComponent(textureFilepath))
            .addComponent(physicsComponent)
            .addComponent(new PhysicsMovementComponent())
            .addComponent(colliderComponent.setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(
                new MovingPlatformComponent(grappleSides, firstTarget, secondTarget, maxSpeed))
            .addComponent(new PlatformGrappleComponent(grappleSides))
            .addComponent(new ActivatableComponent(activateId));

    physicsComponent.setBodyType(BodyType.KinematicBody);
    colliderComponent.setFriction(1.5f);

    return movingPlatform;
  }

  /**
   * Creates a normal crumbling platform.
   *
   * @param grappleSides number of sides that can be grappled
   * @param timeBeforeCrumble how long the player can stand on the platform before it starts to
   *     crumble, in seconds
   * @param crumbleTime how long the crumbling takes before the platform is destroyed, in seconds
   * @return crumbling platform entity
   */
  public static Entity createCrumblingPlatform(
      int grappleSides, String textureFilepath, float timeBeforeCrumble, float crumbleTime) {

    Entity platform =
        new Entity()
            .addComponent(new TextureRenderComponent(textureFilepath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND))
            .addComponent(
                new CrumblingPlatformComponent(grappleSides, timeBeforeCrumble, crumbleTime));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return platform;
  }

  /**
   * Creates a normal triggerable platform.
   *
   * @param grappleSides number of sides that can be grappled
   * @return triggerable platform entity
   */
  public static Entity createTriggerablePlatform(
      int grappleSides, String textureFilepath, String activationId) {
    Entity platform =
        new Entity()
            .addComponent(new DynamicTextureRenderComponent(textureFilepath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND))
            .addComponent(new PlatformGrappleComponent(grappleSides))
            .addComponent(new ActivatableComponent(activationId))
            .addComponent(new TriggerablePlatformComponent());

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    platform.setEnabled(false);

    return platform;
  }

  /**
   * Creates a win condition entity.
   *
   * @return win condition entity
   */
  public static Entity createButton(String activationId) {
    RotatableAnimationRenderComponent animator =
        new RotatableAnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/in_level_button.atlas", TextureAtlas.class));
    animator.addAnimation("default", 1f, Animation.PlayMode.LOOP);
    animator.addAnimation("pressed", 0.075f, Animation.PlayMode.NORMAL);
    animator.startAnimation("default");

    Entity button =
        new Entity()
            .addComponent(animator)
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new ActivatableComponent(activationId))
            .addComponent(new TriggerButtonComponent())
            .addComponent(new RotatableMapComponent(90f));

    button.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return button;
  }

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
   * Creates the normal floor used by the other levels.
   *
   * @param grappleSides number of sides that can be grappled
   * @return floor entity
   */
  public static Entity createFloor(int grappleSides) {
    Entity floor =
        new Entity()
            .addComponent(new TiledRenderComponent("images/Tile_2.png", 0.75f))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new PlatformGrappleComponent(grappleSides));

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

    wall.setScale(width, height);

    return wall;
  }

  /**
   * Creates a ledge entity.
   *
   * @return ledge entity
   */
  public static Entity createLedge() {
    Entity ledge =
        new Entity()
            .addComponent(new TextureRenderComponent("images/platform.png"))
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
        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.DEFAULT));
  }

  /**
   * Creates a spike hazard entity with custom rotation.
   *
   * @param rotationAngle angle in degrees to rotate the spike
   * @return spike entity
   */
  public static Entity createSpike(float rotationAngle) {
    Entity spike =
        new Entity()
            .addComponent(new DynamicTextureRenderComponent("images/spike.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new CombatStatsComponent(100, 2))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER))
            .addComponent(new RotatableMapComponent(90));

    spike.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    // Scale slightly larger to close gaps
    spike.setScale(1.25f, 1.25f);

    PhysicsUtils.setScaledCollider(spike, 0.8f, 0.5f);

    return spike;
  }

  /**
   * Creates a default upward-facing spike hazard.
   *
   * @return spike entity
   */
  public static Entity createSpike() {
    return createSpike(0f);
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
