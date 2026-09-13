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
      boolean active) {
    this.spawnableEntities = spawnableEntities;
    this.spawnInterval = spawnInterval;
    this.maxSpawns = maxSpawns;
    this.active = active;
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

  /** Spawns a random potential child from the list of spawnableEntities and increments how many */
  private void spawnChild() {
    // get a random child from the list of potential spawn entities
    int randIndex = (int) (Math.random() * spawnableEntities.size());
    Supplier<Entity> child = spawnableEntities.get(randIndex);
    Entity childEntity = child.get();

    // set the position to the same position as this entity's position
    Vector2 pos = entity.getPosition();
    childEntity.setPosition(pos);

    entity.getEvents().trigger("spawnEntity", childEntity);
    spawnCounter++;
  }
}
