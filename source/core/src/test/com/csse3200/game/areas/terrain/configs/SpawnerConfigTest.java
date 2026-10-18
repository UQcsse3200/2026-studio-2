package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.csse3200.game.components.level.SpawnerComponent;
import com.csse3200.game.entities.Entity;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class SpawnerConfigTest {
  @Test
  void acceptsAnImmutableListWithoutChangingSupplierOrder() {
    Supplier<Entity> first = Entity::new;
    Supplier<Entity> second = Entity::new;
    SpawnerConfig config =
        new SpawnerConfig(
            List.of(first, second), 2f, 3, SpawnerComponent.ACTIVATION_MODE.NORMAL, true);

    assertEquals(2, config.spawns.size());
    assertSame(first, config.spawns.get(0));
    assertSame(second, config.spawns.get(1));
  }
}
