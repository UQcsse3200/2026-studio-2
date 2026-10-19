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
import org.mockito.verification.VerificationMode;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class BackgroundRenderComponentTest {
  private static final String TEXTURE = "texture";
  private static final String OTHER_TEXTURE = "notTexture";
  private static final Vector2 BACKGROUND_POS = new Vector2(10f, 10f);
  private static final Vector2 WORLD_BOUNDS = new Vector2(90f, 30f);
  private static final Vector2 STILL = new Vector2(0f, 0f);
  private static final Vector2 DRIFTING = new Vector2(1f, -1f);

  @Mock RenderService service;

  /** Builds a game area once the terrain, camera and player it is made from exist. */
  private interface AreaFactory {
    GameArea create(TerrainFactory terrain, CameraComponent camera, Entity player);
  }

  private static BackgroundRenderComponent newBackground(CameraComponent camera) {
    return new BackgroundRenderComponent(camera, BACKGROUND_POS, WORLD_BOUNDS);
  }

  /** Registers a resource service that knows the given textures. */
  private static void registerTextures(String... paths) {
    ResourceService resourceService = mock(ResourceService.class);
    for (String path : paths) {
      Texture texture = mock(Texture.class);
      when(resourceService.getAsset(path, Texture.class)).thenReturn(texture);
    }
    ServiceLocator.registerResourceService(resourceService);
  }

  private static void addLayer(
      BackgroundRenderComponent background,
      String texturePath,
      Vector2 speed,
      RepeatMode repeat,
      boolean flashes,
      int flashFrequency) {
    background.addLayer(
        texturePath,
        new Vector2(0f, 0f),
        30f,
        10f,
        new Vector2(0f, 0f),
        new Vector2(speed),
        repeat,
        1f,
        1f,
        flashes,
        0,
        flashFrequency);
  }

  private static void addPlainLayer(
      BackgroundRenderComponent background, String texturePath, Vector2 speed, RepeatMode repeat) {
    addLayer(background, texturePath, speed, repeat, false, -1);
  }

  @Test
  void shouldCreateParallaxLayer() {
    BackgroundRenderComponent background = newBackground(spy(CameraComponent.class));
    registerTextures("cool_texture");

    addPlainLayer(background, "cool_texture", STILL, RepeatMode.NONE);

    assertEquals(1, background.getLayerCount());
  }

  @Test
  void shouldCreateParallaxLayers() {
    BackgroundRenderComponent background = newBackground(spy(CameraComponent.class));
    registerTextures(TEXTURE);

    addPlainLayer(background, TEXTURE, STILL, RepeatMode.NONE);
    addPlainLayer(background, OTHER_TEXTURE, STILL, RepeatMode.NONE);

    assertEquals(2, background.getLayerCount());
  }

  /** Toggling the map on and then off again should scale the background each way. */
  private void assertMapToggleScalesLayers(String level, AreaFactory areaFactory) {
    CameraComponent camera = spy(CameraComponent.class);
    TerrainFactory terrain = spy(new TerrainFactory(camera));
    Entity player = mock(Entity.class);
    InventoryBarDisplay inventoryBarDisplay = mock(InventoryBarDisplay.class);
    PlayerStatsDisplay playerStatsDisplay = mock(PlayerStatsDisplay.class);

    BackgroundRenderComponent background = spy(newBackground(camera));
    GameArea gameArea = spy(areaFactory.create(terrain, camera, player));

    registerTextures("cool_texture");
    addPlainLayer(background, "cool_texture", STILL, RepeatMode.NONE);

    when(gameArea.getPlayer()).thenReturn(player);
    when(player.getComponent(InventoryBarDisplay.class)).thenReturn(inventoryBarDisplay);
    when(player.getComponent(PlayerStatsDisplay.class)).thenReturn(playerStatsDisplay);

    gameArea.toggleMap(WORLD_BOUNDS, camera, background, level);
    verify(background).scaleEntity(any(Vector2.class), any(Vector2.class), eq(true), anyString());

    gameArea.toggleMap(WORLD_BOUNDS, camera, background, level);
    verify(background).scaleEntity(any(Vector2.class), any(Vector2.class), eq(false), anyString());
  }

  @Test
  void shouldScaleLayersWhenMapToggledLevel1() {
    assertMapToggleScalesLayers(
        "level1", (terrain, camera, player) -> new Level1GameArea(terrain, camera));
  }

  @Test
  void shouldScaleLayersWhenMapToggledLevel2() {
    assertMapToggleScalesLayers("level2", Level2GameArea::new);
  }

  @Test
  void shouldScaleLayersWhenMapToggledLevel3() {
    assertMapToggleScalesLayers("level3", Level3GameArea::new);
  }

  /**
   * Draws one drifting layer, with the weather switched on or off, and checks the batch was drawn
   * to as often as expected.
   */
  private void assertLayerDraws(
      RepeatMode repeat,
      boolean flashes,
      int flashFrequency,
      boolean weatherOn,
      VerificationMode drawn) {
    CameraComponent camera = spy(CameraComponent.class);
    SpriteBatch batch = mock(SpriteBatch.class);
    Entity cameraTarget = mock(Entity.class);
    Entity backgroundEntity = mock(Entity.class);

    BackgroundRenderComponent background = newBackground(camera);
    registerTextures(TEXTURE);
    addLayer(background, TEXTURE, DRIFTING, repeat, flashes, flashFrequency);

    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.001f);
    ServiceLocator.registerTimeSource(time);

    when(camera.getEntity()).thenReturn(cameraTarget);
    background.setEntity(backgroundEntity);
    when(backgroundEntity.getPosition()).thenReturn(new Vector2(10f, 10f));
    when(batch.getColor()).thenReturn(Color.WHITE);

    if (background.getWeather() != weatherOn) {
      background.toggleWeather();
    }
    background.draw(batch);
    verify(batch, drawn).draw(any(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void shouldDrawLayerWithNoWeather() {
    assertLayerDraws(RepeatMode.NONE, false, -1, true, times(1));
  }

  @Test
  void shouldDrawLayerWithWeatherAndNoFlash() {
    assertLayerDraws(RepeatMode.NONE, false, -1, false, times(1));
  }

  @Test
  void shouldDrawLayerWithWeatherAndFlash() {
    assertLayerDraws(RepeatMode.NONE, true, 1, false, times(1));
  }

  @Test
  void shouldDrawLayerWithHorizontalRepeat() {
    assertLayerDraws(RepeatMode.HORIZONTAL, false, -1, true, atLeastOnce());
  }

  @Test
  void shouldDrawLayerWithChaoticRepeat() {
    assertLayerDraws(RepeatMode.CHAOTIC, false, -1, true, atLeastOnce());
  }

  @Test
  void shouldReturnIfNoLayersExistOnDraw() {
    SpriteBatch batch = mock(SpriteBatch.class);
    BackgroundRenderComponent background = newBackground(spy(CameraComponent.class));

    background.draw(batch);

    verifyNoInteractions(batch);
  }
}
