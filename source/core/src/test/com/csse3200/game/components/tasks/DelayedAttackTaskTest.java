package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DelayedAttackTaskTest {
  @BeforeEach
  void beforeEach() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getTime()).thenReturn(0L);
    when(gameTime.getDeltaTime()).thenReturn(20f / 1000);
    ServiceLocator.registerTimeSource(gameTime);
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  @Test
  void attackWhenInRange() {
    Entity target = new Entity().addComponent(new CombatStatsComponent(10, 0));
    target.setPosition(2f, 0f);

    DelayedAttackTask task = new DelayedAttackTask(target, 10, 5, 0, 2, 0f);
    AITaskComponent ai = new AITaskComponent().addTask(task);

    Entity entity =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new CombatStatsComponent(1, 5))
            .addComponent(ai);
    entity.create();
    entity.setPosition(0f, 0f);

    for (int i = 0; i < 1; i++) {
      entity.earlyUpdate();
      entity.update();
      ServiceLocator.getPhysicsService().getPhysics().update();
    }

    assertTrue(task.getPriority() > 0);
  }
}
