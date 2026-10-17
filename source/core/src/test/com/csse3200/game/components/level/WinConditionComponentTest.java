package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class WinConditionComponentTest {

  private EventHandler gameEndEventHandler;
  private WinConditionComponent component;
  private Entity entity;

  @BeforeEach
  void setUp() {
    gameEndEventHandler = mock(EventHandler.class);
    ServiceLocator.registerGameEndEventHandler(gameEndEventHandler);
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerInputService(new InputService());
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(time);

    component = new WinConditionComponent();
    entity = new Entity().addComponent(component);
    entity.create();
  }

  @Test
  void shouldTriggerWinWhenPlayerInRangePressesF() {
    entity.getEvents().trigger("collisionStart", mock(Fixture.class), playerFixture());

    assertTrue(component.keyDown(Input.Keys.F));
    component.update();

    verify(gameEndEventHandler).trigger("gameEnd", GameEndState.WIN);
  }

  @Test
  void shouldNotTriggerWinJustByTouchingIt() {
    entity.getEvents().trigger("collisionStart", mock(Fixture.class), playerFixture());

    verify(gameEndEventHandler, never()).trigger("gameEnd", GameEndState.WIN);
  }

  @Test
  void shouldNotTriggerWinWhenFIsPressedOutOfRange() {
    assertFalse(component.keyDown(Input.Keys.F));

    verify(gameEndEventHandler, never()).trigger("gameEnd", GameEndState.WIN);
  }

  @Test
  void shouldOnlyTriggerWinOnce() {
    entity.getEvents().trigger("collisionStart", mock(Fixture.class), playerFixture());

    component.keyDown(Input.Keys.F);
    component.keyDown(Input.Keys.F);
    component.update();
    component.update();

    verify(gameEndEventHandler, times(1)).trigger("gameEnd", GameEndState.WIN);
  }

  private static Fixture playerFixture() {
    Fixture fixture = mock(Fixture.class);
    Body body = mock(Body.class);
    when(fixture.getBody()).thenReturn(body);
    BodyUserData data = new BodyUserData();
    data.entity = new Entity().addComponent(new PlayerActions());
    when(body.getUserData()).thenReturn(data);
    return fixture;
  }
}
