package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** A projectile that hurts whatever on its target layer it touches. */
@ExtendWith(GameExtension.class)
class ProjectileAttackComponentTest {
  private static final int ATTACK = 3;

  private Entity projectile;
  private Fixture projectileFixture;

  @BeforeEach
  void setUp() {
    projectileFixture = mock(Fixture.class);
    HitboxComponent hitbox = mock(HitboxComponent.class);
    when(hitbox.getFixture()).thenReturn(projectileFixture);
    projectile =
        new Entity()
            .addComponent(new CombatStatsComponent(1, ATTACK))
            .addComponent(hitbox)
            .addComponent(new ProjectileAttackComponent(PhysicsLayer.PLAYER));
    projectile.create();
  }

  private static Entity targetWithHealth(int health) {
    // No invulnerability, so every hit lands
    return new Entity().addComponent(new CombatStatsComponent(health, 0, 0L));
  }

  private static Fixture fixtureOf(Entity entity, short layer) {
    Filter filter = new Filter();
    filter.categoryBits = layer;
    Body body = mock(Body.class);
    BodyUserData userData = new BodyUserData();
    userData.entity = entity;
    when(body.getUserData()).thenReturn(userData);
    Fixture fixture = mock(Fixture.class);
    when(fixture.getFilterData()).thenReturn(filter);
    when(fixture.getBody()).thenReturn(body);
    return fixture;
  }

  private void touch(Fixture mine, Fixture other) {
    projectile.getEvents().trigger("collisionStart", mine, other);
  }

  private static int healthOf(Entity entity) {
    return entity.getComponent(CombatStatsComponent.class).getHealth();
  }

  @Test
  void shouldHurtTheTargetByItsAttackWhenItsHitboxTouchesThem() {
    Entity player = targetWithHealth(10);

    touch(projectileFixture, fixtureOf(player, PhysicsLayer.PLAYER));

    assertEquals(10 - ATTACK, healthOf(player));
  }

  @Test
  void shouldHurtEachTimeItTouchesTheTarget() {
    Entity player = targetWithHealth(10);
    Fixture playerFixture = fixtureOf(player, PhysicsLayer.PLAYER);

    touch(projectileFixture, playerFixture);
    touch(projectileFixture, playerFixture);

    assertEquals(10 - 2 * ATTACK, healthOf(player));
  }

  @Test
  void shouldLeaveThingsOnOtherLayersAlone() {
    Entity enemy = targetWithHealth(10);

    touch(projectileFixture, fixtureOf(enemy, PhysicsLayer.NPC));
    touch(projectileFixture, fixtureOf(enemy, PhysicsLayer.GROUND));

    assertEquals(10, healthOf(enemy));
  }

  @Test
  void shouldIgnoreContactsThatAreNotWithItsOwnHitbox() {
    Entity player = targetWithHealth(10);

    touch(mock(Fixture.class), fixtureOf(player, PhysicsLayer.PLAYER)); // some other fixture

    assertEquals(10, healthOf(player), "only the hitbox does the damage");
  }

  @Test
  void shouldHitATargetThatIsOnSeveralLayersIncludingItsTarget() {
    Entity player = targetWithHealth(10);

    touch(projectileFixture, fixtureOf(player, (short) (PhysicsLayer.PLAYER | PhysicsLayer.NPC)));

    assertEquals(10 - ATTACK, healthOf(player));
  }

  @Test
  void shouldNotCrashOnATargetThatCannotBeHurt() {
    Entity scenery = new Entity(); // nothing to take damage with

    assertDoesNotThrow(() -> touch(projectileFixture, fixtureOf(scenery, PhysicsLayer.PLAYER)));
  }
}
