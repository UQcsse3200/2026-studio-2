package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Right-click aiming while the pointer is hidden starts in front of the player. */
@ExtendWith(GameExtension.class)
class KeyboardPlayerAimStartTest {
  private static final int SCREEN_HEIGHT = 720;
  // The fake camera draws 10 screen pixels per world unit, with world (0, 0) at the bottom left.
  private static final float PIXELS_PER_UNIT = 10f;

  private Input input;

  @BeforeEach
  void setUp() {
    input = mock(Input.class);
    Gdx.input = input;
    Graphics graphics = mock(Graphics.class);
    when(graphics.getWidth()).thenReturn(1280);
    when(graphics.getHeight()).thenReturn(SCREEN_HEIGHT);
    Gdx.graphics = graphics;
    ServiceLocator.registerEntityService(mock(EntityService.class));
  }

  @Test
  void shouldAimAheadAtHeadHeightWhenRightClickingWithThePointerHidden() {
    when(input.isCursorCatched()).thenReturn(true);
    Entity player = playerAt(4f, 6f);
    AtomicReference<Vector2> aim = recordShots(player);

    // The click position is wherever the hidden pointer drifted to, which the player can't see.
    player.getComponent(KeyboardPlayerInputComponent.class).touchDown(1200, 20, 0, Buttons.RIGHT);

    assertNotNull(aim.get());
    assertEquals(KeyboardPlayerInputComponent.AIM_START_DISTANCE, aim.get().x, 0.2f);
    // Head height sits above the centre (half the player's height) by the rest of the fraction.
    float headAboveCentre =
        (KeyboardPlayerInputComponent.AIM_START_HEIGHT - 0.5f) * player.getScale().y;
    assertEquals(headAboveCentre, aim.get().y, 0.2f);
  }

  @Test
  void shouldAimTheWayThePlayerIsFacing() {
    when(input.isCursorCatched()).thenReturn(true);
    Entity player = playerAt(4f, 6f);
    PlayerActions actions = new PlayerActions();
    player.addComponent(actions);
    actions.walk(new Vector2(-1f, 0f));
    AtomicReference<Vector2> aim = recordShots(player);

    player.getComponent(KeyboardPlayerInputComponent.class).touchDown(1200, 20, 0, Buttons.RIGHT);

    assertNotNull(aim.get());
    assertEquals(-KeyboardPlayerInputComponent.AIM_START_DISTANCE, aim.get().x, 0.2f);
  }

  @Test
  void shouldRememberWhereThePointerShouldReappearOnce() {
    when(input.isCursorCatched()).thenReturn(true);
    Entity player = playerAt(4f, 6f);
    KeyboardPlayerInputComponent component =
        player.getComponent(KeyboardPlayerInputComponent.class);

    component.touchDown(1200, 20, 0, Buttons.RIGHT);

    GridPoint2 expected = component.getAimStartScreenPosition();
    assertEquals(expected, component.takePendingPointerPosition());
    assertNull(component.takePendingPointerPosition());
  }

  @Test
  void shouldAimAtTheClickWhenThePointerIsVisible() {
    when(input.isCursorCatched()).thenReturn(false);
    Entity player = playerAt(4f, 6f);
    AtomicReference<Vector2> aim = recordShots(player);
    KeyboardPlayerInputComponent component =
        player.getComponent(KeyboardPlayerInputComponent.class);
    Vector2 centre = player.getCenterPosition();

    // Click 5 world units above the player's centre.
    component.touchDown(
        Math.round(centre.x * PIXELS_PER_UNIT),
        Math.round(SCREEN_HEIGHT - (centre.y + 5f) * PIXELS_PER_UNIT),
        0,
        Buttons.RIGHT);

    assertEquals(0f, aim.get().x, 0.2f);
    assertEquals(5f, aim.get().y, 0.2f);
    assertNull(component.takePendingPointerPosition());
  }

  private static Entity playerAt(float x, float y) {
    Camera camera = mock(Camera.class);
    when(camera.project(any(Vector3.class)))
        .thenAnswer(
            call -> {
              Vector3 v = call.getArgument(0);
              return v.set(v.x * PIXELS_PER_UNIT, v.y * PIXELS_PER_UNIT, 0f);
            });
    when(camera.unproject(any(Vector3.class)))
        .thenAnswer(
            call -> {
              Vector3 v = call.getArgument(0);
              return v.set(v.x / PIXELS_PER_UNIT, (SCREEN_HEIGHT - v.y) / PIXELS_PER_UNIT, 0f);
            });

    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    player.setPosition(x, y);
    component.setCameraComponent(new CameraComponent(camera));
    return player;
  }

  private static AtomicReference<Vector2> recordShots(Entity player) {
    AtomicReference<Vector2> aim = new AtomicReference<>();
    player.getEvents().addListener("shoot", (Vector2 direction) -> aim.set(direction.cpy()));
    return aim;
  }
}
