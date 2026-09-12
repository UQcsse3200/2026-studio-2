package com.csse3200.game.ui.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Instructions delivered by Calypso: the world blurs, she appears beside an unrolled scroll, and
 * her words are written onto the parchment (with an optional item image) above a Continue button.
 * Showing a line pauses the game (which blurs the world via {@code Renderer}); dismissing the last
 * queued line unpauses and hides it.
 */
public class InstructionOverlay extends UIComponent {
  private static final float Z_INDEX = 22f;
  private static final float MESSAGE_SPEED = 30f;

  private static final String SCROLL_TEXTURE = "images/scroll_background.png";
  private static final String SPEAKER_TEXTURE = "images/calypso.png";
  private static final String SPEAKER_NAME = "Calypso";
  private static final Color INK_COLOR = new Color(0.24f, 0.16f, 0.08f, 1f);
  private static final Color NAME_COLOR = new Color(0.55f, 0.35f, 0.08f, 1f);

  // The scroll art is 3:2; the parchment is inset from the rollers/edges by these fractions.
  private static final float SCROLL_ASPECT = 1536f / 1024f;
  private static final float SCROLL_SCREEN_WIDTH_FRACTION = 0.5f;
  private static final float PARCHMENT_INSET_X = 0.15f;
  private static final float PARCHMENT_INSET_TOP = 0.18f;
  private static final float PARCHMENT_INSET_BOTTOM = 0.16f;
  private static final float SPEAKER_SCREEN_HEIGHT_FRACTION = 0.55f;
  private static final float ITEM_IMAGE_SIZE = 140f;

  /**
   * One line of Calypso's dialogue, with an optional sound clip, an optional item image shown
   * above the text (e.g. something just found), and a callback for when it is dismissed.
   */
  public static final class Line {
    final String text;
    final String soundPath;
    final Runnable onDismiss;
    final Texture image;

    public Line(String text) {
      this(text, null, null, null);
    }

    public Line(String text, String soundPath, Runnable onDismiss) {
      this(text, soundPath, onDismiss, null);
    }

    public Line(String text, String soundPath, Runnable onDismiss, Texture image) {
      this.text = text;
      this.soundPath = soundPath;
      this.onDismiss = onDismiss;
      this.image = image;
    }
  }

  private final Queue<Line> queue = new ArrayDeque<>();
  private final TypewriterEffect typewriterEffect = new TypewriterEffect(MESSAGE_SPEED);

  private Table root;
  private Table scroll;
  private Image image;
  private Cell<Image> imageCell;
  private Label messageLabel;
  private Line currentLine;
  private boolean visible = false;
  private Runnable onHidden;

  /**
   * @param onHidden called each time the overlay closes and the game unpauses (e.g. to re-sync
   *     player input that was gated while paused)
   */
  public void setOnHidden(Runnable onHidden) {
    this.onHidden = onHidden;
  }

  @Override
  public void create() {
    super.create();
    buildActors();
  }

  private void buildActors() {
    float screenWidth = Gdx.graphics.getWidth();
    float screenHeight = Gdx.graphics.getHeight();
    float scrollWidth = screenWidth * SCROLL_SCREEN_WIDTH_FRACTION;
    float scrollHeight = scrollWidth / SCROLL_ASPECT;

    root = new Table();
    root.setFillParent(true);
    root.setVisible(false);

    Texture scrollTexture =
        ServiceLocator.getResourceService().getAsset(SCROLL_TEXTURE, Texture.class);
    scroll = new Table();
    scroll.setBackground(new TextureRegionDrawable(scrollTexture));
    scroll
        .pad(
            scrollHeight * PARCHMENT_INSET_TOP,
            scrollWidth * PARCHMENT_INSET_X,
            scrollHeight * PARCHMENT_INSET_BOTTOM,
            scrollWidth * PARCHMENT_INSET_X);

    Label nameLabel = new Label(SPEAKER_NAME, skin, "large");
    nameLabel.setColor(NAME_COLOR);

    image = new Image();
    image.setScaling(Scaling.fit);

    messageLabel = new Label("", skin);
    messageLabel.setWrap(true);
    messageLabel.setAlignment(1);
    messageLabel.setColor(INK_COLOR);

    ImageButton continueButton = buildContinueButton();

    scroll.add(nameLabel).top().row();
    imageCell = scroll.add(image).size(0f).padTop(4f);
    scroll.row();
    scroll.add(messageLabel).fillX().expandX().expandY().padTop(6f).row();
    scroll.add(continueButton).width(160f).height(56f).padTop(6f).row();

    Texture speakerTexture =
        ServiceLocator.getResourceService().getAsset(SPEAKER_TEXTURE, Texture.class);
    Image speaker = new Image(speakerTexture);
    speaker.setScaling(Scaling.fit);
    float speakerHeight = screenHeight * SPEAKER_SCREEN_HEIGHT_FRACTION;
    float speakerWidth = speakerHeight * speakerTexture.getWidth() / speakerTexture.getHeight();

    root.add(speaker).size(speakerWidth, speakerHeight).bottom().padRight(-speakerWidth * 0.15f);
    root.add(scroll).size(scrollWidth, scrollHeight).center();
    stage.addActor(root);
  }

  private ImageButton buildContinueButton() {
    Texture continueUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/continue_up_btn.png", Texture.class);
    Texture continueDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/continue_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle continueButtonStyle = new ImageButton.ImageButtonStyle();
    continueButtonStyle.up = new TextureRegionDrawable(continueUpTexture);
    continueButtonStyle.down = new TextureRegionDrawable(continueDownTexture);

    ImageButton continueButton = new ImageButton(continueButtonStyle);
    continueButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            onContinueClicked();
          }
        });
    return continueButton;
  }

  /** Shows a single plain-text line, queued after anything already showing. */
  public void show(String text) {
    show(new Line(text));
  }

  /** Shows a single line, queued after anything already showing. */
  public void show(Line line) {
    queue.add(line);
    if (!visible) {
      advance();
    }
  }

  /** Shows a sequence of lines, one at a time, queued after anything already showing. */
  public void showSequence(List<Line> lines) {
    queue.addAll(lines);
    if (!visible) {
      advance();
    }
  }

  public boolean isShowing() {
    return visible;
  }

  private void advance() {
    if (currentLine != null && currentLine.onDismiss != null) {
      currentLine.onDismiss.run();
    }

    Line next = queue.poll();
    if (next == null) {
      hide();
      return;
    }

    currentLine = next;
    typewriterEffect.setText(next.text);
    showImage(next.image);
    if (next.soundPath != null) {
      playLineSound(next.soundPath);
    }

    visible = true;
    root.setVisible(true);
    // UI components are created in hash order, so make sure nothing added later sits over us.
    root.toFront();
    ServiceLocator.getEntityService().setPaused(true);
  }

  private void showImage(Texture texture) {
    if (texture == null) {
      image.setDrawable(null);
      image.setVisible(false);
      imageCell.size(0f);
    } else {
      image.setDrawable(new TextureRegionDrawable(texture));
      image.setVisible(true);
      imageCell.size(ITEM_IMAGE_SIZE);
    }
    scroll.invalidateHierarchy();
  }

  private void playLineSound(String soundPath) {
    try {
      ServiceLocator.getResourceService().getAsset(soundPath, Sound.class).play(1f);
    } catch (GdxRuntimeException e) {
      // Sound not loaded for this line yet — the text still shows fine without it.
    }
  }

  private void onContinueClicked() {
    ButtonSound.playClick();
    if (!typewriterEffect.isComplete()) {
      typewriterEffect.skipToEnd();
      updateMessageLabel();
      return;
    }
    advance();
  }

  private void hide() {
    currentLine = null;
    visible = false;
    root.setVisible(false);
    ServiceLocator.getEntityService().setPaused(false);
    if (onHidden != null) {
      onHidden.run();
    }
  }

  private void updateMessageLabel() {
    if (messageLabel == null) {
      return;
    }
    messageLabel.setText(typewriterEffect.getRevealedText());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (!visible) {
      return;
    }
    typewriterEffect.update(Gdx.graphics.getDeltaTime());
    updateMessageLabel();
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    if (root != null) {
      root.remove();
    }
    super.dispose();
  }
}
