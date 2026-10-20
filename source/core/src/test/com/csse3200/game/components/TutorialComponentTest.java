package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.TutorialStepConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.Terminal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(GameExtension.class)
class TutorialComponentTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerEntityService(new EntityService());
  }

  @ParameterizedTest
  @ValueSource(ints = {Keys.SHIFT_LEFT, Keys.SHIFT_RIGHT})
  void advancesOnlyForRequiredInputsAndStopsAfterCompletion(int shift) {
    TextBoxComponent text = mock(TextBoxComponent.class);
    TutorialComponent tutorial = new TutorialComponent(text, movementSteps());
    tutorial.create();
    assertFalse(tutorial.keyDown(Keys.SPACE));
    assertFalse(tutorial.keyDown(Keys.D));
    assertFalse(tutorial.keyDown(Keys.D));
    verify(text, never()).advance();
    assertFalse(tutorial.keyDown(Keys.A));
    verify(text).advance();
    assertFalse(tutorial.keyDown(Keys.A));
    assertFalse(tutorial.keyDown(Keys.SPACE));
    verify(text, times(2)).advance();
    assertFalse(tutorial.keyDown(Keys.SPACE));
    // The UI reports dismissal after dismiss() is called, as the real textbox does.
    doAnswer(
            call -> {
              when(text.isDismissed()).thenReturn(true);
              return null;
            })
        .when(text)
        .dismiss();
    assertFalse(tutorial.keyDown(shift));
    verify(text, times(3)).revealCurrentPage();
    verify(text, times(3)).advance();
    verify(text).dismiss();
    assertFalse(tutorial.keyDown(Keys.A));
    verify(text, times(3)).advance();
  }

  @Test
  void ignoresInputWithoutAnActiveTextbox() {
    assertFalse(new TutorialComponent(null, movementSteps()).keyDown(Keys.A));
    TextBoxComponent text = mock(TextBoxComponent.class);
    when(text.isDismissed()).thenReturn(true);
    TutorialComponent tutorial = new TutorialComponent(text, movementSteps());
    assertFalse(tutorial.keyDown(Keys.A));
    assertFalse(tutorial.keyDown(Keys.D));
    verify(text, never()).advance();
  }

  @Test
  void terminalTypingDoesNotCountAsTutorialMovement() {
    Terminal terminal = new Terminal();
    ServiceLocator.getEntityService().register(new Entity().addComponent(terminal));
    TextBoxComponent text = mock(TextBoxComponent.class);
    TutorialComponent tutorial = new TutorialComponent(text, movementSteps());
    terminal.setOpen();
    assertFalse(tutorial.keyDown(Keys.A));
    terminal.setClosed();
    assertFalse(tutorial.keyDown(Keys.D));
    verify(text, never()).advance();
    assertFalse(tutorial.keyDown(Keys.A));
    verify(text).advance();
  }

  @Test
  void requireAllStepWaitsForEveryInput() {
    TextBoxComponent text = mock(TextBoxComponent.class);
    TutorialStepConfig move = step(new String[] {"A", "D"}, null, null);
    move.requireAll = true;
    TutorialComponent tutorial = new TutorialComponent(text, List.of(move));
    tutorial.create();
    assertFalse(tutorial.keyDown(Keys.A));
    verify(text, never()).advance();
    assertFalse(tutorial.keyDown(Keys.A));
    verify(text, never()).advance();
    assertFalse(tutorial.keyDown(Keys.D));
    verify(text).advance();
    verify(text).dismiss();
  }

  @Test
  void mousePressAdvancesTheCurrentStep() {
    TextBoxComponent text = mock(TextBoxComponent.class);
    TutorialComponent tutorial =
        new TutorialComponent(
            text,
            List.of(
                step(null, new String[] {"RIGHT"}, null),
                step(new String[] {"SPACE"}, null, null)));
    tutorial.create();
    assertFalse(tutorial.touchDown(0, 0, 0, Buttons.LEFT));
    verify(text, never()).advance();
    assertFalse(tutorial.touchDown(0, 0, 0, Buttons.RIGHT));
    verify(text).advance();
    // The second step watches SPACE, so releasing the mouse button does not count.
    assertFalse(tutorial.touchUp(0, 0, 0, Buttons.RIGHT));
    verify(text, times(1)).advance();
    assertFalse(tutorial.keyDown(Keys.SPACE));
    verify(text, times(2)).advance();
    verify(text).dismiss();
  }

  @Test
  void mouseReleaseAdvancesTheCurrentStep() {
    TextBoxComponent text = mock(TextBoxComponent.class);
    TutorialComponent tutorial =
        new TutorialComponent(text, List.of(step(null, null, new String[] {"RIGHT"})));
    tutorial.create();
    assertFalse(tutorial.touchDown(0, 0, 0, Buttons.RIGHT));
    verify(text, never()).advance();
    assertFalse(tutorial.touchUp(0, 0, 0, Buttons.LEFT));
    verify(text, never()).advance();
    assertFalse(tutorial.touchUp(0, 0, 0, Buttons.RIGHT));
    verify(text).advance();
    verify(text).dismiss();
  }

  @Test
  void rejectsUnknownInputNames() {
    TextBoxComponent text = mock(TextBoxComponent.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> new TutorialComponent(text, List.of(step(new String[] {"NOT_A_KEY"}, null, null))));
    assertThrows(
        IllegalArgumentException.class,
        () -> new TutorialComponent(text, List.of(step(null, new String[] {"LEFTY"}, null))));
  }

  private static List<TutorialStepConfig> movementSteps() {
    TutorialStepConfig move = step(new String[] {"A", "D"}, null, null);
    move.requireAll = true;
    return List.of(
        move,
        step(new String[] {"SPACE"}, null, null),
        step(new String[] {"SHIFT_LEFT", "SHIFT_RIGHT"}, null, null));
  }

  private static TutorialStepConfig step(String[] keys, String[] mouse, String[] mouseUp) {
    TutorialStepConfig step = new TutorialStepConfig();
    step.keys = keys;
    step.mouse = mouse;
    step.mouseUp = mouseUp;
    return step;
  }
}
