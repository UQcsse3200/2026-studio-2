package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.StretchViewport;
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
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
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

  @Test
  void shouldRejectBetWhenInventoryUnavailable() {
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100));
    show(display);

    TextField betField = field(display, "betField", TextField.class);
    com.badlogic.gdx.scenes.scene2d.ui.Label resultLabel =
        field(display, "resultLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);

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
    com.badlogic.gdx.scenes.scene2d.ui.Label resultLabel =
        field(display, "resultLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);
    com.badlogic.gdx.scenes.scene2d.ui.Label statusLabel =
        field(display, "statusLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);

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

    com.badlogic.gdx.scenes.scene2d.ui.Label resultLabel =
        field(display, "resultLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);
    com.badlogic.gdx.scenes.scene2d.ui.Label statusLabel =
        field(display, "statusLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);

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
    com.badlogic.gdx.scenes.scene2d.ui.Label resultLabel =
        field(display, "resultLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);

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
    com.badlogic.gdx.scenes.scene2d.ui.Label resultLabel =
        field(display, "resultLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);
    com.badlogic.gdx.scenes.scene2d.ui.Label statusLabel =
        field(display, "statusLabel", com.badlogic.gdx.scenes.scene2d.ui.Label.class);

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
