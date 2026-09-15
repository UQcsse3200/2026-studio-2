package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.weapons.bow.arrow.*;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.extensions.GameExtension;
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
  public void setUp() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());

    ResourceService resourceService = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resourceService);

    Texture mockTexture = mock(Texture.class);
    TextureAtlas mockAtlas = mock(TextureAtlas.class);

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
              pos, (int) size.x, (int) size.y, 0, "", new String[] {"test"}, false)
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
}
