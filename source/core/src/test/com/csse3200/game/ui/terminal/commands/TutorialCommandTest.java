package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.entities.configs.TutorialConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class TutorialCommandTest {

  @Test
  void noArgumentStartsTheDefaultTutorial() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertTrue(new TutorialCommand().action(args()));
      assertEquals(1, boxes.constructed().size());
      assertEquals(1, tutorials.constructed().size());
    }
  }

  @Test
  void extensionOnlyNameIsAcceptedWithoutAPrefix() {
    TutorialConfig config = FileLoader.readClass(TutorialConfig.class, "configs/tut1.json");
    try (var boxes = mockConstruction(TextBoxComponent.class);
        var tutorials = mockConstruction(TutorialComponent.class);
        var files = mockStatic(FileLoader.class)) {
      files
          .when(() -> FileLoader.readClass(TutorialConfig.class, "custom.json"))
          .thenReturn(config);
      assertTrue(new TutorialCommand().action(args("custom.json")));
      assertEquals(1, tutorials.constructed().size());
    }
  }

  @Test
  void bareNameLoadsTheNamedSequence() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertTrue(new TutorialCommand().action(args("tut2")));
      assertEquals(1, boxes.constructed().size());
      assertEquals(1, tutorials.constructed().size());
    }
  }

  @Test
  void explicitPathArgumentIsUsedVerbatim() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertTrue(new TutorialCommand().action(args("configs/tut5.json")));
      assertEquals(1, boxes.constructed().size());
      assertEquals(1, tutorials.constructed().size());
    }
  }

  @Test
  void explicitPathConstructorIgnoresArguments() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertTrue(new TutorialCommand("configs/tut3.json").action(args("tut1")));
      assertEquals(1, boxes.constructed().size());
      assertEquals(1, tutorials.constructed().size());
    }
  }

  @Test
  void missingConfigReturnsFalse() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertFalse(new TutorialCommand().action(args("tut99")));
      assertTrue(boxes.constructed().isEmpty());
      assertTrue(tutorials.constructed().isEmpty());
    }
  }

  @Test
  void invalidConfigReturnsFalse() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertFalse(new TutorialCommand().action(args("test/configs/invalid_steps_tutorial.json")));
      assertTrue(boxes.constructed().isEmpty());
      assertTrue(tutorials.constructed().isEmpty());
    }
  }

  @Test
  void rerunningDisposesThePreviousTutorial() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      TutorialCommand command = new TutorialCommand();
      assertTrue(command.action(args("tut1")));
      assertTrue(command.action(args("tut1")));
      assertEquals(2, tutorials.constructed().size());
      verify(tutorials.constructed().get(0)).dispose();
      verify(tutorials.constructed().get(1), never()).dispose();
    }
  }

  private static ArrayList<String> args(String... values) {
    ArrayList<String> args = new ArrayList<>();
    for (String value : values) {
      args.add(value);
    }
    return args;
  }

  @Test
  void commandUsesConfiguredTextboxAndRestartsItsKeyboardProgress() {
    InputService input = new InputService();
    ServiceLocator.registerInputService(input);
    TutorialConfig config = FileLoader.readClass(TutorialConfig.class, "configs/tut1.json");
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
    config.pages = List.of("Move", "Jump");
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
          .when(() -> FileLoader.readClass(TutorialConfig.class, "configs/test-tutorial.json"))
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
