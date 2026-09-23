package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.areas.terrain.configs.CrumblingPlatformConfig;
import com.csse3200.game.areas.terrain.configs.MovingPlatformConfig;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;
import com.csse3200.game.areas.terrain.configs.SpikeClusterConfig;
import com.csse3200.game.areas.terrain.configs.SpikyBallTrapConfig;
import com.csse3200.game.areas.terrain.configs.TriggerButtonConfig;
import com.csse3200.game.areas.terrain.configs.TriggerablePlatformConfig;
import com.csse3200.game.components.level.ActivatableComponent;
import com.csse3200.game.components.level.AttachableMapComponent;
import com.csse3200.game.components.level.CrumblingPlatformComponent;
import com.csse3200.game.components.level.MovingPlatformComponent;
import com.csse3200.game.components.level.SpawnerComponent;
import com.csse3200.game.components.level.SpikyBallComponent;
import com.csse3200.game.components.level.TriggerButtonComponent;
import com.csse3200.game.components.level.TriggerablePlatformComponent;
import com.csse3200.game.components.level.LevelTriggerComponent;
import com.csse3200.game.components.level.WinConditionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TiledRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ObstacleFactoryTest {
  private PhysicsService physicsService;

  @BeforeEach
  void setUp() {
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq(Texture.class)))
        .thenReturn(texture);
    TextureAtlas atlas = mock(TextureAtlas.class);
    AtlasRegion region = mock(AtlasRegion.class);
    when(atlas.findRegions(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(new com.badlogic.gdx.utils.Array<>(new AtlasRegion[] {region}));
    when(resources.getAsset(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq(TextureAtlas.class)))
        .thenReturn(atlas);
    ServiceLocator.registerResourceService(resources);
  }

  @Test
  void shouldCreateStaticTree() {
    Entity tree = ObstacleFactory.createTree();

    assertNotNull(tree.getComponent(TextureRenderComponent.class));
    assertEquals(
        BodyType.StaticBody, tree.getComponent(PhysicsComponent.class).getBody().getType());
  }

  @Test
  void shouldCreateStaticWinConditionSensor() {
    Entity winCondition = ObstacleFactory.createWinConEntity();

    assertNotNull(winCondition.getComponent(WinConditionComponent.class));
    assertEquals(
        BodyType.StaticBody, winCondition.getComponent(PhysicsComponent.class).getBody().getType());
    assertNotNull(winCondition.getComponent(ColliderComponent.class));
  }

  @Test
  void shouldCreateNextLevelTriggerWithConfiguredName() {
    Entity trigger = ObstacleFactory.createNextLevelTriggerEntity("level2");

    assertNotNull(trigger.getComponent(LevelTriggerComponent.class));
  }

  @Test
  void shouldCreateWallWithRequestedScale() {
    Entity wall = ObstacleFactory.createWall(4f, 2f);

    assertEquals(4f, wall.getScale().x);
    assertEquals(2f, wall.getScale().y);
    assertEquals(
        BodyType.StaticBody, wall.getComponent(PhysicsComponent.class).getBody().getType());
  }

  @Test
  void shouldCreatePlatformWithKinematicBodyAndGrappleComponent() {
    Entity platform =
        ObstacleFactory.createPlatform(
            new PlatformConfig(new GridPoint2(1, 2), 3, 1, 2, "images/platform.png"));

    assertNotNull(platform.getComponent(TextureRenderComponent.class));
    assertNotNull(platform.getComponent(com.csse3200.game.components.level.PlatformGrappleComponent.class));
    assertEquals(
        BodyType.KinematicBody, platform.getComponent(PhysicsComponent.class).getBody().getType());
  }

  @Test
  void shouldCreateLevelTwoPlatformWithTiledRenderer() {
    Entity platform =
        ObstacleFactory.createPlatform(
            new PlatformConfig(new GridPoint2(1, 2), 3, 1, 0, "images/tile-level2.png"));

    assertNotNull(platform.getComponent(TiledRenderComponent.class));
  }

  @Test
  void shouldCreateMovingPlatformWithMovementComponents() {
    MovingPlatformConfig config =
        new MovingPlatformConfig(
            new GridPoint2(1, 2),
            3,
            1,
            0,
            "images/platform.png",
            new Vector2(1f, 1f),
            new Vector2(5f, 1f),
            new Vector2(2f, 0f),
            new String[] {"switch"});

    Entity platform = ObstacleFactory.createMovingPlatform(config);

    assertNotNull(platform.getComponent(MovingPlatformComponent.class));
    assertNotNull(platform.getComponent(ActivatableComponent.class));
    assertEquals(
        BodyType.KinematicBody, platform.getComponent(PhysicsComponent.class).getBody().getType());
  }

  @Test
  void shouldCreateCrumblingPlatformWithCrumbleComponent() {
    CrumblingPlatformConfig config =
        new CrumblingPlatformConfig(
            new GridPoint2(1, 2), 3, 1, 0, "images/platform.png", 1f, 2f, 3f);

    Entity platform = ObstacleFactory.createCrumblingPlatform(config);

    assertNotNull(platform.getComponent(CrumblingPlatformComponent.class));
    assertEquals(
        BodyType.StaticBody, platform.getComponent(PhysicsComponent.class).getBody().getType());
  }

  @Test
  void shouldCreateTriggerablePlatformWithActivationComponents() {
    TriggerablePlatformConfig config =
        new TriggerablePlatformConfig(
            new GridPoint2(1, 2), 3, 1, 0, "images/platform.png", new String[] {"switch"}, true);

    Entity platform = ObstacleFactory.createTriggerablePlatform(config);

    assertNotNull(platform.getComponent(TriggerablePlatformComponent.class));
    assertNotNull(platform.getComponent(ActivatableComponent.class));
  }

  @Test
  void shouldCreateAttachedSpikeWithAttachmentComponent() {
    Entity spike = ObstacleFactory.createSpike(new SpikeClusterConfig(0, 1, 0, 1, 90f, true));

    assertNotNull(spike.getComponent(AttachableMapComponent.class));
    assertEquals(1.25f, spike.getScale().x);
  }

  @Test
  void shouldCreateButtonWithOptionalAttachment() {
    Entity button =
        ObstacleFactory.createButton(
            new TriggerButtonConfig(new GridPoint2(1, 2), 0f, true, new String[] {"switch"}));

    assertNotNull(button.getComponent(TriggerButtonComponent.class));
    assertNotNull(button.getComponent(AttachableMapComponent.class));
  }

  @Test
  void shouldCreateSpikyBallWithConfiguredMovement() {
    Entity ball = ObstacleFactory.createSpikyBall(new Vector2(1f, 0f));

    assertNotNull(ball.getComponent(SpikyBallComponent.class));
    assertEquals(0.75f, ball.getScale().x);
    assertEquals(BodyType.DynamicBody, ball.getComponent(PhysicsComponent.class).getBody().getType());
  }

  @Test
  void shouldCreateSpikyBallTrapWithSpawner() {
    SpikyBallTrapConfig config =
        new SpikyBallTrapConfig(
            new GridPoint2(1, 2),
            90f,
            new String[] {"switch"},
            1f,
            true,
            SpawnerComponent.ACTIVATION_MODE.TOGGLE);

    Entity trap = ObstacleFactory.createSpikyBallTrap(config);

    assertNotNull(trap.getComponent(SpawnerComponent.class));
    assertNotNull(trap.getComponent(ActivatableComponent.class));
  }
}
