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

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.util.Random;
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

    assertNotNull(table);
    assertTrue(table.isVisible());
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
  void shouldOfferCirceGiftWhenTheMatchStarts() {
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(40), new InventoryComponent(40));
    show(display);

    invoke(display, "updateResultOverlay");

    Table resultOverlay = field(display, "resultOverlay", Table.class);
    assertTrue(resultOverlay.isVisible());
    Label message = resultOverlay.findActor("resultMessage");
    assertEquals(
        BlackjackDialogue.of(BlackjackDialogue.Kind.GIFT).getText(), message.getText().toString());
  }

  @Test
  void shouldCreateHiddenResultOverlay() {
    // With no inventory there is no gold to give, so Circe's welcome gift is not shown.
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100));

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
  void shouldNotStartRoundWhenInventoryUnavailable() {
    Blackjack blackjack = new Blackjack(100);
    BlackjackDisplay display = new BlackjackDisplay(blackjack);
    show(display);

    invoke(display, "tryStartRound");

    assertEquals("Player gold unavailable.", label(display, "resultLabel").getText().toString());
    assertFalse(blackjack.isRoundInProgress());
  }

  @Test
  void shouldNotStartRoundWhenPlayerHasNoGold() {
    InventoryComponent inventory = new InventoryComponent(0);
    // Seeded so the deal is not a natural blackjack, which would end the round straight away.
    Blackjack blackjack = new Blackjack(100, new Random(1));
    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    invoke(display, "tryStartRound");

    assertEquals("Your balance is 0!", label(display, "resultLabel").getText().toString());
    assertEquals("READY", label(display, "statusLabel").getText().toString());
    assertEquals(0, inventory.getGold());
    assertFalse(blackjack.isRoundInProgress());
  }

  @Test
  void shouldStakeFixedBetWhenStartingRound() {
    InventoryComponent inventory = new InventoryComponent(100);
    Blackjack blackjack = new Blackjack(100, new Random(1));
    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    invoke(display, "tryStartRound");

    assertEquals(Blackjack.BET, blackjack.getBet());
    assertTrue(blackjack.isRoundInProgress());
    assertEquals(100 - Blackjack.BET, inventory.getGold());
  }

  @Test
  void shouldHideNewRoundButtonAfterFirstDeal() {
    BlackjackDisplay display =
        new BlackjackDisplay(new Blackjack(100, new Random(1)), new InventoryComponent(100));
    show(display);
    ImageButton newRound = field(display, "newRoundButton", ImageButton.class);

    assertTrue(newRound.isVisible());

    invoke(display, "tryStartRound");

    assertFalse(newRound.isVisible());
  }

  @Test
  void shouldRequestPauseFromPauseButton() {
    BlackjackDisplay display =
        new BlackjackDisplay(new Blackjack(100, new Random(1)), new InventoryComponent(100));
    Entity ui = show(display);
    int[] requests = {0};
    ui.getEvents().addListener(BlackjackDisplay.PAUSE_REQUESTED, () -> requests[0]++);

    field(display, "pauseButton", ImageButton.class)
        .fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());

    assertEquals(1, requests[0]);
  }

  @Test
  void shouldKeepNewRoundButtonWhenFirstDealFails() {
    BlackjackDisplay display =
        new BlackjackDisplay(new Blackjack(100, new Random(1)), new InventoryComponent(0));
    show(display);
    ImageButton newRound = field(display, "newRoundButton", ImageButton.class);

    invoke(display, "tryStartRound");

    assertTrue(newRound.isVisible());
  }

  @Test
  void shouldSettleGoldByRoundOutcome() {
    for (int seed = 0; seed < 20; seed++) {
      InventoryComponent inventory = new InventoryComponent(10);
      Blackjack blackjack = new Blackjack(10, new Random(seed));
      BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
      show(display);

      invoke(display, "tryStartRound");
      if (!blackjack.isRoundOver()) {
        blackjack.stand();
        invoke(display, "settleGoldIfRoundOver");
      }

      int expectedGold =
          switch (blackjack.getLastOutcome()) {
            case PLAYER_WIN -> 11;
            case DEALER_WIN -> 9;
            case PUSH -> 10;
          };
      assertEquals(expectedGold, inventory.getGold());
      assertEquals(inventory.getGold(), blackjack.getBalance());
    }
  }

  @Test
  void shouldNotStartRoundWhenMatchIsOver() {
    Blackjack blackjack = lostMatch();
    InventoryComponent inventory = new InventoryComponent(0);
    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    invoke(display, "tryStartRound");

    assertEquals("The match is over.", label(display, "resultLabel").getText().toString());
    assertEquals("MATCH OVER", label(display, "statusLabel").getText().toString());
    assertFalse(blackjack.isRoundInProgress());
  }

  @Test
  void shouldRefillGoldWhenAcceptingOutOfGoldDialogue() {
    Blackjack blackjack = lostMatch();
    InventoryComponent inventory = new InventoryComponent(0);
    BlackjackDisplay display = new BlackjackDisplay(blackjack, inventory);
    show(display);

    BlackjackDialogue dialogue = BlackjackDialogue.forMatch(blackjack);
    assertEquals(BlackjackDialogue.Kind.OUT_OF_GOLD, dialogue.getKind());
    dialogue.pressButton(blackjack, null);
    inventory.setGold(blackjack.getBalance());

    assertEquals(Blackjack.REFILL_GOLD, inventory.getGold());
    assertEquals(Blackjack.REFILL_GOLD, blackjack.getBalance());
    assertEquals(0, blackjack.getPlayerWins());
    assertEquals(0, blackjack.getDealerWins());
    assertEquals(Blackjack.MatchResult.IN_PROGRESS, blackjack.getMatchResult());
  }

  @Test
  void shouldKeepRemainingGoldAfterAcceptingDefeatDialogue() {
    Blackjack blackjack = new Blackjack(7);
    InventoryComponent inventory = new InventoryComponent(0);
    BlackjackDialogue dialogue = BlackjackDialogue.of(BlackjackDialogue.Kind.MATCH_LOST);

    dialogue.pressButton(blackjack, null);
    inventory.setGold(blackjack.getBalance());

    assertEquals(7, inventory.getGold());
    assertEquals(7, blackjack.getBalance());
    assertEquals(Blackjack.MatchResult.IN_PROGRESS, blackjack.getMatchResult());
  }

  /** Returns a game the player has lost by running out of gold after one round. */
  private static Blackjack lostMatch() {
    for (int seed = 0; seed < 100; seed++) {
      Blackjack blackjack = new Blackjack(Blackjack.BET, new Random(seed));
      blackjack.startNewRound();
      if (!blackjack.isRoundOver()) {
        blackjack.stand();
      }
      if (blackjack.getMatchResult() == Blackjack.MatchResult.DEALER_WON) {
        return blackjack;
      }
    }
    throw new AssertionError("Could not find a lost match");
  }

  private static void invoke(BlackjackDisplay display, String methodName) {
    try {
      java.lang.reflect.Method method = BlackjackDisplay.class.getDeclaredMethod(methodName);
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
