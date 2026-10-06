package com.csse3200.game.components.level;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class WinConditionComponentTest {

  private EventHandler gameEndEventHandler;

  @BeforeEach
  void setUp() {
    gameEndEventHandler = mock(EventHandler.class);
    ServiceLocator.registerGameEndEventHandler(gameEndEventHandler);
  }

  @Test
  void shouldTriggerWinOnCollision() {
    WinConditionComponent component = new WinConditionComponent();

    Entity entity = new Entity().addComponent(component);
    entity.create();

    Fixture me = mock(Fixture.class);
    Fixture other = mock(Fixture.class);

    entity.getEvents().trigger("collisionStart", me, other);

    verify(gameEndEventHandler).trigger("gameEnd", GameEndState.WIN);
  }

  @Test
  void shouldNotTriggerWinBeforeCollision() {
    WinConditionComponent component = new WinConditionComponent();

    Entity entity = new Entity().addComponent(component);
    entity.create();

    verify(gameEndEventHandler, never())
        .trigger("gameEnd", GameEndState.WIN);
  }
}