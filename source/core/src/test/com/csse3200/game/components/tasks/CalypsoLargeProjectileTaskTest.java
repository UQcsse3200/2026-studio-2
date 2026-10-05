package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CalypsoLargeProjectileTaskTest {
  private GameTime gameTime;

  @BeforeEach
  void beforeEach() {
    gameTime = mock(GameTime.class);
    when(gameTime.getTime()).thenReturn(0L);
    ServiceLocator.registerTimeSource(gameTime);
  }

  @Test
  void shouldNotRunWhileOnCooldown() {
    Entity calypso = new Entity();
    calypso.setPosition(new Vector2(1, 1));

    Entity target = new Entity();
    target.setPosition(new Vector2(2, 1));

    CalypsoLargeProjectileTask task =
        new CalypsoLargeProjectileTask(target, 25, 5f, 6f, 4, 3.5f, 6f);
    task.create(() -> calypso);

    when(gameTime.getTime()).thenReturn(3000L);

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldRunWhenCooldownReadyAndTargetInRange() {
    Entity calypso = new Entity();
    calypso.setPosition(new Vector2(1, 1));

    Entity target = new Entity();
    target.setPosition(new Vector2(2, 1));

    CalypsoLargeProjectileTask task =
        new CalypsoLargeProjectileTask(target, 25, 5f, 6f, 4, 3.5f, 6f);
    task.create(() -> calypso);

    when(gameTime.getTime()).thenReturn(6000L);

    assertEquals(25, task.getPriority());
  }

  @Test
  void shouldNotRunWhenTargetOutsideRange() {
    Entity calypso = new Entity();
    calypso.setPosition(new Vector2(1, 1));

    Entity target = new Entity();
    target.setPosition(new Vector2(20, 1));

    CalypsoLargeProjectileTask task =
        new CalypsoLargeProjectileTask(target, 25, 5f, 6f, 4, 3.5f, 6f);
    task.create(() -> calypso);

    when(gameTime.getTime()).thenReturn(6000L);

    assertEquals(-1, task.getPriority());
  }
}
