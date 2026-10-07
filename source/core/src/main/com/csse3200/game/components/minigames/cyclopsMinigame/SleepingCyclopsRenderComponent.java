package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Draws the sleeping cyclops from a sprite sheet and plays his animations.
 *
 * <p>Sheet layout ({@code sleeping_cyclops.png}, 1200x1568): 8 columns of 150x112 frames, read left
 * to right, top to bottom, 12 fps. Frames 0-23 sleep loop, 24-47 leg twitch, 48-71 eye opening and
 * closing, 72-77 waking up, 78-101 awake loop (no snoring), 102-107 settling back to sleep.
 *
 * <p>He reacts to the same minigame events the timing logic already triggers on {@link
 * ServiceLocator#getCyclopsMinigameEventHandler()}: {@code cyclopsWake} (the player missed) makes
 * him wake and stay awake, and {@code cyclopsSleep} (the player retreated or the minigame
 * restarted) sends him back to sleep. An optional {@code cyclopsStir} event queues a leg twitch.
 *
 * <p>The entity position is the point on the floor under the middle of his body.
 */
public class SleepingCyclopsRenderComponent extends RenderComponent {
  /** Triggered by the minigame logic on a miss. */
  public static final String WAKE_EVENT = CyclopsMinigameLogic.CYCLOPS_WAKE_EVENT;

  /** Triggered by the minigame logic when the player retreats or the minigame restarts. */
  public static final String SLEEP_EVENT = CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT;

  /** Optional: trigger on the minigame event handler to queue a leg twitch. */
  public static final String STIR_EVENT = "cyclopsStir";

  static final int FRAME_WIDTH = 150;
  static final int FRAME_HEIGHT = 112;
  static final int COLUMNS = 8;
  static final int TOTAL_FRAMES = 108;
  static final float FRAME_TIME = 1f / 12f;

  /** Column of the sheet frame that sits over the entity position (middle of his body). */
  static final int PIVOT_COLUMN = 74;

  /** Empty rows below the floor line inside every frame. */
  static final int FLOOR_GAP_ROWS = 8;

  static final float AUTO_MIN_SECONDS = 5f;
  static final float AUTO_MAX_SECONDS = 11f;
  static final float AUTO_WIGGLE_CHANCE = 0.6f;

  /** The animation clips: where each starts in the sheet and how many frames it has. */
  public enum Clip {
    SLEEP(0, 24),
    WIGGLE(24, 24),
    EYES(48, 24),
    WAKE(72, 6),
    AWAKE(78, 24),
    SETTLE(102, 6);

    private final int firstFrame;
    private final int length;

    Clip(int firstFrame, int length) {
      this.firstFrame = firstFrame;
      this.length = length;
    }

    int firstFrame() {
      return firstFrame;
    }

    int length() {
      return length;
    }
  }

  private final String sheetPath;
  private final float pixelWorldSize;
  private final boolean autoWiggle;
  private final boolean autoEyes;

  private TextureRegion[] frames;
  private Clip clip = Clip.SLEEP;
  private float clipTime = 0f;
  private float untilAuto;
  private boolean awakeWanted = false;
  private float tintRed = 1f;
  private float tintGreen = 1f;
  private float tintBlue = 1f;
  private final Deque<Clip> queue = new ArrayDeque<>();

  /**
   * @param sheetPath asset path of the sprite sheet (must be loaded by the resource service)
   * @param pixelWorldSize world units covered by one sprite pixel
   * @param autoWiggle occasionally twitch a leg on his own while asleep
   * @param autoEyes occasionally open an eye on his own while asleep
   */
  public SleepingCyclopsRenderComponent(
      String sheetPath, float pixelWorldSize, boolean autoWiggle, boolean autoEyes) {
    this.sheetPath = sheetPath;
    this.pixelWorldSize = pixelWorldSize;
    this.autoWiggle = autoWiggle;
    this.autoEyes = autoEyes;
  }

  /** Multiplies his colours (1,1,1 = unchanged), e.g. to push him into the background. */
  public SleepingCyclopsRenderComponent setTint(float red, float green, float blue) {
    this.tintRed = red;
    this.tintGreen = green;
    this.tintBlue = blue;
    return this;
  }

  @Override
  public void create() {
    super.create();
    Texture sheet = ServiceLocator.getResourceService().getAsset(sheetPath, Texture.class);
    sheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    frames = new TextureRegion[TOTAL_FRAMES];
    for (int i = 0; i < frames.length; i++) {
      frames[i] =
          new TextureRegion(
              sheet,
              (i % COLUMNS) * FRAME_WIDTH,
              (i / COLUMNS) * FRAME_HEIGHT,
              FRAME_WIDTH,
              FRAME_HEIGHT);
    }
    if (ServiceLocator.getCyclopsMinigameEventHandler() != null) {
      ServiceLocator.getCyclopsMinigameEventHandler().addListener(WAKE_EVENT, this::wake);
      ServiceLocator.getCyclopsMinigameEventHandler().addListener(SLEEP_EVENT, this::sleep);
      ServiceLocator.getCyclopsMinigameEventHandler().addListener(STIR_EVENT, this::stir);
    }
    scheduleNextAuto();
  }

  /** Wakes him up now and keeps him awake until {@link #sleep()} (ignored if already awake). */
  public void wake() {
    awakeWanted = true;
    if (clip == Clip.WAKE || clip == Clip.AWAKE) {
      return;
    }
    queue.clear();
    startClip(Clip.WAKE);
  }

  /** Lets him drift back to sleep (if he is still waking up he finishes that first). */
  public void sleep() {
    awakeWanted = false;
    if (clip == Clip.AWAKE) {
      startClip(Clip.SETTLE);
    }
  }

  /** Queues a leg twitch to play once the current clip has finished (ignored while awake). */
  public void stir() {
    if (!awakeWanted && !queue.contains(Clip.WIGGLE)) {
      queue.add(Clip.WIGGLE);
    }
  }

  @Override
  public void update() {
    advance(ServiceLocator.getTimeSource().getDeltaTime());
  }

  /** Advances the animation by the given number of seconds. */
  void advance(float deltaSeconds) {
    clipTime += deltaSeconds;
    if (clip == Clip.SLEEP && !awakeWanted && queue.isEmpty() && (autoWiggle || autoEyes)) {
      untilAuto -= deltaSeconds;
      if (untilAuto <= 0f) {
        queue.add(pickAutoClip());
        scheduleNextAuto();
      }
    }
    while (clipTime >= clip.length() * FRAME_TIME) {
      clipTime -= clip.length() * FRAME_TIME;
      clip = nextClip();
    }
  }

  private Clip nextClip() {
    if (clip == Clip.WAKE || clip == Clip.AWAKE) {
      return awakeWanted ? Clip.AWAKE : Clip.SETTLE;
    }
    return queue.isEmpty() ? Clip.SLEEP : queue.poll();
  }

  private Clip pickAutoClip() {
    if (autoWiggle && autoEyes) {
      return MathUtils.random() < AUTO_WIGGLE_CHANCE ? Clip.WIGGLE : Clip.EYES;
    }
    return autoWiggle ? Clip.WIGGLE : Clip.EYES;
  }

  private void scheduleNextAuto() {
    untilAuto = MathUtils.random(AUTO_MIN_SECONDS, AUTO_MAX_SECONDS);
  }

  private void startClip(Clip next) {
    clip = next;
    clipTime = 0f;
  }

  /** Index into the sheet of the frame that is currently showing. */
  int currentFrameIndex() {
    int inClip = Math.min(clip.length() - 1, (int) (clipTime / FRAME_TIME));
    return clip.firstFrame() + inClip;
  }

  /** The clip that is currently playing. */
  public Clip getClip() {
    return clip;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    float left = position.x - PIVOT_COLUMN * pixelWorldSize;
    float bottom = position.y - FLOOR_GAP_ROWS * pixelWorldSize;
    float previous = batch.getPackedColor();
    batch.setColor(tintRed, tintGreen, tintBlue, 1f);
    batch.draw(
        frames[currentFrameIndex()],
        left,
        bottom,
        FRAME_WIDTH * pixelWorldSize,
        FRAME_HEIGHT * pixelWorldSize);
    batch.setPackedColor(previous);
  }
}
