package com.csse3200.game.ui.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
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
 * A reusable "blurred background + (optional image) + text + Continue button" overlay. Showing a
 * line pauses the game (which blurs the world via {@code Renderer}); the overlay itself is UI and
 * always renders sharp. Dismissing the last queued line unpauses and hides it.
 */
public class InstructionOverlay extends UIComponent {
  private static final float Z_INDEX = 22f;
  private static final int BORDER_THICKNESS = 3;
  private static final float MESSAGE_SPEED = 30f;
  private static final float IMAGE_SIZE = 220f;

  private static NinePatchDrawable cachedBackground;

  /**
   * One line of text, with an optional sound clip, an optional image shown above the text (e.g. an
   * item that was just found), and a callback for when it is dismissed.
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
  private Table panel;
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
    root = new Table();
    root.setFillParent(true);

    panel = new Table();
    panel.setVisible(false);
    panel.setBackground(getBackgroundDrawable());
    Value padding = Value.percentWidth(0.02f, root);

    image = new Image();
    image.setScaling(Scaling.fit);

    messageLabel = new Label("", skin);
    messageLabel.setWrap(true);
    messageLabel.setAlignment(1);
    messageLabel.setColor(Color.WHITE);

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

    imageCell = panel.add(image).size(0f).padTop(padding);
    panel.row();
    panel.add(messageLabel).fillX().expandX().pad(padding).row();
    panel.add(continueButton).width(160f).height(56f).padTop(padding).row();
    panel.pack();

    root.add(panel).width(Value.percentWidth(0.6f, root)).fillX().center();
    stage.addActor(root);
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
    panel.setVisible(true);
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
      imageCell.size(IMAGE_SIZE);
    }
    panel.invalidateHierarchy();
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
    panel.setVisible(false);
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

  private static NinePatchDrawable getBackgroundDrawable() {
    if (cachedBackground != null) {
      return cachedBackground;
    }

    int size = 16;
    int border = BORDER_THICKNESS + 2;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0.35f, 0.35f, 0.38f, 0.88f));
    pixmap.fill();
    pixmap.setColor(new Color(0.85f, 0.8f, 0.4f, 1f));
    for (int i = 0; i < border; i++) {
      pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, border, border, border, border);
    cachedBackground = new NinePatchDrawable(patch);
    return cachedBackground;
  }
}
