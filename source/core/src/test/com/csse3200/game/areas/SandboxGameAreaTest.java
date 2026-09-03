package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.GrappleRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class SandboxGameAreaTest {
  @Test
  void shouldPlacePlayerAboveGroundOnLeft() {
    assertEquals(new GridPoint2(3, 4), SandboxGameArea.getPlayerSpawn());
  }

  @Test
  void shouldOrderEveryItemTypeByAscendingId() {
    assertEquals(
        List.of(
            ItemType.ARROW,
            ItemType.RopeArrow,
            ItemType.CONSUMABLE,
            ItemType.FireArrow,
            ItemType.ColdArrow),
        SandboxGameArea.getOrderedItemTypes());
  }

  @Test
  void shouldLayItemsOutFromLeftToRightWithConsistentSpacing() {
    assertEquals(
        List.of(
            new GridPoint2(8, 3),
            new GridPoint2(11, 3),
            new GridPoint2(14, 3),
            new GridPoint2(17, 3),
            new GridPoint2(20, 3)),
        List.of(
            SandboxGameArea.getItemPosition(0),
            SandboxGameArea.getItemPosition(1),
            SandboxGameArea.getItemPosition(2),
            SandboxGameArea.getItemPosition(3),
            SandboxGameArea.getItemPosition(4)));
  }

  @Test
  void shouldSpawnOneClearlyVisibleEntityForEveryItemType() {
    PhysicsService physicsService = new PhysicsService();
    EntityService entityService = new EntityService();
    ServiceLocator.registerPhysicsService(physicsService);
    ServiceLocator.registerEntityService(entityService);
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);
    ServiceLocator.registerTimeSource(new GameTime());

    InputFactory inputFactory = mock(InputFactory.class);
    when(inputFactory.createForPlayer()).thenReturn(new KeyboardPlayerInputComponent());
    InputService inputService = mock(InputService.class);
    when(inputService.getInputFactory()).thenReturn(inputFactory);
    ServiceLocator.registerInputService(inputService);

    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    AtlasRegion defaultRegion = mock(AtlasRegion.class);
    when(defaultRegion.getRegionWidth()).thenReturn(1);
    when(defaultRegion.getRegionHeight()).thenReturn(1);
    TextureAtlas atlas = mock(TextureAtlas.class);
    when(atlas.findRegions(anyString())).thenReturn(new Array<>());
    when(atlas.findRegion("default")).thenReturn(defaultRegion);
    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    when(resourceService.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
    ServiceLocator.registerResourceService(resourceService);

    TerrainComponent terrain = mock(TerrainComponent.class);
    when(terrain.getTileSize()).thenReturn(1f);
    when(terrain.tileToWorldPosition(any(GridPoint2.class)))
        .thenAnswer(
            invocation -> {
              GridPoint2 point = invocation.getArgument(0);
              return new Vector2(point.x, point.y);
            });
    TerrainFactory terrainFactory = mock(TerrainFactory.class);
    when(terrainFactory.createTerrain(TerrainFactory.TerrainType.BACKGROUND_DESERT))
        .thenReturn(terrain);

    SandboxGameArea area = new SandboxGameArea(terrainFactory, new CameraComponent());
    try (MockedConstruction<GrappleRenderComponent> ignored =
        mockConstruction(GrappleRenderComponent.class)) {
      area.create();

      List<Entity> itemEntities = new ArrayList<>();
      for (Entity entity : entityService.getEntities()) {
        if (entity.getComponent(ItemComponent.class) != null) {
          itemEntities.add(entity);
        }
      }

      assertEquals(ItemType.values().length, itemEntities.size());
      assertTrue(itemEntities.stream().allMatch(entity -> entity.getScale().y >= 1f));
      verify(resourceService).loadSounds(aryEq(new String[] {"sounds/Impact4.ogg"}));
    } finally {
      area.dispose();
      physicsService.getPhysics().dispose();
    }
  }
}
