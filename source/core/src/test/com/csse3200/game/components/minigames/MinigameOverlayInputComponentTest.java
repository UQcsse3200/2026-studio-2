package com.csse3200.game.components.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class MinigameOverlayInputComponentTest {
  @Test
  void overlayBlocksGameplayAndEscapeClosesOnlyOnKeyDown() {
    InputService service = new InputService();
    ServiceLocator.registerInputService(service);
    InputComponent gameplay = mock(InputComponent.class);
    service.register(gameplay);
    Runnable close = mock(Runnable.class);
    MinigameOverlayInputComponent overlay = new MinigameOverlayInputComponent(close);
    overlay.create();
    assertEquals(100, overlay.getPriority());
    assertTrue(service.keyDown(Input.Keys.W));
    assertTrue(service.keyUp(Input.Keys.W));
    assertTrue(service.keyTyped('w'));
    verify(close, never()).run();
    assertTrue(service.keyDown(Input.Keys.ESCAPE));
    assertTrue(service.keyUp(Input.Keys.ESCAPE));
    verify(close).run();
    verify(gameplay, never()).keyDown(anyInt());
    verify(gameplay, never()).keyUp(anyInt());
    verify(gameplay, never()).keyTyped(anyChar());
    overlay.dispose();
    service.keyDown(Input.Keys.W);
    verify(gameplay).keyDown(Input.Keys.W);
  }

  @Test
  void typingModeForwardsCharactersButStillBlocksGameplayKeyPressesAndReleases() {
    InputService service = new InputService();
    ServiceLocator.registerInputService(service);
    InputComponent textField = mock(InputComponent.class);
    when(textField.keyTyped('7')).thenReturn(true);
    service.register(textField);
    Runnable close = mock(Runnable.class);
    MinigameOverlayInputComponent overlay = new MinigameOverlayInputComponent(close, true);
    overlay.create();
    assertFalse(overlay.keyTyped('7'));
    assertTrue(service.keyTyped('7'));
    verify(textField).keyTyped('7');
    assertTrue(service.keyDown(Input.Keys.NUM_7));
    assertTrue(service.keyUp(Input.Keys.NUM_7));
    verify(textField, never()).keyDown(anyInt());
    verify(textField, never()).keyUp(anyInt());
    assertTrue(service.keyDown(Input.Keys.ESCAPE));
    verify(close).run();
    overlay.dispose();
  }
}
