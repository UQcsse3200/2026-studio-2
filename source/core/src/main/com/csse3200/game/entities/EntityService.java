package com.csse3200.game.entities;

import com.badlogic.gdx.utils.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a global access point for entities to register themselves. This allows for iterating
 * over entities to perform updates each loop. All game entities should be registered here.
 *
 * <p>Avoid adding additional state here! Global access is often the easy but incorrect answer to
 * sharing data.
 */
public class EntityService {
  private static final Logger logger = LoggerFactory.getLogger(EntityService.class);
  private static final int INITIAL_CAPACITY = 16;

  private final Array<Entity> entities = new Array<>(false, INITIAL_CAPACITY);
  private final Array<Entity> entitiesToDispose = new Array<>();
  private final Array<Entity> pendingRemoval = new Array<>(false, INITIAL_CAPACITY);

  private boolean paused;

  /**
   * Register a new entity with the entity service. The entity will be created and start updating.
   *
   * @param entity new entity.
   */
  public void register(Entity entity) {
    logger.debug("Registering {} in entity service", entity);
    entities.add(entity);
    entity.create();
  }

  /**
   * Unregister an entity with the entity service. The entity will be removed and stop updating.
   *
   * @param entity entity to be removed.
   */
  public void unregister(Entity entity) {
    logger.debug("Unregistering {} in entity service", entity);
    entities.removeValue(entity, true);
    pendingRemoval.removeValue(entity, true);
  }

  /**
   * Schedules an entity for safe disposal after the current update finishes.
   *
   * <p>This should be used by components which expire during an entity update, since immediately
   * removing an entity while the service is iterating can skip the next entity.
   *
   * @param entity entity to remove
   */
  public void scheduleRemoval(Entity entity) {
    if (entity == null || pendingRemoval.contains(entity, true)) {
      return;
    }
    entity.setEnabled(false);
    pendingRemoval.add(entity);
  }

  /**
   * Schedules an entity to be disposed after the current update cycle.
   *
   * @param entity entity to dispose
   */
  public void scheduleForDisposal(Entity entity) {
    if (!entitiesToDispose.contains(entity, true)) {
      entitiesToDispose.add(entity);
    }
  }

  /** Update all registered entities. Should only be called from the main game loop. */
  public void update() {
    if (!paused) {
      for (Entity entity : entities) {
        entity.earlyUpdate();
        entity.update();
      }
    }
    for (Entity entity : entitiesToDispose) {
      entity.dispose();
    }
    entitiesToDispose.clear();
    removeScheduledEntities();
  }

  /** Dispose all entities. */
  public void dispose() {
    Array<Entity> existingEntities = new Array<>(entities);
    entities.clear();
    pendingRemoval.clear();
    for (Entity entity : existingEntities) {
      entity.dispose();
    }
  }

  /** Getter function for a snapshot (shallow copy) of currently registered entities * */
  public Array<Entity> getEntities() {
    return new Array<>(entities);
  }

  private void removeScheduledEntities() {
    Array<Entity> removals = new Array<>(pendingRemoval);
    pendingRemoval.clear();
    for (Entity entity : removals) {
      if (entities.contains(entity, true)) {
        entity.dispose();
      }
    }
  }

  public void setPaused(boolean newPaused) {
    paused = newPaused;
  }

  public void togglePaused() {
    paused = !paused;
  }

  public boolean getPaused() {
    return paused;
  }
}
