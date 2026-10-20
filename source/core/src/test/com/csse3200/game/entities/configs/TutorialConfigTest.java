package com.csse3200.game.entities.configs;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(GameExtension.class)
class TutorialConfigTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "tut0", "tut1", "tut2", "tut3", "tut5", "tut6", "tut7", "tut8", "tut9", "tut10", "tut11",
        "tut12", "tut13", "tut14", "tut15"
      })
  void shippedTutorialConfigsAreValid(String name) {
    TutorialConfig config = FileLoader.readClass(TutorialConfig.class, "configs/" + name + ".json");
    assertNotNull(config, name + " should load");
    assertEquals(config.pages.size(), config.steps.size(), name + " needs one step per page");
    assertNull(config.validateSteps(), name);
  }

  @Test
  void missingStepsIsRejected() {
    assertNotNull(new TutorialConfig().validateSteps());
  }

  @ParameterizedTest
  @ValueSource(strings = {"tut7", "tut8"})
  void timedPromptsConfigureASevenSecondDeadline(String name) {
    TutorialConfig config = FileLoader.readClass(TutorialConfig.class, "configs/" + name + ".json");

    assertNotNull(config, name + " should load");
    assertEquals(7f, config.autoDismissSeconds);
  }

  @ParameterizedTest
  @ValueSource(strings = {"tut10", "tut11", "tut12", "tut13", "tut14", "tut15"})
  void shortTipsDismissThemselvesAfterFiveSeconds(String name) {
    TutorialConfig config = FileLoader.readClass(TutorialConfig.class, "configs/" + name + ".json");

    assertNotNull(config, name + " should load");
    assertEquals(5f, config.autoDismissSeconds);
  }

  @Test
  void stepCountMustMatchPageCount() {
    TutorialConfig config = new TutorialConfig();
    config.pages = List.of("one", "two");
    config.steps = List.of(stepWithKeys("SPACE"));
    assertTrue(config.validateSteps().contains("1 steps"));
  }

  @Test
  void unknownKeyNamesAreRejected() {
    TutorialConfig config = new TutorialConfig();
    config.pages = List.of("one");
    config.steps = List.of(stepWithKeys("NOT_A_KEY"));
    assertTrue(config.validateSteps().contains("NOT_A_KEY"));
  }

  @Test
  void unknownMouseButtonsAreRejected() {
    TutorialConfig config = new TutorialConfig();
    config.pages = List.of("one");
    TutorialStepConfig step = new TutorialStepConfig();
    step.mouse = new String[] {"LEFTY"};
    config.steps = List.of(step);
    assertTrue(config.validateSteps().contains("LEFTY"));
  }

  @Test
  void stepsWithoutInputsAreRejected() {
    TutorialConfig config = new TutorialConfig();
    config.pages = List.of("one");
    config.steps = List.of(new TutorialStepConfig());
    assertTrue(config.validateSteps().contains("no keys"));
  }

  @Test
  void negativeAutoDismissIsRejected() {
    TutorialConfig config = new TutorialConfig();
    config.pages = List.of("one");
    config.steps = List.of(stepWithKeys("SPACE"));
    config.autoDismissSeconds = -1f;
    assertTrue(config.validateSteps().contains("autoDismissSeconds"));
  }

  private static TutorialStepConfig stepWithKeys(String... keys) {
    TutorialStepConfig step = new TutorialStepConfig();
    step.keys = keys;
    return step;
  }
}
