package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Full-screen wake-up cinematic: the world is blacked out and a sleeping animation loops in the
 * centre of the screen while the wake-up voice plays, then a one-shot rising animation plays. Once
 * standing, {@code onStoodUp} fires, the black fades out to reveal the world, and {@code
 * onRevealed} fires. A Skip button jumps straight to the end.
 */
public class WakeUpCinematicDisplay extends UIComponent {
  private static final float Z_INDEX = 30f;
  private static final float FADE_OUT_DURATION = 1.5f;
  private static final float SKIP_BUTTON_WIDTH = 340f;
  private static final float SKIP_BUTTON_HEIGHT = 124f;
  private static final float SKIP_BUTTON_MARGIN = 30f;
  // Spotlight diameter relative to the standing figure's height.
  private static final float SPOTLIGHT_DIAMETER_FACTOR = 1.5f;

  private final Animation<TextureRegion> sleeping;
  private final Animation<TextureRegion> rising;
  private final float sleepDuration;
  private final Vector2 sleepFrameSizePx;
  private final Vector2 riseFrameSizePx;
  private final Sound wakeUpSound;
  private final Runnable onStoodUp;
  private final Runnable onRevealed;

  private Image blackout;
  private Image spotlight;
  private Image figure;
  private ImageButton skipButton;
  private Texture blackTexture;
  private Texture spotlightTexture;
  private long wakeUpSoundId = -1;
  private float stateTime = 0f;
  private boolean isRising = false;
  private boolean complete = false;

  /**
   * @param sleeping looping animation shown while asleep
   * @param sleepDuration seconds to stay asleep before rising (e.g. the voice clip's length)
   * @param sleepFrameSizePx screen size to draw each sleeping frame at
   * @param rising one-shot animation from lying to standing
   * @param riseFrameSizePx screen size to draw each rising frame at
   * @param wakeUpSound voice clip played when the cinematic starts, cut short on skip
   * @param onStoodUp called once the rising animation finishes (or on skip), before the fade
   * @param onRevealed called once the blackout has fully faded out
   */
  public WakeUpCinematicDisplay(
      Animation<TextureRegion> sleeping,
      float sleepDuration,
      Vector2 sleepFrameSizePx,
      Animation<TextureRegion> rising,
      Vector2 riseFrameSizePx,
      Sound wakeUpSound,
      Runnable onStoodUp,
      Runnable onRevealed) {
    this.sleeping = sleeping;
    this.sleepDuration = sleepDuration;
    this.sleepFrameSizePx = sleepFrameSizePx;
    this.rising = rising;
    this.riseFrameSizePx = riseFrameSizePx;
    this.wakeUpSound = wakeUpSound;
    this.onStoodUp = onStoodUp;
    this.onRevealed = onRevealed;
  }

  @Override
  public void create() {
    super.create();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();
    blackTexture = new Texture(pixmap);
    pixmap.dispose();

    // Added straight to the stage: setFillParent only sizes against a parent that has a size, and
    // the stage root does, whereas a plain Group would leave the blackout at 0x0.
    blackout = new Image(blackTexture);
    blackout.setFillParent(true);

    spotlight = buildSpotlight();

    figure = new Image(new TextureRegionDrawable(sleeping.getKeyFrame(0f)));
    layoutFigure(sleepFrameSizePx);

    skipButton = buildSkipButton();

    stage.addActor(blackout);
    stage.addActor(spotlight);
    stage.addActor(figure);
    stage.addActor(skipButton);
    // Cover any UI that was added to the stage before this cinematic (HUD, buttons, overlays).
    blackout.toFront();
    spotlight.toFront();
    figure.toFront();
    skipButton.toFront();

    wakeUpSoundId = wakeUpSound.play(1f);
  }

  private ImageButton buildSkipButton() {
    Texture upTexture =
        ServiceLocator.getResourceService().getAsset("images/Buttons/skip_up_btn.png", Texture.class);
    Texture downTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/skip_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = new TextureRegionDrawable(upTexture);
    style.down = new TextureRegionDrawable(downTexture);

    ImageButton button = new ImageButton(style);
    button.setSize(SKIP_BUTTON_WIDTH, SKIP_BUTTON_HEIGHT);
    button.setPosition(
        Gdx.graphics.getWidth() - SKIP_BUTTON_WIDTH - SKIP_BUTTON_MARGIN,
        Gdx.graphics.getHeight() - SKIP_BUTTON_HEIGHT - SKIP_BUTTON_MARGIN);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            ButtonSound.playClick();
            skip();
          }
        });
    return button;
  }

  /** A soft grey disc behind the figure so it reads clearly against the black. */
  private Image buildSpotlight() {
    int size = 256;
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0.42f, 0.42f, 0.45f, 1f));
    pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
    spotlightTexture = new Texture(pixmap);
    spotlightTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    pixmap.dispose();

    Image disc = new Image(spotlightTexture);
    float diameter = riseFrameSizePx.y * SPOTLIGHT_DIAMETER_FACTOR;
    disc.setSize(diameter, diameter);
    // Centred on the middle of the standing figure, which stands on the shared baseline.
    float centreY = figureBaselineY() + riseFrameSizePx.y / 2f;
    disc.setPosition((Gdx.graphics.getWidth() - diameter) / 2f, centreY - diameter / 2f);
    return disc;
  }

  private float figureBaselineY() {
    return Gdx.graphics.getHeight() * 0.3f;
  }

  private void layoutFigure(Vector2 sizePx) {
    figure.setSize(sizePx.x, sizePx.y);
    // Bottom-centre anchored at a fixed point so the figure stays "on the ground" as frames change.
    figure.setPosition((Gdx.graphics.getWidth() - sizePx.x) / 2f, figureBaselineY());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (complete || ServiceLocator.getEntityService().getPaused()) {
      return;
    }
    stateTime += Gdx.graphics.getDeltaTime();

    if (!isRising) {
      if (stateTime >= sleepDuration) {
        isRising = true;
        stateTime = 0f;
        layoutFigure(riseFrameSizePx);
      } else {
        figure.setDrawable(new TextureRegionDrawable(sleeping.getKeyFrame(stateTime)));
        return;
      }
    }

    figure.setDrawable(new TextureRegionDrawable(rising.getKeyFrame(stateTime)));
    if (rising.isAnimationFinished(stateTime)) {
      finishCinematic();
    }
  }

  /** Skips straight to the end of the cinematic, cutting the voice clip short. */
  private void skip() {
    if (complete) {
      return;
    }
    if (wakeUpSoundId != -1) {
      wakeUpSound.stop(wakeUpSoundId);
    }
    finishCinematic();
  }

  private void finishCinematic() {
    complete = true;
    if (onStoodUp != null) {
      onStoodUp.run();
    }
    // Hand the scene over: the figure gives way to the real player as the world fades in.
    figure.setVisible(false);
    spotlight.setVisible(false);
    skipButton.remove();
    blackout.addAction(
        Actions.sequence(
            Actions.fadeOut(FADE_OUT_DURATION),
            Actions.run(
                () -> {
                  blackout.remove();
                  spotlight.remove();
                  figure.remove();
                  if (onRevealed != null) {
                    onRevealed.run();
                  }
                })));
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    if (blackout != null) {
      blackout.remove();
    }
    if (spotlight != null) {
      spotlight.remove();
    }
    if (figure != null) {
      figure.remove();
    }
    if (skipButton != null) {
      skipButton.remove();
    }
    if (blackTexture != null) {
      blackTexture.dispose();
    }
    if (spotlightTexture != null) {
      spotlightTexture.dispose();
    }
    super.dispose();
  }

  /**
   * Builds an animation from a sprite sheet laid out as a uniform grid of equally sized cells.
   *
   * @param sheet the sheet texture
   * @param columns frame columns in the sheet
   * @param rows frame rows in the sheet
   * @param frameDuration seconds per frame
   * @param playMode how the animation plays
   * @return the animation, frames in left-to-right, top-to-bottom order
   */
  public static Animation<TextureRegion> fromGrid(
      Texture sheet, int columns, int rows, float frameDuration, Animation.PlayMode playMode) {
    TextureRegion[][] grid =
        TextureRegion.split(sheet, sheet.getWidth() / columns, sheet.getHeight() / rows);
    TextureRegion[] frames = new TextureRegion[columns * rows];
    int index = 0;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < columns; col++) {
        frames[index++] = grid[row][col];
      }
    }
    Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
    animation.setPlayMode(playMode);
    return animation;
  }
}
