package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.level.CheckpointComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class LevelConfigTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());

    LightingEngine lightingEngine = mock(LightingEngine.class);
    box2dLight.RayHandler rayHandlerMock = mock(box2dLight.RayHandler.class);
    when(lightingEngine.getRayHandler()).thenReturn(rayHandlerMock);
    LightingService lightingService = mock(LightingService.class);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerLightingService(lightingService);

    ResourceService resourceService = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resourceService);

    Texture mockTexture = mock(Texture.class);
    TextureAtlas mockAtlas = mock(TextureAtlas.class);
    TextureAtlas.AtlasRegion mockRegion = mock(TextureAtlas.AtlasRegion.class);
    when(mockRegion.getRegionWidth()).thenReturn(1);
    when(mockRegion.getRegionHeight()).thenReturn(1);
    when(mockAtlas.findRegion(anyString())).thenReturn(mockRegion);

    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
    when(resourceService.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(mockAtlas);
  }

  @Test
  void shouldCreateFloor() {
    GridPoint2 pos = new GridPoint2(0, 0);
    Vector2 size = new Vector2(1, 1);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.floors =
        new PlatformConfig[] {new PlatformConfig(pos, (int) size.x, (int) size.y, 0, "")};
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
    assertEquals(size, levelConfig.entities.getFirst().entity.getScale());
  }

  @Test
  void shouldCreateCheckpointWithInactiveWarmLight() {
    GridPoint2 position = new GridPoint2(3, 4);
    LevelConfig levelConfig = new LevelConfig();
    levelConfig.checkpoints = new CheckpointConfig[] {new CheckpointConfig(position)};

    levelConfig.createEntities();

    var checkpoint = levelConfig.entities.getFirst().entity;
    assertNotNull(checkpoint.getComponent(CheckpointComponent.class));
    assertEquals(position, levelConfig.entities.getFirst().pos);
    assertEquals(
        new Vector2(CheckpointComponent.ATLAS_WIDTH, CheckpointComponent.ATLAS_HEIGHT),
        checkpoint.getScale());

    PointLightComponent light = checkpoint.getComponent(PointLightComponent.class);
    assertNotNull(light);
    assertFalse(light.isActive());
    assertEquals(0.75f, light.getBaseRed(), 0.001f);
    assertEquals(0.2f, light.getBaseGreen(), 0.001f);
    assertEquals(0f, light.getBaseBlue(), 0.001f);
  }

  @Test
  void shouldCreatePlatform() {
    GridPoint2 pos = new GridPoint2(0, 0);
    Vector2 size = new Vector2(1, 1);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.platforms =
        new PlatformConfig[] {new PlatformConfig(pos, (int) size.x, (int) size.y, 0, "")};
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
    assertEquals(size, levelConfig.entities.getFirst().entity.getScale());
  }

  @Test
  void shouldCreateMovingPlatform() {
    GridPoint2 pos = new GridPoint2(0, 0);
    Vector2 size = new Vector2(1, 1);
    Vector2 target = new Vector2(0, 0);
    Vector2 speed = new Vector2(1, 1);
    String[] ids = new String[] {"test"};

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              pos, (int) size.x, (int) size.y, 0, "", target, target, speed, ids)
        };
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
    assertEquals(size, levelConfig.entities.getFirst().entity.getScale());
  }

  @Test
  void shouldCreateCrumblingPlatform() {
    GridPoint2 pos = new GridPoint2(0, 0);
    Vector2 size = new Vector2(1, 1);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(pos, (int) size.x, (int) size.y, 0, "", 0f, 0f, 0f)
        };
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
    assertEquals(size, levelConfig.entities.getFirst().entity.getScale());
  }

  @Test
  void shouldCreateTriggerablePlatform() {
    GridPoint2 pos = new GridPoint2(0, 0);
    Vector2 size = new Vector2(1, 1);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.triggerablePlatforms =
        new TriggerablePlatformConfig[] {
          new TriggerablePlatformConfig(
              pos, (int) size.x, (int) size.y, 0, "", new String[] {"test"}, false, false)
        };
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
    assertEquals(size, levelConfig.entities.getFirst().entity.getScale());
  }

  @Test
  void shouldCreateLedge() {
    GridPoint2 pos = new GridPoint2(0, 0);
    Vector2 size = new Vector2(1, 1);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.ledges =
        new PlatformConfig[] {new PlatformConfig(pos, (int) size.x, (int) size.y, 0, "")};
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
    assertEquals(size, levelConfig.entities.getFirst().entity.getScale());
  }

  @Test
  void shouldCreateSpike() {
    LevelConfig levelConfig = new LevelConfig();
    levelConfig.spikes = new SpikeClusterConfig[] {new SpikeClusterConfig(0, 0, 0, 0, 0f, false)};
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(new GridPoint2(0, 0), levelConfig.entities.getFirst().pos);
  }

  @Test
  void shouldCreateButton() {
    GridPoint2 pos = new GridPoint2(0, 0);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.triggerButtons =
        new TriggerButtonConfig[] {new TriggerButtonConfig(pos, 0f, false, new String[] {"test "})};
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
  }

  @Test
  void shouldCreateWinCondition() {
    GridPoint2 pos = new GridPoint2(0, 0);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.winConditionSpawn = pos;
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
  }

  @Test
  void shouldCreateLevelTrigger() {
    GridPoint2 pos = new GridPoint2(0, 0);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.nextLevelTriggerSpawn = pos;
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
  }

  @Test
  void shouldCreateItems() {
    GridPoint2 pos = new GridPoint2(0, 0);

    LevelConfig levelConfig = new LevelConfig();
    levelConfig.items =
        new HashMap<>(
            Map.of(
                pos, ItemFactory.createRopeArrow(1).getComponent(ItemComponent.class).getItem()));
    levelConfig.createEntities();

    assertEquals(1, levelConfig.entities.size());
    assertEquals(pos, levelConfig.entities.getFirst().pos);
  }

  @Test
  void getPlayerSpawnReturnsConfiguredPosition() {
    LevelConfig config = new LevelConfig();
    GridPoint2 spawn = new GridPoint2(4, 7);

    config.playerSpawn = spawn;

    assertSame(spawn, config.getPlayerSpawn());
  }

  @Test
  void getWheelSpinSpawnsReturnsConfiguredPositions() {
    LevelConfig config = new LevelConfig();
    GridPoint2[] spawns = {new GridPoint2(2, 3), new GridPoint2(8, 5)};

    config.wheelSpinSpawns = spawns;

    assertSame(spawns, config.getWheelSpinSpawns());
  }

  @Test
  void getCheckpointsReturnsEmptyListWhenUnconfigured() {
    LevelConfig config = new LevelConfig();

    assertTrue(config.getCheckpoints().isEmpty());
  }

  @Test
  void getCheckpointsReturnsEmptyListForEmptyArray() {
    LevelConfig config = new LevelConfig();
    config.checkpoints = new CheckpointConfig[0];

    assertTrue(config.getCheckpoints().isEmpty());
  }

  @Test
  void shouldCreateMapTriggerWithConfiguredPositionAndScale() {
    GridPoint2 position = new GridPoint2(5, 8);
    Vector2 scale = new Vector2(3, 2);

    LevelConfig config = new LevelConfig();
    config.mapTriggers =
        new TriggerConfig[] {
          new TriggerConfig(position, scale, new String[] {"test-trigger"}, false)
        };

    config.createEntities();

    assertEquals(1, config.entities.size());
    assertEquals(position, config.entities.getFirst().pos);
    assertEquals(scale, config.entities.getFirst().entity.getScale());
  }

}
