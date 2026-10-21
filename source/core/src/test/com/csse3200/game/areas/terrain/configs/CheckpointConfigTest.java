package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

class CheckpointConfigTest {

  @Test
  void constructorStoresPosition() {
    GridPoint2 position = new GridPoint2(3, 5);
    CheckpointConfig config = new CheckpointConfig(position);

    assertSame(position, config.getPosition());
  }

  @Test
  void entityIsInitiallyNull() {
    CheckpointConfig config = new CheckpointConfig(new GridPoint2(1, 2));

    assertNull(config.getEntity());
  }

  @Test
  void setEntityStoresEntity() {
    CheckpointConfig config = new CheckpointConfig(new GridPoint2(1, 2));
    Entity entity = new Entity();

    config.setEntity(entity);

    assertSame(entity, config.getEntity());
  }

  @Test
  void setEntityReplacesExistingEntity() {
    CheckpointConfig config = new CheckpointConfig(new GridPoint2(1, 2));
    Entity firstEntity = new Entity();
    Entity secondEntity = new Entity();

    config.setEntity(firstEntity);
    config.setEntity(secondEntity);

    assertSame(secondEntity, config.getEntity());
  }

  @Test
  void setEntityAcceptsNull() {
    CheckpointConfig config = new CheckpointConfig(new GridPoint2(1, 2));

    config.setEntity(new Entity());
    config.setEntity(null);

    assertNull(config.getEntity());
  }
}
