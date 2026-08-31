package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
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
  void shouldFireTowardClickedWorldPosition() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));

    AtomicInteger shots = new AtomicInteger();
    AtomicReference<Vector2> direction = new AtomicReference<>();
    player
        .getEvents()
        .addListener(
            "shoot",
            (Vector2 aimDirection) -> {
              shots.incrementAndGet();
              direction.set(aimDirection);
            });

    assertTrue(component.touchDown(4, 2, 0, Buttons.LEFT));
    assertEquals(1, shots.get());
    assertTrue(direction.get().epsilonEquals(new Vector2(9.5f, 4.5f)));

    assertTrue(component.touchUp(4, 2, 0, Buttons.LEFT));
    assertTrue(component.touchDown(4, 2, 0, Buttons.LEFT));
    assertEquals(2, shots.get());
  }

  @Test
  void shouldNotFireWithoutCamera() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger shots = new AtomicInteger();
    player.getEvents().addListener("shoot", (Vector2 ignored) -> shots.incrementAndGet());

    assertFalse(component.touchDown(4, 2, 0, Buttons.LEFT));
    assertEquals(0, shots.get());
  }

  @Test
  void shouldTriggerHorizontalMovementJumpAndSprintEvents() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicReference<Vector2> walkDirection = new AtomicReference<>();
    AtomicInteger walkStops = new AtomicInteger();
    AtomicInteger jumps = new AtomicInteger();
    AtomicInteger sprints = new AtomicInteger();
    AtomicInteger sprintStops = new AtomicInteger();

    player.getEvents().addListener("walk", (Vector2 direction) -> walkDirection.set(direction));
    player.getEvents().addListener("walkStop", walkStops::incrementAndGet);
    player.getEvents().addListener("jump", jumps::incrementAndGet);
    player.getEvents().addListener("sprint", sprints::incrementAndGet);
    player.getEvents().addListener("sprintStop", sprintStops::incrementAndGet);

    assertTrue(component.keyDown(Keys.A));
    assertTrue(walkDirection.get().epsilonEquals(new Vector2(-1f, 0f)));
    assertTrue(component.keyUp(Keys.A));
    assertEquals(1, walkStops.get());

    assertTrue(component.keyDown(Keys.RIGHT));
    assertTrue(walkDirection.get().epsilonEquals(new Vector2(1f, 0f)));
    assertTrue(component.keyUp(Keys.RIGHT));
    assertEquals(2, walkStops.get());

    assertTrue(component.keyDown(Keys.SPACE));
    assertEquals(1, jumps.get());
    assertTrue(component.keyDown(Keys.SHIFT_LEFT));
    assertEquals(1, sprints.get());
    assertTrue(component.keyUp(Keys.SHIFT_LEFT));
    assertEquals(1, sprintStops.get());
  }

  @Test
  void shouldShootSelectedArrowTowardClickedWorldPosition() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));
    AtomicReference<Vector2> direction = new AtomicReference<>();
    player.getEvents().addListener("shoot", (Vector2 aim) -> direction.set(aim));

    assertFalse(component.touchDown(4, 2, 0, Buttons.RIGHT));
    assertTrue(component.touchDown(4, 2, 0, Buttons.LEFT));
    assertTrue(direction.get().epsilonEquals(new Vector2(9.5f, 4.5f)));
  }
}
