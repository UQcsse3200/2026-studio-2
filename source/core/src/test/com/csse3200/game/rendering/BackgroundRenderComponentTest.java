package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.GameArea.RepeatMode;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.areas.Level2GameArea;
import com.csse3200.game.areas.Level3GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.inventory.InventoryBarDisplay;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class BackgroundRenderComponentTest {
  @Mock RenderService service;

  @Test
  void shouldCreateParallaxLayer() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "cool_texture";

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);
    int layerCount = backgroundComponent.getLayerCount();
    assertEquals(1, layerCount);
  }

  @Test
  void shouldCreateParallaxLayers() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "texture";
    String texturePath2 = "notTexture";

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);
    backgroundComponent.addLayer(
        texturePath2,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);
    int layerCount = backgroundComponent.getLayerCount();
    assertEquals(2, layerCount);
  }

  @Test
  void shouldScaleLayersWhenMapToggledLevel1() {
    CameraComponent camera = spy(CameraComponent.class);
    TerrainFactory terrain = spy(new TerrainFactory(camera));
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "cool_texture";
    Entity player = mock(Entity.class);
    InventoryBarDisplay inventoryBarDisplay = mock(InventoryBarDisplay.class);
    PlayerStatsDisplay playerStatsDisplay = mock(PlayerStatsDisplay.class);

    BackgroundRenderComponent backgroundComponent =
        spy(new BackgroundRenderComponent(camera, backgroundPos, worldBounds));
    GameArea gameArea1 = spy(new Level1GameArea(terrain, camera));

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);

    when(gameArea1.getPlayer()).thenReturn(player);
    when(player.getComponent(InventoryBarDisplay.class)).thenReturn(inventoryBarDisplay);
    when(player.getComponent(PlayerStatsDisplay.class)).thenReturn(playerStatsDisplay);

    gameArea1.toggleMap(worldBounds, camera, backgroundComponent, "level1");
    verify(backgroundComponent)
        .scaleEntity(any(Vector2.class), any(Vector2.class), eq(true), anyString());

    gameArea1.toggleMap(worldBounds, camera, backgroundComponent, "level1");
    verify(backgroundComponent)
        .scaleEntity(any(Vector2.class), any(Vector2.class), eq(false), anyString());
  }

  @Test
  void shouldScaleLayersWhenMapToggledLevel2() {
    CameraComponent camera = spy(CameraComponent.class);
    TerrainFactory terrain = spy(new TerrainFactory(camera));
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "cool_texture";
    Entity player = mock(Entity.class);
    InventoryBarDisplay inventoryBarDisplay = mock(InventoryBarDisplay.class);
    PlayerStatsDisplay playerStatsDisplay = mock(PlayerStatsDisplay.class);

    BackgroundRenderComponent backgroundComponent =
        spy(new BackgroundRenderComponent(camera, backgroundPos, worldBounds));
    GameArea gameArea2 = spy(new Level2GameArea(terrain, camera, player));

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);

    when(gameArea2.getPlayer()).thenReturn(player);
    when(player.getComponent(InventoryBarDisplay.class)).thenReturn(inventoryBarDisplay);
    when(player.getComponent(PlayerStatsDisplay.class)).thenReturn(playerStatsDisplay);

    gameArea2.toggleMap(worldBounds, camera, backgroundComponent, "level2");
    verify(backgroundComponent)
        .scaleEntity(any(Vector2.class), any(Vector2.class), eq(true), anyString());

    gameArea2.toggleMap(worldBounds, camera, backgroundComponent, "level2");
    verify(backgroundComponent)
        .scaleEntity(any(Vector2.class), any(Vector2.class), eq(false), anyString());
  }

  @Test
  void shouldScaleLayersWhenMapToggledLevel3() {
    CameraComponent camera = spy(CameraComponent.class);
    TerrainFactory terrain = spy(new TerrainFactory(camera));
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "cool_texture";
    Entity player = mock(Entity.class);
    InventoryBarDisplay inventoryBarDisplay = mock(InventoryBarDisplay.class);
    PlayerStatsDisplay playerStatsDisplay = mock(PlayerStatsDisplay.class);

    BackgroundRenderComponent backgroundComponent =
        spy(new BackgroundRenderComponent(camera, backgroundPos, worldBounds));
    GameArea gameArea3 = spy(new Level3GameArea(terrain, camera, player));

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);

    when(gameArea3.getPlayer()).thenReturn(player);
    when(player.getComponent(InventoryBarDisplay.class)).thenReturn(inventoryBarDisplay);
    when(player.getComponent(PlayerStatsDisplay.class)).thenReturn(playerStatsDisplay);

    gameArea3.toggleMap(worldBounds, camera, backgroundComponent, "level3");
    verify(backgroundComponent)
        .scaleEntity(any(Vector2.class), any(Vector2.class), eq(true), anyString());

    gameArea3.toggleMap(worldBounds, camera, backgroundComponent, "level3");
    verify(backgroundComponent)
        .scaleEntity(any(Vector2.class), any(Vector2.class), eq(false), anyString());
  }

  @Test
  void shouldDrawLayerWithNoWeather() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "texture";
    SpriteBatch batch = mock(SpriteBatch.class);
    Entity cameraTarget = mock(Entity.class);
    Entity backgroundEntity = mock(Entity.class);
    Color colour = Color.WHITE;

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(1f, -1f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);

    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.001f);
    ServiceLocator.registerTimeSource(time);

    when(camera.getEntity()).thenReturn(cameraTarget);
    backgroundComponent.setEntity(backgroundEntity);
    when(backgroundEntity.getPosition()).thenReturn(new Vector2(10f, 10f));
    when(batch.getColor()).thenReturn(colour);

    if (!backgroundComponent.getWeather()) {
      backgroundComponent.toggleWeather();
    }
    backgroundComponent.draw(batch);
  }

  @Test
  void shouldDrawLayerWithWeatherAndNoFlash() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "texture";
    SpriteBatch batch = mock(SpriteBatch.class);
    Entity cameraTarget = mock(Entity.class);
    Entity backgroundEntity = mock(Entity.class);
    Color colour = Color.WHITE;

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(1f, -1f),
        RepeatMode.NONE,
        1f,
        1f,
        false,
        0,
        -1);

    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.001f);
    ServiceLocator.registerTimeSource(time);

    when(camera.getEntity()).thenReturn(cameraTarget);
    backgroundComponent.setEntity(backgroundEntity);
    when(backgroundEntity.getPosition()).thenReturn(new Vector2(10f, 10f));
    when(batch.getColor()).thenReturn(colour);

    if (backgroundComponent.getWeather()) {
      backgroundComponent.toggleWeather();
    }
    backgroundComponent.draw(batch);
  }

  @Test
  void shouldDrawLayerWithWeatherAndFlash() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "texture";
    SpriteBatch batch = mock(SpriteBatch.class);
    Entity cameraTarget = mock(Entity.class);
    Entity backgroundEntity = mock(Entity.class);
    Color colour = Color.WHITE;

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(1f, -1f),
        RepeatMode.NONE,
        1f,
        1f,
        true,
        0,
        1);

    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.001f);
    ServiceLocator.registerTimeSource(time);

    when(camera.getEntity()).thenReturn(cameraTarget);
    backgroundComponent.setEntity(backgroundEntity);
    when(backgroundEntity.getPosition()).thenReturn(new Vector2(10f, 10f));
    when(batch.getColor()).thenReturn(colour);

    if (backgroundComponent.getWeather()) {
      backgroundComponent.toggleWeather();
    }
    backgroundComponent.draw(batch);
  }

  @Test
  void shouldDrawLayerWithHorizontalRepeat() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "texture";
    SpriteBatch batch = mock(SpriteBatch.class);
    Entity cameraTarget = mock(Entity.class);
    Entity backgroundEntity = mock(Entity.class);
    Color colour = Color.WHITE;

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(1f, -1f),
        RepeatMode.HORIZONTAL,
        1f,
        1f,
        false,
        0,
        -1);

    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.001f);
    ServiceLocator.registerTimeSource(time);

    when(camera.getEntity()).thenReturn(cameraTarget);
    backgroundComponent.setEntity(backgroundEntity);
    when(backgroundEntity.getPosition()).thenReturn(new Vector2(10f, 10f));
    when(batch.getColor()).thenReturn(colour);

    if (!backgroundComponent.getWeather()) {
      backgroundComponent.toggleWeather();
    }
    backgroundComponent.draw(batch);
  }

  @Test
  void shouldDrawLayerWithChaoticRepeat() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    String texturePath = "texture";
    SpriteBatch batch = mock(SpriteBatch.class);
    Entity cameraTarget = mock(Entity.class);
    Entity backgroundEntity = mock(Entity.class);
    Color colour = Color.WHITE;

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    backgroundComponent.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(1f, -1f),
        RepeatMode.CHAOTIC,
        1f,
        1f,
        false,
        0,
        -1);

    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.001f);
    ServiceLocator.registerTimeSource(time);

    when(camera.getEntity()).thenReturn(cameraTarget);
    backgroundComponent.setEntity(backgroundEntity);
    when(backgroundEntity.getPosition()).thenReturn(new Vector2(10f, 10f));
    when(batch.getColor()).thenReturn(colour);

    if (!backgroundComponent.getWeather()) {
      backgroundComponent.toggleWeather();
    }
    backgroundComponent.draw(batch);
  }

  @Test
  void shouldReturnIfNoLayersExistOnDraw() {
    CameraComponent camera = spy(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(10f, 10f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    SpriteBatch batch = mock(SpriteBatch.class);

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(camera, backgroundPos, worldBounds);

    backgroundComponent.draw(batch);
  }
}
