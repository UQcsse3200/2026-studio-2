package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.level.MovingPlatformComponent;
import com.csse3200.game.components.level.PlatformGrappleComponent;
import com.csse3200.game.components.level.WinConditionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TiledRenderComponent;

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

  public static Entity createPlatform(int grappleSides) {
    return createPlatform(grappleSides, false);
  }

  /**
   * Creates a platform entity.
   *
   * @param grappleSides sides the platform can be grappled from
   * @param tall use the tall platform art instead of stretching the default wide plank onto a
   *     tall/square shape
   * @return platform entity
   */
  public static Entity createPlatform(int grappleSides, boolean tall) {
    String texturePath;
    if (tall) {
      texturePath = "images/tall_platform.png";
    } else if (grappleSides != 0) {
      texturePath = "images/hook_platform.png";
    } else {
      texturePath = "images/platform.png";
    }

    return createPlatform(grappleSides, texturePath);
  }

  /**
   * Creates a platform entity using a custom texture, so a level can use different platform art
   * without changing the default platform art used elsewhere.
   *
   * @param grappleSides sides the platform can be grappled from
   * @param texturePath platform texture to use
   * @return platform entity
   */
  public static Entity createPlatform(int grappleSides, String texturePath) {
    Entity platform =
        new Entity()
            .addComponent(new TextureRenderComponent(texturePath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND))
            .addComponent(new PlatformGrappleComponent(grappleSides));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return platform;
  }

  public static Entity createMovingPlatform(
      int grappleSides, Vector2 firstTarget, Vector2 secondTarget, Vector2 maxSpeed) {
    return createMovingPlatform(
        grappleSides, firstTarget, secondTarget, maxSpeed, "images/platform.png");
  }

  /**
   * Creates a moving platform using a custom texture, so a level can use different platform art
   * without changing the default used elsewhere.
   *
   * @param grappleSides sides the platform can be grappled from
   * @param firstTarget first position the platform moves between
   * @param secondTarget second position the platform moves between
   * @param maxSpeed movement speed
   * @param texturePath platform texture to use
   * @return moving platform entity
   */
  public static Entity createMovingPlatform(
      int grappleSides,
      Vector2 firstTarget,
      Vector2 secondTarget,
      Vector2 maxSpeed,
      String texturePath) {
    PhysicsComponent physicsComponent = new PhysicsComponent();
    ColliderComponent colliderComponent = new ColliderComponent();
    Entity movingPlatform =
        new Entity()
            .addComponent(new TextureRenderComponent(texturePath))
            .addComponent(physicsComponent)
            .addComponent(new PhysicsMovementComponent())
            .addComponent(colliderComponent.setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(
                new MovingPlatformComponent(grappleSides, firstTarget, secondTarget, maxSpeed))
            .addComponent(new PlatformGrappleComponent(grappleSides));

    physicsComponent.getBody().setGravityScale(0f);
    physicsComponent.setBodyType(BodyType.KinematicBody);
    colliderComponent.setFriction(1.5f);
    return movingPlatform;
  }

  public static Entity createWinConEntity() {
    // set up sensor collider
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
   * Creates an invisible sensor zone that fires "collisionStart" on overlap but applies no
   * physical collision response. Pair with a behaviour component such as
   * EnterZoneTriggerComponent to react to the overlap.
   *
   * @param size box size in world units
   * @return sensor trigger entity
   */
  public static Entity createTriggerZone(Vector2 size) {
    ColliderComponent collider = new ColliderComponent();
    collider.setLayer(PhysicsLayer.NPC);
    collider.setSensor(true);

    Entity zone = new Entity().addComponent(new PhysicsComponent()).addComponent(collider);

    zone.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    zone.getComponent(ColliderComponent.class).setAsBox(size);

    return zone;
  }

  public static Entity createFloor(int grappleSides) {
    return createFloor(grappleSides, "images/Tile_2.png");
  }

  /**
   * Creates a floor entity tiled with a custom texture, so a level can use different ground art
   * without changing the default floor used elsewhere.
   *
   * @param grappleSides sides the floor can be grappled from
   * @param texturePath tileable floor texture to use
   * @return floor entity
   */
  public static Entity createFloor(int grappleSides, String texturePath) {
    Entity floor =
        new Entity()
            .addComponent(new TiledRenderComponent(texturePath, 0.75f))
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new PlatformGrappleComponent(grappleSides));

    floor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    return floor;
  }

  /**
   * Creates an invisible physics wall.
   *
   * @param width Wall width in world units
   * @param height Wall height in world units
   * @return Wall entity of given width and height
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
   * Creates a statue that is able to be stood infront of
   *
   * @return Statue entity
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
   * @param rotationAngle Angle in degrees to rotate the spike (0 = UP, 180 = DOWN, 270 = LEFT, 90 =
   *     RIGHT)
   * @return spike entity
   */
  public static Entity createSpike(float rotationAngle) {
    Entity spike =
        new Entity()
            .addComponent(new TextureRenderComponent("images/spike.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new CombatStatsComponent(100, 2))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER));

    spike.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    // Scale slightly larger to close gaps
    spike.setScale(1.25f, 1.25f);

    PhysicsUtils.setScaledCollider(spike, 0.8f, 0.5f);

    return spike;
  }

  /**
   * Creates a default upward-facing spike hazard entity.
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
