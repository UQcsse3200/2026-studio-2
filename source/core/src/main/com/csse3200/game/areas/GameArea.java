package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.level.PlatformGrappleComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
  /** Camera used by this game area. */
  protected final CameraComponent cameraComponent;
  protected TerrainComponent terrain;
  protected List<Entity> areaEntities;
  protected List<Entity> platforms = new ArrayList<>();

  /**
   * Creates a game area using the provided camera component.
   *
   * @param cameraComponent active camera component
   */
  protected GameArea(CameraComponent cameraComponent) {
    this.cameraComponent = cameraComponent;
    areaEntities = new ArrayList<>();
  }

  /** Create the game area in the world. */
  public abstract void create();

  /** Dispose of all internal entities in the area */
  public void dispose() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
  }

  /**
   * Spawn entity at its current position
   *
   * @param entity Entity (not yet registered)
   */
  protected void spawnEntity(Entity entity) {
    areaEntities.add(entity);

    // keep track of all grappleable platforms
    if (entity.getComponent(PlatformGrappleComponent.class) != null) {
      platforms.add(entity);
    }

    ServiceLocator.getEntityService().register(entity);
  }

  /**
   * Spawn entity on a given tile. Requires the terrain to be set first.
   *
   * @param entity Entity (not yet registered)
   * @param tilePos tile position to spawn at
   * @param centerX true to center entity X on the tile, false to align the bottom left corner
   * @param centerY true to center entity Y on the tile, false to align the bottom left corner
   */
  protected void spawnEntityAt(
      Entity entity, GridPoint2 tilePos, boolean centerX, boolean centerY) {
    Vector2 worldPos = terrain.tileToWorldPosition(tilePos);
    float tileSize = terrain.getTileSize();

    if (centerX) {
      worldPos.x += (tileSize / 2) - entity.getCenterPosition().x;
    }
    if (centerY) {
      worldPos.y += (tileSize / 2) - entity.getCenterPosition().y;
    }

    entity.setPosition(worldPos);
    spawnEntity(entity);
  }

  /**
   * A protected method that loops through all stored platforms
   *
   * @param raycastEnd the Vector2 object created by the physics engine's raycast that corresponds
   *     to the final point in the world the grapple hit
   * @return An entity that is guaranteed to be a platform entity (the platform check is performed
   *     before adding to the platforms list) that is the closest to the point at the end of the
   *     raycast.<br>
   *     Note: this does not guarantee the platform was successfully hit, it just returns the most
   *     likely entity to do the check on
   */
  protected Entity findTargetedPlatform(Vector2 raycastEnd) {
    Entity platform = null;
    float lowestDistance = Float.MAX_VALUE; // ensures platforms[0] will be best candidate initially

    for (Entity entity : platforms) {
      // find distance between this and raycast end and compare to best known candidate
      Vector2 pos = entity.getCenterPosition();
      float currentDistance = pos.dst(raycastEnd);

      // update best candidate
      if (currentDistance < lowestDistance) {
        platform = entity;
        lowestDistance = currentDistance;
      }
    }
    return platform;
  }

  /**
   * Public method for grapples to check the end of the raycast position hits a valid side of a
   * platform to confirm a successful grapple location was hit
   *
   * @param raycastEnd the Vector2 object created by the physics engine's raycast that corresponds
   *     to the final point in the world the grapple hit
   * @return true if the grapple successfully hit a platform and the hit side was a valid,
   *     grappleable side of that platform, false otherwise
   */
  public boolean checkSuccessfulGrapple(Vector2 raycastEnd) {
    Entity p = findTargetedPlatform(raycastEnd);
    PlatformGrappleComponent grappleComponent = p.getComponent(PlatformGrappleComponent.class);
    int hit = grappleComponent.checkSideHit(p, raycastEnd);
    return grappleComponent.successfulGrapple(hit);
  }
}
