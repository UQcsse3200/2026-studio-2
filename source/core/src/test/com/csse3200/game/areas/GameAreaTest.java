package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.SpawnData;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.inventory.InventoryBarDisplay;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.components.level.ActivatableComponent;
import com.csse3200.game.components.level.CheckpointComponent;
import com.csse3200.game.components.level.LevelTriggerComponent;
import com.csse3200.game.components.level.PlatformGrappleComponent;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameAreaTest {
  @Test
  void mapWidthDoesNotOverflowWhenAddingCameraPadding() {
    CameraComponent camera = new CameraComponent();
    GameArea area =
        new GameArea(camera) {
          @Override
          public void create() {
            // The map calculation needs no level assets.
          }
        };
    area.player =
        new Entity()
            .addComponent(mock(InventoryBarDisplay.class))
            .addComponent(mock(PlayerStatsDisplay.class));

    area.toggleMap(
        new Vector2(2147483648f, 100f), camera, mock(BackgroundRenderComponent.class), "level1");

    assertEquals(2147483648f, camera.getCamera().viewportWidth);
  }

  @Test
  void shouldNotDisposeCollectedItemsAgainWhenLeavingArea() {
    EntityService entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    GameArea area =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };
    ItemComponent item = spy(new ItemComponent(new Arrow(ItemType.ICE_ARROW, 3)));
    Entity pickup = new Entity().addComponent(item);
    area.spawnEntity(pickup);

    // Successful pickups dispose their world entity immediately, before the area is left.
    pickup.dispose();
    area.dispose();

    verify(item, times(1)).dispose();
    assertTrue(entities.getEntities().isEmpty());
    assertTrue(area.areaEntities.isEmpty());
  }

  @Test
  void shouldDisposeWorldItemsButPreservePlayerInventoryOnAreaChange() {
    EntityService entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    GameArea area =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };
    InventoryComponent inventory = new InventoryComponent(50);
    inventory.addItem(ItemType.FIRE_ARROW, 5);
    Entity player = new Entity().addComponent(inventory);
    area.player = player;
    area.spawnEntity(player);
    Entity pickup = new Entity().addComponent(new ItemComponent(new Arrow(ItemType.ICE_ARROW, 3)));
    area.spawnEntity(pickup);

    area.dispose();

    assertFalse(entities.getEntities().contains(pickup, true));
    assertTrue(entities.getEntities().contains(player, true));
    assertEquals(5, inventory.getItemCount(ItemType.FIRE_ARROW));
    assertEquals(50, inventory.getGold());
    assertTrue(area.areaEntities.isEmpty());
    entities.dispose();
  }

  @Test
  void shouldSpawnEntities() {

    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };

    ServiceLocator.registerEntityService(new EntityService());
    Entity entity = mock(Entity.class);
    EventHandler mockEventHandler = mock(EventHandler.class);

    when(entity.getEvents()).thenReturn(mockEventHandler);

    gameArea.spawnEntity(entity);
    verify(entity).create();

    gameArea.dispose();
    verify(entity).dispose();
  }

  @Test
  void shouldSavePlatformReference() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };

    ServiceLocator.registerEntityService(new EntityService());

    Entity p1 = spy(Entity.class);
    p1.addComponent(new PlatformGrappleComponent(0));
    gameArea.spawnEntity(p1);
    verify(p1).create();

    assertFalse(gameArea.platforms.contains(p1));

    Entity p2 = spy(Entity.class);
    p2.addComponent(new PlatformGrappleComponent(8));
    gameArea.spawnEntity(p2);
    verify(p2).create();

    assertTrue(gameArea.platforms.contains(p2));

    gameArea.dispose();
    verify(p1).dispose();
    verify(p2).dispose();
  }

  @Test
  void findsBestPlatformCandidate() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };

    ServiceLocator.registerEntityService(new EntityService());
    PlatformGrappleComponent comp = new PlatformGrappleComponent(15);

    Entity p1 = spy(Entity.class);
    p1.setPosition(0.5f, 5.5f);
    p1.addComponent(comp);

    Entity p2 = spy(Entity.class);
    p2.setPosition(1.5f, 0.5f);
    p2.addComponent(comp);

    Entity p3 = spy(Entity.class);
    p3.setPosition(7.5f, 4.5f);
    p3.addComponent(comp);

    gameArea.spawnEntity(p1);
    gameArea.spawnEntity(p2);
    gameArea.spawnEntity(p3);

    assertEquals(p1, gameArea.findTargetedPlatform(new Vector2(-15, 5))); // test negatives
    assertEquals(p2, gameArea.findTargetedPlatform(new Vector2(2, 1))); // test exact center
    assertEquals(p3, gameArea.findTargetedPlatform(new Vector2(15, 10))); // test far
    assertEquals(p1, gameArea.findTargetedPlatform(new Vector2(1.5f, 3.5f))); // test equidistant
  }

  @Test
  void shouldSaveActivatableReference() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };

    ServiceLocator.registerEntityService(new EntityService());

    Entity p1 = spy(Entity.class);
    gameArea.spawnEntity(p1);
    verify(p1).create();

    assertTrue(gameArea.triggerableEntities.isEmpty());

    Entity p2 = spy(Entity.class);
    p2.addComponent(new ActivatableComponent(new String[] {"test"}));
    gameArea.spawnEntity(p2);
    verify(p2).create();

    assertFalse(gameArea.triggerableEntities.isEmpty());
    assertTrue(gameArea.triggerableEntities.containsKey("test"));

    gameArea.dispose();
    verify(p1).dispose();
    verify(p2).dispose();
  }

  @Test
  void shouldSaveLevelTriggerReference() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };

    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerInputService(new InputService());

    Entity p1 = spy(Entity.class);
    gameArea.spawnEntity(p1);
    verify(p1).create();

    assertNull(gameArea.levelChanger);

    Entity p2 = spy(Entity.class);
    p2.addComponent(new LevelTriggerComponent("test"));
    gameArea.spawnEntity(p2);
    verify(p2).create();

    assertEquals(gameArea.levelChanger, p2);

    gameArea.dispose();
    verify(p1).dispose();
    verify(p2).dispose();
  }

  @Test
  void shouldSpawnUsingSpawnData() {
    GameArea gameArea =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // This test double does not create or render game content.
          }
        };

    ServiceLocator.registerEntityService(new EntityService());

    gameArea.config = mock(LevelConfig.class);
    gameArea.terrain = mock(TerrainComponent.class);
    when(gameArea.terrain.tileToWorldPosition(any())).thenReturn(new Vector2(0, 0));

    SpawnData spawnData = new SpawnData(new GridPoint2(0, 0), new Entity());
    ArrayList<SpawnData> allSpawnData = new ArrayList<>();
    allSpawnData.add(spawnData);

    when(gameArea.config.createEntities()).thenReturn(allSpawnData);

    gameArea.spawnConfigEntities();
    assertEquals(1, gameArea.areaEntities.size());
  }

  @Test
  void returnsOnlyActiveCheckpointsInConfigurationOrder() {
    GameArea area =
        new GameArea(new CameraComponent()) {
          @Override
          public void create() {
            // Checkpoint queries do not require rendering or spawning a level.
          }
        };
    area.config = mock(LevelConfig.class);
    CheckpointComponent first = new CheckpointComponent(true, new GridPoint2(20, 3));
    CheckpointComponent inactive = new CheckpointComponent(false, new GridPoint2(10, 2));
    CheckpointComponent last = new CheckpointComponent(true, new GridPoint2(5, 1));
    when(area.config.getCheckpoints()).thenReturn(List.of(first, inactive, last));

    assertEquals(
        List.of(new GridPoint2(20, 3), new GridPoint2(5, 1)), area.getActiveCheckpointPositions());
    first.deactivate();
    last.deactivate();
    assertTrue(area.getActiveCheckpointPositions().isEmpty());
  }
}
