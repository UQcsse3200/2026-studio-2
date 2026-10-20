package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.Input.Buttons;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class TutorialFactoryTest {

  @BeforeEach
  void setUp() {
    ServiceLocator.registerInputService(new InputService());
  }

  @Test
  void startsTheNamedTutorialFromItsConfig() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      TutorialComponent started = TutorialFactory.start("tut5", null);

      assertNotNull(started);
      assertSame(tutorials.constructed().getFirst(), started);
      assertEquals(1, boxes.constructed().size());
    }
  }

  @Test
  void runsTheCompletionCallbackWhenTheSequenceFinishes() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class)) {
      Runnable onFinished = mock(Runnable.class);
      TutorialComponent started = TutorialFactory.start("tut5", onFinished);
      assertNotNull(started);

      // tut5 watches two left clicks: completing them finishes the tutorial and reports it.
      started.touchDown(0, 0, 0, Buttons.LEFT);
      verify(onFinished, never()).run();
      started.touchDown(0, 0, 0, Buttons.LEFT);
      verify(onFinished).run();
    }
  }

  @Test
  void missingConfigStartsNothing() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertNull(TutorialFactory.start("tut99", null));
      assertTrue(boxes.constructed().isEmpty());
      assertTrue(tutorials.constructed().isEmpty());
    }
  }

  @Test
  void invalidConfigStartsNothing() {
    try (MockedConstruction<TextBoxComponent> boxes = mockConstruction(TextBoxComponent.class);
        MockedConstruction<TutorialComponent> tutorials =
            mockConstruction(TutorialComponent.class)) {
      assertNull(TutorialFactory.start("test/configs/invalid_steps_tutorial.json", null));
      assertTrue(boxes.constructed().isEmpty());
      assertTrue(tutorials.constructed().isEmpty());
    }
  }
}
