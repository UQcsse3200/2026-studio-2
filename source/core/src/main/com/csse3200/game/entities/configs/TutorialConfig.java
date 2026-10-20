package com.csse3200.game.entities.configs;

import java.util.ArrayList;
import java.util.List;

/**
 * A tutorial sequence config: a {@link TextConfig} textbox plus the input steps that advance it.
 * Loaded by {@code TutorialCommand} from {@code configs/tut<n>.json}.
 *
 * <p>Each entry of {@link #steps} pairs with the page at the same index; completing a step reveals
 * and advances to the next page, and completing the last step dismisses the textbox.
 */
public class TutorialConfig extends TextConfig {
  public List<TutorialStepConfig> steps = new ArrayList<>();

  /**
   * Seconds the textbox stays up before dismissing itself without player input; 0 (the default)
   * keeps the step-driven behaviour where only the player dismisses it.
   */
  public float autoDismissSeconds = 0f;

  /**
   * Checks this config can drive a tutorial: it has one step per page, and every step lists at
   * least one input with a resolvable key or mouse button name.
   *
   * @return null when the config is valid, otherwise a description of the first problem found
   */
  public String validateSteps() {
    if (steps == null || steps.isEmpty()) {
      return "missing \"steps\" (one step per page is required)";
    }
    int pageCount = pages == null ? 0 : pages.size();
    if (steps.size() != pageCount) {
      return "has " + steps.size() + " steps but " + pageCount + " pages";
    }
    for (int i = 0; i < steps.size(); i++) {
      String problem = validateStep(steps.get(i), i);
      if (problem != null) {
        return problem;
      }
    }
    if (autoDismissSeconds < 0) {
      return "autoDismissSeconds must not be negative";
    }
    return null;
  }

  private static String validateStep(TutorialStepConfig step, int index) {
    if (step == null) {
      return "step " + index + " is empty";
    }
    try {
      int keys = step.resolveKeyCodes().length;
      int pressed = step.resolvePressedButtons().length;
      int released = step.resolveReleasedButtons().length;
      if (keys + pressed + released == 0) {
        return "step " + index + " lists no keys or mouse buttons";
      }
    } catch (IllegalArgumentException e) {
      return "step " + index + ": " + e.getMessage();
    }
    return null;
  }
}
