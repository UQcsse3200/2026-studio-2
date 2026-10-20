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
  private final float autoDismissSeconds;
  private int step = 0;
  private boolean finished = false;
  private float elapsedSeconds = 0f;
  private int completionKeycode = 0;

  /**
   * Creates a tutorial over the given textbox.
   *
   * @param textBox tutorial textbox to advance, or null to ignore input
   * @param stepConfigs per-step trigger configs, one per textbox page; resolved to keycodes and
   *     mouse buttons eagerly
   * @throws IllegalArgumentException if a step names an unknown key or mouse button
   */
  public TutorialComponent(TextBoxComponent textBox, List<TutorialStepConfig> stepConfigs) {
    this(textBox, stepConfigs, null, 0f);
  }

  /**
   * Creates a tutorial that reports completion, e.g. to persist a "seen" flag in the save.
   *
   * @param onFinished run once the player completes the final step (not when the tutorial is
   *     dismissed early), or null for no callback
   */
  public TutorialComponent(
      TextBoxComponent textBox, List<TutorialStepConfig> stepConfigs, Runnable onFinished) {
    this(textBox, stepConfigs, onFinished, 0f);
  }

  /**
   * Creates a tutorial with an optional deadline and completion callback.
   *
   * @param onFinished run once the player completes the final step (not when the tutorial is
   *     dismissed early), or null for no callback
   * @param autoDismissSeconds seconds before the textbox dismisses itself without input; 0 or
   *     less waits forever for the player's steps
   * @throws IllegalArgumentException if a step names an unknown key or mouse button
   */
  public TutorialComponent(
      TextBoxComponent textBox,
      List<TutorialStepConfig> stepConfigs,
      Runnable onFinished,
      float autoDismissSeconds) {
    super(6);
    this.textBox = textBox;
    this.onFinished = onFinished;
    this.autoDismissSeconds = autoDismissSeconds;
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
      completionKeycode = keycode;
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

  /**
   * Advances the auto-dismiss deadline, if this tutorial has one. When it elapses the textbox
   * closes like an early dismissal: the player's steps are no longer needed and {@code onFinished}
   * does not run. This component is registered for input rather than entity updates, so the owning
   * screen ticks it once per frame; calls are ignored once the tutorial is finished or dismissed.
   *
   * @param delta seconds since the previous frame
   */
  public void tickAutoDismiss(float delta) {
    if (autoDismissSeconds <= 0f || finished || textBox == null || textBox.isDismissed()) {
      return;
    }
    elapsedSeconds += delta;
    if (elapsedSeconds < autoDismissSeconds) {
      return;
    }
    dispose();
  }

  /**
   * The keycode that completed the most recent step, or 0 when no key has completed one (e.g. a
   * mouse button did, or nothing has completed yet). Lets a choice tutorial such as tut0 report
   * which option the player picked to its completion callback.
   *
   * @return the completing keycode
   */
  public int getCompletionKeycode() {
    return completionKeycode;
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
