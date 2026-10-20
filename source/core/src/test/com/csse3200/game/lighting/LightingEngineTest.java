package com.csse3200.game.lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class LightingEngineTest {
  private PointLightComponent first;
  private PointLightComponent second;

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(time);
  }

  @AfterEach
  void tearDown() {
    if (first != null) {
      LightingEngine.removePointLight(first);
    }
    if (second != null) {
      LightingEngine.removePointLight(second);
    }
  }

  @Test
  void overlappingLightsShareRayCountsAndBlendTheirColours() {
    first = createLight(20, 1f, 0f, 0f);
    second = createLight(8, 0f, 0f, 1f);

    LightingEngine.addPointLight(first);
    LightingEngine.addPointLight(second);

    verify(first).setIntensity(0.5f);
    verify(second).setIntensity(0.5f);
    verify(first).setRayCount(14);
    verify(second).setRayCount(14);
    verifyBlendedColour(first);
    verifyBlendedColour(second);

    LightingEngine.removePointLight(second);

    verify(first, times(2)).setRayCount(20);
    verify(first, times(2)).setIntensity(1f);
  }

  private PointLightComponent createLight(int rays, float red, float green, float blue) {
    PointLightComponent light = mock(PointLightComponent.class);
    when(light.getRayCount()).thenReturn(rays);
    when(light.getOldRayCount()).thenReturn(rays);
    when(light.isActive()).thenReturn(true);
    when(light.getLightX()).thenReturn(0f);
    when(light.getLightY()).thenReturn(0f);
    when(light.getLightDistance()).thenReturn(4f);
    when(light.getBaseRed()).thenReturn(red);
    when(light.getBaseGreen()).thenReturn(green);
    when(light.getBaseBlue()).thenReturn(blue);
    when(light.getSmoothedRed()).thenReturn(red);
    when(light.getSmoothedGreen()).thenReturn(green);
    when(light.getSmoothedBlue()).thenReturn(blue);
    return light;
  }

  private void verifyBlendedColour(PointLightComponent light) {
    ArgumentCaptor<Float> red = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> green = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> blue = ArgumentCaptor.forClass(Float.class);

    verify(light, atLeastOnce()).setSmoothedColor(red.capture(), green.capture(), blue.capture());
    int lastCall = red.getAllValues().size() - 1;
    assertEquals(0.5f, red.getAllValues().get(lastCall), 0.001f);
    assertEquals(0f, green.getAllValues().get(lastCall), 0.001f);
    assertEquals(0.5f, blue.getAllValues().get(lastCall), 0.001f);
  }
}
