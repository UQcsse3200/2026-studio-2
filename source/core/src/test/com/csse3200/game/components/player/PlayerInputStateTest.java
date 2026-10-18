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
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
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
    ServiceLocator.registerEntityService(mock(EntityService.class));
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
    List<Vector2> shots = new ArrayList<>();
    player.getEvents().<Vector2>addListener("shoot", shots::add);
    player.setPosition(-0.5f, -0.5f);
    assertFalse(input.touchDown(0, 0, 0, Buttons.RIGHT));
    input.setCameraComponent(null);
    assertFalse(input.touchDown(10, 10, 0, Buttons.RIGHT));
    assertTrue(shots.isEmpty());
  }

  @Test
  void shouldNotConsumeUnmappedKeysOrMouseButtons() {
    assertFalse(input.keyDown(Keys.F12));
    assertFalse(input.keyDown(Keys.NUM_0));
    assertFalse(input.keyUp(Keys.F12));
    assertFalse(input.touchDown(1, 1, 0, Buttons.MIDDLE));
    assertFalse(input.touchUp(1, 1, 0, Buttons.MIDDLE));
    assertTrue(events.isEmpty());
    assertTrue(directions.isEmpty());
  }

  @Test
  void shouldKeepWalkingUntilBothLeftAliasesAreReleased() {
    input.keyDown(Keys.A);
    input.keyDown(Keys.LEFT);
    events.clear();
    directions.clear();

    input.keyUp(Keys.A);

    assertTrue(events.isEmpty());
    assertEquals(List.of(new Vector2(-1f, 0f)), directions);
    input.keyUp(Keys.LEFT);
    assertEquals(List.of("walkStop"), events);
  }

  @Test
  void shouldKeepSprintingUntilBothShiftKeysAreReleased() {
    input.keyDown(Keys.SHIFT_LEFT);
    input.keyDown(Keys.SHIFT_RIGHT);
    events.clear();

    input.keyUp(Keys.SHIFT_LEFT);

    assertFalse(events.contains("sprintStop"));
    input.keyUp(Keys.SHIFT_RIGHT);
    assertEquals("sprintStop", events.getLast());
  }

  @Test
  void shouldRejectRightClickWhilePausedWithoutHoldingTheMouse() {
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);
    List<Vector2> shots = new ArrayList<>();
    player.getEvents().<Vector2>addListener("shoot", shots::add);

    assertFalse(input.touchDown(10, 10, 0, Buttons.RIGHT));

    assertFalse(input.isRightMouseHeld());
    assertTrue(shots.isEmpty());
  }

  @Test
  void shouldCancelReleaseOfAChargeWhilePaused() {
    input.touchDown(10, 10, 0, Buttons.RIGHT);
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);

    input.touchUp(10, 10, 0, Buttons.RIGHT);

    assertFalse(input.isRightMouseHeld());
    assertEquals(List.of("chargeCancel"), events);
  }

  @Test
  void shouldNotReplayKeysReleasedDuringDeathAfterRevival() {
    input.keyDown(Keys.D);
    input.keyDown(Keys.SHIFT_LEFT);
    player.getEvents().trigger("death");
    input.keyUp(Keys.D);
    input.keyUp(Keys.SHIFT_LEFT);
    player.getEvents().trigger("revive");
    events.clear();
    directions.clear();

    input.unpause();

    assertTrue(directions.isEmpty());
    assertEquals(List.of("walkStop", "sprintStop"), events);
  }

  @Test
  void shouldConsumeNoPotionWhenEIsPressedWhilePaused() {
    var inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.HEALTH_POTION, 1);
    var stats = new CombatStatsComponent(8, 10, 1);
    var use = new ItemUseComponent();
    input = new KeyboardPlayerInputComponent();
    player =
        new Entity()
            .addComponent(input)
            .addComponent(inventory)
            .addComponent(stats)
            .addComponent(use);
    player.create();
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);

    input.keyDown(Keys.E);

    assertEquals(8, stats.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
    input.keyUp(Keys.E);
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(false);
    input.keyDown(Keys.E);
    assertEquals(10, stats.getHealth());
    assertEquals(0, inventory.getItemCount(ItemType.HEALTH_POTION));
  }

  @Test
  void shouldKeepRightAliasHeldDespiteRepeatedKeyDownAndRelease() {
    input.keyDown(Keys.D);
    input.keyDown(Keys.D);
    input.keyDown(Keys.RIGHT);
    input.keyUp(Keys.RIGHT);
    assertEquals(new Vector2(1f, 0f), directions.getLast());
    input.keyUp(Keys.D);
    assertEquals(List.of("walkStop"), events);
  }

  @Test
  void shouldStopGrappleClimbWhenKeyIsReleasedDuringPause() {
    List<String> climbing = new ArrayList<>();
    player.getEvents().addListener("grappleClimbStart", () -> climbing.add("start"));
    player.getEvents().addListener("grappleClimbStop", () -> climbing.add("stop"));
    input.keyDown(Keys.W);
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);
    input.keyUp(Keys.W);
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(false);
    input.unpause();
    assertEquals(List.of("start", "stop"), climbing);
  }

  @Test
  void shouldApplyReleasedKeysWhenPauseClosesAfterTheEscapeCallback() {
    input.keyDown(Keys.D);
    input.keyDown(Keys.SHIFT_LEFT);
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);
    input.keyUp(Keys.D);
    input.keyUp(Keys.SHIFT_LEFT);
    input.keyDown(Keys.ESCAPE);
    events.clear();
    directions.clear();
    // Screens close the pause overlay after rendering, after the input callback has returned.
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(false);
    input.earlyUpdate();
    assertTrue(directions.isEmpty());
    assertEquals(List.of("walkStop", "sprintStop"), events);
    events.clear();
    input.earlyUpdate();
    assertTrue(events.isEmpty(), "synchronize only once after resume");
  }

  @Test
  void shouldApplyMovementPressedWhilePausedOnTheFirstResumedFrame() {
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);
    input.keyDown(Keys.D);
    assertTrue(directions.isEmpty());
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(false);
    input.earlyUpdate();
    assertEquals(List.of(new Vector2(1f, 0f)), directions);
  }

  @Test
  void shouldDispatchMapBackpackInteractionAndInventoryNavigation() {
    List<String> actions = new ArrayList<>();
    for (String event : new String[] {"toggleMap", "toggleBackpack", "interact"}) {
      player.getEvents().addListener(event, () -> actions.add(event));
    }
    List<Integer> switches = new ArrayList<>();
    player.getEvents().<Integer>addListener("switchItem", switches::add);
    assertTrue(input.keyDown(Keys.M));
    assertTrue(input.keyDown(Keys.B));
    assertTrue(input.keyDown(Keys.F));
    assertTrue(input.keyDown(Keys.PERIOD));
    assertTrue(input.keyDown(Keys.COMMA));
    assertEquals(List.of("toggleMap", "toggleBackpack", "interact"), actions);
    assertEquals(List.of(1, -1), switches);
  }

  @Test
  void shouldConsumeEveryPausedActionWithoutDispatchingItAndAllowMap() {
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);
    List<String> actions = new ArrayList<>();
    for (String event :
        new String[] {
          "toggleMap",
          "toggleBackpack",
          "interact",
          "dropItem",
          "deleteItem",
          "openArrowWheel",
          "instrumentStart"
        }) {
      player.getEvents().addListener(event, () -> actions.add(event));
    }
    List<Integer> slots = new ArrayList<>();
    player.getEvents().<Integer>addListener("selectQuickSlot", slots::add);
    player.getEvents().<Integer>addListener("switchItem", slots::add);
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
          Keys.NUM_9,
          Keys.SPACE,
          Keys.E,
          Keys.F,
          Keys.B,
          Keys.R,
          Keys.FORWARD_DEL,
          Keys.PERIOD,
          Keys.COMMA,
          Keys.TAB,
          Keys.Q
        }) {
      assertTrue(input.keyDown(key));
    }
    assertFalse(input.keyDown(Keys.F12));
    assertTrue(actions.isEmpty());
    assertTrue(events.isEmpty());
    assertTrue(slots.isEmpty());
    assertTrue(input.keyDown(Keys.M));
    assertEquals(List.of("toggleMap"), actions);
  }

  @Test
  void shouldKeepPauseUntoggledWhenSettingsOwnEscape() {
    when(ServiceLocator.getEntityService().getSettingsOpen()).thenReturn(true);
    List<String> toggles = new ArrayList<>();
    player.getEvents().addListener("togglePause", () -> toggles.add("pause"));
    assertTrue(input.keyDown(Keys.ESCAPE));
    assertTrue(toggles.isEmpty());
  }

  @Test
  void shouldReplayHeldClimbAndDescentOnlyAfterPauseEnds() {
    List<String> grapple = new ArrayList<>();
    player.getEvents().addListener("grappleClimbStart", () -> grapple.add("climb"));
    player.getEvents().addListener("grappleDescendStart", () -> grapple.add("descend"));
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(true);
    input.keyDown(Keys.W);
    input.keyDown(Keys.S);
    input.earlyUpdate();
    assertTrue(grapple.isEmpty());
    when(ServiceLocator.getEntityService().getPaused()).thenReturn(false);
    input.earlyUpdate();
    assertEquals(List.of("climb", "descend"), grapple);
  }

  @Test
  void shouldNotResumeMovementWhileDead() {
    input.keyDown(Keys.D);
    player.getEvents().trigger("death");
    directions.clear();
    events.clear();
    input.unpause();
    input.earlyUpdate();
    assertTrue(directions.isEmpty());
    assertTrue(events.isEmpty());
  }

  @Test
  void shouldCancelChargeWhenInputDeviceDisappearsBeforeShopCloses() {
    input.touchDown(10, 10, 0, Buttons.RIGHT);
    Gdx.input = null;
    player.getEvents().trigger("closeShop");
    assertFalse(input.isRightMouseHeld());
    assertEquals(List.of("chargeCancel"), events);
  }

  @Test
  void shouldBlockItemUseAndShootingWhileArrowWheelIsOpen() {
    ArrowWheelComponent wheel = new ArrowWheelComponent();
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.HEALTH_POTION, 1);
    CombatStatsComponent stats = new CombatStatsComponent(5, 10, 1);
    ItemUseComponent use = new ItemUseComponent();
    input = new KeyboardPlayerInputComponent();
    player =
        new Entity()
            .addComponent(input)
            .addComponent(wheel)
            .addComponent(inventory)
            .addComponent(stats)
            .addComponent(use);
    player.create();
    input.keyDown(Keys.TAB);
    assertFalse(input.touchDown(10, 10, 0, Buttons.RIGHT));
    input.keyDown(Keys.E);
    assertEquals(5, stats.getHealth());
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
    input.keyUp(Keys.TAB);
    input.keyDown(Keys.E);
    assertEquals(10, stats.getHealth());
  }

  @Test
  void shouldUseConfiguredCameraToReportCurrentMouseAim() {
    when(Gdx.input.getX()).thenReturn(10);
    when(Gdx.input.getY()).thenReturn(20);
    assertNotNull(input.getCameraComponent());
    assertEquals(new Vector2(9.5f, 19.5f), input.getMouseAimDirection());
  }

  @Test
  void shouldReportPointerOffsetEvenWithoutAnArrowWheel() {
    var originalGraphics = Gdx.graphics;
    try {
      Gdx.graphics = mock(com.badlogic.gdx.Graphics.class);
      when(Gdx.graphics.getWidth()).thenReturn(800);
      when(Gdx.graphics.getHeight()).thenReturn(600);
      List<Vector2> offsets = new ArrayList<>();
      player.getEvents().<Vector2>addListener("arrowWheelPointerMoved", offsets::add);
      assertFalse(input.mouseMoved(450, 200));
      assertEquals(List.of(new Vector2(50, 100)), offsets);
    } finally {
      Gdx.graphics = originalGraphics;
    }
  }
}
