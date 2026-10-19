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

  /**
   * A hook has to land at least this far above the shooter's feet. Anything lower is the floor or a
   * platform underneath them, which is not something to swing from. The margin covers the floor
   * they are standing on sitting exactly at their feet.
   */
  private static final float ABOVE_FEET_MARGIN = 0.1f;

  /** How long a hook that could not catch stays where it landed before it is reeled in. */
  private static final float HOLD_SECONDS = 0.25f;

  /** How fast the rope reels a missed hook back in, in world units per second. */
  private static final float REEL_SPEED = 20f;

  /** A hook being reeled in is gone once it gets this close to the player. */
  private static final float REEL_ARRIVAL_DISTANCE = 0.5f;

  /** Gives up and removes a hook that is still being reeled in after this long. */
  private static final float MAX_REEL_SECONDS = 2f;

  private final Entity shooter;
  private boolean spent = false;

  // Landed somewhere it could not hook: held, then reeled back to the player, by update()
  private boolean missed = false;
  private boolean frozen = false;
  private float heldFor = 0f;
  private float reeledFor = 0f;

  public GrappleArrowComponent(Entity shooter) {
    this.shooter = shooter;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollision);
    entity
        .getEvents()
        .addListener(ArrowProjectileComponent.RANGE_EXCEEDED_EVENT, this::onOutOfRange);
  }

  /** The arrow flew its full range without reaching anything: the player gets the same marker. */
  private void onOutOfRange(Vector2 point) {
    // A hook that already landed (and is being held or reeled in) has had its marker
    if (spent) {
      return;
    }
    shooter.getEvents().trigger("grappleFailed", point.cpy());
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
    // Sensors share layers with real terrain (the rising water's surface is an OBSTACLE sensor)
    // but have nothing solid to hook onto, so the layer alone isn't enough.
    if (other.isSensor()
        || !PhysicsLayer.contains(GRAPPLE_TARGETS, other.getFilterData().categoryBits)) {
      return;
    }
    // Any terrain the arrow reaches ends its flight, whether or not it makes a hook. Otherwise an
    // arrow that lands somewhere it cannot hook would just carry on through the platform.
    spent = true;

    Vector2 anchor = findSurfacePoint(other);
    GrappleComponent grapple = shooter.getComponent(GrappleComponent.class);
    if (grapple != null && canHookAt(anchor)) {
      grapple.attachTo(other.getBody(), anchor);

      // check if other fixture that was hit with the arrow is a slippery platform to inform it
      // the player has grappled to it
      if (other.getBody().getUserData() instanceof BodyUserData data) {
        SlipperyPlatformComponent platform =
            data.entity.getComponent(SlipperyPlatformComponent.class);
        if (platform != null) {
          platform.setGrappled(true);
        }
      }
    } else if (grapple != null) {
      // Landed below the shooter or right on top of them: it can't hook, so tell the player. One
      // event for both reasons, so they get the same marker whichever it was.
      shooter.getEvents().trigger("grappleFailed", anchor.cpy());
      // The hook stays where it landed rather than vanishing, then update() reels it back in. The
      // rope keeps following it the whole way, because the grapple still tracks this arrow.
      missed = true;
      return;
    }

    // The rope takes over from here
    removeArrow();
  }

  /**
   * Holds a hook that could not catch where it landed for a moment, then reels it back along the
   * rope to the player, removing it when it arrives. Does nothing for an arrow that has not missed.
   */
  @Override
  public void update() {
    if (!missed) {
      return;
    }
    ArrowProjectileComponent projectile = entity.getComponent(ArrowProjectileComponent.class);
    if (projectile == null) {
      removeArrow(); // nothing to animate
      return;
    }
    // Stopped here rather than in the collision callback, which Box2D does not allow it in.
    if (!frozen) {
      projectile.stop();
      frozen = true;
    }

    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    if (heldFor < HOLD_SECONDS) {
      heldFor += delta;
      return;
    }

    reeledFor += delta;
    Vector2 here = projectile.getWorldCenter();
    Vector2 toPlayer = shooter.getCenterPosition().sub(here);
    float step = REEL_SPEED * delta;
    if (toPlayer.len() <= Math.max(step, REEL_ARRIVAL_DISTANCE) || reeledFor > MAX_REEL_SECONDS) {
      removeArrow();
    } else {
      projectile.moveCentreTo(here.mulAdd(toPlayer.nor(), step));
    }
  }

  private void removeArrow() {
    if (ServiceLocator.getEntityService() != null) {
      ServiceLocator.getEntityService().scheduleRemoval(entity);
    }
  }

  /**
   * @return true if a rope can be anchored at this point: not right on top of the shooter, and not
   *     below their feet
   */
  private boolean canHookAt(Vector2 anchor) {
    boolean farEnough = arrowCenter().dst2(shooter.getCenterPosition()) >= MIN_TRAVEL * MIN_TRAVEL;
    boolean aboveFeet = anchor.y >= shooter.getPosition().y + ABOVE_FEET_MARGIN;
    return farEnough && aboveFeet;
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
