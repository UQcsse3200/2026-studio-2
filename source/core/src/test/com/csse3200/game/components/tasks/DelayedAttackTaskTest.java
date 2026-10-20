package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(GameExtension.class)
public class DelayedAttackTaskTest {
  private GameTime gameTime;

  @BeforeEach
  void beforeEach() {
    gameTime = mock(GameTime.class);
    when(gameTime.getTime()).thenReturn(0L);
    when(gameTime.getDeltaTime()).thenReturn(20f / 1000);
    ServiceLocator.registerTimeSource(gameTime);
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  @AfterEach
  void afterEach() {
    ServiceLocator.getEntityService().dispose();
    ServiceLocator.getPhysicsService().getPhysics().dispose();
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 1L << 30})
  void respectsAttackAndRepeatDelayBoundariesRegardlessOfClockUptime(long startTime) {
    when(gameTime.getTime()).thenReturn(startTime);
    Entity target = new Entity();
    target.setPosition(2f, 0f);
    Entity attacker = new Entity();
    DelayedAttackTask task = new DelayedAttackTask(target, 10, 5f, 0.5f, 2, 0f);
    task.create(() -> attacker);
    task.start();
    EntityService entities = ServiceLocator.getEntityService();

    when(gameTime.getTime()).thenReturn(startTime + 499L);
    task.update();
    assertEquals(0, entities.getEntities().size, "The attack must wait the full 500 ms");

    when(gameTime.getTime()).thenReturn(startTime + 500L);
    task.update();
    assertEquals(1, entities.getEntities().size);
    target.setPosition(100f, 0f);
    assertEquals(10, task.getPriority(), "An active attack finishes even if the target moves away");

    when(gameTime.getTime()).thenReturn(startTime + 1499L);
    task.update();
    assertEquals(10, task.getPriority(), "The attack must remain active for its full duration");

    when(gameTime.getTime()).thenReturn(startTime + 1500L);
    task.update();
    assertEquals(-1, task.getPriority(), "The attack has ended and the target is out of range");
    target.setPosition(2f, 0f);

    when(gameTime.getTime()).thenReturn(startTime + 1999L);
    task.update();
    assertEquals(1, entities.getEntities().size, "The next attack must respect the new delay");

    when(gameTime.getTime()).thenReturn(startTime + 2000L);
    task.update();
    assertEquals(2, entities.getEntities().size);
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
