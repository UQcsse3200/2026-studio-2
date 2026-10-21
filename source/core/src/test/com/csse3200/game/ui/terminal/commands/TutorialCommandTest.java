package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TutorialCommandTest {
  @Test
  void commandUsesConfiguredTextboxAndRestartsItsKeyboardProgress() {
    InputService input = new InputService();
    ServiceLocator.registerInputService(input);
    TextConfig config = new TextConfig();
    config.xPos = 25f;
    config.yPos = 40f;
    config.maxWidth = 320;
    config.padding = 12;
    config.textAlignment = "left";
    config.textColour = "WHITE";
    config.backgroundColour = "BLUE";
    config.borderColour = "BLACK";
    config.borderThickness = 4;
    config.fontPath = "fonts/tutorial.fnt";
    config.charsPerSecond = 18f;
    config.pages = List.of("Move", "Jump", "Dash");
    config.portraitPaths = List.of("portraits/tutorial.png");
    try (var files = mockStatic(FileLoader.class);
        var textboxes =
            mockConstruction(
                TextBoxComponent.class,
                (text, context) -> {
                  TextBoxComponent.Layout expectedLayout =
                      new TextBoxComponent.Layout(25f, 40f, 320, 12, Align.left);
                  TextBoxComponent.Style expectedStyle =
                      new TextBoxComponent.Style(
                          Color.WHITE, Color.BLUE, Color.BLACK, 4, "fonts/tutorial.fnt");
                  assertEquals(expectedLayout, context.arguments().get(0));
                  assertEquals(expectedStyle, context.arguments().get(1));
                  assertEquals(18f, context.arguments().get(2));
                  assertEquals(config.pages, context.arguments().get(3));
                  assertEquals(config.portraitPaths, context.arguments().get(4));
                })) {
      files
          .when(() -> FileLoader.readClass(TextConfig.class, "configs/test-tutorial.json"))
          .thenReturn(config);
      TutorialCommand command = new TutorialCommand("configs/test-tutorial.json");
      assertTrue(command.action(new ArrayList<>()));
      TextBoxComponent first = textboxes.constructed().getFirst();
      verify(first).setExternallyControlled(true);
      verify(first).setPosition(25f, 40f, Align.bottomLeft);
      verify(first).create();
      input.keyDown(Keys.A);
      input.keyDown(Keys.D);
      verify(first).advance();

      assertTrue(command.action(new ArrayList<>()));
      verify(first).dismiss();
      TextBoxComponent restarted = textboxes.constructed().get(1);
      verify(restarted).create();
      input.keyDown(Keys.SPACE);
      verify(restarted, never()).advance();
      input.keyDown(Keys.A);
      input.keyDown(Keys.D);
      verify(restarted).advance();
      verify(first, times(1)).advance();
    }
  }
}
