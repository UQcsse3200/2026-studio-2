package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.csse3200.game.components.player.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class GrappleRenderComponentTest {
  @Mock RenderService service;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerRenderService(service);
  }

  @Test
  void shouldFindGrappleOnCreate() {
    Entity player =
        new Entity()
            .addComponent(new GrappleComponent())
            .addComponent(new GrappleRenderComponent());

    assertDoesNotThrow(player::create);
    assertNotNull(player.getComponent(GrappleRenderComponent.class));
  }

  @Test
  void shouldHandleMissingGrapple() {
    Entity player = new Entity().addComponent(new GrappleRenderComponent());

    // The null guard in draw() means this is safe even with no grapple present
    assertDoesNotThrow(player::create);
  }
}
