package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SpikyBallComponentTest {

  private GameTime timeSource;
  private EntityService entityService;

  @BeforeEach
  void setUp() {
    timeSource = mock(GameTime.class);
    entityService = mock(EntityService.class);

    ServiceLocator.registerTimeSource(timeSource);
    ServiceLocator.registerEntityService(entityService);
  }

  @Test
  void shouldStoreDirection() {
    Vector2 direction = new Vector2(1f, 0f);

    SpikyBallComponent component = new SpikyBallComponent(direction);

    assertSame(direction, component.direction);
  }

  @Test
  void shouldMoveRight() {
    when(timeSource.getDeltaTime()).thenReturn(1f);

    SpikyBallComponent component = new SpikyBallComponent(new Vector2(1f, 0f));

    Entity entity = new Entity().addComponent(component);

    entity.setPosition(2f, 3f);
    entity.create();

    component.update();

    assertEquals(7f, entity.getPosition().x, 0.001f);
    assertEquals(3f, entity.getPosition().y, 0.001f);
  }

  @Test
  void shouldMoveLeft() {
    when(timeSource.getDeltaTime()).thenReturn(1f);

    SpikyBallComponent component = new SpikyBallComponent(new Vector2(-1f, 0f));

    Entity entity = new Entity().addComponent(component);

    entity.setPosition(10f, 3f);
    entity.create();

    component.update();

    assertEquals(5f, entity.getPosition().x, 0.001f);
    assertEquals(3f, entity.getPosition().y, 0.001f);
  }

  @Test
  void shouldMoveVertically() {
    when(timeSource.getDeltaTime()).thenReturn(1f);

    SpikyBallComponent component = new SpikyBallComponent(new Vector2(0f, 1f));

    Entity entity = new Entity().addComponent(component);

    entity.setPosition(2f, 3f);
    entity.create();

    component.update();

    assertEquals(2f, entity.getPosition().x, 0.001f);
    assertEquals(8f, entity.getPosition().y, 0.001f);
  }

  @Test
  void shouldScaleMovementByDeltaTime() {
    when(timeSource.getDeltaTime()).thenReturn(0.5f);

    SpikyBallComponent component = new SpikyBallComponent(new Vector2(1f, 0f));

    Entity entity = new Entity().addComponent(component);

    entity.setPosition(0f, 0f);
    entity.create();

    component.update();

    assertEquals(2.5f, entity.getPosition().x, 0.001f);
    assertEquals(0f, entity.getPosition().y, 0.001f);
  }

  @Test
  void shouldMoveDiagonally() {
    when(timeSource.getDeltaTime()).thenReturn(1f);

    SpikyBallComponent component = new SpikyBallComponent(new Vector2(1f, -1f));

    Entity entity = new Entity().addComponent(component);

    entity.setPosition(10f, 10f);
    entity.create();

    component.update();

    assertEquals(15f, entity.getPosition().x, 0.001f);
    assertEquals(5f, entity.getPosition().y, 0.001f);
  }

  @Test
  void shouldScheduleDisposalOnCollision() {
    SpikyBallComponent component = new SpikyBallComponent(new Vector2(1f, 0f));

    Entity entity = new Entity().addComponent(component);

    entity.create();

    Fixture fixtureA = mock(Fixture.class);
    Fixture fixtureB = mock(Fixture.class);

    entity.getEvents().trigger("collisionStart", fixtureA, fixtureB);

    verify(entityService).scheduleForDisposal(entity);
  }

  @Test
  void shouldScheduleDisposalWhenHandleDisposeCalledDirectly() {
    SpikyBallComponent component = new SpikyBallComponent(new Vector2(1f, 0f));

    Entity entity = new Entity().addComponent(component);

    entity.create();

    Fixture fixtureA = mock(Fixture.class);
    Fixture fixtureB = mock(Fixture.class);

    component.handleDispose(fixtureA, fixtureB);

    verify(entityService).scheduleForDisposal(entity);
  }

  private Fixture fixtureOf(Entity owner) {
    BodyUserData data = new BodyUserData();
    data.entity = owner;
    Body body = mock(Body.class);
    when(body.getUserData()).thenReturn(data);
    Fixture fixture = mock(Fixture.class);
    when(fixture.getBody()).thenReturn(body);
    return fixture;
  }

  private Entity createBall() {
    Entity ball = new Entity().addComponent(new SpikyBallComponent(new Vector2(1f, 0f)));
    ball.create();
    return ball;
  }

  @Test
  void shouldKillPlayerOnCollision() {
    CombatStatsComponent stats = new CombatStatsComponent(10, 0, 1000L);
    Entity player = new Entity().addComponent(mock(PlayerActions.class)).addComponent(stats);
    EventListener0 death = mock(EventListener0.class);
    EventListener0 hurt = mock(EventListener0.class);
    player.getEvents().addListener("death", death);
    player.getEvents().addListener("hurt", hurt);
    Entity ball = createBall();

    ball.getEvents().trigger("collisionStart", mock(Fixture.class), fixtureOf(player));

    assertTrue(stats.isDead());
    verify(death).handle();
    verify(hurt).handle();
    verify(entityService).scheduleForDisposal(ball);
  }

  @Test
  void shouldKillPlayerEvenWhileInvulnerable() {
    when(timeSource.getTime()).thenReturn(0L);
    CombatStatsComponent stats = new CombatStatsComponent(10, 0, 1000L);
    Entity player = new Entity().addComponent(mock(PlayerActions.class)).addComponent(stats);
    stats.hit(new CombatStatsComponent(1, 2)); // starts the invulnerability window

    createBall().getEvents().trigger("collisionStart", mock(Fixture.class), fixtureOf(player));

    assertTrue(stats.isDead());
  }

  @Test
  void shouldTriggerDeathOnlyOnceForRepeatedContacts() {
    CombatStatsComponent stats = new CombatStatsComponent(10, 0, 1000L);
    Entity player = new Entity().addComponent(mock(PlayerActions.class)).addComponent(stats);
    EventListener0 death = mock(EventListener0.class);
    player.getEvents().addListener("death", death);
    Entity ball = createBall();
    Fixture playerFixture = fixtureOf(player);

    ball.getEvents().trigger("collisionStart", mock(Fixture.class), playerFixture);
    ball.getEvents().trigger("collisionStart", mock(Fixture.class), playerFixture);
    createBall().getEvents().trigger("collisionStart", mock(Fixture.class), playerFixture);

    verify(death, times(1)).handle();
  }

  @Test
  void shouldNotDamageNonPlayerEntities() {
    CombatStatsComponent stats = new CombatStatsComponent(10, 0);
    Entity enemy = new Entity().addComponent(stats);
    Entity ball = createBall();

    ball.getEvents().trigger("collisionStart", mock(Fixture.class), fixtureOf(enemy));

    assertEquals(10, stats.getHealth());
    verify(entityService).scheduleForDisposal(ball);
  }
}
