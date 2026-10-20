package com.csse3200.game.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.player.ArrowWheelComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameplayCursorTest {
  private EntityService entityService;
  private Input input;

  @BeforeEach
  void setUp() {
    entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    input = mock(Input.class);
    Gdx.input = input;
    Graphics graphics = mock(Graphics.class);
    when(graphics.getWidth()).thenReturn(1280);
    when(graphics.getHeight()).thenReturn(720);
    when(graphics.newCursor(any(Pixmap.class), anyInt(), anyInt())).thenReturn(mock(Cursor.class));
    Gdx.graphics = graphics;
    ThemedCursor.dispose();
  }

  @AfterEach
  void tearDown() {
    ThemedCursor.dispose();
  }

  @Test
  void shouldHideDuringNormalPlay() {
    assertFalse(GameplayCursor.shouldShow(playerHolding(ItemType.STANDARD_ARROW), false));
  }

  @Test
  void shouldHideWithoutAPlayer() {
    assertFalse(GameplayCursor.shouldShow(null, false));
  }

  @Test
  void shouldShowWhileAScreenMenuIsOpen() {
    assertTrue(GameplayCursor.shouldShow(null, true));
  }

  @Test
  void shouldShowWhileTheGameIsPaused() {
    when(entityService.getPaused()).thenReturn(true);
    assertTrue(GameplayCursor.shouldShow(null, false));
  }

  @Test
  void shouldShowWhileSettingsAreOpen() {
    when(entityService.getSettingsOpen()).thenReturn(true);
    assertTrue(GameplayCursor.shouldShow(null, false));
  }

  @Test
  void shouldShowWhileAimingTheBow() {
    Entity player = playerHolding(ItemType.STANDARD_ARROW);
    holdRightClick(player);
    assertTrue(GameplayCursor.shouldShow(player, false));
  }

  @Test
  void shouldShowWhileAimingTheGrapple() {
    Entity player = playerHolding(ItemType.ROPE_ARROW);
    holdRightClick(player);
    assertTrue(GameplayCursor.shouldShow(player, false));
  }

  @Test
  void shouldStayHiddenWhenRightClickingWithAnItemThatDoesNotAim() {
    Entity player = playerHolding(ItemType.HEALTH_POTION);
    holdRightClick(player);
    assertFalse(GameplayCursor.shouldShow(player, false));
  }

  @Test
  void shouldHideAgainOnceRightClickIsReleased() {
    Entity player = playerHolding(ItemType.STANDARD_ARROW);
    holdRightClick(player);
    player.getComponent(KeyboardPlayerInputComponent.class).touchUp(0, 0, 0, Buttons.RIGHT);
    assertFalse(GameplayCursor.shouldShow(player, false));
  }

  @Test
  void shouldShowWhileTheArrowWheelIsOpen() {
    Entity player = playerHolding(ItemType.STANDARD_ARROW);
    ArrowWheelComponent wheel = new ArrowWheelComponent();
    player.addComponent(wheel);
    wheel.create();
    player.getEvents().trigger("bowEquipped", true);
    player.getEvents().trigger("openArrowWheel");

    assertTrue(wheel.isOpen());
    assertTrue(GameplayCursor.shouldShow(player, false));
  }

  @Test
  void shouldCapturePointerOnlyWhenVisibilityChanges() {
    GameplayCursor cursor = new GameplayCursor();
    Entity player = playerHolding(ItemType.STANDARD_ARROW);

    cursor.update(player, false);
    cursor.update(player, false);
    assertFalse(cursor.isVisible());
    verify(input, times(1)).setCursorCatched(true);

    cursor.update(player, true);
    cursor.update(player, true);
    assertTrue(cursor.isVisible());
    verify(input, times(1)).setCursorCatched(false);
  }

  @Test
  void shouldNotTouchThePointerWhileItShouldStayVisible() {
    GameplayCursor cursor = new GameplayCursor();
    cursor.update(null, true);
    verify(input, never()).setCursorCatched(anyBoolean());
    verify(input, never()).setCursorPosition(anyInt(), anyInt());
  }

  @Test
  void shouldReleaseThePointerWhereTheMouseIsInsideTheWindow() {
    GameplayCursor cursor = new GameplayCursor();
    cursor.update(playerHolding(ItemType.STANDARD_ARROW), false);
    when(input.getX()).thenReturn(400);
    when(input.getY()).thenReturn(300);

    cursor.update(null, true);

    verify(input).setCursorPosition(400, 300);
  }

  @Test
  void shouldClampTheReleasedPointerToTheWindow() {
    GameplayCursor cursor = new GameplayCursor();
    cursor.update(playerHolding(ItemType.STANDARD_ARROW), false);
    // While captured the tracked position can run past the window edges.
    when(input.getX()).thenReturn(5000);
    when(input.getY()).thenReturn(-200);

    cursor.update(null, true);

    verify(input).setCursorPosition(1279, 0);
  }

  @Test
  void shouldReleaseThePointerWhereTheAimStartedWhenAimingFromHidden() {
    GameplayCursor cursor = new GameplayCursor();
    Entity player = playerHolding(ItemType.STANDARD_ARROW);
    Camera camera = mock(Camera.class);
    // Every world point projects to (640, 300), i.e. input position (640, 420) on a 720px screen.
    when(camera.project(any(Vector3.class)))
        .thenAnswer(call -> call.<Vector3>getArgument(0).set(640f, 300f, 0f));
    when(camera.unproject(any(Vector3.class)))
        .thenAnswer(call -> call.<Vector3>getArgument(0).set(5f, 0f, 0f));
    KeyboardPlayerInputComponent input = player.getComponent(KeyboardPlayerInputComponent.class);
    input.setCameraComponent(new CameraComponent(camera));
    cursor.update(player, false);
    when(this.input.isCursorCatched()).thenReturn(true);
    // The stale tracked position, which should be ignored.
    when(this.input.getX()).thenReturn(5000);
    when(this.input.getY()).thenReturn(-200);

    input.touchDown(5000, -200, 0, Buttons.RIGHT);
    cursor.update(player, false);

    assertTrue(cursor.isVisible());
    verify(this.input).setCursorPosition(640, 420);
  }

  @Test
  void shouldShowTheCrosshairWhileAiming() {
    GameplayCursor cursor = new GameplayCursor();
    Entity player = playerHolding(ItemType.STANDARD_ARROW);
    holdRightClick(player);

    cursor.update(player, false);

    assertEquals(ThemedCursor.Style.CROSSHAIR, ThemedCursor.getCurrent());
  }

  @Test
  void shouldShowTheArrowInMenus() {
    GameplayCursor cursor = new GameplayCursor();

    cursor.update(null, true);

    assertEquals(ThemedCursor.Style.ARROW, ThemedCursor.getCurrent());
  }

  @Test
  void shouldSwitchToTheArrowIfAMenuOpensWhileAiming() {
    GameplayCursor cursor = new GameplayCursor();
    Entity player = playerHolding(ItemType.STANDARD_ARROW);
    holdRightClick(player);
    cursor.update(player, false);

    when(entityService.getPaused()).thenReturn(true);
    cursor.update(player, false);

    assertEquals(ThemedCursor.Style.ARROW, ThemedCursor.getCurrent());
  }

  @Test
  void shouldPutTheArrowBackWhenLeavingTheScreen() {
    GameplayCursor cursor = new GameplayCursor();
    Entity player = playerHolding(ItemType.STANDARD_ARROW);
    holdRightClick(player);
    cursor.update(player, false);

    cursor.restore();

    assertEquals(ThemedCursor.Style.ARROW, ThemedCursor.getCurrent());
  }

  @Test
  void shouldReleaseThePointerOnDispose() {
    GameplayCursor cursor = new GameplayCursor();
    cursor.update(playerHolding(ItemType.STANDARD_ARROW), false);

    cursor.dispose();

    assertTrue(cursor.isVisible());
    verify(input).setCursorCatched(false);
  }

  private static Entity playerHolding(ItemType item) {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(item, 1);
    inventory.selectSlot(0);
    return new Entity().addComponent(inventory).addComponent(new KeyboardPlayerInputComponent());
  }

  private static void holdRightClick(Entity player) {
    KeyboardPlayerInputComponent input = player.getComponent(KeyboardPlayerInputComponent.class);
    input.touchDown(0, 0, 0, Buttons.RIGHT);
    assertTrue(input.isRightMouseHeld());
  }
}
