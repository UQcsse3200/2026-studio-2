package com.csse3200.game.components.item.weapons.bow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BowLifecycleTest {
  private final List<Float> speeds = new ArrayList<>();
  private final List<Vector2> directions = new ArrayList<>();
  private final List<Vector2> animations = new ArrayList<>();
  private EntityService entities;
  private GameTime time;
  private BowComponent bow;
  private Entity player;

  @BeforeEach
  void setUp() {
    entities = new EntityService();
    time = mock(GameTime.class);
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerTimeSource(time);
    ServiceLocator.registerResourceService(null);
    bow =
        new BowComponent(
            (shooter, position, direction, speed) -> {
              speeds.add(speed);
              directions.add(direction.cpy());
              Entity projectile = new Entity();
              projectile.setPosition(position);
              return projectile;
            });
    player = new Entity().addComponent(bow);
    player.getEvents().<Vector2>addListener("attackAnimation", animations::add);
    entities.register(player);
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
  }

  @Test
  void repeatedChargeStartPreservesOriginalDrawTime() {
    when(time.getTime()).thenReturn(1000L);
    player.getEvents().trigger("chargeStart", Vector2.X.cpy());
    when(time.getTime()).thenReturn(2000L);
    player.getEvents().trigger("chargeStart", Vector2.X.cpy());
    when(time.getTime()).thenReturn(2500L);
    player.getEvents().trigger("chargeRelease", Vector2.X.cpy());

    assertEquals(List.of(1.5f), speeds);
    assertEquals(2, entities.getEntities().size);
  }

  @Test
  void ordinaryAttackCannotInterruptChargedShot() {
    bow.startCharge(Vector2.X.cpy());
    player.getEvents().trigger("attack", Vector2.Y.cpy());
    assertTrue(speeds.isEmpty());
    assertFalse(bow.isReady());

    bow.releaseCharge(Vector2.X.cpy());
    assertEquals(List.of(0.3f), speeds);
    assertEquals(List.of(Vector2.X), directions);
  }

  @Test
  void releaseUsesLatestAimWithoutMutatingCallerVector() {
    bow.startCharge(Vector2.X.cpy());
    Vector2 releaseAim = new Vector2(0f, -8f);
    bow.releaseCharge(releaseAim);

    assertEquals(new Vector2(0f, -8f), releaseAim);
    assertEquals(List.of(new Vector2(0f, -1f)), directions);
    assertEquals(directions, animations);
    assertEquals(2, entities.getEntities().size);
  }

  @Test
  void invalidChargeStartsLeaveBowAvailable() {
    bow.startCharge(null);
    bow.startCharge(Vector2.Zero.cpy());
    bow.releaseCharge(Vector2.X.cpy());
    assertTrue(bow.isReady());
    assertTrue(speeds.isEmpty());

    bow.attack(Vector2.X.cpy());
    assertEquals(List.of(1f), speeds);
  }

  @Test
  void invalidReleaseClearsChargeWithoutConsumingCooldown() {
    for (Vector2 invalid : new Vector2[] {null, Vector2.Zero.cpy()}) {
      bow.startCharge(Vector2.X.cpy());
      bow.releaseCharge(invalid);
      assertTrue(bow.isReady());
      assertEquals(0f, bow.getCooldownRemaining());
      assertTrue(speeds.isEmpty());
      assertTrue(animations.isEmpty());
    }
    bow.attack(Vector2.X.cpy());
    assertEquals(List.of(1f), speeds);
  }

  @Test
  void invalidAttackDoesNotDelayNextValidShot() {
    bow.attack(null);
    bow.attack(Vector2.Zero.cpy());
    assertTrue(bow.isReady());
    assertEquals(0f, bow.getCooldownRemaining());
    assertEquals(1, entities.getEntities().size);

    bow.attack(Vector2.X.cpy());
    assertEquals(List.of(1f), speeds);
  }

  @Test
  void cooldownRejectsEarlyShotAndAllowsShotAtExactBoundary() {
    bow.attack(Vector2.X.cpy());
    when(time.getDeltaTime()).thenReturn(0.2f);
    bow.update();
    bow.attack(Vector2.Y.cpy());
    assertEquals(1, speeds.size());
    assertEquals(0.2f, bow.getCooldownRemaining(), 1e-6f);

    bow.update();
    assertTrue(bow.isReady());
    bow.attack(Vector2.Y.cpy());
    assertEquals(List.of(Vector2.X, Vector2.Y), directions);
    assertEquals(0.4f, bow.getCooldownRemaining(), 1e-6f);
  }

  @Test
  void longChargeRetainsFullPostShotCooldown() {
    bow.startCharge(Vector2.X.cpy());
    when(time.getTime()).thenReturn(5000L);
    when(time.getDeltaTime()).thenReturn(5f);
    bow.update();
    bow.releaseCharge(Vector2.X.cpy());

    assertEquals(List.of(1.5f), speeds);
    assertEquals(0.4f, bow.getCooldownRemaining(), 1e-6f);
    assertFalse(bow.isReady());
    bow.update();
    assertEquals(0f, bow.getCooldownRemaining());
    assertTrue(bow.isReady());
  }

  @Test
  void deathCancelsPendingShotAndFreshChargeUsesNewStartTime() {
    bow.startCharge(Vector2.X.cpy());
    when(time.getTime()).thenReturn(5000L);
    player.getEvents().trigger("death");
    bow.releaseCharge(Vector2.X.cpy());
    assertTrue(speeds.isEmpty());
    assertTrue(bow.isReady());

    bow.startCharge(Vector2.Y.cpy());
    bow.releaseCharge(Vector2.Y.cpy());
    assertEquals(List.of(0.3f), speeds);
  }

  @Test
  void missingSoundAssetDoesNotPreventShotOrAnimation() {
    ResourceService resources = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resources);
    bow.attack(Vector2.X.cpy());

    assertEquals(2, entities.getEntities().size);
    assertEquals(List.of(Vector2.X), animations);
    assertFalse(bow.isReady());
    verify(resources, never()).getAsset("sounds/Impact4.ogg", Sound.class);
  }

  @Test
  void nullSoundAssetDoesNotPreventShotOrAnimation() {
    ResourceService resources = mock(ResourceService.class);
    when(resources.containsAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(true);
    ServiceLocator.registerResourceService(resources);
    bow.attack(Vector2.X.cpy());

    assertEquals(2, entities.getEntities().size);
    assertEquals(List.of(Vector2.X), animations);
    assertEquals(List.of(1f), speeds);
    assertFalse(bow.isReady());
    verify(resources).getAsset("sounds/Impact4.ogg", Sound.class);
  }
}
