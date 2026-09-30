package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerInputStateTest {
  private Entity player;
  private KeyboardPlayerInputComponent input;
  private Input originalInput;
  private final List<String> events = new ArrayList<>();
  private final List<Vector2> directions = new ArrayList<>();

  @BeforeEach
  void setUp() {
    originalInput = Gdx.input;
    Gdx.input = mock(Input.class);
    ServiceLocator.registerInputService(mock(InputService.class));
    input = new KeyboardPlayerInputComponent();
    player = new Entity().addComponent(input);
    Camera camera = mock(Camera.class);
    when(camera.unproject(any(Vector3.class))).thenAnswer(call -> call.getArgument(0));
    input.setCameraComponent(new CameraComponent(camera));
    player.create();
    player.getEvents().<Vector2>addListener("walk", directions::add);
    for (String event :
        new String[] {
          "walkStop", "sprint", "sprintStop", "stopShoot", "chargeCancel", "stopMelee", "jump"
        }) {
      player.getEvents().addListener(event, () -> events.add(event));
    }
  }

  @AfterEach
  void tearDown() {
    Gdx.input = originalInput;
  }

  @Test
  void shouldCancelOpposingDirectionsAndResumeKeyStillHeld() {
    input.keyDown(Keys.A);
    input.keyDown(Keys.D);
    assertEquals(List.of("walkStop"), events);
    input.keyUp(Keys.A);
    assertEquals(List.of(new Vector2(-1f, 0f), new Vector2(1f, 0f)), directions);
    input.keyUp(Keys.D);
    assertEquals(List.of("walkStop", "walkStop"), events);
    assertEquals(new Vector2(-1f, 0f), directions.getFirst());
  }

  @Test
  void shouldForwardEveryNumberKeyToItsQuickSlot() {
    List<Integer> slots = new ArrayList<>();
    player.getEvents().<Integer>addListener("selectQuickSlot", slots::add);
    for (int key :
        new int[] {
          Keys.NUM_1,
          Keys.NUM_2,
          Keys.NUM_3,
          Keys.NUM_4,
          Keys.NUM_5,
          Keys.NUM_6,
          Keys.NUM_7,
          Keys.NUM_8,
          Keys.NUM_9
        }) {
      assertTrue(input.keyDown(key));
    }
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8), slots);
  }

  @Test
  void shouldLeaveMouseMovementUnconsumedWithoutGraphics() {
    var originalGraphics = Gdx.graphics;
    List<Vector2> pointerEvents = new ArrayList<>();
    player.getEvents().<Vector2>addListener("arrowWheelPointerMoved", pointerEvents::add);
    try {
      Gdx.graphics = null;
      assertFalse(input.mouseMoved(100, 200));
      assertTrue(pointerEvents.isEmpty());
    } finally {
      Gdx.graphics = originalGraphics;
    }
  }

  @Test
  void shouldReplayHeldMovementAndSprintOnResume() {
    input.keyDown(Keys.RIGHT);
    input.keyDown(Keys.SHIFT_RIGHT);
    directions.clear();
    events.clear();
    player.getEvents().trigger("togglePause");
    assertEquals(List.of(new Vector2(1f, 0f)), directions);
    assertEquals(List.of("sprint"), events);
  }

  @Test
  void shouldClearHeldMovementSprintAndChargeWhenOverlayRequestsRelease() {
    input.keyDown(Keys.D);
    input.keyDown(Keys.SHIFT_LEFT);
    input.touchDown(10, 10, 0, Buttons.RIGHT);
    assertTrue(input.isRightMouseHeld());
    events.clear();
    directions.clear();
    player.getEvents().trigger("releaseHeldGameplayInput");
    assertFalse(input.isRightMouseHeld());
    assertEquals(List.of("walkStop", "sprintStop", "chargeCancel"), events);
    events.clear();
    player.getEvents().trigger("togglePause");
    assertTrue(directions.isEmpty());
    assertEquals(List.of("walkStop", "sprintStop"), events);
  }

  @Test
  void shouldReleaseRightMouseNormallyWithoutCancellingCharge() {
    input.touchDown(10, 10, 0, Buttons.RIGHT);
    assertTrue(input.touchUp(10, 10, 0, Buttons.RIGHT));
    assertFalse(input.isRightMouseHeld());
    assertEquals(List.of("stopShoot"), events);
    assertTrue(input.touchUp(10, 10, 0, Buttons.LEFT));
    assertEquals(List.of("stopShoot", "stopMelee"), events);
  }

  @Test
  void shouldKeepChargeWhenMouseStillHeldOnShopClose() {
    input.touchDown(10, 10, 0, Buttons.RIGHT);
    when(Gdx.input.isButtonPressed(Buttons.RIGHT)).thenReturn(true);
    player.getEvents().trigger("closeShop");
    assertTrue(input.isRightMouseHeld());
    assertTrue(events.isEmpty());
    when(Gdx.input.isButtonPressed(Buttons.RIGHT)).thenReturn(false);
    player.getEvents().trigger("closeShop");
    assertFalse(input.isRightMouseHeld());
    assertEquals(List.of("chargeCancel"), events);
    player.getEvents().trigger("closeShop");
    assertEquals(1, events.size());
  }

  @Test
  void shouldIgnoreAllGameplayInputAfterDeath() {
    player.getEvents().trigger("death");
    assertFalse(input.keyDown(Keys.D));
    assertFalse(input.keyUp(Keys.D));
    assertFalse(input.keyDown(Keys.SPACE));
    assertFalse(input.touchDown(10, 10, 0, Buttons.LEFT));
    assertFalse(input.touchUp(10, 10, 0, Buttons.RIGHT));
    assertTrue(events.isEmpty());
    assertTrue(directions.isEmpty());
  }

  @Test
  void shouldRejectAimAtPlayerCentreAndMissingCamera() {
    List<Vector2> attacks = new ArrayList<>();
    player.getEvents().<Vector2>addListener("melee", attacks::add);
    player.setPosition(-0.5f, -0.5f);
    assertFalse(input.touchDown(0, 0, 0, Buttons.LEFT));
    input.setCameraComponent(null);
    assertFalse(input.touchDown(10, 10, 0, Buttons.LEFT));
    assertTrue(attacks.isEmpty());
  }

  @Test
  void shouldNotConsumeUnmappedKeysOrMouseButtons() {
    assertFalse(input.keyDown(Keys.F12));
    assertFalse(input.keyUp(Keys.F12));
    assertFalse(input.touchDown(1, 1, 0, Buttons.MIDDLE));
    assertFalse(input.touchUp(1, 1, 0, Buttons.MIDDLE));
    assertTrue(events.isEmpty());
    assertTrue(directions.isEmpty());
  }
}
