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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.dialogue.TypewriterEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Displays a game-ending result in a modal panel over the gameplay. */
public class GameEndDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(GameEndDisplay.class);
  private static final float Z_INDEX = 20f;
  private static final int BORDER_THICKNESS = 3;
  private static final float BUTTON_WIDTH = 200f;
  private static final float BUTTON_HEIGHT = 70f;

  private static final float MESSAGE_SPEED = 21f;

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
    logger.info(">>> GameEndDisplay CONSTRUCTOR START with state: {}", state);
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
    logger.info("GameEndDisplay.setState() called with state: {}", state);
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
      logger.info(
          "Panel is not null, setting visibility to true. Panel size: {} x {}",
          panel.getWidth(),
          panel.getHeight());
      panel.setVisible(true);
    } else {
      logger.warn("Panel is NULL in setState()! buildActors() may not have been called.");
    }
    if (entity != null) {
      MainGameExitDisplay exitDisplay = entity.getComponent(MainGameExitDisplay.class);
      if (exitDisplay != null) {
        exitDisplay.setVisible(false);
      }
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
    logger.info(">>> GameEndDisplay.create() START");
    super.create();
    if (ServiceLocator.getGameEndEventHandler() == null) {
      logger.warn("GameEndEventHandler was null, creating new EventHandler");
      ServiceLocator.registerGameEndEventHandler(new EventHandler());
    }
    logger.info(">>> Registering gameEnd listener for GameEndDisplay");
    ServiceLocator.getGameEndEventHandler()
        .addListener(
            "gameEnd",
            new com.csse3200.game.events.listeners.EventListener1<GameEndState>() {
              @Override
              public void handle(GameEndState state) {
                logger.info("GameEndDisplay received gameEnd event with state: {}", state);
                setState(state);
              }
            });
    buildActors();
  }

  private void buildActors() {
    Table root = new Table();
    root.setFillParent(true);

    panel = new Table();
    panel.setVisible(visible);
    panel.setBackground(getBackgroundDrawable());
    Value padding = Value.percentWidth(0.02f, root);

    titleLabel = new Label(titleText, skin);
    titleLabel.setFontScale(2f);
    titleLabel.setColor(Color.WHITE);

    messageLabel = new Label("", skin);
    messageLabel.setWrap(true);
    messageLabel.setAlignment(1);
    messageLabel.setColor(Color.WHITE);

    Texture restartUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/restart_up_btn.png", Texture.class);
    Texture restartDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/restart_down_btn.png", Texture.class);
    Texture mainMenuUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/main_menu_up_btn.png", Texture.class);
    Texture mainMenuDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/main_menu_down_btn.png", Texture.class);
    Texture exitGameUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_game_up_btn.png", Texture.class);
    Texture exitGameDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_game_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle restartButtonStyle = new ImageButton.ImageButtonStyle();
    restartButtonStyle.up = new TextureRegionDrawable(restartUpTexture);
    restartButtonStyle.down = new TextureRegionDrawable(restartDownTexture);
    ImageButton restartBtn = new ImageButton(restartButtonStyle);
    restartBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            entity.getEvents().trigger("restart");
          }
        });

    ImageButton.ImageButtonStyle mainMenuButtonStyle = new ImageButton.ImageButtonStyle();
    mainMenuButtonStyle.up = new TextureRegionDrawable(mainMenuUpTexture);
    mainMenuButtonStyle.down = new TextureRegionDrawable(mainMenuDownTexture);
    ImageButton mainMenuBtn = new ImageButton(mainMenuButtonStyle);
    mainMenuBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            entity.getEvents().trigger("mainMenu");
          }
        });

    ImageButton.ImageButtonStyle exitGameButtonStyle = new ImageButton.ImageButtonStyle();
    exitGameButtonStyle.up = new TextureRegionDrawable(exitGameUpTexture);
    exitGameButtonStyle.down = new TextureRegionDrawable(exitGameDownTexture);
    ImageButton exitGameBtn = new ImageButton(exitGameButtonStyle);
    exitGameBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            entity.getEvents().trigger("exitGame");
          }
        });

    panel.add(titleLabel).pad(padding).row();
    panel.add(messageLabel).fillX().expandX().pad(padding).row();
    panel.add(restartBtn).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(padding).row();
    panel.add(mainMenuBtn).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(padding).row();
    panel.add(exitGameBtn).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(padding).row();
    panel.pack();

    root.add(panel).width(Value.percentWidth(0.8f, root)).fillX().center();
    stage.addActor(root);
    root.invalidateHierarchy();
    root.layout();
    updateMessageLabel();
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
