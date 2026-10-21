package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.ParticleEffectsRenderingComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SummonTaskTest {
  private GameTime gameTime;

  @BeforeEach
  void setUp() {
    gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerEntityService(new EntityService());

    RenderService renderService = mock(RenderService.class);
    DebugRenderer debugRenderer = mock(DebugRenderer.class);
    when(renderService.getDebug()).thenReturn(debugRenderer);
    ServiceLocator.registerRenderService(renderService);

    LightingEngine engine = mock(LightingEngine.class);
    box2dLight.RayHandler rayHandler = mock(box2dLight.RayHandler.class);
    when(engine.getRayHandler()).thenReturn(rayHandler);
    LightingService lighting = mock(LightingService.class);
    when(lighting.getEngine()).thenReturn(engine);
    ServiceLocator.registerLightingService(lighting);

    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(256);
    when(texture.getHeight()).thenReturn(256);

    TextureAtlas atlas = mock(TextureAtlas.class);
    AtlasRegion region = mock(AtlasRegion.class);
    when(region.getRegionWidth()).thenReturn(1);
    when(region.getRegionHeight()).thenReturn(1);

    Array<AtlasRegion> regions = new Array<>();
    regions.add(region);
    when(atlas.findRegions(anyString())).thenReturn(regions);
    when(atlas.findRegion(anyString())).thenReturn(region);

    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resources);
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.CsvSource({"Necromancer,2", "Calypso,5", "Other,6"})
  void priorityHonoursTheSummonerSpecificLimit(String type, int count) {
    Entity player = new Entity();
    Entity summoner = new Entity();
    com.csse3200.game.components.BossPhaseComponent phase =
        mock(com.csse3200.game.components.BossPhaseComponent.class);
    when(phase.getCurrentPhase()).thenReturn(2);
    summoner.addComponent(phase);
    SummonTask task = new SummonTask(player, 30, 10f, 5f, type);
    task.create(() -> summoner);
    try (var lights = mockConstruction(PointLightComponent.class);
        var particles = mockConstruction(ParticleEffectsRenderingComponent.class)) {
      for (int i = 1; i <= count; i++) {
        when(gameTime.getTime()).thenReturn(i * 6000L);
        assertEquals(30, task.getPriority());
        task.update();
      }
      when(gameTime.getTime()).thenReturn((count + 1) * 6000L);
      assertEquals(type.equals("Other") ? 30 : -1, task.getPriority());
      summoner.getEvents().trigger("summonedEnemyDied");
      assertEquals(30, task.getPriority());
      player.setPosition(20f, 0f);
      assertEquals(-1, task.getPriority());
    }
  }

  @Test
  void summonPriorityWaitsUntilCooldownExpires() {
    Entity summoner = new Entity();
    SummonTask task = new SummonTask(new Entity(), 30, 10f, 5f, "Necromancer");
    task.create(() -> summoner);

    when(gameTime.getTime()).thenReturn(4999L);
    assertEquals(-1, task.getPriority());

    when(gameTime.getTime()).thenReturn(5000L);
    assertEquals(30, task.getPriority());
  }

  @Test
  void calypsoCannotSummonWithoutPhaseTwo() {
    Entity summoner = new Entity();
    SummonTask task = new SummonTask(new Entity(), 30, 10f, 5f, "Calypso");
    task.create(() -> summoner);
    when(gameTime.getTime()).thenReturn(6000L);
    assertEquals(-1, task.getPriority());
    var phase = mock(com.csse3200.game.components.BossPhaseComponent.class);
    when(phase.getCurrentPhase()).thenReturn(1);
    summoner.addComponent(phase);
    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldSummonSkeletonAtExpectedPosition() {
    Entity player = new Entity();
    player.setPosition(3f, 2f);

    SummonTask summonTask = new SummonTask(player, 30, 10f, 5f, "Necromancer");

    AITaskComponent aiTaskComponent = new AITaskComponent();

    Entity necromancer = new Entity().addComponent(aiTaskComponent);

    necromancer.setPosition(2f, 2f);
    necromancer.create();

    aiTaskComponent.addTask(summonTask);

    final Entity[] summoned = {null};
    necromancer.getEvents().addListener("spawnEntity", (Entity spawned) -> summoned[0] = spawned);

    when(gameTime.getTime()).thenReturn(6000L);

    try (var lights = mockConstruction(PointLightComponent.class);
        var particles = mockConstruction(ParticleEffectsRenderingComponent.class)) {
      summonTask.update();
    }

    assertNotNull(summoned[0], "Necromancer should summon a Skeleton Warrior");
    assertEquals(3f, summoned[0].getPosition().x, 0.001f);
    assertEquals(2f, summoned[0].getPosition().y, 0.001f);
  }

  @Test
  void shouldNotSpawnTwoSkeletonsAtSamePosition() {
    Entity player = new Entity();
    player.setPosition(3f, 2f);

    SummonTask summonTask = new SummonTask(player, 30, 10f, 5f, "Necromancer");

    AITaskComponent aiTaskComponent = new AITaskComponent();

    Entity necromancer = new Entity().addComponent(aiTaskComponent);

    necromancer.setPosition(2f, 2f);
    necromancer.create();

    aiTaskComponent.addTask(summonTask);

    java.util.List<Entity> summoned = new java.util.ArrayList<>();

    necromancer
        .getEvents()
        .addListener(
            "spawnEntity",
            (Entity spawned) -> {
              summoned.add(spawned);
              ServiceLocator.getEntityService().register(spawned);
            });

    try (var lights = mockConstruction(PointLightComponent.class);
        var particles = mockConstruction(ParticleEffectsRenderingComponent.class)) {

      // First summon at 6 seconds
      when(gameTime.getTime()).thenReturn(6000L);
      summonTask.update();

      // Second summon at 12 seconds
      when(gameTime.getTime()).thenReturn(12000L);
      summonTask.update();

      // Third summon at 18 seconds
      when(gameTime.getTime()).thenReturn(18000L);
      summonTask.update();
    }

    assertEquals(3, summoned.size(), "Each direct update summons at an unoccupied position");

    for (int i = 0; i < summoned.size(); i++) {
      System.out.println("Skeleton " + (i + 1) + " position: " + summoned.get(i).getPosition());
    }

    assertFalse(
        summoned.get(0).getPosition().epsilonEquals(summoned.get(1).getPosition(), 0.001f),
        "Skeleton Warriors should not spawn at the same position");
  }

  @Test
  void shouldIgnoreTheSummonerAndNonEnemiesWhenChoosingASpawnPosition() {
    Entity player = new Entity();
    Entity necromancer = new Entity();
    necromancer.setPosition(2f, 2f);
    ServiceLocator.getEntityService().register(necromancer);
    Entity scenery = new Entity();
    scenery.setPosition(3f, 2f);
    ServiceLocator.getEntityService().register(scenery);
    SummonTask task = new SummonTask(player, 30, 10f, 5f, "Necromancer");
    task.create(() -> necromancer);
    java.util.List<Entity> summoned = new java.util.ArrayList<>();
    com.csse3200.game.events.listeners.EventListener1<Entity> onSpawn = summoned::add;
    necromancer.getEvents().addListener("spawnEntity", onSpawn);
    when(gameTime.getTime()).thenReturn(6000L);

    try (var lights = mockConstruction(PointLightComponent.class);
        var particles = mockConstruction(ParticleEffectsRenderingComponent.class)) {
      task.update();
    }

    assertEquals(1, summoned.size());
    assertEquals(3f, summoned.getFirst().getPosition().x, 0.001f);
    assertEquals(2f, summoned.getFirst().getPosition().y, 0.001f);
  }
}
