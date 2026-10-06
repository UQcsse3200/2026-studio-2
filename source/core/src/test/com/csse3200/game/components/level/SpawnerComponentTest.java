package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SpawnerComponentTest {

  private GameTime timeSource;

  @BeforeEach
  void setUp() {
    timeSource = mock(GameTime.class);
    ServiceLocator.registerTimeSource(timeSource);
  }

  private ArrayList<Supplier<Entity>> createSpawnables(Entity child) {
    ArrayList<Supplier<Entity>> spawnables = new ArrayList<>();
    spawnables.add(() -> child);
    return spawnables;
  }

  @Test
  void shouldNotSpawnWhenInactive() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            false,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(2f);

    component.update();

    assertEquals(0, spawnCount[0]);
  }

  @Test
  void shouldNotSpawnWhenIntervalIsNegativeOne() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            -1f,
            5,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(10f);

    component.update();

    assertEquals(0, spawnCount[0]);
  }

  @Test
  void shouldWaitUntilSpawnIntervalReached() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(0.4f);

    component.update();
    component.update();

    assertEquals(0, spawnCount[0]);

    component.update();

    assertEquals(1, spawnCount[0]);
  }

  @Test
  void shouldSpawnEntityWhenIntervalReached() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final Entity[] spawnedEntity = {null};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnedEntity[0] = spawned);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    component.update();

    assertSame(child, spawnedEntity[0]);
  }

  @Test
  void shouldSpawnChildAtCenterOfSpawner() {
    Entity child = new Entity();
    child.setScale(2f, 2f);

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.setPosition(4f, 6f);
    spawner.setScale(4f, 4f);
    spawner.create();

    when(timeSource.getDeltaTime()).thenReturn(1f);

    component.update();

    Vector2 center = spawner.getCenterPosition();

    assertEquals(center.x - 1f, child.getPosition().x, 0.001f);
    assertEquals(center.y - 1f, child.getPosition().y, 0.001f);
  }

  @Test
  void shouldStopAfterMaximumSpawns() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            2,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    component.update();
    component.update();

    // These updates should neither spawn more entities nor throw an NPE
    // when no activation IDs have been explicitly configured.
    assertDoesNotThrow(component::update);
    assertDoesNotThrow(component::update);

    assertEquals(2, spawnCount[0]);
  }

  @Test
  void shouldActivateNormalSpawner() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            false,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    spawner.getEvents().trigger("activatedMapComponent", true);
    component.update();

    assertEquals(1, spawnCount[0]);
  }

  @Test
  void shouldDeactivateNormalSpawner() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    spawner.getEvents().trigger("activatedMapComponent", false);
    component.update();

    assertEquals(0, spawnCount[0]);
  }

  @Test
  void shouldToggleSpawnerOn() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            false,
            SpawnerComponent.ACTIVATION_MODE.TOGGLE);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    spawner.getEvents().trigger("activatedMapComponent", true);
    component.update();

    assertEquals(1, spawnCount[0]);
  }

  @Test
  void shouldToggleSpawnerOff() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            5,
            true,
            SpawnerComponent.ACTIVATION_MODE.TOGGLE);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    spawner.getEvents().trigger("activatedMapComponent", true);
    component.update();

    assertEquals(0, spawnCount[0]);
  }

  @Test
  void shouldActivateIdsWhenSpawningComplete() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            1,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    component.setIds(new String[] {"door1", "platform1"});

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] activations = {0};
    spawner
        .getEvents()
        .addListener("activateByKey", (String id) -> activations[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    // First update performs the final spawn.
    component.update();
    assertEquals(0, activations[0]);

    // Next update detects that spawning is complete.
    component.update();
    assertEquals(2, activations[0]);
  }

  @Test
  void shouldOnlyTriggerCompletionIdsOnce() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            1,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    component.setIds(new String[] {"door1"});

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] activations = {0};
    spawner
        .getEvents()
        .addListener("activateByKey", (String id) -> activations[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    component.update();
    component.update();
    component.update();
    component.update();

    assertEquals(1, activations[0]);
  }

  @Test
  void shouldSpawnIndefinitelyWhenMaxSpawnsIsNegativeOne() {
    Entity child = new Entity();

    SpawnerComponent component =
        new SpawnerComponent(
            createSpawnables(child),
            1f,
            -1,
            true,
            SpawnerComponent.ACTIVATION_MODE.NORMAL);

    Entity spawner = new Entity().addComponent(component);
    spawner.create();

    final int[] spawnCount = {0};
    spawner
        .getEvents()
        .addListener("spawnEntity", (Entity spawned) -> spawnCount[0]++);

    when(timeSource.getDeltaTime()).thenReturn(1f);

    component.update();
    component.update();
    component.update();

    assertEquals(3, spawnCount[0]);
  }
}