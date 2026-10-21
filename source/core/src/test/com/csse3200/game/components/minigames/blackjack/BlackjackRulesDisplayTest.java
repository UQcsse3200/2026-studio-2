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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
    Sound sound = mock(Sound.class);
    when(resources.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
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
    assertNotNull(infoButton(rules));
  }

  @Test
  void shouldOpenRulesFromInfoButton() {
    BlackjackRulesDisplay rules = new BlackjackRulesDisplay();
    Entity entity = new Entity().addComponent(rules);
    List<String> events = recordEvents(entity);
    entity.create();

    click(infoButton(rules));

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
    click(closeButton(rules));

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
    for (Label label : labelsIn(list)) {
      shown.add(label.getText().toString());
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
  void shouldKeepRulesTextInsideTheScroll() {
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
    ImageButton closeButton = closeButton(rules);

    // The scroll is 159 x 79 art pixels, enlarged by a whole number. Text must stay inside its
    // margins (17px sides, 13px top, 11px bottom) and above the close button.
    float scale = popup.getWidth() / 159f;
    assertEquals(Math.round(scale), scale, 0.001f, "scroll scaled by a whole number");
    assertEquals(79f * scale, popup.getHeight(), 0.001f);

    float buttonTop =
        closeButton.localToActorCoordinates(popup, new Vector2(0, 0)).y + closeButton.getHeight();
    for (Label label : labelsIn(list)) {
      Vector2 bottomLeft = label.localToActorCoordinates(popup, new Vector2(0, 0));
      float right = bottomLeft.x + label.getWidth();
      float top = bottomLeft.y + label.getHeight();
      assertTrue(bottomLeft.x >= 17f * scale, "left edge of " + label);
      assertTrue(right <= popup.getWidth() - 17f * scale, "right edge of " + label);
      assertTrue(bottomLeft.y >= 11f * scale, "bottom edge of " + label);
      assertTrue(bottomLeft.y >= buttonTop, "label overlaps close button: " + label);
      assertTrue(top <= popup.getHeight() - 13f * scale, "top edge of " + label);
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

  private static List<Label> labelsIn(Group group) {
    List<Label> labels = new ArrayList<>();
    for (Actor child : group.getChildren()) {
      if (child instanceof Label label) {
        labels.add(label);
      } else if (child instanceof Group nested) {
        labels.addAll(labelsIn(nested));
      }
    }
    return labels;
  }

  private static ImageButton infoButton(BlackjackRulesDisplay rules) {
    return field(rules, "corner", Table.class).findActor(BlackjackRulesDisplay.INFO_BUTTON_NAME);
  }

  private static ImageButton closeButton(BlackjackRulesDisplay rules) {
    return field(rules, "rulesOverlay", Table.class)
        .findActor(BlackjackRulesDisplay.CLOSE_BUTTON_NAME);
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
