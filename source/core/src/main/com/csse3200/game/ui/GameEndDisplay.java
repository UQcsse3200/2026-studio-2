package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a game-ending result in a modal panel over the gameplay.
 * Now UI style guide compliant, with parchment scroll background, Pixeloid Sans fonts, and TextButtons swapped with ImageButtons.
 */
public class GameEndDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(GameEndDisplay.class);
  private static final float Z_INDEX = 20f;
  private static final float CHARS_PER_SECOND = 21f;

  private static final String WIN_TITLE = "VICTORY!";
  private static final String LOSE_TITLE = "DEFEAT!";
  private static final String WIN_MESSAGE = "You are one step closer to getting home...";
  private static final String LOSE_MESSAGE = "What would Penelope think...";

  // Style-guide palette (Desert Shores / Sands & Stones)
  private static final Color PARCHMENT = new Color(0.93f, 0.86f, 0.68f, 1f);
  private static final Color ROLLER = new Color(0.56f, 0.44f, 0.33f, 1f);
  private static final Color KNOB = new Color(0.50f, 0.38f, 0.28f, 1f);
  private static final Color TEXT_COLOR = new Color(0.18f, 0.14f, 0.11f, 1f);

  private static NinePatchDrawable cachedBackground;
  private static BitmapFont titleFont;
  private static BitmapFont messageFont;

  private GameEndState state;
  private String resultText;
  private final String titleText;
  private boolean visible = false;

  // Inline typewriter fields (replaces deprecated TypewriterEffect)
  private float typeTimer = 0f;
  private int revealedChars = 0;

  private Table panel;
  private Label titleLabel;
  private Label messageLabel;

  public GameEndDisplay(GameEndState state) {
    logger.info("GameEndDisplay constructor with state: {}", state);
    this.state = state;
    this.titleText = state == GameEndState.WIN ? WIN_TITLE : LOSE_TITLE;
    this.resultText = state == GameEndState.WIN ? WIN_MESSAGE : LOSE_MESSAGE;
  }

  public GameEndState getState() {
    return state;
  }

  public void setState(GameEndState state) {
    logger.info("GameEndDisplay.setState() called with state: {}", state);
    this.state = state;
    if (titleLabel != null) {
      titleLabel.setText(state == GameEndState.WIN ? WIN_TITLE : LOSE_TITLE);
    }
    this.resultText = state == GameEndState.WIN ? WIN_MESSAGE : LOSE_MESSAGE;
    // Reset typewriter for the new message
    typeTimer = 0f;
    revealedChars = 0;
    visible = true;
    // Freeze the game world (entities + physics) so the player can't act while
    // the game-end panel is up. Same mechanism as PauseMenuDisplay.pause().
    if (ServiceLocator.getEntityService() != null) {
      ServiceLocator.getEntityService().setPaused(true);
    }
    if (panel != null) {
      panel.setVisible(true);
      panel.getColor().a = 0f;
      panel.addAction(Actions.fadeIn(0.3f));
      logger.info("Panel shown, size: {} x {}", panel.getWidth(), panel.getHeight());
    } else {
      logger.warn("Panel is NULL in setState() — buildActors() may not have been called.");
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

  /** Returns how many characters of the result text are currently revealed. */
  public int getRevealedChars() {
    return revealedChars;
  }

  @Override
  public void create() {
    logger.info("GameEndDisplay.create() START");
    super.create();
    if (ServiceLocator.getGameEndEventHandler() == null) {
      logger.warn("GameEndEventHandler was null, creating new EventHandler");
      ServiceLocator.registerGameEndEventHandler(new EventHandler());
    }
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
    loadFonts();

    Table root = new Table();
    root.setFillParent(true);

    panel = new Table();
    panel.setVisible(visible);
    panel.setBackground(getBackgroundDrawable());

    // Fixed padding: percent-based padding evaluates to 0 during panel.pack()
    // (root has no width yet), which packs the panel too small and pushes the
    // last button outside the panel's clickable bounds.
    Value padding = new Value.Fixed(20f);

    // Title — Pixeloid Sans Bold, uppercase, warm gold accent
    titleLabel = new Label(titleText, skin, "title");
    if (titleFont != null) {
      Label.LabelStyle titleStyle = new Label.LabelStyle(titleLabel.getStyle());
      titleStyle.font = titleFont;
      titleLabel.setStyle(titleStyle);
    }
    titleLabel.setFontScale(2f);
    titleLabel.setColor(TEXT_COLOR);

    // Message — Pixeloid Sans regular
    messageLabel = new Label("", skin);
    if (messageFont != null) {
      Label.LabelStyle msgStyle = new Label.LabelStyle(messageLabel.getStyle());
      msgStyle.font = messageFont;
      msgStyle.fontColor = TEXT_COLOR;
      messageLabel.setStyle(msgStyle);
    }
    messageLabel.setFontScale(1.5f);
    messageLabel.setWrap(true);
    messageLabel.setAlignment(1);

    ImageButton.ImageButtonStyle backButtonStyle = createButtonStyle(
        "images/Buttons/back_up_btn.png", "images/Buttons/back_down_btn.png");
    ImageButton backBtn = new ImageButton(backButtonStyle);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("mainMenu"));
          }
        });

    // TODO: Restart button commented out — restart mechanism not yet implemented.
    // Uncomment when game-reset flow is wired up.
    //
    // ImageButton.ImageButtonStyle restartButtonStyle = createButtonStyle(
    //     "images/Buttons/restart_up_btn.png", "images/Buttons/restart_down_btn.png");
    // ImageButton restartBtn = new ImageButton(restartButtonStyle);
    // restartBtn.addListener(
    //     new ChangeListener() {
    //       @Override
    //       public void changed(ChangeEvent event, Actor actor) {
    //         ButtonSound.playClickThen(() -> entity.getEvents().trigger("restart"));
    //       }
    //     });

    ImageButton.ImageButtonStyle quitButtonStyle = createButtonStyle(
        "images/Buttons/quit_up_btn.png", "images/Buttons/quit_down_btn.png");
    ImageButton quitBtn = new ImageButton(quitButtonStyle);
    quitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("exitDesktop"));
          }
        });

    // Layout
    panel.add(titleLabel).pad(padding).row();
    panel.add(messageLabel).fillX().expandX().pad(padding).row();
    // panel.add(restartBtn).width(200f).height(70f).padBottom(padding).row();  // TODO: uncomment
    panel.add(backBtn).width(200f).height(70f).padBottom(padding).row();
    panel.add(quitBtn).width(200f).height(70f).padBottom(padding).row();
    panel.pack();

    root.add(panel).width(Value.percentWidth(0.8f, root)).fillX().center();
    stage.addActor(root);
    root.invalidateHierarchy();
    root.layout();
    updateMessageLabel();
  }

  /**
   * Creates an {@link ImageButton.ImageButtonStyle} from up/down texture paths, following the
   * PauseMenuDisplay / MainMenuDisplay pattern.
   */
  private static ImageButton.ImageButtonStyle createButtonStyle(
      String upPath, String downPath) {
    Texture up =
        ServiceLocator.getResourceService().getAsset(upPath, Texture.class);
    Texture down =
        ServiceLocator.getResourceService().getAsset(downPath, Texture.class);
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = new TextureRegionDrawable(up);
    style.down = new TextureRegionDrawable(down);
    return style;
  }

  /**
   * Lazily builds a parchment-scroll NinePatch background matching the TextBoxComponent scroll
   * style and the style-guide Desert Shores palette.
   */
  private static NinePatchDrawable getBackgroundDrawable() {
    if (cachedBackground != null) {
      return cachedBackground;
    }

    int rodHeight = 18;
    int paperEdge = 12;
    int width = paperEdge * 2 + 40;
    int height = rodHeight * 2 + 24;

    Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

    // Parchment fill
    pixmap.setColor(PARCHMENT);
    pixmap.fillRectangle(paperEdge, 0, width - 2 * paperEdge, height);

    // Wooden roller bars (top and bottom)
    pixmap.setColor(ROLLER);
    pixmap.fillRectangle(0, 0, width, rodHeight);
    pixmap.fillRectangle(0, height - rodHeight, width, rodHeight);

    // Roller shading
    Color shadow = ROLLER.cpy().mul(0.7f, 0.7f, 0.7f, 1f);
    pixmap.setColor(shadow);
    pixmap.drawLine(0, rodHeight / 2, width, rodHeight / 2);
    pixmap.drawLine(0, height - rodHeight / 2, width, height - rodHeight / 2);

    // Rounded knobs at corners
    int knobRadius = Math.min(paperEdge, rodHeight) / 2;
    pixmap.setColor(KNOB);
    pixmap.fillCircle(knobRadius, knobRadius, knobRadius);
    pixmap.fillCircle(width - knobRadius, knobRadius, knobRadius);
    pixmap.fillCircle(knobRadius, height - knobRadius, knobRadius);
    pixmap.fillCircle(width - knobRadius, height - knobRadius, knobRadius);

    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, paperEdge, paperEdge, rodHeight, rodHeight);
    cachedBackground = new NinePatchDrawable(patch);
    return cachedBackground;
  }

  private void updateMessageLabel() {
    if (messageLabel == null || resultText == null) {
      return;
    }
    int end = Math.min(revealedChars, resultText.length());
    messageLabel.setText(resultText.substring(0, end));
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (panel == null || !visible) {
      return;
    }
    // Inline typewriter reveal
    typeTimer += Gdx.graphics.getDeltaTime();
    revealedChars = Math.min(resultText.length(), (int) (typeTimer * CHARS_PER_SECOND));
    updateMessageLabel();
  }

  @Override
  public void dispose() {
    // Release the freeze applied in setState() so the next screen isn't paused.
    if (ServiceLocator.getEntityService() != null) {
      ServiceLocator.getEntityService().setPaused(false);
    }
    if (panel != null) {
      panel.remove();
    }
    super.dispose();
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  /** Preloads fonts used by this display. Call from screen's loadAssets(). */
  public static void loadFonts() {
    if (titleFont == null) {
      try {
        titleFont = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/PixeloidSans-Bold.fnt"));
      } catch (Exception e) {
        logger.warn("Could not load PixeloidSans-Bold font: {}", e.getMessage());
      }
    }
    if (messageFont == null) {
      try {
        messageFont = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/PixeloidSans.fnt"));
      } catch (Exception e) {
        logger.warn("Could not load PixeloidSans font: {}", e.getMessage());
      }
    }
  }

  /** Disposes preloaded fonts. Call from screen's unloadAssets() or dispose(). */
  public static void unloadFonts() {
    if (titleFont != null) {
      titleFont.dispose();
      titleFont = null;
    }
    if (messageFont != null) {
      messageFont.dispose();
      messageFont = null;
    }
  }
}
