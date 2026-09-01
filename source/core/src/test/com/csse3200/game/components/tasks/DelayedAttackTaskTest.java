package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
public class DelayedAttackTaskTest {
  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getTime()).thenReturn(0L);
    ServiceLocator.registerTimeSource(gameTime);
  }

  @Test
  void shouldAttackTarget() {
    Entity entity = new Entity().addComponent(new CombatStatsComponent(10, 10));
    entity.create();
    entity.setPosition(new Vector2(1, 1));

    Entity target = new Entity().addComponent(new CombatStatsComponent(10, 0));
    target.create();
    target.setPosition(new Vector2(1, 1));

    DelayedAttackTask task = new DelayedAttackTask(target, 10, 1f, 0f);
    task.create(() -> entity);
    task.start();
    task.update();
    assertEquals(0, target.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void doesNotAttackOutsideRange() {
    Entity entity = new Entity().addComponent(new CombatStatsComponent(10, 10));
    entity.create();
    entity.setPosition(new Vector2(1, 1));

    Entity target = new Entity().addComponent(new CombatStatsComponent(10, 0));
    target.create();
    target.setPosition(new Vector2(5, 5));

    DelayedAttackTask task = new DelayedAttackTask(target, 10, 1f, 0f);
    task.create(() -> entity);
    task.start();
    task.update();
    assertEquals(10, target.getComponent(CombatStatsComponent.class).getHealth());
  }
}
