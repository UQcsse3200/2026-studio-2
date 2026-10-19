package com.csse3200.game.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(GameExtension.class)
class CutsceneInputComponentTest {

  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerInputService(new InputService());
  }

  @Test
  void keyDownTabShouldInvokeAdvanceAction() {
    AtomicBoolean invoked = new AtomicBoolean(false);
    CutsceneInputComponent input = new CutsceneInputComponent(() -> invoked.set(true));

    assertTrue(input.keyDown(Keys.TAB));
    assertTrue(invoked.get());
  }

  @Test
  void keyDownTabShouldReturnTrue() {
    CutsceneInputComponent input = new CutsceneInputComponent(() -> {});

    assertTrue(input.keyDown(Keys.TAB));
  }

  @Test
  void keyDownOtherKeyShouldNotInvokeAdvanceAction() {
    AtomicBoolean invoked = new AtomicBoolean(false);
    CutsceneInputComponent input = new CutsceneInputComponent(() -> invoked.set(true));

    assertFalse(input.keyDown(Keys.SPACE));
    assertFalse(input.keyDown(Keys.ENTER));
    assertFalse(input.keyDown(Keys.A));
    assertFalse(invoked.get());
  }

  @Test
  void keyDownOtherKeyShouldReturnFalse() {
    CutsceneInputComponent input = new CutsceneInputComponent(() -> {});

    assertFalse(input.keyDown(Keys.SPACE));
    assertFalse(input.keyDown(Keys.ENTER));
  }

  @Test
  void touchUpShouldReturnTrue() {
    CutsceneInputComponent input = new CutsceneInputComponent(() -> {});

    assertTrue(input.touchUp(0, 0, 0, 0));
  }

  @ParameterizedTest
  @CsvSource({
    "100, 200, 0, 0", // primary pointer, left click
    "100, 200, 1, 0", // pointer 1 (second finger)
    "100, 200, 0, 1" // right click
  })
  void touchUpShouldInvokeAdvanceAction(int screenX, int screenY, int pointer, int button) {
    AtomicBoolean invoked = new AtomicBoolean(false);
    CutsceneInputComponent input = new CutsceneInputComponent(() -> invoked.set(true));

    assertTrue(input.touchUp(screenX, screenY, pointer, button));
    assertTrue(invoked.get());
  }
}
