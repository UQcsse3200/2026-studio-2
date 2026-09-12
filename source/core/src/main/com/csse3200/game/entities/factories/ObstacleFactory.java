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
    PhysicsComponent physicsComponent = new PhysicsComponent();
    ColliderComponent colliderComponent = new ColliderComponent();
    Entity movingPlatform =
        new Entity()
            .addComponent(new TextureRenderComponent("images/platform.png"))
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
   * Creates a default upward-facing spike hazard entity.
   *
   * @return spike entity
   */
  public static Entity createSpike() {
    Entity spike =
            new Entity()
                    .addComponent(new TextureRenderComponent("images/spike.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
                    .addComponent(new CombatStatsComponent(100, 2))
                    .addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE))
                    .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER));

    spike.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);

    // Scale slightly larger to close grid gaps
    spike.setScale(1.25f, 1.25f);

    PhysicsUtils.setScaledCollider(spike, 0.8f, 0.5f);

    return spike;
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}