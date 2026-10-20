package com.csse3200.game.components;

import com.csse3200.game.entities.configs.TutorialStepConfig;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.ui.terminal.Terminal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Observes raw keyboard and mouse input to advance a tutorial textbox through a configured sequence
 * of steps. Each step lists the inputs that complete it: key names, mouse buttons pressed, and/or
 * mouse buttons released. Any listed input completes the step unless requireAll is set, in which
 * case every listed input must be seen. See {@code configs/tut<n>.json} for the sequences.
 *
 * <p>Input is never blocked, this component always returns false so the player input handler still
 * receives every key and click. Priority exceeds the player handler so presses are seen before the
 * player consumes them.
 */
public class TutorialComponent extends InputComponent {
  private final TextBoxComponent textBox;
  private final List<Step> steps;
  private final Runnable onFinished;
  private int step = 0;
  private boolean finished = false;

  /**
   * Creates a tutorial over the given textbox.
   *
   * @param textBox tutorial textbox to advance, or null to ignore input
   * @param stepConfigs per-step trigger configs, one per textbox page; resolved to keycodes and
   *     mouse buttons eagerly
   * @throws IllegalArgumentException if a step names an unknown key or mouse button
   */
  public TutorialComponent(TextBoxComponent textBox, List<TutorialStepConfig> stepConfigs) {
    this(textBox, stepConfigs, null);
  }

  /**
   * Creates a tutorial that reports completion, e.g. to persist a "seen" flag in the save.
   *
   * @param onFinished run once the player completes the final step (not when the tutorial is
   *     dismissed early), or null for no callback
   */
  public TutorialComponent(
      TextBoxComponent textBox, List<TutorialStepConfig> stepConfigs, Runnable onFinished) {
    super(6);
    this.textBox = textBox;
    this.onFinished = onFinished;
    this.steps = new ArrayList<>();
    if (stepConfigs != null) {
      for (TutorialStepConfig config : stepConfigs) {
        steps.add(new Step(config));
      }
    }
  }

  @Override
  public boolean keyDown(int keycode) {
    if (canObserve() && steps.get(step).markKeySeen(keycode)) {
      completeStep();
    }
    // Never consume the key, so the player still moves while following the tutorial.
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (canObserve() && steps.get(step).markPressedSeen(button)) {
      completeStep();
    }
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (canObserve() && steps.get(step).markReleasedSeen(button)) {
      completeStep();
    }
    return false;
  }

  private boolean canObserve() {
    return !finished
        && textBox != null
        && !textBox.isDismissed()
        && !Terminal.isAnyOpen()
        && step < steps.size();
  }

  private void completeStep() {
    textBox.revealCurrentPage();
    textBox.advance();
    step++;
    if (step >= steps.size()) {
      finishTutorial();
    }
  }

  private void finishTutorial() {
    textBox.dismiss();
    finished = true;
    dispose();
    if (onFinished != null) {
      onFinished.run();
    }
  }

  @Override
  public void dispose() {
    if (!finished && textBox != null && !textBox.isDismissed()) {
      textBox.dismiss();
    }
    super.dispose();
  }

  /** A resolved tutorial step and the inputs of the current attempt already seen for it. */
  private static final class Step {
    private final Set<Integer> keys;
    private final Set<Integer> pressedButtons;
    private final Set<Integer> releasedButtons;
    private final boolean requireAll;
    private final Set<Integer> seenKeys = new HashSet<>();
    private final Set<Integer> seenPressed = new HashSet<>();
    private final Set<Integer> seenReleased = new HashSet<>();

    Step(TutorialStepConfig config) {
      keys = toSet(config.resolveKeyCodes());
      pressedButtons = toSet(config.resolvePressedButtons());
      releasedButtons = toSet(config.resolveReleasedButtons());
      requireAll = config.requireAll;
    }

    boolean markKeySeen(int keycode) {
      if (!keys.contains(keycode)) {
        return false;
      }
      seenKeys.add(keycode);
      return isComplete();
    }

    boolean markPressedSeen(int button) {
      if (!pressedButtons.contains(button)) {
        return false;
      }
      seenPressed.add(button);
      return isComplete();
    }

    boolean markReleasedSeen(int button) {
      if (!releasedButtons.contains(button)) {
        return false;
      }
      seenReleased.add(button);
      return isComplete();
    }

    private boolean isComplete() {
      if (!requireAll) {
        return true;
      }
      return seenKeys.containsAll(keys)
          && seenPressed.containsAll(pressedButtons)
          && seenReleased.containsAll(releasedButtons);
    }

    private static Set<Integer> toSet(int[] codes) {
      Set<Integer> set = new HashSet<>();
      for (int code : codes) {
        set.add(code);
      }
      return set;
    }
  }
}
