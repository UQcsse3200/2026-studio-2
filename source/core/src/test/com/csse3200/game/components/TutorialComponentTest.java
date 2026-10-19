package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.Terminal;
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
    TutorialComponent tutorial = new TutorialComponent(text);
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
    assertFalse(new TutorialComponent(null).keyDown(Keys.A));
    TextBoxComponent text = mock(TextBoxComponent.class);
    when(text.isDismissed()).thenReturn(true);
    TutorialComponent tutorial = new TutorialComponent(text);
    assertFalse(tutorial.keyDown(Keys.A));
    assertFalse(tutorial.keyDown(Keys.D));
    verify(text, never()).advance();
  }

  @Test
  void terminalTypingDoesNotCountAsTutorialMovement() {
    Terminal terminal = new Terminal();
    ServiceLocator.getEntityService().register(new Entity().addComponent(terminal));
    TextBoxComponent text = mock(TextBoxComponent.class);
    TutorialComponent tutorial = new TutorialComponent(text);
    terminal.setOpen();
    assertFalse(tutorial.keyDown(Keys.A));
    terminal.setClosed();
    assertFalse(tutorial.keyDown(Keys.D));
    verify(text, never()).advance();
    assertFalse(tutorial.keyDown(Keys.A));
    verify(text).advance();
  }
}
