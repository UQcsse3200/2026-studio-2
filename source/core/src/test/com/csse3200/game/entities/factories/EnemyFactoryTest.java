package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.components.EnemyItemDropComponent;
import com.csse3200.game.components.EnemyTeleportComponent;
import com.csse3200.game.components.PoisonStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.npc.ChaseSoundComponent;
import com.csse3200.game.components.npc.EnemyAnimationController;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.DelayedAttackTask;
import com.csse3200.game.components.tasks.FlyingChaseTask;
import com.csse3200.game.components.tasks.FlyingRepositionTask;
import com.csse3200.game.components.tasks.FlyingWanderTask;
import com.csse3200.game.components.tasks.RangedAttackTask;
import com.csse3200.game.components.tasks.RepositionTask;
import com.csse3200.game.components.tasks.SummonTask;
import com.csse3200.game.components.tasks.SweepAttackTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.EnemyHealthRenderComponent;
import com.csse3200.game.rendering.ParticleEffectsRenderingComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class EnemyFactoryTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    RenderService renderService = mock(RenderService.class);
    com.csse3200.game.rendering.DebugRenderer debugRendererMock =
        mock(com.csse3200.game.rendering.DebugRenderer.class);
    when(renderService.getDebug()).thenReturn(debugRendererMock);
    ServiceLocator.registerRenderService(renderService);

    LightingEngine lightingEngine = mock(LightingEngine.class);
    box2dLight.RayHandler rayHandlerMock = mock(box2dLight.RayHandler.class);
    when(lightingEngine.getRayHandler()).thenReturn(rayHandlerMock);
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
  void shouldCreateSkeletonWarriorWithEnemyAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity warrior = EnemyFactory.createSkeletonWarrior(new Entity());

      assertNotNull(warrior.getComponent(EnemyAnimationController.class));
      assertNotNull(warrior.getComponent(AnimationRenderComponent.class));
    }
  }

  @Test
  void shouldCreateSkeletonArcherWithEnemyAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity archer = EnemyFactory.createSkeletonArcher(new Entity());

      assertNotNull(archer.getComponent(EnemyAnimationController.class));
      assertNotNull(archer.getComponent(AnimationRenderComponent.class));
    }
  }

  @Test
  void shouldCreateVultureWithEnemyAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity vulture = EnemyFactory.createVulture(new Entity());

      assertNotNull(vulture.getComponent(EnemyAnimationController.class));
      assertNotNull(vulture.getComponent(AnimationRenderComponent.class));
    }
  }

  @Test
  void shouldCreateNecromancerWithEnemyAnimationController() {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      Entity necromancer = EnemyFactory.createNecromancer(new Entity());

      assertNotNull(necromancer.getComponent(EnemyAnimationController.class));
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

  // --- helpers for looking inside a built enemy ---

  /** Builds an enemy with its lights and particles stubbed out, as a headless test needs. */
  private static Entity built(Supplier<Entity> factory) {
    try (MockedConstruction<PointLightComponent> lights =
            mockConstruction(PointLightComponent.class);
        MockedConstruction<ParticleEffectsRenderingComponent> particles =
            mockConstruction(ParticleEffectsRenderingComponent.class)) {
      return factory.get();
    }
  }

  private static EnemyConfig config(String behaviour, String attackType) {
    EnemyConfig config = new EnemyConfig();
    config.health = 40;
    config.baseAttack = 8;
    config.behaviour = behaviour;
    config.attackType = attackType;
    return config;
  }

  @SuppressWarnings("unchecked")
  private static List<PriorityTask> tasksOf(Entity enemy) throws ReflectiveOperationException {
    Field field = AITaskComponent.class.getDeclaredField("priorityTasks");
    field.setAccessible(true);
    return (List<PriorityTask>) field.get(enemy.getComponent(AITaskComponent.class));
  }

  /** How many tasks are exactly this class (not a subclass or a sibling). */
  private static long countOf(List<PriorityTask> tasks, Class<?> type) {
    return tasks.stream().filter(task -> task.getClass() == type).count();
  }

  // --- how an enemy's behaviour and attack type choose its tasks ---

  @Test
  void shouldGiveGroundEnemiesGroundMovementTasks() throws Exception {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("chase", "melee")));
    List<PriorityTask> tasks = tasksOf(enemy);

    assertEquals(1, countOf(tasks, WanderTask.class));
    assertEquals(1, countOf(tasks, ChaseTask.class));
    assertEquals(1, countOf(tasks, RepositionTask.class));
    assertEquals(0, countOf(tasks, FlyingWanderTask.class));
    assertEquals(0, countOf(tasks, FlyingChaseTask.class));
    assertEquals(0, countOf(tasks, FlyingRepositionTask.class));
    assertEquals(3, tasks.size(), "and a melee enemy has no ranged attack on top");
  }

  @Test
  void shouldGiveFlyingEnemiesFlyingMovementTasks() throws Exception {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("flying", "melee")));
    List<PriorityTask> tasks = tasksOf(enemy);

    assertEquals(1, countOf(tasks, FlyingWanderTask.class));
    assertEquals(1, countOf(tasks, FlyingChaseTask.class));
    assertEquals(1, countOf(tasks, FlyingRepositionTask.class));
    assertEquals(0, countOf(tasks, WanderTask.class));
    assertEquals(0, countOf(tasks, ChaseTask.class));
    assertEquals(0, countOf(tasks, RepositionTask.class));
    assertEquals(3, tasks.size());
  }

  @Test
  void shouldGiveRangedEnemiesOneRangedAttack() throws Exception {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("chase", "range")));
    List<PriorityTask> tasks = tasksOf(enemy);

    assertEquals(1, countOf(tasks, RangedAttackTask.class));
    assertEquals(0, countOf(tasks, SummonTask.class));
    assertEquals(4, tasks.size());
  }

  @Test
  void shouldGiveSummonersARangedAttackAndASummon() throws Exception {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("chase", "summon")));
    List<PriorityTask> tasks = tasksOf(enemy);

    assertEquals(1, countOf(tasks, RangedAttackTask.class));
    assertEquals(1, countOf(tasks, SummonTask.class));
    assertEquals(5, tasks.size());
  }

  @Test
  void shouldGiveACyclopsOnlyTheBasicMovementTasks() throws Exception {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("chase", "cyclops")));

    assertEquals(3, tasksOf(enemy).size());
  }

  @Test
  void shouldGiveCalypsoGroundMovementEvenIfMarkedFlyingAndNoRepositioning() throws Exception {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("flying", "calypso")));
    List<PriorityTask> tasks = tasksOf(enemy);

    assertEquals(1, countOf(tasks, WanderTask.class));
    assertEquals(1, countOf(tasks, ChaseTask.class));
    assertEquals(0, countOf(tasks, RepositionTask.class));
    assertEquals(0, countOf(tasks, FlyingChaseTask.class));
    assertEquals(1, countOf(tasks, RangedAttackTask.class), "Calypso also fires projectiles");
  }

  // --- what every enemy is made of ---

  @Test
  void shouldGiveEveryEnemyItsConfiguredHealthAndAttack() {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("chase", "melee")));

    CombatStatsComponent stats = enemy.getComponent(CombatStatsComponent.class);
    assertEquals(40, stats.getHealth());
    assertEquals(8, stats.getBaseAttack());
  }

  @Test
  void shouldGiveEveryEnemyTheStandardCombatAndStatusParts() {
    Entity enemy = built(() -> EnemyFactory.createEnemy(new Entity(), config("chase", "melee")));

    assertNotNull(enemy.getComponent(TouchAttackComponent.class));
    assertNotNull(enemy.getComponent(EnemyDeathComponent.class));
    assertNotNull(enemy.getComponent(PoisonStatsComponent.class));
    assertNotNull(enemy.getComponent(BurnStatsComponent.class));
    assertNotNull(enemy.getComponent(SlowStatsComponent.class));
    assertNotNull(enemy.getComponent(EnemyItemDropComponent.class));
    assertNotNull(enemy.getComponent(EnemyHealthRenderComponent.class));
  }

  @Test
  void shouldGiveEachNamedEnemyItsOwnSignatureAttack() throws Exception {
    List<PriorityTask> warrior =
        tasksOf(built(() -> EnemyFactory.createSkeletonWarrior(new Entity())));
    List<PriorityTask> archer =
        tasksOf(built(() -> EnemyFactory.createSkeletonArcher(new Entity())));
    List<PriorityTask> necromancer =
        tasksOf(built(() -> EnemyFactory.createNecromancer(new Entity())));
    List<PriorityTask> vulture = tasksOf(built(() -> EnemyFactory.createVulture(new Entity())));

    assertEquals(1, countOf(warrior, DelayedAttackTask.class), "warriors swing after a wind-up");
    assertEquals(0, countOf(warrior, RangedAttackTask.class));
    assertEquals(1, countOf(archer, RangedAttackTask.class), "archers shoot");
    assertEquals(0, countOf(archer, DelayedAttackTask.class));
    assertEquals(1, countOf(necromancer, SummonTask.class), "necromancers summon");
    assertEquals(1, countOf(necromancer, RangedAttackTask.class));
    assertEquals(1, countOf(vulture, FlyingChaseTask.class), "vultures fly");
    assertEquals(0, countOf(vulture, ChaseTask.class));
  }

  @Test
  void shouldGiveWarriorsAndArchersTheChaseLaugh() {
    Entity warrior = built(() -> EnemyFactory.createSkeletonWarrior(new Entity()));
    Entity archer = built(() -> EnemyFactory.createSkeletonArcher(new Entity()));

    assertNotNull(warrior.getComponent(ChaseSoundComponent.class));
    assertNotNull(archer.getComponent(ChaseSoundComponent.class));
  }

  @Test
  void shouldMakePassiveEnemiesHarmlessTargetsWithNoBrain() {
    Entity warrior = built(EnemyFactory::createPassiveSkeletonWarrior);
    Entity archer = built(EnemyFactory::createPassiveSkeletonArcher);

    assertNull(warrior.getComponent(AITaskComponent.class));
    assertNull(archer.getComponent(AITaskComponent.class));
    assertNull(warrior.getComponent(TouchAttackComponent.class));
    assertTrue(warrior.getComponent(CombatStatsComponent.class).getHealth() > 0);
  }

  // --- Calypso, the boss ---

  private static Entity calypso(Entity target) {
    List<Vector2> teleportSpots = List.of(new Vector2(5f, 5f), new Vector2(15f, 5f));
    return built(() -> EnemyFactory.createCalypso(target, teleportSpots));
  }

  @Test
  void shouldGiveCalypsoHerBossAbilities() throws Exception {
    Entity boss = calypso(new Entity());
    List<PriorityTask> tasks = tasksOf(boss);

    assertNotNull(boss.getComponent(EnemyTeleportComponent.class), "she teleports");
    assertNotNull(boss.getComponent(EnemyAnimationController.class));
    assertNotNull(boss.getComponent(AnimationRenderComponent.class));
    assertEquals(1, countOf(tasks, SweepAttackTask.class), "a telegraphed melee sweep");
    assertEquals(1, countOf(tasks, RangedAttackTask.class), "and a projectile attack");
    assertEquals(300, boss.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldMakeCalypsoAsTallAsHerTarget() {
    Entity target = new Entity();
    target.setScale(1f, 2.5f);

    Entity boss = calypso(target);

    assertEquals(2.5f, boss.getScale().y, 1e-4f);
  }

  @Test
  void shouldFallBackToAFixedWidthIfTheTargetHasNoHeight() {
    Entity target = new Entity();
    target.setScale(0f, 0f);

    Entity boss = calypso(target);

    assertEquals(1f, boss.getScale().x, 1e-4f, "scaled by width instead");
  }
}
