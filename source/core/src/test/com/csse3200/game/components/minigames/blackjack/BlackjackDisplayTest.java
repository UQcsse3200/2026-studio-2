package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class BlackjackDisplayTest {
  private Stage stage;

  @BeforeEach
  void setUp() {
    stage = new Stage(new StretchViewport(800, 600), mock(SpriteBatch.class));

    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    ResourceService resources = mock(ResourceService.class);
    Texture texture = mock(Texture.class);
    Sound sound = mock(Sound.class);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    when(resources.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
  }

  @AfterEach
  void tearDown() {
    stage.dispose();
  }

  @Test
  void shouldCreateBlackjackUi() {
    Blackjack blackjack = new Blackjack(100);
    InventoryComponent inventory = new InventoryComponent(100);

    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    Table table = field(display, "table", Table.class);
    TextField betField = field(display, "betField", TextField.class);

    assertNotNull(table);
    assertNotNull(betField);
    assertTrue(table.isVisible());
    assertEquals("", betField.getText());
    assertEquals("Enter bet", betField.getMessageText());
  }

  @Test
  void shouldShowPlayerGold() {
    Blackjack blackjack = new Blackjack(100);
    InventoryComponent inventory = new InventoryComponent(75);

    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    assertEquals(75, inventory.getGold());
  }

  @Test
  void shouldShowZeroGoldState() {
    Blackjack blackjack = new Blackjack(100);
    InventoryComponent inventory = new InventoryComponent(0);

    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    assertEquals(0, inventory.getGold());
  }

  @Test
  void shouldCreateHiddenResultOverlay() {
    BlackjackDisplay display =
        new BlackjackDisplay(new Blackjack(100), new InventoryComponent(100));

    show(display);

    Table resultOverlay = field(display, "resultOverlay", Table.class);

    assertNotNull(resultOverlay);
    assertFalse(resultOverlay.isVisible());
  }

  @Test
  void shouldCreateWithoutInventory() {
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100));

    assertDoesNotThrow(() -> show(display));
  }

  @Test
  void shouldLeaveDrawingToStage() {
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100));

    assertDoesNotThrow(() -> display.draw(mock(SpriteBatch.class)));
  }

  @Test
  void shouldDrawAboveNormalUi() {
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100));

    assertEquals(2f, display.getZIndex());
  }

  private static Label label(BlackjackDisplay display, String name) {
    return field(display, name, Label.class);
  }

  @Test
  void shouldRejectBetWhenInventoryUnavailable() {
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100));
    show(display);

    TextField betField = field(display, "betField", TextField.class);
    Label resultLabel = label(display, "resultLabel");

    betField.setText("10");
    invokeTryStartRound(display);

    assertEquals("Player gold unavailable.", resultLabel.getText().toString());
  }

  @Test
  void shouldRejectBetWhenPlayerHasZeroGold() {
    InventoryComponent inventory = new InventoryComponent(0);
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100), inventory);
    show(display);

    TextField betField = field(display, "betField", TextField.class);
    Label resultLabel = label(display, "resultLabel");
    Label statusLabel = label(display, "statusLabel");

    betField.setText("10");
    invokeTryStartRound(display);

    assertEquals("Your balance is 0 - grind hard!", resultLabel.getText().toString());
    assertEquals("PLACE BET", statusLabel.getText().toString());
    assertEquals("", betField.getText());
    assertEquals(0, inventory.getGold());
  }

  @Test
  void shouldRejectEmptyBet() {
    InventoryComponent inventory = new InventoryComponent(100);
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100), inventory);
    show(display);

    Label resultLabel = label(display, "resultLabel");
    Label statusLabel = label(display, "statusLabel");

    invokeTryStartRound(display);

    assertEquals("Enter a bet amount.", resultLabel.getText().toString());
    assertEquals("PLACE BET", statusLabel.getText().toString());
    assertEquals(100, inventory.getGold());
  }

  @Test
  void shouldRejectZeroBet() {
    InventoryComponent inventory = new InventoryComponent(100);
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100), inventory);
    show(display);

    TextField betField = field(display, "betField", TextField.class);
    Label resultLabel = label(display, "resultLabel");

    betField.setText("0");
    invokeTryStartRound(display);

    assertEquals("Bet must be greater than 0.", resultLabel.getText().toString());
    assertEquals(100, inventory.getGold());
  }

  @Test
  void shouldRejectBetGreaterThanPlayerGold() {
    InventoryComponent inventory = new InventoryComponent(50);
    Blackjack blackjack = new Blackjack(100);
    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    TextField betField = field(display, "betField", TextField.class);
    Label resultLabel = label(display, "resultLabel");
    Label statusLabel = label(display, "statusLabel");

    betField.setText("51");
    invokeTryStartRound(display);

    assertEquals("Insufficient balance.", resultLabel.getText().toString());
    assertEquals("PLACE BET", statusLabel.getText().toString());
    assertEquals(50, inventory.getGold());
    assertFalse(blackjack.isRoundInProgress());
  }

  @Test
  void shouldDeductGoldForValidBet() {
    InventoryComponent inventory = new InventoryComponent(100);
    Blackjack blackjack = new Blackjack(100);
    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    TextField betField = field(display, "betField", TextField.class);
    betField.setText("10");

    invokeTryStartRound(display);

    assertEquals(10, blackjack.getBet());
    assertTrue(blackjack.isRoundInProgress() || blackjack.isRoundOver());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void acknowledgesTheRoundResultBeforeNotifyingTheScreen(boolean won) {
    Blackjack blackjack = mock(Blackjack.class);
    when(blackjack.getPlayerHand()).thenReturn(java.util.List.of());
    when(blackjack.getDealerHand()).thenReturn(java.util.List.of());
    when(blackjack.isRoundOver()).thenReturn(true);
    when(blackjack.isPlayerWinner()).thenReturn(won);
    when(blackjack.getResultMessage()).thenReturn(won ? "You win." : "Dealer wins.");
    BlackjackDisplay display = new BlackjackDisplay(blackjack, new InventoryComponent(100));
    Entity ui = show(display);
    java.util.List<Boolean> results = new java.util.ArrayList<>();
    ui.getEvents().<Boolean>addListener("roundCompleted", results::add);
    assertTrue(results.isEmpty());

    Table overlay = field(display, "resultOverlay", Table.class);
    ImageButton next = overlay.findActor("resultContinue");
    next.fire(new ChangeEvent());

    assertEquals(java.util.List.of(won), results);
    assertFalse(overlay.isVisible());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void soundButtonUpdatesItsLabelAndOnlyPlaysWhenEnabled(boolean hasCallback) {
    java.util.List<Boolean> soundStates = new java.util.ArrayList<>();
    BlackjackDisplay display =
        new BlackjackDisplay(
            new Blackjack(100), new InventoryComponent(100), hasCallback ? soundStates::add : null);
    show(display);
    TextButton soundButton = findSoundButton(field(display, "table", Table.class));
    assertNotNull(soundButton);

    try (MockedStatic<ButtonSound> sounds = mockStatic(ButtonSound.class)) {
      soundButton.fire(new ChangeEvent());
      assertEquals("SOUND: OFF", soundButton.getText().toString());
      sounds.verifyNoInteractions();

      soundButton.fire(new ChangeEvent());
      assertEquals("SOUND: ON", soundButton.getText().toString());
      sounds.verify(ButtonSound::playClick);
    }
    assertEquals(hasCallback ? java.util.List.of(false, true) : java.util.List.of(), soundStates);
  }

  @ParameterizedTest
  @CsvSource({
    "false, false, false, true, false, false",
    "true, false, false, false, true, false",
    "true, false, true, true, true, false",
    "true, true, false, true, true, true",
    "false, true, true, true, true, false",
    "false, true, false, true, false, true"
  })
  void roundStateControlsActionsAndResultVisibility(
      boolean inProgress,
      boolean roundOver,
      boolean dealing,
      boolean turnDisabled,
      boolean newRoundDisabled,
      boolean resultVisible) {
    Blackjack blackjack = mock(Blackjack.class);
    when(blackjack.getPlayerHand()).thenReturn(java.util.List.of());
    when(blackjack.getDealerHand()).thenReturn(java.util.List.of());
    BlackjackDisplay display = new BlackjackDisplay(blackjack, new InventoryComponent(100));
    show(display);
    when(blackjack.isRoundInProgress()).thenReturn(inProgress);
    when(blackjack.isRoundOver()).thenReturn(roundOver);
    when(blackjack.getResultMessage()).thenReturn("Push: same total.");
    setField(display, "dealInProgress", dealing);

    invokeUpdateResultOverlay(display);

    assertButtonState(display, "hitButton", turnDisabled);
    assertButtonState(display, "standButton", turnDisabled);
    assertButtonState(display, "newRoundButton", newRoundDisabled);
    assertButtonState(display, "backButton", false);
    Table overlay = field(display, "resultOverlay", Table.class);
    assertEquals(resultVisible, overlay.isVisible());
    assertEquals(resultVisible ? Touchable.enabled : Touchable.disabled, overlay.getTouchable());
    if (resultVisible) {
      Label title = overlay.findActor("resultTitle");
      Label message = overlay.findActor("resultMessage");
      assertEquals("PUSH!", title.getText().toString());
      assertEquals("Push: same total.", message.getText().toString());
      int actionCount = overlay.getActions().size;
      invokeUpdateResultOverlay(display);
      assertEquals(actionCount, overlay.getActions().size, "Refresh must not restart the fade");
    }
  }

  @Test
  void resultRefreshToleratesUninitialisedUiAndMissingButtons() {
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100));
    assertDoesNotThrow(() -> invokeUpdateResultOverlay(display));
    show(display);
    setField(display, "hitButton", null);
    setField(display, "standButton", null);
    setField(display, "newRoundButton", null);
    setField(display, "backButton", null);

    assertDoesNotThrow(() -> invokeUpdateResultOverlay(display));
    assertFalse(field(display, "resultOverlay", Table.class).isVisible());
  }

  private static void assertButtonState(BlackjackDisplay display, String name, boolean disabled) {
    ImageButton button = field(display, name, ImageButton.class);
    assertEquals(disabled, button.isDisabled(), name);
    assertEquals(disabled ? Touchable.disabled : Touchable.enabled, button.getTouchable(), name);
  }

  private static TextButton findSoundButton(Group group) {
    for (Actor actor : group.getChildren()) {
      if (actor instanceof TextButton button && button.getText().toString().startsWith("SOUND:")) {
        return button;
      }
      if (actor instanceof Group child) {
        TextButton button = findSoundButton(child);
        if (button != null) {
          return button;
        }
      }
    }
    return null;
  }

  private static void setField(Object object, String name, Object value) {
    try {
      Field field = object.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.set(object, value);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  private static void invokeUpdateResultOverlay(BlackjackDisplay display) {
    try {
      java.lang.reflect.Method method =
          BlackjackDisplay.class.getDeclaredMethod("updateResultOverlay");
      method.setAccessible(true);
      method.invoke(display);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  private static void invokeTryStartRound(BlackjackDisplay display) {
    try {
      java.lang.reflect.Method method = BlackjackDisplay.class.getDeclaredMethod("tryStartRound");
      method.setAccessible(true);
      method.invoke(display);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  private Entity show(BlackjackDisplay display) {
    Entity ui = new Entity().addComponent(display);
    ui.create();
    return ui;
  }

  private static <T> T field(Object object, String name, Class<T> type) {
    try {
      Field field = object.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return type.cast(field.get(object));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
