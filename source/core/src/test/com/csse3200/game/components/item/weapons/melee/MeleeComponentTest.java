package com.csse3200.game.components.item.weapons.melee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MeleeComponentTest {
  private static final float STEP = 0.2f;

  private GameTime time;
  private PhysicsEngine physics;
  private EntityService entityService;
  private Entity player;
  private Entity enemy;
  private MeleeComponent melee;

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(STEP);
    ServiceLocator.registerTimeSource(time);

    physics = mock(PhysicsEngine.class);
    when(physics.raycastAll(any(Vector2.class), any(Vector2.class), anyShort()))
        .thenReturn(new RaycastHit[0]);
    ServiceLocator.registerPhysicsService(new PhysicsService(physics));

    entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);

    melee = new MeleeComponent();
    player = new Entity().addComponent(melee).addComponent(new CombatStatsComponent(100, 20));
    player.create();

    enemy = new Entity().addComponent(new CombatStatsComponent(100, 5));
    enemy.create();
  }

  private void enemyIsInArc() {
    BodyUserData data = new BodyUserData();
    data.entity = enemy;
    Body body = mock(Body.class);
    when(body.getUserData()).thenReturn(data);
    Fixture fixture = mock(Fixture.class);
    when(fixture.getBody()).thenReturn(body);
    RaycastHit hit = new RaycastHit();
    hit.fixture = fixture;
    when(physics.raycastAll(any(Vector2.class), any(Vector2.class), anyShort()))
        .thenReturn(new RaycastHit[] {hit});
  }

  private void run(int updates) {
    for (int i = 0; i < updates; i++) {
      melee.update();
    }
  }

  @Test
  void swingStartsAndAnnouncesFacing() {
    AtomicInteger swings = new AtomicInteger();
    player.getEvents().addListener("meleeSwing", (Integer facing) -> swings.set(facing));

    player.getEvents().trigger("meleeStart");

    assertTrue(melee.isSwinging());
    assertEquals(1, swings.get());
  }

  @Test
  void damagesAnEnemyInTheArcOncePerSwing() {
    enemyIsInArc();
    AtomicInteger hits = new AtomicInteger();
    player.getEvents().addListener("meleeHit", (Entity target) -> hits.incrementAndGet());

    player.getEvents().trigger("meleeStart");
    run(4);

    assertEquals(80, enemy.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(1, hits.get());
    assertFalse(melee.isSwinging());
  }

  @Test
  void doesNotDamageAnythingBeforeTheArcLands() {
    enemyIsInArc();

    // A step shorter than the delay before the arc lands.
    when(time.getDeltaTime()).thenReturn(0.05f);
    player.getEvents().trigger("meleeStart");
    run(1);

    assertEquals(100, enemy.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void missesWhenNothingIsInTheArc() {
    player.getEvents().trigger("meleeStart");
    run(3);

    assertEquals(100, enemy.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void cannotSwingAgainUntilTheSwingAndCooldownEnd() {
    AtomicInteger swings = new AtomicInteger();
    player.getEvents().addListener("meleeSwing", (Integer facing) -> swings.incrementAndGet());

    player.getEvents().trigger("meleeStart");
    player.getEvents().trigger("meleeStart");
    assertEquals(1, swings.get());

    // Finish the swing and let the cooldown run out.
    run(5);
    player.getEvents().trigger("meleeStart");
    assertEquals(2, swings.get());
  }

  @Test
  void deadPlayerCannotSwing() {
    player.getEvents().trigger("death");
    player.getEvents().trigger("meleeStart");

    assertFalse(melee.isSwinging());
  }

  @Test
  void swingDoesNotAdvanceWhilePaused() {
    when(entityService.getPaused()).thenReturn(true);
    enemyIsInArc();

    player.getEvents().trigger("meleeStart");
    run(5);

    assertTrue(melee.isSwinging());
    assertEquals(100, enemy.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void neverHitsThePlayerItself() {
    BodyUserData data = new BodyUserData();
    data.entity = player;
    Body body = mock(Body.class);
    when(body.getUserData()).thenReturn(data);
    Fixture fixture = mock(Fixture.class);
    when(fixture.getBody()).thenReturn(body);
    RaycastHit hit = new RaycastHit();
    hit.fixture = fixture;
    when(physics.raycastAll(any(Vector2.class), any(Vector2.class), anyShort()))
        .thenReturn(new RaycastHit[] {hit});

    player.getEvents().trigger("meleeStart");
    run(3);

    assertEquals(100, player.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void revivedPlayerCanDamageAnEnemyAgain() {
    enemyIsInArc();
    player.getEvents().trigger("death");
    player.getEvents().trigger("revive");

    player.getEvents().trigger("meleeStart");
    run(4);

    assertEquals(80, enemy.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void deathCancelsTheDelayedHitAndSwing() {
    enemyIsInArc();
    player.getEvents().trigger("meleeStart");
    player.getEvents().trigger("death");

    run(4);

    assertFalse(melee.isSwinging());
    assertEquals(100, enemy.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void reviveClearsThePreviousSwingCooldown() {
    player.getEvents().trigger("meleeStart");
    run(4);
    player.getEvents().trigger("death");
    player.getEvents().trigger("revive");

    player.getEvents().trigger("meleeStart");

    assertTrue(melee.isSwinging());
    assertEquals(0f, melee.getSwingProgress());
  }
}
