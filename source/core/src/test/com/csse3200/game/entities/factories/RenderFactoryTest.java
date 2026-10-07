package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class RenderFactoryTest {
  @Test
  void shouldCreateCameraEntity() {
    Entity camera = RenderFactory.createCamera();

    assertNotNull(camera.getComponent(CameraComponent.class));
  }

  @Test
  void shouldCreateIndependentCameraEntities() {
    Entity first = RenderFactory.createCamera();
    Entity second = RenderFactory.createCamera();

    org.junit.jupiter.api.Assertions.assertNotSame(first, second);
    org.junit.jupiter.api.Assertions.assertNotSame(
        first.getComponent(CameraComponent.class), second.getComponent(CameraComponent.class));
  }
}
