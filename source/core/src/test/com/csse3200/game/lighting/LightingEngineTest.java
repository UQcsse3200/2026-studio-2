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
  private GameTime time;

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
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

  @Test
  void distantLightsKeepTheirOriginalRayCountsAndIntensity() {
    first = createLight(20, 1f, 0f, 0f);
    second = createLight(8, 0f, 0f, 1f);
    when(second.getLightX()).thenReturn(8f);

    LightingEngine.addPointLight(first);
    LightingEngine.addPointLight(second);

    verify(first, times(2)).setIntensity(1f);
    verify(second).setIntensity(1f);
    verify(first, times(2)).setRayCount(20);
    verify(second).setRayCount(8);
  }

  @Test
  void registeringTheSameLightTwiceDoesNotRegisterItAgain() {
    first = createLight(20, 1f, 0f, 0f);

    LightingEngine.addPointLight(first);
    LightingEngine.addPointLight(first);

    verify(first).setIntensity(1f);
    verify(first).setRayCount(20);
  }

  @Test
  void zeroDistanceLightsDoNotOverlapAndRespectTheMinimumRayCount() {
    first = createLight(1, 1f, 0f, 0f);
    second = createLight(2, 0f, 0f, 1f);
    when(first.getLightDistance()).thenReturn(0f);
    when(second.getLightDistance()).thenReturn(0f);

    LightingEngine.addPointLight(first);
    LightingEngine.addPointLight(second);

    verify(first, times(2)).setIntensity(1f);
    verify(first, times(2)).setRayCount(3);
    verify(second).setRayCount(3);
  }

  @Test
  void inactiveLightsDoNotContributeToOverlap() {
    first = createLight(12, 1f, 0f, 0f);
    second = createLight(6, 0f, 0f, 1f);
    when(first.isActive()).thenReturn(false);

    LightingEngine.addPointLight(first);
    LightingEngine.addPointLight(second);

    verify(first, times(2)).setIntensity(1f);
    verify(first, times(2)).setRayCount(12);
    verify(second).setIntensity(1f);
    verify(second).setRayCount(6);
  }

  @Test
  void zeroDeltaTimeDoesNotImmediatelyEqualiseRayCounts() {
    first = createLight(20, 1f, 0f, 0f);
    second = createLight(8, 0f, 0f, 1f);
    LightingEngine.addPointLight(first);
    when(time.getDeltaTime()).thenReturn(0f);

    LightingEngine.addPointLight(second);

    verify(first, times(2)).setRayCount(20);
    verify(second).setRayCount(8);
    verify(first).setIntensity(0.5f);
    verify(second).setIntensity(0.5f);
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
