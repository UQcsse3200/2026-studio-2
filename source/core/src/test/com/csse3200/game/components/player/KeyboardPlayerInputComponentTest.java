package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class KeyboardPlayerInputComponentTest {
  private Input input;
  private Camera camera;

  @BeforeEach
  void setUp() {
    input = mock(Input.class);
    Gdx.input = input;
    camera = mock(Camera.class);
    when(camera.unproject(any(Vector3.class)))
        .thenAnswer(
            invocation -> {
              Vector3 position = invocation.getArgument(0);
              return position.set(10f, 5f, 0f);
            });
  }

  @Test
  void shouldFireOncePerEPressTowardCursor() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    player.setPosition(0f, 0f);
    Entity cameraEntity = new Entity().addComponent(new CameraComponent(camera));
    component.setCameraEntity(cameraEntity);

    AtomicInteger shots = new AtomicInteger();
    AtomicReference<Vector2> direction = new AtomicReference<>();
    player
        .getEvents()
        .addListener(
            "fireArrow",
            (Vector2 aimDirection) -> {
              shots.incrementAndGet();
              direction.set(aimDirection);
            });

    assertTrue(component.keyDown(Keys.E));
    assertTrue(component.keyDown(Keys.E));
    assertEquals(1, shots.get());
    assertTrue(direction.get().epsilonEquals(new Vector2(9.5f, 4.5f)));

    assertTrue(component.keyUp(Keys.E));
    assertTrue(component.keyDown(Keys.E));
    assertEquals(2, shots.get());
  }

  @Test
  void shouldNotFireWithoutCamera() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger shots = new AtomicInteger();
    player.getEvents().addListener("fireArrow", (Vector2 ignored) -> shots.incrementAndGet());

    assertTrue(component.keyDown(Keys.E));
    assertEquals(0, shots.get());
  }
}
