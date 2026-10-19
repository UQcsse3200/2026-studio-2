package com.csse3200.game.ui.terminal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Input;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * What every terminal input handler has to do, however it opens and closes the terminal: type into
 * it while it is open, and swallow input while it is open so the game does not also react.
 */
@ExtendWith(GameExtension.class)
abstract class AbstractTerminalInputComponentTest {
  /** The handler under test, using the given terminal. */
  abstract TerminalInputComponent createInput(Terminal terminal);

  @Test
  void closedTerminalDoesNotProcessTypedInput() {
    Terminal terminal = mock(Terminal.class);
    TerminalInputComponent input = createInput(terminal);
    assertFalse(input.keyTyped('a'));
    verify(terminal, never()).appendToMessage('a');
    verify(terminal, never()).processMessage();
  }

  @ParameterizedTest
  @ValueSource(chars = {'a', '7', ' '})
  void openTerminalAcceptsLettersNumbersAndSpaces(char character) {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    assertTrue(createInput(terminal).keyTyped(character));
    verify(terminal).appendToMessage(character);
  }

  @Test
  void openTerminalRejectsUnsupportedCharacters() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    assertFalse(createInput(terminal).keyTyped('@'));
    verify(terminal, never()).appendToMessage('@');
  }

  @ParameterizedTest
  @ValueSource(chars = {'\r', '\n'})
  void successfulCommandClosesTerminalAndClearsMessage(char enter) {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    when(terminal.processMessage()).thenReturn(true);
    assertTrue(createInput(terminal).keyTyped(enter));
    verify(terminal).processMessage();
    verify(terminal).toggleIsOpen();
    verify(terminal).setEnteredMessage("");
  }

  @Test
  void unsuccessfulCommandClearsMessageAndKeepsTerminalOpen() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    assertTrue(createInput(terminal).keyTyped('\n'));
    verify(terminal).processMessage();
    verify(terminal, never()).toggleIsOpen();
    verify(terminal).setEnteredMessage("");
  }

  @Test
  void shouldUpdateMessageOnKeyTyped() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    TerminalInputComponent terminalInput = createInput(terminal);

    terminalInput.keyTyped('a');
    terminalInput.keyTyped('b');
    verify(terminal).appendToMessage('a');
    verify(terminal).appendToMessage('b');

    terminalInput.keyTyped('\b');
    verify(terminal).handleBackspace();

    terminalInput.keyTyped('\n');
    verify(terminal).processMessage();
  }

  @Test
  void shouldHandleMessageWhenTerminalOpen() {
    Terminal terminal = mock(Terminal.class);
    TerminalInputComponent terminalInput = createInput(terminal);

    when(terminal.isOpen()).thenReturn(true);
    assertTrue(terminalInput.keyDown('a'));
    assertTrue(terminalInput.keyUp('a'));

    when(terminal.isOpen()).thenReturn(false);
    assertFalse(terminalInput.keyDown('a'));
    assertFalse(terminalInput.keyUp('a'));
  }

  @Test
  void shouldSwallowPointerInputWhenTerminalOpen() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    TerminalInputComponent terminalInput = createInput(terminal);

    assertTrue(terminalInput.touchDown(100, 100, 0, Input.Buttons.LEFT));
    assertTrue(terminalInput.touchUp(100, 100, 0, Input.Buttons.LEFT));
    assertTrue(terminalInput.touchDragged(100, 100, 0));
    assertTrue(terminalInput.mouseMoved(100, 100));
  }

  @Test
  void shouldNotSwallowPointerInputWhenTerminalClosed() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(false);
    TerminalInputComponent terminalInput = createInput(terminal);

    assertFalse(terminalInput.touchDown(100, 100, 0, Input.Buttons.LEFT));
    assertFalse(terminalInput.touchUp(100, 100, 0, Input.Buttons.LEFT));
    assertFalse(terminalInput.touchDragged(100, 100, 0));
    assertFalse(terminalInput.mouseMoved(100, 100));
  }
}
