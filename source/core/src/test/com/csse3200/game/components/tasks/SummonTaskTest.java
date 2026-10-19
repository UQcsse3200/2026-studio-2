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
    when(renderService.getDebug()).thenReturn(mock(DebugRenderer.class));
    ServiceLocator.registerRenderService(renderService);

    LightingEngine engine = mock(LightingEngine.class);
    when(engine.getRayHandler()).thenReturn(mock(box2dLight.RayHandler.class));
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

  @Test
  void shouldSummonSkeletonAtExpectedPosition() {
    Entity player = new Entity();
    player.setPosition(3f, 2f);

    SummonTask summonTask = new SummonTask(player, 30, 10f, 5f);
    Entity necromancer = new Entity().addComponent(new AITaskComponent().addTask(summonTask));
    necromancer.setPosition(2f, 2f);
    necromancer.create();

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

    SummonTask summonTask = new SummonTask(player, 30, 10f, 5f);

    Entity necromancer = new Entity().addComponent(new AITaskComponent().addTask(summonTask));
    necromancer.setPosition(2f, 2f);
    necromancer.create();

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

    assertEquals(3, summoned.size(), "Expected three summoned Skeleton Warriors");

    for (int i = 0; i < summoned.size(); i++) {
      System.out.println("Skeleton " + (i + 1) + " position: " + summoned.get(i).getPosition());
    }

    assertFalse(
        summoned.get(0).getPosition().epsilonEquals(summoned.get(1).getPosition(), 0.001f),
        "Skeleton Warriors should not spawn at the same position");

    assertFalse(
        summoned.get(1).getPosition().epsilonEquals(summoned.get(2).getPosition(), 0.001f),
        "Third Skeleton Warrior should not overlap the first");
  }
}
