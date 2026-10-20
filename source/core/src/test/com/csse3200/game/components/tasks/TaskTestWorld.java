package com.csse3200.game.components.tasks;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * A small world for testing enemy tasks: real physics, a clock the test controls, and some ground
 * to build. Registers its services on creation, so make one per test.
 */
final class TaskTestWorld {
  private long now = 0L;
  final PhysicsService physics = new PhysicsService();
  final DebugRenderer debugRenderer = mock(DebugRenderer.class);

  TaskTestWorld() {
    GameTime time = mock(GameTime.class);
    when(time.getTime()).thenAnswer(call -> now);
    when(time.getTimeSince(anyLong())).thenAnswer(call -> now - (long) call.getArgument(0));
    when(time.getDeltaTime()).thenReturn(0.02f);
    ServiceLocator.registerTimeSource(time);
    ServiceLocator.registerPhysicsService(physics);
    RenderService renderService = new RenderService();
    renderService.setDebug(debugRenderer);
    ServiceLocator.registerRenderService(renderService);
  }

  /** Moves the clock on, in milliseconds. */
  void advance(long milliseconds) {
    now += milliseconds;
  }

  /** A created entity with a body and movement, at the given position, one unit square. */
  Entity enemyAt(float x, float y) {
    Entity enemy =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent());
    enemy.create();
    enemy.setPosition(x, y);
    return enemy;
  }

  /** As enemyAt, with combat stats too: no invulnerability, so every hit lands. */
  Entity fighterAt(float x, float y, int health, int attack) {
    Entity fighter =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new CombatStatsComponent(health, attack, 0L));
    fighter.create();
    fighter.setPosition(x, y);
    return fighter;
  }

  static PhysicsMovementComponent movementOf(Entity enemy) {
    return enemy.getComponent(PhysicsMovementComponent.class);
  }

  /** A solid block on the given layer, centred at (x, y) with the given half sizes. */
  void addBlock(short layer, float x, float y, float halfWidth, float halfHeight) {
    BodyDef def = new BodyDef();
    def.position.set(x, y);
    Body body = physics.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(halfWidth, halfHeight);
    Fixture fixture = body.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = layer;
    fixture.setFilterData(filter);
  }

  /** Ground whose top surface is at y = 0, spanning from x = left to x = right. */
  void addGround(float left, float right) {
    addBlock(PhysicsLayer.GROUND, (left + right) / 2f, -1f, (right - left) / 2f, 1f);
  }
}
