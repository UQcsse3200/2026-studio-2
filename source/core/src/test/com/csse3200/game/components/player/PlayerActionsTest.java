package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Covers how the player's movement freezes once they've died. */
@ExtendWith(GameExtension.class)
class PlayerActionsTest {
  private GameTime gameTime;

  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    gameTime = mock(GameTime.class);
    when(gameTime.getTime()).thenReturn(0L);
    ServiceLocator.registerTimeSource(gameTime);
  }

  private Entity createPlayer() {
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new PlayerActions());
    player.create();
    return player;
  }

  @Test
  void shouldZeroHorizontalVelocityOnDeathButKeepVertical() {
    Entity player = createPlayer();
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    physics.getBody().setLinearVelocity(3f, -2f);

    player.getEvents().trigger("death");

    assertEquals(0f, physics.getBody().getLinearVelocity().x);
    assertEquals(-2f, physics.getBody().getLinearVelocity().y);
  }

  @Test
  void shouldIgnoreWalkAfterDeath() {
    Entity player = createPlayer();
    player.getEvents().trigger("death");

    player.getEvents().trigger("walk", new com.badlogic.gdx.math.Vector2(1f, 0f));
    player.update(); // Would apply a movement impulse if the death guard were missing.

    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    assertEquals(0f, physics.getBody().getLinearVelocity().x);
  }

  @Test
  void shouldNotQueueJumpAfterDeath() {
    Entity player = createPlayer();
    player.getEvents().trigger("death");

    player.getEvents().trigger("jump");

    // Advance well past the jump wind-up window and update; a wrongly-queued impulse would apply
    // here.
    when(gameTime.getTime()).thenReturn(10_000L);
    player.update();

    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    assertEquals(0f, physics.getBody().getLinearVelocity().y);
  }
}
