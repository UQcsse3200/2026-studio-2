package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.ai.tasks.TaskRunner;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Fires skeleton archer arrows through the real {@link RangedAttackTask} and real physics, over a
 * real floor, so the arrow's flight is checked against what actually happens rather than against
 * numbers the test made up.
 */
@ExtendWith(GameExtension.class)
class EnemyArrowFlightIntegrationTest {
  private static final float STEP = 0.016f;
  private static final int DAMAGE = 5;
  private static final int PLAYER_HEALTH = 20;
  private static final int MAX_STEPS = 400;

  private PhysicsService physics;
  private EntityService entities;

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(STEP);
    when(time.getTime()).thenReturn(0L);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerRenderService(mock(RenderService.class));

    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1254);
    when(texture.getHeight()).thenReturn(1254);
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resources);

    // A long floor whose top surface is at y = 0.
    BodyDef def = new BodyDef();
    def.position.set(0f, -1f);
    Body floor = physics.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(100f, 1f);
    Fixture fixture = floor.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.GROUND;
    fixture.setFilterData(filter);
  }

  @AfterEach
  void tearDown() {
    physics.getPhysics().dispose();
  }

  /** A stationary one by one character with a hitbox on the given layer and 20 health. */
  private Entity character(Vector2 position, short layer) {
    Entity character =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new HitboxComponent().setLayer(layer))
            .addComponent(new CombatStatsComponent(PLAYER_HEALTH, 0));
    character.setPosition(position);
    entities.register(character);
    return character;
  }

  private static int healthOf(Entity character) {
    return character.getComponent(CombatStatsComponent.class).getHealth();
  }

  /** What happened to one arrow. */
  private record Flight(Entity arrow, int stepsUntilGone, float highestPoint, boolean gone) {}

  /** Has the archer fire once at the target, then steps the world until the arrow is gone. */
  private Flight fire(Entity archer, Entity target) {
    RangedAttackTask task =
        new RangedAttackTask(
            target, 1, 100f, 0f, DAMAGE, ProjectileFactory.STANDARD_ARROW_SPEED, 5f, "range");
    TaskRunner runner = () -> archer;
    task.create(runner);
    task.start();
    int before = entities.getEntities().size;
    task.update();
    assertEquals(before + 1, entities.getEntities().size, "the archer should fire exactly one");
    Entity arrow = entities.getEntities().peek();
    assertNotNull(arrow);

    float highest = arrow.getCenterPosition().y;
    for (int step = 0; step < MAX_STEPS; step++) {
      physics.getPhysics().update();
      entities.update();
      highest = Math.max(highest, arrow.getCenterPosition().y);
      if (!entities.getEntities().contains(arrow, true)) {
        return new Flight(arrow, step, highest, true);
      }
    }
    return new Flight(arrow, MAX_STEPS, highest, false);
  }

  @Test
  void shouldLandOnATargetOnTheSameLevelAtAnyDistanceOnEitherSide() {
    for (float distance : new float[] {3f, 6f, 9f, 12f}) {
      for (float side : new float[] {1f, -1f}) {
        tearDown();
        setUp();
        Entity archer = new Entity();
        archer.setPosition(0f, 0f);
        Entity player = character(new Vector2(side * distance, 0f), PhysicsLayer.PLAYER);

        Flight flight = fire(archer, player);

        assertEquals(
            PLAYER_HEALTH - DAMAGE,
            healthOf(player),
            "arrow shot " + distance + " units to the " + (side > 0 ? "right" : "left"));
        assertTrue(flight.gone(), "and the arrow should be gone once it has landed");
      }
    }
  }

  @Test
  void shouldReachATargetAtTheDistanceTheSkeletonArcherShootsFrom() {
    float attackRange =
        FileLoader.readClass(EnemyConfigs.class, "configs/Enemies.json").skeletonArcher.attackRange;
    assertTrue(attackRange >= 10f, "archers should open fire from at least ten units away");

    for (float side : new float[] {1f, -1f}) {
      tearDown();
      setUp();
      Entity archer = new Entity();
      archer.setPosition(0f, 0f);
      Entity player = character(new Vector2(side * attackRange, 0f), PhysicsLayer.PLAYER);

      fire(archer, player);

      assertEquals(
          PLAYER_HEALTH - DAMAGE,
          healthOf(player),
          "an arrow shot " + attackRange + " units away should still land");
    }
  }

  @Test
  void shouldLandOnATargetStandingHigherUp() {
    Entity archer = new Entity();
    archer.setPosition(0f, 0f);
    Entity player = character(new Vector2(6f, 3f), PhysicsLayer.PLAYER);

    fire(archer, player);

    assertEquals(PLAYER_HEALTH - DAMAGE, healthOf(player));
  }

  @Test
  void shouldLandOnATargetOnTheFloorBelowALedge() {
    Entity archer = new Entity();
    archer.setPosition(0f, 6f);
    Entity player = character(new Vector2(6f, 0f), PhysicsLayer.PLAYER);

    fire(archer, player);

    assertEquals(PLAYER_HEALTH - DAMAGE, healthOf(player));
  }

  @Test
  void shouldArcUpAndOverLikeAPlayersArrowRatherThanFlyStraight() {
    Entity archer = new Entity();
    archer.setPosition(0f, 0f);
    Entity player = character(new Vector2(12f, 0f), PhysicsLayer.PLAYER);
    float spawnHeight = 0.5f - 0.15f;

    Flight flight = fire(archer, player);

    // A straight shot at a target level with the archer never climbs; an arc has to.
    assertTrue(
        flight.highestPoint() > spawnHeight + 1f,
        "the arrow only reached " + flight.highestPoint() + " from " + spawnHeight);
    assertEquals(PLAYER_HEALTH - DAMAGE, healthOf(player));
  }

  @Test
  void shouldBreakOnTheFloorInsteadOfDraggingAlongIt() {
    Entity archer = new Entity();
    archer.setPosition(0f, 0f);
    // Below the floor's surface, so the only thing the arrow can do is run into the floor. It
    // used to graze the floor on its solid collider and crawl along it until its lifetime ran out.
    Entity player = character(new Vector2(8f, -4f), PhysicsLayer.PLAYER);

    Flight flight = fire(archer, player);

    assertTrue(flight.gone(), "the arrow should have broken on the floor");
    assertTrue(
        flight.stepsUntilGone() * STEP < 1.5f,
        "it took " + flight.stepsUntilGone() * STEP + " seconds to break");
    assertEquals(PLAYER_HEALTH, healthOf(player));
  }

  @Test
  void shouldBreakOnTheFloorAfterAShortArcFromALedge() {
    Entity archer = new Entity();
    archer.setPosition(0f, 6f);
    Entity player = character(new Vector2(3f, -4f), PhysicsLayer.PLAYER);

    Flight flight = fire(archer, player);

    assertTrue(flight.gone());
    assertTrue(flight.stepsUntilGone() * STEP < 1.5f);
  }

  @Test
  void shouldNotHurtItsOwnArcherOrFlyIntoOtherEnemiesBeforeHittingThePlayer() {
    Entity archer = character(new Vector2(0f, 0f), PhysicsLayer.NPC);
    Entity bystander = character(new Vector2(3f, 0f), PhysicsLayer.NPC);
    Entity player = character(new Vector2(9f, 0f), PhysicsLayer.PLAYER);

    fire(archer, player);

    assertEquals(PLAYER_HEALTH, healthOf(archer));
    assertEquals(PLAYER_HEALTH, healthOf(bystander));
    assertEquals(PLAYER_HEALTH - DAMAGE, healthOf(player));
  }

  @Test
  void shouldNotCarryOnThroughThePlayerAfterHittingThem() {
    Entity archer = new Entity();
    archer.setPosition(0f, 0f);
    Entity player = character(new Vector2(6f, 0f), PhysicsLayer.PLAYER);

    Flight flight = fire(archer, player);

    assertTrue(flight.gone());
    // 6 units at 18 per second is about a third of a second, not a trip to the arrow's range.
    assertTrue(flight.stepsUntilGone() * STEP < 1f);
    assertFalse(entities.getEntities().contains(flight.arrow(), true));
  }
}
