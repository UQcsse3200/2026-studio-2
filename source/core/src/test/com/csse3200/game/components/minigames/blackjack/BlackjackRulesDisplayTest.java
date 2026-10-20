package com.csse3200.game.components.minigames.blackjack;

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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BlackjackRulesDisplayTest {
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
    when(resources.getAsset(anyString(), eq(Sound.class))).thenReturn(mock(Sound.class));
    ServiceLocator.registerResourceService(resources);
  }

  @AfterEach
  void tearDown() {
    stage.dispose();
  }

  @Test
  void shouldStartWithRulesHidden() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    new Entity().addComponent(rules).create();

    assertFalse(rules.isOpen());
    assertFalse(field(rules, "rulesOverlay", Table.class).isVisible());
    assertNotNull(field(rules, "infoButton", ImageButton.class));
  }

  @Test
  void shouldOpenRulesFromInfoButton() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    Entity entity = new Entity().addComponent(rules);
    List<String> events = recordEvents(entity);
    entity.create();

    click(field(rules, "infoButton", ImageButton.class));

    assertTrue(rules.isOpen());
    assertTrue(field(rules, "rulesOverlay", Table.class).isVisible());
    assertEquals(List.of(BlackjackRulesDisplay.RULES_OPENED), events);
  }

  @Test
  void shouldCloseRulesFromCloseButton() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    Entity entity = new Entity().addComponent(rules);
    List<String> events = recordEvents(entity);
    entity.create();

    rules.openRules();
    click(field(rules, "closeButton", ImageButton.class));

    assertFalse(rules.isOpen());
    assertFalse(field(rules, "rulesOverlay", Table.class).isVisible());
    assertEquals(
        List.of(BlackjackRulesDisplay.RULES_OPENED, BlackjackRulesDisplay.RULES_CLOSED), events);
  }

  @Test
  void shouldIgnoreRepeatedOpenAndClose() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    Entity entity = new Entity().addComponent(rules);
    List<String> events = recordEvents(entity);
    entity.create();

    rules.closeRules();
    rules.openRules();
    rules.openRules();
    rules.closeRules();
    rules.closeRules();

    assertEquals(
        List.of(BlackjackRulesDisplay.RULES_OPENED, BlackjackRulesDisplay.RULES_CLOSED), events);
  }

  @Test
  void shouldShowMatchRules() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    new Entity().addComponent(rules).create();

    Table overlay = field(rules, "rulesOverlay", Table.class);
    Table list = overlay.findActor("rulesList");
    List<String> shown = new ArrayList<>();
    for (com.badlogic.gdx.scenes.scene2d.Actor actor : list.getChildren()) {
      shown.add(((Label) actor).getText().toString());
    }

    assertTrue(shown.contains("THE MATCH"));
    assertTrue(shown.contains("THE CARDS"));
    for (String line : BlackjackRulesDisplay.MATCH_RULES) {
      assertTrue(shown.contains("-  " + line), line);
    }
    for (String line : BlackjackRulesDisplay.CARD_RULES) {
      assertTrue(shown.contains("-  " + line), line);
    }
    assertTrue(BlackjackRulesDisplay.MATCH_RULES.contains("First to 3 round wins beats Circe."));
    assertTrue(BlackjackRulesDisplay.MATCH_RULES.contains("Draws don't count towards the score."));
  }

  @Test
  void shouldKeepRulesTextInsideTheParchment() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    new Entity().addComponent(rules).create();
    rules.openRules();

    Table overlay = field(rules, "rulesOverlay", Table.class);
    overlay.setSize(stage.getWidth(), stage.getHeight());
    overlay.validate();
    Table list = overlay.findActor("rulesList");
    Table popup = (Table) list.getParent();
    popup.validate();
    list.validate();

    // The parchment is an 80px image with fixed 16px sides and 24px rods. Its 4px brown strip sits
    // in the 48px stretched middle, so on screen it grows with the panel width.
    float sideBand = 16f + 4f * (popup.getWidth() - 32f) / 48f;
    for (com.badlogic.gdx.scenes.scene2d.Actor line : list.getChildren()) {
      com.badlogic.gdx.math.Vector2 bottomLeft =
          line.localToActorCoordinates(popup, new com.badlogic.gdx.math.Vector2(0, 0));
      float right = bottomLeft.x + line.getWidth();
      float top = bottomLeft.y + line.getHeight();
      assertTrue(bottomLeft.x >= sideBand + 8f, "left edge of " + line);
      assertTrue(right <= popup.getWidth() - sideBand - 8f, "right edge of " + line);
      assertTrue(bottomLeft.y >= 24f, "bottom edge of " + line);
      assertTrue(top <= popup.getHeight() - 24f, "top edge of " + line);
    }
  }

  @Test
  void shouldLockTableButtonsWhileRulesAreOpen() {
    BlackjackDisplay display =
        new BlackjackDisplay(new Blackjack(100, new Random(1)), new InventoryComponent(100));
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    new Entity().addComponent(display).addComponent(rules).create();
    ImageButton newRound = field(display, "newRoundButton", ImageButton.class);

    assertFalse(newRound.isDisabled());

    rules.openRules();
    assertTrue(newRound.isDisabled());

    rules.closeRules();
    assertFalse(newRound.isDisabled());
  }

  @Test
  void shouldKeepInfoButtonAboveTable() {
    BlackjackDisplay display =
        new BlackjackDisplay(new Blackjack(100, new Random(1)), new InventoryComponent(100));
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    // Rules first, so the table is added to the stage after the corner button.
    new Entity().addComponent(rules).addComponent(display).create();

    Table corner = field(rules, "corner", Table.class);
    Table table = field(display, "table", Table.class);

    assertTrue(corner.getZIndex() > table.getZIndex());
  }

  @Test
  void shouldRemoveActorsOnDispose() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    new Entity().addComponent(rules).create();
    Table corner = field(rules, "corner", Table.class);
    Table overlay = field(rules, "rulesOverlay", Table.class);

    rules.dispose();

    assertFalse(stage.getActors().contains(corner, true));
    assertFalse(stage.getActors().contains(overlay, true));
  }

  private static List<String> recordEvents(Entity entity) {
    List<String> events = new ArrayList<>();
    entity
        .getEvents()
        .addListener(
            BlackjackRulesDisplay.RULES_OPENED,
            () -> events.add(BlackjackRulesDisplay.RULES_OPENED));
    entity
        .getEvents()
        .addListener(
            BlackjackRulesDisplay.RULES_CLOSED,
            () -> events.add(BlackjackRulesDisplay.RULES_CLOSED));
    return events;
  }

  private static void click(ImageButton button) {
    button.fire(new ChangeListener.ChangeEvent());
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
