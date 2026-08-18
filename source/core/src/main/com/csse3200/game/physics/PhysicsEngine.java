package com.csse3200.game.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.physics.raycast.AllHitCallback;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.physics.raycast.SingleHitCallback;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process game physics using the Box2D library. See the Box2D documentation for examples or use
 * cases.
 */
public class PhysicsEngine implements Disposable {
  private static final Logger logger = LoggerFactory.getLogger(PhysicsEngine.class);
  private static final float MAX_UPDATE_TIME = 0.25f;
  private static final float PHYSICS_TIMESTEP = 0.016f;
  private static final Vector2 GRAVITY = new Vector2(0f, -5f);
  private static final int VELOCITY_ITERATIONS = 6;
  private static final int POSITION_ITERATIONS = 2;

  private final World world;
  private final GameTime timeSource;
  private final SingleHitCallback singleHitCallback = new SingleHitCallback();
  private final AllHitCallback allHitCallback = new AllHitCallback();
  private float accumulator;

  public PhysicsEngine() {
    this(new World(GRAVITY, true), ServiceLocator.getTimeSource());
  }

  public PhysicsEngine(World world, GameTime timeSource) {
    this.world = world;
    world.setContactListener(new PhysicsContactListener());
    this.timeSource = timeSource;
  }

  public void update() {
    float deltaTime = timeSource.getDeltaTime();
    float maxTime = Math.min(deltaTime, MAX_UPDATE_TIME);
    accumulator += maxTime;

    while (accumulator >= PHYSICS_TIMESTEP) {
      world.step(PHYSICS_TIMESTEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
      accumulator -= PHYSICS_TIMESTEP;
    }
  }

  public Body createBody(BodyDef bodyDef) {
    logger.debug("Creating physics body {}", bodyDef);
    return world.createBody(bodyDef);
  }

  public void destroyBody(Body body) {
    logger.debug("Destroying physics body {}", body);
    world.destroyBody(body);
  }

  public Joint createJoint(JointDef jointDef) {
    logger.debug("Creating physics joint {}", jointDef);
    return world.createJoint(jointDef);
  }

  public void destroyJoint(Joint joint) {
    logger.debug("Destroying physics joint {}", joint);
    world.destroyJoint(joint);
  }

  public World getWorld() {
    return world;
  }

  public boolean raycast(Vector2 from, Vector2 to, RaycastHit hit) {
    return raycast(from, to, PhysicsLayer.ALL, hit);
  }

  public boolean raycast(Vector2 from, Vector2 to, short layerMask, RaycastHit hit) {
    singleHitCallback.didHit = false;
    singleHitCallback.layerMask = layerMask;
    singleHitCallback.hit = hit;
    world.rayCast(singleHitCallback, from, to);
    return singleHitCallback.didHit;
  }

  public RaycastHit[] raycastAll(Vector2 from, Vector2 to) {
    return raycastAll(from, to, PhysicsLayer.ALL);
  }

  public RaycastHit[] raycastAll(Vector2 from, Vector2 to, short layerMask) {
    allHitCallback.layerMask = layerMask;
    world.rayCast(allHitCallback, from, to);
    return allHitCallback.getHitsAndClear();
  }

  @Override
  public void dispose() {
    world.dispose();
  }
}
