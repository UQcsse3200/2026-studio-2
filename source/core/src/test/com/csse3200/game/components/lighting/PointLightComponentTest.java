package com.csse3200.game.components.lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class PointLightComponentTest {
  private PointLightComponent component;
  private GameTime time;

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.5f);
    ServiceLocator.registerTimeSource(time);
  }

  @AfterEach
  void tearDown() {
    if (component != null) {
      component.dispose();
    }
  }

  @Test
  void createsAnInactiveLightAndRecreatesItWhenItsRayCountChanges() {
    RayHandler rayHandler = mock(RayHandler.class);
    component = new PointLightComponent(rayHandler, 128, new Color(0.2f, 0.4f, 0.6f, 0.8f), 4f);
    component.setActive(false);
    component.setDistance(5f);
    component.setColor(new Color(0.2f, 0.4f, 0.6f, 0.8f));
    component.update();
    assertEquals(128, component.getRayCount(), "ray count is unchanged before creation");

    Entity entity = new Entity().addComponent(component);
    entity.setPosition(2f, 3f);
    List<List<?>> constructorArguments = new ArrayList<>();

    try (var lights =
        mockConstruction(
            PointLight.class,
            (light, context) -> {
              constructorArguments.add(context.arguments());
              stubLight(light, context);
            })) {
      entity.create();

      PointLight originalLight = lights.constructed().get(0);
      List<?> originalArguments = constructorArguments.get(0);
      assertEquals(rayHandler, originalArguments.get(0));
      assertEquals(128, originalArguments.get(1));
      assertEquals(5f, (float) originalArguments.get(3));
      assertEquals(2f, (float) originalArguments.get(4));
      assertEquals(3f, (float) originalArguments.get(5));
      verify(originalLight).setSoftnessLength(1f);
      verify(originalLight).setXray(false);
      verify(originalLight).setActive(false);
      component.setRayCount(128);
      component.update();

      component.setDistance(6f);
      component.setRayCount(64);
      component.setActive(true);
      component.update();

      assertEquals(64, component.getRayCount());
      assertEquals(128, component.getOldRayCount());
      assertEquals(6f, component.getDistance());
      verify(originalLight).setDistance(6f);
      verify(originalLight).remove();
      verify(originalLight).setActive(true);

      PointLight replacement = lights.constructed().get(1);
      List<?> replacementArguments = constructorArguments.get(1);
      assertEquals(64, replacementArguments.get(1));
      assertEquals(6f, (float) replacementArguments.get(3));
      assertEquals(2f, (float) replacementArguments.get(4));
      assertEquals(3f, (float) replacementArguments.get(5));
      when(replacement.isActive()).thenReturn(true);
      when(time.getDeltaTime()).thenReturn(-0.5f);
      component.update();
      assertEquals(2f, component.getLightX());
      assertEquals(3f, component.getLightY());
      assertEquals(6f, component.getLightDistance());
      verify(replacement, times(2)).setPosition(2.5f, 3.5f);
    }
  }

  @Test
  void colorChangesPreserveAlphaAndApplyClampedIntensity() {
    component =
        new PointLightComponent(mock(RayHandler.class), 32, new Color(0.2f, 0.4f, 0.6f, 0.8f), 3f);
    component.setColor(new Color(0.2f, 0.4f, 0.6f, 0.8f));
    Entity entity = new Entity().addComponent(component);

    try (var lights = mockConstruction(PointLight.class, this::stubLight)) {
      entity.create();
      PointLight light = lights.constructed().get(0);

      assertEquals(0.2f, component.getBaseRed(), 0.001f);
      assertEquals(0.4f, component.getSmoothedGreen(), 0.001f);
      component.setIntensity(0.5f);
      component.setColor(new Color(0.8f, 0.6f, 0.4f, 0.5f));
      component.setSmoothedColor(0.6f, 0.4f, 0.2f);

      assertEquals(0.8f, component.getBaseRed(), 0.001f);
      assertEquals(0.6f, component.getSmoothedRed(), 0.001f);
      assertEquals(0.2f, component.getSmoothedBlue(), 0.001f);

      component.setIntensity(2f);
      component.setIntensity(3f);
      component.setIntensity(-1f);

      ArgumentCaptor<Color> colors = ArgumentCaptor.forClass(Color.class);
      verify(light, atLeastOnce()).setColor(colors.capture());
      Color finalColor = colors.getValue();
      assertEquals(0f, finalColor.r, 0.001f);
      assertEquals(0f, finalColor.g, 0.001f);
      assertEquals(0f, finalColor.b, 0.001f);
      assertEquals(0.5f, finalColor.a, 0.001f);
    }
  }

  @Test
  void canBeDisposedBeforeItsLightIsCreated() {
    component = new PointLightComponent(mock(RayHandler.class), 32, Color.WHITE, 3f);

    component.dispose();

    assertEquals(32, component.getRayCount());
  }

  private void stubLight(PointLight light, org.mockito.MockedConstruction.Context context) {
    when(light.getRayNum()).thenReturn((int) context.arguments().get(1));
    when(light.isActive()).thenReturn(false);
    when(light.getX()).thenReturn((float) context.arguments().get(4));
    when(light.getY()).thenReturn((float) context.arguments().get(5));
    when(light.getDistance()).thenReturn((float) context.arguments().get(3));
  }
}
