package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.dialogue.TypewriterEffect;

/** Displays a game-ending result in a modal panel over the gameplay. */
public class GameEndDisplay extends UIComponent {
  private static final float Z_INDEX = 20f;
  private static final int PANEL_WIDTH = 520;
  private static final int PANEL_HEIGHT = 310;
  private static final int BORDER_THICKNESS = 3;
  private static final float MESSAGE_SPEED = 30f;

  private static NinePatchDrawable cachedBackground;

  private GameEndState state;
  private final TypewriterEffect typewriterEffect;
  private String resultText;
  private final String titleText;
  private boolean visible = false;

  private Table panel;
  private Label titleLabel;
  private Label messageLabel;

  public GameEndDisplay(GameEndState state) {
    this.state = state;
    this.titleText = state == GameEndState.WIN ? "YOU WIN!" : "GAME OVER!";
    this.resultText =
        state == GameEndState.WIN
            ? "You achieved victory and completed the objective."
            : "better luck next time bub...";
    this.typewriterEffect = new TypewriterEffect(MESSAGE_SPEED);
    this.typewriterEffect.setText(this.resultText);
  }

  public GameEndState getState() {
    return state;
  }

  public void setState(GameEndState state) {
    this.state = state;
    if (titleLabel != null) {
      titleLabel.setText(state == GameEndState.WIN ? "YOU WIN!" : "GAME OVER!");
    }
    String newResultText =
        state == GameEndState.WIN
            ? "You achieved victory and completed the objective."
            : "better luck next time bub...";
    this.resultText = newResultText;
    visible = true;
    if (typewriterEffect != null) {
      typewriterEffect.setText(this.resultText);
    }
    if (panel != null) {
      panel.setVisible(true);
    }
  }

  public String getTitleText() {
    return titleText;
  }

  public String getResultText() {
    return resultText;
  }

  public TypewriterEffect getTypewriterEffect() {
    return typewriterEffect;
  }

  @Override
  public void create() {
    super.create();
    if (ServiceLocator.getGameEndEventHandler() == null) {
      ServiceLocator.registerGameEndEventHandler(new EventHandler());
    }
    ServiceLocator.getGameEndEventHandler().addListener("gameEnd", this::setState);
    buildActors();
  }

  private void buildActors() {
    panel = new Table();
    panel.setFillParent(true);
    panel.setVisible(false);
    panel.setBackground(getBackgroundDrawable());
    panel.center();

    titleLabel = new Label(titleText, skin);
    titleLabel.setFontScale(2f);
    titleLabel.setColor(Color.WHITE);

    messageLabel = new Label("", skin);
    messageLabel.setWrap(true);
    messageLabel.setAlignment(1);
    messageLabel.setColor(Color.WHITE);

    TextButton restartBtn = new TextButton("Restart", skin);
    TextButton mainMenuBtn = new TextButton("Exit to Main Menu", skin);
    TextButton exitDesktopBtn = new TextButton("Exit to Desktop", skin);

    restartBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            entity.getEvents().trigger("restart");
          }
        });

    mainMenuBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            entity.getEvents().trigger("mainMenu");
          }
        });

    exitDesktopBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            entity.getEvents().trigger("exitDesktop");
          }
        });

    panel.add(titleLabel).pad(20f).row();
    panel.add(messageLabel).width(PANEL_WIDTH - 80).pad(10f, 20f, 20f, 20f).row();
    panel.add(restartBtn).padBottom(10f).row();
    panel.add(mainMenuBtn).padBottom(10f).row();
    panel.add(exitDesktopBtn).padBottom(20f).row();

    stage.addActor(panel);
    updateMessageLabel();
  }

  private static NinePatchDrawable getBackgroundDrawable() {
    if (cachedBackground != null) {
      return cachedBackground;
    }

    int size = 16;
    int border = BORDER_THICKNESS + 2;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0.07f, 0.07f, 0.09f, 0.88f));
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

  private void updateMessageLabel() {
    if (messageLabel == null) {
      return;
    }
    String revealedText = typewriterEffect.getRevealedText();
    messageLabel.setText(revealedText);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (panel == null || !visible) {
      return;
    }
    typewriterEffect.update(Gdx.graphics.getDeltaTime());
    updateMessageLabel();
  }

  @Override
  public void dispose() {
    if (panel != null) {
      panel.remove();
    }
    super.dispose();
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }
}
