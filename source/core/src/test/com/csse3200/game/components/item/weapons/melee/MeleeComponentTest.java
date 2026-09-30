package com.csse3200.game.components.item.weapons.melee;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Exercises melee damage and targeting through real Box2D raycasts. */
@ExtendWith(GameExtension.class)
class MeleeComponentTest {
  private PhysicsService physics;
  private GameTime time;
  private Entity player;
  private MeleeComponent melee;
  private final List<Vector2> animations = new ArrayList<>();

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    melee = new MeleeComponent();
    player = new Entity().addComponent(new CombatStatsComponent(20, 3)).addComponent(melee);
    player.create();
    player.getEvents().<Vector2>addListener("attackAnimation", animations::add);
  }

  @AfterEach
  void tearDown() {
    physics.getPhysics().dispose();
  }

  private Body targetBody(float offsetX, short layer, Object data) {
    BodyDef def = new BodyDef();
    def.position.set(player.getCenterPosition()).add(offsetX, 0f);
    Body body = physics.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    try {
      shape.setAsBox(0.05f, 0.1f);
      Fixture fixture = body.createFixture(shape, 0f);
      Filter filter = fixture.getFilterData();
      filter.categoryBits = layer;
      fixture.setFilterData(filter);
    } finally {
      shape.dispose();
    }
    body.setUserData(data);
    return body;
  }

  private Entity enemy(float offsetX) {
    Entity enemy = new Entity().addComponent(new CombatStatsComponent(10, 1));
    enemy.create();
    BodyUserData data = new BodyUserData();
    data.entity = enemy;
    targetBody(offsetX, PhysicsLayer.NPC, data);
    return enemy;
  }

  @Test
  void shouldDamageOnlyClosestEnemyAndEmitNormalizedAimWithoutMutatingInput() {
    Entity near = enemy(0.4f);
    Entity far = enemy(0.8f);
    Vector2 aim = new Vector2(8f, 0f);
    player.getEvents().trigger("melee", aim);
    assertEquals(7, near.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(10, far.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(List.of(new Vector2(1f, 0f)), animations);
    assertEquals(new Vector2(8f, 0f), aim);
    assertFalse(melee.isReady());
  }

  @Test
  void shouldRejectRepeatedAttackUntilCooldownExpires() {
    Entity enemy = enemy(0.5f);
    player.getEvents().trigger("attack", Vector2.X);
    when(time.getDeltaTime()).thenReturn(0.1f);
    melee.update();
    melee.attack(Vector2.X);
    assertEquals(7, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(1, animations.size());
    assertEquals(0.2f, melee.getCooldownRemaining(), 0.0001f);
    when(time.getDeltaTime()).thenReturn(0.3f);
    melee.update();
    assertTrue(melee.isReady());
    assertEquals(0f, melee.getCooldownRemaining());
    melee.attack(Vector2.X);
    assertEquals(4, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(2, animations.size());
  }

  @Test
  void shouldIgnoreInvalidAimWithoutSpendingCooldown() {
    Entity enemy = enemy(0.5f);
    melee.attack(null);
    melee.attack(Vector2.Zero);
    melee.update();
    assertTrue(melee.isReady());
    assertEquals(10, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertTrue(animations.isEmpty());
  }

  @Test
  void shouldMissEnemiesBehindOrBeyondReachEvenWithLongAimVector() {
    Entity behind = enemy(-0.5f);
    Entity far = enemy(1.2f);
    melee.attack(new Vector2(100f, 0f));
    assertEquals(10, behind.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(10, far.getComponent(CombatStatsComponent.class).getHealth());
    assertFalse(melee.isReady());
    assertTrue(animations.isEmpty());
  }

  @Test
  void shouldAttackToTheLeft() {
    Entity enemy = enemy(-0.5f);
    melee.attack(new Vector2(-4f, 0f));
    assertEquals(7, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(List.of(new Vector2(-1f, 0f)), animations);
  }

  @Test
  void shouldIgnoreFixturesOutsideNpcLayer() {
    Entity target = new Entity().addComponent(new CombatStatsComponent(10, 1));
    BodyUserData data = new BodyUserData();
    data.entity = target;
    targetBody(0.5f, PhysicsLayer.PLAYER, data);
    melee.attack(Vector2.X);
    assertEquals(10, target.getComponent(CombatStatsComponent.class).getHealth());
    assertTrue(animations.isEmpty());
  }

  @Test
  void shouldIgnoreUnrecognisedBodyData() {
    targetBody(0.5f, PhysicsLayer.NPC, "not an entity");
    melee.attack(Vector2.X);
    assertFalse(melee.isReady());
    assertTrue(animations.isEmpty());
  }

  @Test
  void shouldIgnoreBodyWithoutTargetEntity() {
    targetBody(0.5f, PhysicsLayer.NPC, new BodyUserData());
    melee.attack(Vector2.X);
    assertFalse(melee.isReady());
    assertTrue(animations.isEmpty());
  }

  @Test
  void shouldAnimateContactWithTargetWithoutCombatStats() {
    BodyUserData data = new BodyUserData();
    data.entity = new Entity();
    targetBody(0.5f, PhysicsLayer.NPC, data);
    melee.attack(Vector2.X);
    assertEquals(List.of(new Vector2(1f, 0f)), animations);
  }
}
