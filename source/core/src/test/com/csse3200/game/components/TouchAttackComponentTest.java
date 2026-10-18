package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TouchAttackComponentTest {
  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(0.3f);
    ServiceLocator.registerTimeSource(gameTime);
  }

  @Test
  void shouldAttack() {
    short targetLayer = (1 << 3);
    Entity entity = createAttacker(targetLayer);
    Entity target = createTarget(targetLayer);

    Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
    Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();
    entity.getEvents().trigger("collisionStart", entityFixture, targetFixture);

    entity.update();

    assertEquals(0, target.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldNotAttackOtherLayer() {
    short targetLayer = (1 << 3);
    short attackLayer = (1 << 4);
    Entity entity = createAttacker(attackLayer);
    Entity target = createTarget(targetLayer);

    Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
    Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();
    entity.getEvents().trigger("collisionStart", entityFixture, targetFixture);

    assertEquals(10, target.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldNotAttackWithoutCombatComponent() {
    short targetLayer = (1 << 3);
    Entity entity = createAttacker(targetLayer);
    // Target does not have a combat component
    Entity target =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(targetLayer));
    target.create();

    Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
    Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();

    // This should not cause an exception, but the attack should be ignored
    assertDoesNotThrow(
        () -> entity.getEvents().trigger("collisionStart", entityFixture, targetFixture));
  }

  Entity createAttacker(short targetLayer) {
    Entity entity =
        new Entity()
            .addComponent(new TouchAttackComponent(targetLayer))
            .addComponent(new CombatStatsComponent(0, 10))
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent());
    entity.create();
    return entity;
  }

  Entity createTarget(short layer) {
    Entity target =
        new Entity()
            .addComponent(new CombatStatsComponent(10, 0))
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(layer));
    target.create();
    return target;
  }

  // --- hitting over time, and being pushed back ---

  private static final short LAYER = (1 << 3);

  private void deltaPerUpdate(float seconds) {
    when(ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(seconds);
  }

  private static void touch(Entity attacker, Entity target) {
    Fixture mine = attacker.getComponent(HitboxComponent.class).getFixture();
    Fixture theirs = target.getComponent(HitboxComponent.class).getFixture();
    attacker.getEvents().trigger("collisionStart", mine, theirs);
  }

  private static void stopTouching(Entity attacker, Entity target) {
    Fixture mine = attacker.getComponent(HitboxComponent.class).getFixture();
    Fixture theirs = target.getComponent(HitboxComponent.class).getFixture();
    attacker.getEvents().trigger("collisionEnd", mine, theirs);
  }

  private static int healthOf(Entity entity) {
    return entity.getComponent(CombatStatsComponent.class).getHealth();
  }

  @Test
  void shouldNotHurtUntilTheyHaveBeenTouchingForALittleWhile() {
    deltaPerUpdate(0.1f);
    Entity attacker = createAttacker(LAYER);
    Entity target = createTarget(LAYER);
    touch(attacker, target);

    attacker.update();
    attacker.update();
    assertEquals(10, healthOf(target), "0.2s: not yet");

    attacker.update();
    assertEquals(0, healthOf(target), "0.3s: the first hit lands");
  }

  @Test
  void shouldKeepAnnouncingHitsWhileTheyStayInContact() {
    deltaPerUpdate(0.1f);
    Entity attacker = createAttacker(LAYER);
    Entity target = createTarget(LAYER);
    int[] announced = {0};
    attacker.getEvents().addListener("hitPlayer", () -> announced[0]++);
    touch(attacker, target);

    for (int update = 1; update <= 10; update++) {
      attacker.update();
    }

    // Hits land on updates 3, 6 and 9, and each is announced on the update after it
    assertEquals(3, announced[0]);
  }

  @Test
  void shouldStopHurtingOnceTheContactEnds() {
    deltaPerUpdate(0.1f);
    Entity attacker = createAttacker(LAYER);
    Entity target = createTarget(LAYER);
    int[] announced = {0};
    attacker.getEvents().addListener("hitPlayer", () -> announced[0]++);
    touch(attacker, target);
    for (int update = 1; update <= 4; update++) {
      attacker.update(); // one hit, announced on update 4
    }

    stopTouching(attacker, target);
    for (int update = 1; update <= 10; update++) {
      attacker.update();
    }

    assertEquals(1, announced[0], "nothing further once they stop touching");
  }

  @Test
  void shouldKeepHurtingWhenSomethingElseStopsTouching() {
    deltaPerUpdate(0.1f);
    Entity attacker = createAttacker(LAYER);
    Entity target = createTarget(LAYER);
    Entity bystander = createTarget(LAYER);
    int[] announced = {0};
    attacker.getEvents().addListener("hitPlayer", () -> announced[0]++);
    touch(attacker, target);

    stopTouching(attacker, bystander);
    for (int update = 1; update <= 4; update++) {
      attacker.update();
    }

    assertEquals(1, announced[0]);
  }

  @Test
  void shouldIgnoreContactsThatAreNotWithItsOwnHitbox() {
    deltaPerUpdate(0.3f);
    Entity attacker = createAttacker(LAYER);
    Entity target = createTarget(LAYER);
    Fixture someOtherFixture = mock(Fixture.class);
    Fixture theirs = target.getComponent(HitboxComponent.class).getFixture();

    attacker.getEvents().trigger("collisionStart", someOtherFixture, theirs);
    attacker.update();

    assertEquals(10, healthOf(target));
  }

  @Test
  void shouldIgnoreTheEndOfAContactThatWasNotWithItsOwnHitbox() {
    deltaPerUpdate(0.1f);
    Entity attacker = createAttacker(LAYER);
    Entity target = createTarget(LAYER);
    Fixture theirs = target.getComponent(HitboxComponent.class).getFixture();
    int[] announced = {0};
    attacker.getEvents().addListener("hitPlayer", () -> announced[0]++);
    touch(attacker, target);

    attacker.getEvents().trigger("collisionEnd", mock(Fixture.class), theirs);
    for (int update = 1; update <= 4; update++) {
      attacker.update();
    }

    assertEquals(1, announced[0], "still touching: that end was somebody else's");
  }

  @Test
  void shouldAnnounceTheHitOnTheResponsibleEntityWhenGivenOne() {
    deltaPerUpdate(0.3f);
    Entity owner = new Entity();
    Entity attacker =
        new Entity()
            .addComponent(new TouchAttackComponent(LAYER, 0f, owner))
            .addComponent(new CombatStatsComponent(0, 10))
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent());
    attacker.create();
    Entity target = createTarget(LAYER);
    int[] ownerHits = {0};
    int[] attackerHits = {0};
    owner.getEvents().addListener("hitPlayer", () -> ownerHits[0]++);
    attacker.getEvents().addListener("hitPlayer", () -> attackerHits[0]++);
    touch(attacker, target);

    attacker.update(); // the hit lands
    attacker.update(); // and is announced

    assertEquals(1, ownerHits[0], "the owner is credited");
    assertEquals(0, attackerHits[0], "not the throwaway attack entity");
  }

  @Test
  void shouldNotAnnounceAHitOnATargetThatCannotBeHurt() {
    deltaPerUpdate(0.3f);
    Entity attacker = createAttacker(LAYER);
    Entity scenery =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(LAYER));
    scenery.create();
    int[] announced = {0};
    attacker.getEvents().addListener("hitPlayer", () -> announced[0]++);
    touch(attacker, scenery);

    attacker.update();
    attacker.update();

    assertEquals(0, announced[0]);
  }

  private Entity attackerWithKnockback(float knockback) {
    Entity attacker =
        new Entity()
            .addComponent(new TouchAttackComponent(LAYER, knockback))
            .addComponent(new CombatStatsComponent(0, 1))
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent());
    attacker.create();
    attacker.setPosition(0f, 0f);
    return attacker;
  }

  private com.badlogic.gdx.math.Vector2 velocityAfterHitWithKnockback(
      float knockback, float targetX, float targetY) {
    deltaPerUpdate(0.3f);
    Entity attacker = attackerWithKnockback(knockback);
    Entity target = createTarget(LAYER);
    target.setPosition(targetX, targetY);
    touch(attacker, target);
    attacker.update();
    return target.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().cpy();
  }

  @Test
  void shouldPushTheTargetAwayFromTheAttacker() {
    com.badlogic.gdx.math.Vector2 pushedRight = velocityAfterHitWithKnockback(10f, 4f, 0f);
    com.badlogic.gdx.math.Vector2 pushedLeft = velocityAfterHitWithKnockback(10f, -4f, 0f);

    assertTrue(pushedRight.x > 0f, "target on the right goes right");
    assertEquals(0f, pushedRight.y, 1e-4f);
    assertTrue(pushedLeft.x < 0f, "target on the left goes left");
  }

  @Test
  void shouldPushUpwardWhenTheTargetIsAbove() {
    com.badlogic.gdx.math.Vector2 pushed = velocityAfterHitWithKnockback(10f, 0f, 4f);

    assertTrue(pushed.y > 0f);
    assertEquals(0f, pushed.x, 1e-4f);
  }

  @Test
  void shouldPushHarderWithMoreKnockback() {
    float gentle = velocityAfterHitWithKnockback(10f, 4f, 0f).len();
    float hard = velocityAfterHitWithKnockback(20f, 4f, 0f).len();

    assertEquals(2f * gentle, hard, gentle * 0.01f);
  }

  @Test
  void shouldNotPushWithoutKnockback() {
    com.badlogic.gdx.math.Vector2 velocity = velocityAfterHitWithKnockback(0f, 4f, 0f);

    assertEquals(0f, velocity.len(), 1e-6f);
  }
}
