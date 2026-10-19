package com.csse3200.game.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.player.ArrowWheelComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameplayCursorTest {
  private EntityService entityService;
  private Graphics graphics;
  private Cursor hiddenCursor;

  @BeforeEach
  void setUp() {
    entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
    graphics = mock(Graphics.class);
    hiddenCursor = mock(Cursor.class);
    when(graphics.newCursor(any(Pixmap.class), any(Integer.class), any(Integer.class)))
        .thenReturn(hiddenCursor);
    Gdx.graphics = graphics;
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
  void shouldSwapToTheHiddenCursorOnlyWhenVisibilityChanges() {
    GameplayCursor cursor = new GameplayCursor();
    Entity player = playerHolding(ItemType.STANDARD_ARROW);

    cursor.update(player, false);
    cursor.update(player, false);
    assertFalse(cursor.isVisible());
    verify(graphics, times(1)).setCursor(hiddenCursor);

    cursor.update(player, true);
    cursor.update(player, true);
    assertTrue(cursor.isVisible());
    verify(graphics, times(1)).setSystemCursor(SystemCursor.Arrow);
  }

  @Test
  void shouldNotTouchTheCursorWhileItShouldStayVisible() {
    GameplayCursor cursor = new GameplayCursor();
    cursor.update(null, true);
    verify(graphics, never()).setCursor(any());
    verify(graphics, never()).setSystemCursor(any());
  }

  @Test
  void shouldRestoreTheNormalCursorOnDispose() {
    GameplayCursor cursor = new GameplayCursor();
    cursor.update(playerHolding(ItemType.STANDARD_ARROW), false);

    cursor.dispose();

    assertTrue(cursor.isVisible());
    verify(graphics).setSystemCursor(SystemCursor.Arrow);
    verify(hiddenCursor).dispose();
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
