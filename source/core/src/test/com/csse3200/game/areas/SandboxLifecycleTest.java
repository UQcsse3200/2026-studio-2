package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.sandbox.SandboxEnemyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputService;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UiTestSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class SandboxLifecycleTest extends UiTestSupport {
  private SandboxGameArea area;
  private ResourceService realResources;
  private MockedConstruction<PointLightComponent> pointLights;

  @BeforeEach
  void createSandbox() {
    ServiceLocator.registerInputService(new InputService());
    realResources = new ResourceService();
    ServiceLocator.registerResourceService(realResources);
    LightingEngine lightingEngine = mock(LightingEngine.class);
    when(lightingEngine.getRayHandler()).thenReturn(mock(box2dLight.RayHandler.class));
    LightingService lightingService = mock(LightingService.class);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerLightingService(lightingService);
    TerrainFactory factory = mock(TerrainFactory.class);
    TerrainComponent terrain = mock(TerrainComponent.class);
    when(factory.createTerrain(any())).thenReturn(terrain);
    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.getMapBounds(0)).thenReturn(new GridPoint2(50, 30));
    when(terrain.tileToWorldPosition(any()))
        .thenAnswer(
            call -> {
              GridPoint2 tile = call.getArgument(0);
              return new Vector2(tile.x, tile.y);
            });
    area = new SandboxGameArea(factory, new CameraComponent());
    pointLights = mockConstruction(PointLightComponent.class);
    area.create();
  }

  @AfterEach
  void disposeSandbox() {
    if (area != null) {
      area.dispose();
    }
    entities.dispose();
    if (realResources != null) {
      realResources.dispose();
    }
    if (pointLights != null) {
      pointLights.close();
    }
  }

  @Test
  void shouldSpawnEveryItemInIdOrderWithLoadedIconsAndPhysics() {
    List<Entity> pickups =
        area.areaEntities.stream()
            .filter(entity -> entity.getComponent(ItemComponent.class) != null)
            .toList();
    List<ItemType> expected =
        Arrays.stream(ItemType.values()).sorted(Comparator.comparingInt(ItemType::getId)).toList();
    assertEquals(expected.size(), pickups.size());
    for (int i = 0; i < pickups.size(); i++) {
      Entity pickup = pickups.get(i);
      assertEquals(
          expected.get(i), pickup.getComponent(ItemComponent.class).getItem().getItemType());
      assertNotNull(pickup.getComponent(PhysicsComponent.class).getBody());
      assertNotNull(realResources.getAsset(expected.get(i).getTexturePath(), Texture.class));
      if (i > 0) {
        assertTrue(pickup.getPosition().x > pickups.get(i - 1).getPosition().x);
      }
    }
  }

  @Test
  void shouldReplaceEveryActiveAndPassiveMonsterThroughSpawnerUi() {
    Entity previous = null;
    for (SandboxEnemyType type : SandboxEnemyType.values()) {
      for (boolean active : new boolean[] {false, true}) {
        click(stage.getRoot().findActor("sandbox-monster-spawner-npc"));
        click(
            stage
                .getRoot()
                .findActor("sandbox-enemy-card-" + type.name().toLowerCase(Locale.ROOT)));
        CheckBox activeBox = stage.getRoot().findActor("sandbox-monster-active-checkbox");
        activeBox.setChecked(active);
        click(stage.getRoot().findActor("sandbox-monster-spawn-button"));
        Entity current = field(area, "spawnedMonster", Entity.class);
        assertNotNull(current);
        assertTrue(entities.getEntities().contains(current, true));
        assertNotNull(current.getComponent(PhysicsComponent.class).getBody());
        if (previous != null) {
          assertFalse(entities.getEntities().contains(previous, true));
          assertFalse(area.areaEntities.contains(previous));
        }
        assertFalse(entities.getPaused());
        previous = current;
      }
    }
  }

  @Test
  void shouldRestoreSandboxGoldOnTheNextApplicationCallback() {
    InventoryComponent inventory = area.getPlayer().getComponent(InventoryComponent.class);
    int initialGold = inventory.getGold();
    List<Runnable> callbacks = new ArrayList<>();
    Application application = Gdx.app;
    Gdx.app = mock(Application.class);
    doAnswer(
            call -> {
              callbacks.add(call.getArgument(0));
              return null;
            })
        .when(Gdx.app)
        .postRunnable(any());
    try {
      inventory.addGold(-10);
      assertEquals(initialGold - 10, inventory.getGold());
      assertEquals(1, callbacks.size());
      callbacks.remove(0).run();
      assertEquals(initialGold, inventory.getGold());
      assertTrue(callbacks.isEmpty());
    } finally {
      Gdx.app = application;
    }
  }

  @Test
  void shouldRemoveAreaEntitiesAndUnloadItsTextures() {
    Entity player = area.getPlayer();
    area.dispose();
    area = null;
    assertEquals(1, entities.getEntities().size);
    assertTrue(entities.getEntities().contains(player, true));
    assertFalse(realResources.containsAsset(ItemType.SpeedPotion.getTexturePath(), Texture.class));
    player.dispose();
    assertEquals(0, stage.getActors().size);
  }
}
