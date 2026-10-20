package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.extensions.GameExtension;
import java.util.ArrayList;
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
}
