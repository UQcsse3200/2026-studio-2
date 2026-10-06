package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class RenderComponentTest {
  @Mock RenderService service;

  @Test
  void shouldRegisterSelf() {
    ServiceLocator.registerRenderService(service);
    RenderComponent component = spy(RenderComponent.class);
    component.create();
    verify(service).register(component);
  }

  @Test
  void shouldUnregisterOnDispose() {
    ServiceLocator.registerRenderService(service);
    RenderComponent component = spy(RenderComponent.class);
    component.create();
    component.dispose();
    verify(service).unregister(component);
  }

  @Test
  void shouldDrawOnRender() {
    RenderComponent component = spy(RenderComponent.class);
    component.render(null);
    verify(component).draw(any());
  }

  @Test
  void shouldGiveCorrectRenderOrder() {
    RenderComponent component1 = spy(RenderComponent.class);
    RenderComponent component2 = spy(RenderComponent.class);
    assertEquals(component1.getLayer(), component2.getLayer());

    Entity entity1 = new Entity();
    Entity entity2 = new Entity();
    component1.setEntity(entity1);
    component2.setEntity(entity2);

    entity1.setPosition(0f, 1f);
    entity2.setPosition(0f, 2f);
    assertTrue(component1.getZIndex() > component2.getZIndex());

    entity2.setPosition(5f, -3f);
    assertTrue(component1.getZIndex() < component2.getZIndex());
  }

  @Test
  void shouldGiveCorrectLayerOrder() {
    CameraComponent cameraComponent = mock(CameraComponent.class);
    Vector2 backgroundPos = new Vector2(0f, 0f);
    Vector2 worldBounds = new Vector2(90f, 30f);
    float tileSize = 1f;
    String texturePath = "water_texture";

    ResourceService resourceService = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset(texturePath, Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(cameraComponent, backgroundPos, worldBounds);
    ForegroundRenderComponent foregroundComponent =
        new ForegroundRenderComponent(cameraComponent, backgroundPos, worldBounds);
    TiledRenderComponent tiledComponent = new TiledRenderComponent(texturePath, tileSize);
    assertTrue(foregroundComponent.getLayer() > backgroundComponent.getLayer());
    assertTrue(tiledComponent.getLayer() > foregroundComponent.getLayer());
  }

  @Test
  void shouldReturnCorrectValuesAfterInstantiation() {
    RenderComponent renderComponent = spy(RenderComponent.class);
    assertEquals(1f, renderComponent.getDarkness());
    assertEquals(0f, renderComponent.getLightning());
    assertFalse(renderComponent.getWeather());
  }

  @Test
  void shouldToggleWeatherCorrectly() {
    RenderComponent renderComponent = spy(RenderComponent.class);
    assertFalse(renderComponent.getWeather());
    renderComponent.toggleWeather();
    assertTrue(renderComponent.getWeather());
    renderComponent.toggleWeather();
    assertFalse(renderComponent.getWeather());
  }

  @Test
  void shouldUpdateDarknessWhenWeatherIsOn() {
    RenderComponent renderComponent = spy(RenderComponent.class);
    GameTime time = mock(GameTime.class);
    LightingService lightingService = mock(LightingService.class);
    LightingEngine lightingEngine = mock(LightingEngine.class);

    when(time.getDeltaTime()).thenReturn(0.001f);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerTimeSource(time);
    ServiceLocator.registerLightingService(lightingService);

    float prevDarkness = renderComponent.getDarkness();
    renderComponent.toggleWeather();
    renderComponent.update();
    float newDarkness = renderComponent.getDarkness();
    assertTrue(prevDarkness != newDarkness);
  }

  @Test
  void shouldNotUpdateDarknessWhenWeatherIsOff() {
    RenderComponent renderComponent = spy(RenderComponent.class);
    float prevDarkness = renderComponent.getDarkness();
    renderComponent.update();
    float newDarkness = renderComponent.getDarkness();
    assertEquals(prevDarkness, newDarkness);
  }

  @Test
  void shouldUpdateBackgroundLightWhenWeatherIsOn() {
    RenderComponent renderComponent = spy(RenderComponent.class);
    GameTime time = mock(GameTime.class);
    LightingService lightingService = mock(LightingService.class);
    LightingEngine lightingEngine = mock(LightingEngine.class);

    when(time.getDeltaTime()).thenReturn(0.001f);
    when(lightingService.getEngine()).thenReturn(lightingEngine);
    ServiceLocator.registerTimeSource(time);
    ServiceLocator.registerLightingService(lightingService);

    float prevBackgroundLight = renderComponent.getBackgroundLight();
    renderComponent.toggleWeather();
    renderComponent.update();
    float newBackgroundLight = renderComponent.getBackgroundLight();
    assertTrue(prevBackgroundLight != newBackgroundLight);
  }

  @Test
  void shouldNotUpdateBackgroundLightWhenWeatherIsOff() {
    RenderComponent renderComponent = spy(RenderComponent.class);
    float prevBackgroundLight = renderComponent.getBackgroundLight();
    renderComponent.update();
    float newBackgroundLight = renderComponent.getBackgroundLight();
    assertEquals(prevBackgroundLight, newBackgroundLight);
  }
}
