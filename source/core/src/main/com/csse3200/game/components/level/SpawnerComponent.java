package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.function.Supplier;

public class SpawnerComponent extends Component {
  // each spawn will pick a random supplier method to spawn a new entity
  private final ArrayList<Supplier<Entity>> spawnableEntities;

  private final float spawnInterval; // specify -1 for trigger based spawning only
  private float timeSinceLastSpawn;
  private final int maxSpawns; // specify -1 for infinite spawning
  private int spawnCounter;
  private boolean active;
  private ACTIVATION_MODE mode;

  /**
   * @param spawnableEntities all viable entities that can be spawned by this component. the
   *     presence of an entity in this list does not guarantee they will spawn. it's at the mercy of
   *     Math.random()!
   * @param spawnInterval how frequently to attempt to spawn enemies. if the component should only
   *     spawn enemies when triggered by a trigger button, set to -1
   * @param maxSpawns how many enemies should be spawned total by this component. if the component
   *     should indefinitely spawn enemies, set to -1
   * @param active the initial activation state of this component
   */
  public SpawnerComponent(
      ArrayList<Supplier<Entity>> spawnableEntities,
      float spawnInterval,
      int maxSpawns,
      boolean active,
      ACTIVATION_MODE mode) {
    this.spawnableEntities = spawnableEntities;
    this.spawnInterval = spawnInterval;
    this.maxSpawns = maxSpawns;
    this.active = active;
    this.mode = mode;
  }

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("activatedMapComponent", this::handleActivation);
  }

  @Override
  public void update() {
    if (!active || spawnInterval == -1 || (maxSpawns != -1 && spawnCounter >= maxSpawns)) {
      return;
    }

    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    timeSinceLastSpawn += delta;

    if (timeSinceLastSpawn >= spawnInterval) {
      spawnChild();
      timeSinceLastSpawn = 0;
    }
  }

  /** Details how the activation handler should behave when receiving an activation attempt */
  public enum ACTIVATION_MODE {
    NORMAL,
    TOGGLE
  }

  /**
   * Spawns a random potential child from the list of spawnableEntities and increments how many have
   * been spawned total
   */
  private void spawnChild() {
    // get a random child from the list of potential spawn entities
    int randIndex = (int) (Math.random() * spawnableEntities.size());
    Supplier<Entity> child = spawnableEntities.get(randIndex);
    Entity childEntity = child.get();

    // set the spawned child's position to the same position as this entity's position
    // to do this properly, we need to spawn it in the middle of the parent entity by figuring out
    // the offset from the bottom left of the child to the center of the parent
    Vector2 centerPosition = entity.getCenterPosition();
    float childX = centerPosition.x - (childEntity.getScale().x / 2f);
    float childY = centerPosition.y - (childEntity.getScale().y / 2f);
    childEntity.setPosition(childX, childY);

    entity.getEvents().trigger("spawnEntity", childEntity);
    spawnCounter++;
  }

  /**
   * Responds to being activated by this entity's ActivatableComponent
   *
   * @param activated the new activation state to apply according to the mode specified
   */
  private void handleActivation(boolean activated) {
    switch (mode) {
      case NORMAL:
        active = activated;
        break;
      case TOGGLE:
        active = !active;
        break;
      default:
        break;
    }
  }
}
