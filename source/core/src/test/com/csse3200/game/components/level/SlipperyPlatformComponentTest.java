package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SlipperyPlatformComponentTest {

  private SlipperyPlatformComponent component;
  private GameTime timeSource;

  @BeforeEach
  void setUp() {
    timeSource = mock(GameTime.class);
    ServiceLocator.registerTimeSource(timeSource);

    component = new SlipperyPlatformComponent(0.5f, 2f);
  }

  @Test
  void shouldReturnSlipperiness() {
    assertEquals(0.5f, component.getSlipperiness());
  }

  @Test
  void shouldAccumulateGrappleTimeWhenGrappled() {
    when(timeSource.getDeltaTime()).thenReturn(0.5f);

    component.setGrappled(true);
    component.update();

    assertEquals(0.5f, component.currentGrappleTime);
    assertTrue(component.grappled);
  }

  @Test
  void shouldResetGrappleTimeWhenNoLongerGrappled() {
    when(timeSource.getDeltaTime()).thenReturn(0.5f);

    component.setGrappled(true);
    component.update();

    assertEquals(0.5f, component.currentGrappleTime);

    component.setGrappled(false);
    component.update();

    assertEquals(0f, component.currentGrappleTime);
    assertFalse(component.grappled);
  }

  @Test
  void shouldTriggerWhenMaximumGrappleTimeExceeded() {
    when(timeSource.getDeltaTime()).thenReturn(2f);

    Entity entity = new Entity();
    entity.addComponent(component);

    component.setGrappled(true);
    component.update();

    assertFalse(component.grappled);
    assertEquals(0f, component.currentGrappleTime);
  }

  @Test
  void shouldDoNothingWhenNotGrappled() {
    component.update();

    assertEquals(0f, component.currentGrappleTime);
    assertFalse(component.grappled);
  }
}
