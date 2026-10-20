package com.csse3200.game.rendering.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Previews where an arrow would land while a shot is being drawn: a dotted white arc from the
 * player out to the landing point, following the mouse, ending in a bigger dot. The arc is the same
 * parabola the real arrow falls under, so it stretches and flattens as the draw builds. Subclasses
 * say when it shows and what shot it previews; this class owns the simulation and drawing.
 */
public abstract class ArcIndicatorRenderComponent extends RenderComponent {

  /** Radius of the dots along the arc, in world units. */
  public static final float DOT_RADIUS = 0.025f;

  /** Radius of the larger dot marking where the shot lands. */
  public static final float LANDING_DOT_RADIUS = 0.075f;

  /** How solid the dots are, from 0 (invisible) to 1 (fully solid). */
  public static final float DOT_OPACITY = 0.75f;

  private static final Color DOT_COLOR = new Color(1f, 1f, 1f, DOT_OPACITY);
  private static final int DOT_SEGMENTS = 12;

  // Small enough that consecutive samples land close together for a smooth-looking curve, without
  // costing an unreasonable number of raycasts over an arrow's full range.
  private static final float SIMULATION_STEP = 0.03f;
  private static final int MAX_SIMULATION_STEPS = 400;

  private ShapeRenderer shapeRenderer;

  /**
   * @return true while the preview should be on screen
   */
  protected abstract boolean isActive();

  /**
   * @return the mouse's offset from the player in world space, or null if it isn't available
   */
  protected abstract Vector2 aimOffset();

  /**
   * @param aim normalised aim direction
   * @return the world point the arrow would spawn at
   */
  protected abstract Vector2 launchPoint(Vector2 aim);

  /**
   * @return the speed the arrow would leave at right now, charge included
   */
  protected abstract float launchSpeed();

  /**
   * @return how far, in a straight line from the player, the arrow flies before expiring
   */
  protected abstract float maxRange();

  /**
   * How far along the aim, from the player's centre, the arc may be drawn before it stops. By
   * default there is no limit, so the arc runs to where the shot lands.
   *
   * @param aimOffset the mouse's offset from the player in world space
   * @return the furthest distance along the aim to draw
   */
  protected float maxDrawReach(Vector2 aimOffset) {
    return Float.MAX_VALUE;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (!isActive()) {
      return;
    }
    Vector2 aimOffset = aimOffset();
    if (aimOffset == null || aimOffset.isZero()) {
      return;
    }

    Vector2 aim = aimOffset.cpy().nor();

    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }

    batch.end();

    // Without blending switched on, the shape renderer ignores the colour's alpha and draws solid.
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(DOT_COLOR);
    Vector2 endPoint = simulateArc(launchPoint(aim), aim, maxDrawReach(aimOffset));
    // No landing dot when the arc was cut short before the shot lands.
    if (endPoint != null) {
      shapeRenderer.circle(endPoint.x, endPoint.y, LANDING_DOT_RADIUS, DOT_SEGMENTS);
    }
    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);

    batch.begin();
  }

  /**
   * Steps the exact same parabolic arc a real arrow falls under - launch speed from the subclass,
   * gravity scaled the same way {@link ArrowProjectileComponent} configures it - drawing a dot at
   * each sample point along the way, until the path either crosses solid terrain or travels beyond
   * {@link #maxRange()} in a straight line from the player (matching the real arrow's own range
   * check). It also stops, without a landing point, once the path is further along the aim than
   * {@code drawReach}.
   *
   * @return the point the simulated shot would land at, or null if the arc was cut short first
   */
  private Vector2 simulateArc(Vector2 start, Vector2 aim, float drawReach) {
    Vector2 rangeOrigin = entity.getCenterPosition();
    float range = maxRange();
    Vector2 gravity =
        ServiceLocator.getPhysicsService()
            .getPhysics()
            .getWorld()
            .getGravity()
            .cpy()
            .scl(ArrowProjectileComponent.ARC_GRAVITY_SCALE);
    Vector2 velocity = aim.cpy().scl(launchSpeed());
    Vector2 pos = start.cpy();

    for (int step = 0; step < MAX_SIMULATION_STEPS; step++) {
      Vector2 nextPos =
          pos.cpy()
              .mulAdd(velocity, SIMULATION_STEP)
              .mulAdd(gravity, 0.5f * SIMULATION_STEP * SIMULATION_STEP);

      Vector2 hitPoint = raycastSolidTerrain(pos, nextPos);
      if (hitPoint != null) {
        return hitPoint;
      }
      if (nextPos.dst(rangeOrigin) >= range) {
        return nextPos;
      }
      float along = (nextPos.x - rangeOrigin.x) * aim.x + (nextPos.y - rangeOrigin.y) * aim.y;
      if (along > drawReach) {
        return null;
      }

      velocity.mulAdd(gravity, SIMULATION_STEP);
      pos = nextPos;
      shapeRenderer.circle(pos.x, pos.y, DOT_RADIUS, DOT_SEGMENTS);
    }
    return pos;
  }

  /**
   * Same filter the real grapple arrow uses, so the preview never lies: it passes through the
   * water's surface and spiky balls exactly as the arrow does.
   *
   * @return the closest point hit on solid terrain, or null if the ray reaches {@code to} clear
   */
  private Vector2 raycastSolidTerrain(Vector2 from, Vector2 to) {
    Vector2[] hitPoint = new Vector2[1];
    RayCastCallback callback =
        (fixture, point, normal, fraction) -> {
          if (!GrappleComponent.isGrappleSurface(fixture)) {
            return -1f;
          }
          hitPoint[0] = point.cpy();
          return fraction;
        };
    ServiceLocator.getPhysicsService().getPhysics().getWorld().rayCast(callback, from, to);
    return hitPoint[0];
  }

  @Override
  public void dispose() {
    if (shapeRenderer != null) {
      shapeRenderer.dispose();
    }
    super.dispose();
  }
}
