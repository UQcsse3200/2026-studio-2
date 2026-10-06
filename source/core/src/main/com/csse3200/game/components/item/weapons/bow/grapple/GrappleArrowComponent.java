package com.csse3200.game.components.item.weapons.bow.grapple;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.level.SlipperyPlatformComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.services.ServiceLocator;

/** Rides on a fired grapple arrow and hooks the player onto solid ground or a platform. */
public class GrappleArrowComponent extends Component {
  /** The grapple only sticks to solid terrain - not enemies, boundary walls, or trigger zones. */
  private static final short GRAPPLE_TARGETS = PhysicsLayer.SOLID; // GROUND | OBSTACLE

  /**
   * The arrow must get this far from the shooter before it can hook, so it doesn't grab the
   * platform the player is standing on the instant it spawns.
   */
  private static final float MIN_TRAVEL = 1.2f;

  private final Entity shooter;
  private boolean spent = false;

  public GrappleArrowComponent(Entity shooter) {
    this.shooter = shooter;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollision);
  }

  @Override
  public void dispose() {
    cancel();
    GrappleComponent grapple = shooter.getComponent(GrappleComponent.class);
    if (grapple != null) {
      grapple.onArrowRemoved(entity);
    }
  }

  /** Prevents a cancelled shot from attaching during a remaining contact callback. */
  void cancel() {
    spent = true;
  }

  private void onCollision(Fixture me, Fixture other) {
    if (spent) {
      return;
    }
    if (!PhysicsLayer.contains(GRAPPLE_TARGETS, other.getFilterData().categoryBits)) {
      return;
    }
    if (arrowCenter().dst2(shooter.getCenterPosition()) < MIN_TRAVEL * MIN_TRAVEL) {
      return;
    }
    spent = true;

    GrappleComponent grapple = shooter.getComponent(GrappleComponent.class);
    if (grapple != null) {
      grapple.attachTo(other.getBody(), findSurfacePoint(other));

      // check if other fixture that was hit with the arrow is a slippery platform to inform it
      // the player has grappled to it
      if (other.getBody().getUserData() instanceof BodyUserData data) {
        SlipperyPlatformComponent platform =
            data.entity.getComponent(SlipperyPlatformComponent.class);
        if (platform != null) {
          platform.setGrappled(true);
        }
      }
    }

    // The rope takes over from here; either way the arrow is done
    if (ServiceLocator.getEntityService() != null) {
      ServiceLocator.getEntityService().scheduleRemoval(entity);
    }
  }

  /**
   * @return where the arrow is, i.e. the centre of its collision box - which matches where it's
   *     drawn
   */
  private Vector2 arrowCenter() {
    ArrowProjectileComponent projectile = entity.getComponent(ArrowProjectileComponent.class);
    return projectile != null ? projectile.getWorldCenter() : entity.getCenterPosition();
  }

  /**
   * The arrow's own position is never quite on the surface it hit: contact is reported when its
   * leading edge touches (or has just sunk into) the wall, so its centre is a little short of - or
   * past - it. Anchoring there left the rope visibly hovering off the wall. The point on the wall's
   * surface nearest the arrow is where it actually met it, however steeply or shallowly it came in.
   *
   * @return the point on {@code target}'s surface nearest the arrow, or the arrow's own position if
   *     the target isn't a polygon (every platform and wall in the game is)
   */
  private Vector2 findSurfacePoint(Fixture target) {
    Vector2 center = arrowCenter();
    if (target.getShape() instanceof PolygonShape polygon) {
      return nearestPointOnSurface(polygon, target.getBody(), center);
    }
    return center;
  }

  private static Vector2 nearestPointOnSurface(PolygonShape polygon, Body body, Vector2 point) {
    int count = polygon.getVertexCount();
    Vector2[] corners = new Vector2[count];
    Vector2 local = new Vector2();
    for (int i = 0; i < count; i++) {
      polygon.getVertex(i, local);
      // Box2D hands back one shared vector that every later call overwrites, so take a copy.
      corners[i] = body.getWorldPoint(local).cpy();
    }

    Vector2 nearest = null;
    float nearestDistance = Float.MAX_VALUE;
    Vector2 candidate = new Vector2();
    for (int i = 0; i < count; i++) {
      Intersector.nearestSegmentPoint(corners[i], corners[(i + 1) % count], point, candidate);
      float distance = candidate.dst2(point);
      if (distance < nearestDistance) {
        nearestDistance = distance;
        nearest = candidate.cpy();
      }
    }
    return nearest;
  }
}
