package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.npc.SkeletonAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.ParticleEffectsRenderingComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

@ExtendWith(GameExtension.class)
class EnemyFactoryTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    RenderService renderService = Mockito.mock(RenderService.class);
    Mockito.when(renderService.getDebug())
        .thenReturn(Mockito.mock(com.csse3200.game.rendering.DebugRenderer.class));
    ServiceLocator.registerRenderService(renderService);

    LightingEngine lightingEngine = mock(LightingEngine.class);
    when(lightingEngine.getRayHandler()).thenReturn(mock(box2dLight.RayHandler.class));
    LightingService lightingService = mock(LightingService.class);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerLightingService(lightingService);

    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
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
  void shouldCreateEnemyWithCombatAndAiComponents() {
    EnemyConfig config = new EnemyConfig();
    config.health = 40;
    config.baseAttack = 8;

    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity enemy = EnemyFactory.createEnemy(new Entity(), config);

      assertNotNull(enemy.getComponent(CombatStatsComponent.class));
      assertNotNull(enemy.getComponent(EnemyDeathComponent.class));
      assertNotNull(enemy.getComponent(AITaskComponent.class));
    }
  }

  @Test
  void shouldCreateRangedEnemyWithConfiguredStats() {
    EnemyConfig config = new EnemyConfig();
    config.health = 25;
    config.baseAttack = 6;
    config.attackType = "range";

    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity enemy = EnemyFactory.createEnemy(new Entity(), config);

      assertNotNull(enemy.getComponent(CombatStatsComponent.class));
      assertNotNull(enemy.getComponent(AITaskComponent.class));
    }
  }

  @Test
  void shouldCreateSkeletonWarriorWithSkeletonAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity warrior = EnemyFactory.createSkeletonWarrior(new Entity());

      assertNotNull(warrior.getComponent(SkeletonAnimationController.class));
      assertNotNull(warrior.getComponent(AnimationRenderComponent.class));
    }
  }

  @Test
  void shouldCreateSkeletonArcherWithSkeletonAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity archer = EnemyFactory.createSkeletonArcher(new Entity());

      assertNotNull(archer.getComponent(SkeletonAnimationController.class));
      assertNotNull(archer.getComponent(AnimationRenderComponent.class));
    }
  }

  @Test
  void shouldCreateVultureWithSkeletonAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity vulture = EnemyFactory.createVulture(new Entity());

      assertNotNull(vulture.getComponent(SkeletonAnimationController.class));
      assertNotNull(vulture.getComponent(AnimationRenderComponent.class));
    }
  }

  @Test
  void shouldCreateNecromancerWithSkeletonAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity necromancer = EnemyFactory.createNecromancer(new Entity());

      assertNotNull(necromancer.getComponent(SkeletonAnimationController.class));
      assertNotNull(necromancer.getComponent(AnimationRenderComponent.class));
    }
  }

  @Test
  void shouldCreatePassiveSkeletonWarriorWithStaticTexture() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity warrior = EnemyFactory.createPassiveSkeletonWarrior();

      assertNotNull(warrior.getComponent(TextureRenderComponent.class));
      assertNotNull(warrior.getComponent(CombatStatsComponent.class));
    }
  }

  @Test
  void shouldCreatePassiveSkeletonArcherWithStaticTexture() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity archer = EnemyFactory.createPassiveSkeletonArcher();

      assertNotNull(archer.getComponent(TextureRenderComponent.class));
      assertNotNull(archer.getComponent(CombatStatsComponent.class));
    }
  }
}
