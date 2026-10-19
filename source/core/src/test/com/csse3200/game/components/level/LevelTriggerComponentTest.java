package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class LevelTriggerComponentTest {
  private Entity portal;
  private LevelTriggerComponent trigger;
  private AtomicInteger transitions;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerInputService(new InputService());
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(time);

    trigger = new LevelTriggerComponent("level2");
    portal = new Entity().addComponent(trigger);
    portal.create();
    transitions = new AtomicInteger();
    portal
        .getEvents()
        .addListener("triggerNextLevel", (String level) -> transitions.incrementAndGet());
  }

  @Test
  void shouldNotTriggerWhenFIsPressedOutOfRange() {
    assertFalse(trigger.keyDown(Input.Keys.F));
    assertEquals(0, transitions.get());
  }

  @Test
  void shouldNotTriggerJustByTouchingIt() {
    enter(playerFixture());

    assertTrue(trigger.isPlayerInRange());
    assertEquals(0, transitions.get());
  }

  @Test
  void shouldTriggerOnlyOnceWhenPlayerInRangePressesF() {
    enter(playerFixture());

    assertTrue(trigger.keyDown(Input.Keys.F));
    assertFalse(trigger.keyDown(Input.Keys.F));
    trigger.update();
    trigger.update();
    assertEquals(1, transitions.get());
  }

  @Test
  void shouldLightUpImmediatelyButChangeLevelOnlyAfterTheDelay() {
    AtomicInteger lit = new AtomicInteger();
    portal.getEvents().addListener("levelEndActivated", lit::incrementAndGet);
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.3f);
    ServiceLocator.registerTimeSource(time);
    enter(playerFixture());

    trigger.keyDown(Input.Keys.F);
    assertEquals(1, lit.get());
    trigger.update();
    assertEquals(0, transitions.get());
    trigger.update();
    trigger.update();
    assertEquals(1, transitions.get());
  }

  @Test
  void shouldIgnoreOtherKeys() {
    enter(playerFixture());

    assertFalse(trigger.keyDown(Input.Keys.E));
    assertEquals(0, transitions.get());
  }

  @Test
  void shouldIgnoreEntitiesThatAreNotThePlayer() {
    Fixture enemy = playerFixture();
    ((BodyUserData) enemy.getBody().getUserData()).entity = new Entity();

    enter(enemy);

    assertFalse(trigger.isPlayerInRange());
    assertFalse(trigger.keyDown(Input.Keys.F));
    assertEquals(0, transitions.get());
  }

  @Test
  void shouldStopTriggeringOnceThePlayerLeaves() {
    Fixture player = playerFixture();
    enter(player);
    portal.getEvents().trigger("collisionEnd", mock(Fixture.class), player);

    assertFalse(trigger.isPlayerInRange());
    assertFalse(trigger.keyDown(Input.Keys.F));
    assertEquals(0, transitions.get());
  }

  @Test
  void shouldNotTriggerWhileGameIsPaused() {
    enter(playerFixture());
    EntityService pausedService = mock(EntityService.class);
    when(pausedService.getPaused()).thenReturn(true);
    ServiceLocator.registerEntityService(pausedService);

    assertFalse(trigger.keyDown(Input.Keys.F));
    assertEquals(0, transitions.get());
  }

  private void enter(Fixture fixture) {
    portal.getEvents().trigger("collisionStart", mock(Fixture.class), fixture);
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
