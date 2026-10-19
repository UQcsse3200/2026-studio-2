package com.csse3200.game.ui;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Displays a game-ending result in a modal panel over the gameplay. */
public class GameEndDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(GameEndDisplay.class);
  private static final float Z_INDEX = 20f;
  // private static final int BORDER_THICKNESS = 3;
  private static final float BUTTON_WIDTH = 200f;
  private static final float BUTTON_HEIGHT = 70f;

  private static final float MESSAGE_CHARS_PER_SECOND = 21f;

  /** Fraction of the stage height where the top of the result message sits. */
  private static final float MESSAGE_Y_FRACTION = 0.68f;

  /** Gap in pixels kept between the top of the restart button and the message above it. */
  private static final float MESSAGE_BUTTON_GAP = 24f;

  /** Breathing room in pixels between the title and the buttons below it. */
  private static final float TITLE_BUTTON_SPACING = 90f;

  /** Fraction of the stage width used as the message wrap width. */
  private static final float MESSAGE_WIDTH_FRACTION = 0.7f;

  private static final Color TRANSPARENT = new Color(0f, 0f, 0f, 0f);

  // private static NinePatchDrawable cachedBackground;

  private GameEndState state;
  private String resultText;
  private final String titleText;
  private boolean visible = false;

  private Table root;
  private Stack stack;
  private Table backgroundTable;
  private Image background;
  private Table panel;
  private Label titleLabel;
  private TextBoxComponent messageBox;
  private ImageButton restartBtn;
  private Entity backdropEntity;

  public GameEndDisplay(GameEndState state) {
    logger.info(">>> GameEndDisplay CONSTRUCTOR START with state: {}", state);
    this.state = state;
    this.titleText = state == GameEndState.WIN ? "YOU WIN!" : "GAME OVER!";
    this.resultText = resultTextFor(state);
  }

  private static String resultTextFor(GameEndState state) {
    return state == GameEndState.WIN
        ? "You are getting closer to home, but the journey is not over yet..."
        : "What would Penelepe say...";
  }

  public GameEndState getState() {
    return state;
  }

  public void setState(GameEndState state) {
    logger.info("GameEndDisplay.setState() called with state: {}", state);
    this.state = state;
    if (titleLabel != null) {
      titleLabel.setText(state == GameEndState.WIN ? "Victory!" : "Defeat!");
    }
    this.resultText = resultTextFor(state);
    visible = true;
    if (panel != null) {
      logger.info(
          "Panel is not null, setting visibility to true. Panel size: {} x {}",
          panel.getWidth(),
          panel.getHeight());
      panel.setVisible(true);
      root.setVisible(true);
      showBackdrop();
    } else {
      logger.warn("Panel is NULL in setState()! buildActors() may not have been called.");
    }
    // Rebuild after the panel/backdrop are shown: the message box is a standalone stage actor,
    // so it must be (re)created on top of the fullscreen backdrop. showBackdrop() brings the
    // backdrop and panel forward, then the fresh box goes above them.
    rebuildMessageBox();
    if (messageBox != null) {
      messageBox.toFront();
    }
    if (entity != null) {
      MainGameExitDisplay exitDisplay = entity.getComponent(MainGameExitDisplay.class);
      if (exitDisplay != null) {
        exitDisplay.setVisible(false);
      }
    }
    try {
      Music gameplay =
          ServiceLocator.getResourceService().getAsset("sounds/gameplay_bg.ogg", Music.class);
      gameplay.stop();
    } catch (Exception e) {
      logger.warn("Could not stop gameplay music: {}", e.getMessage());
    }
    try {
      if (state == GameEndState.WIN) {
        Music music =
            ServiceLocator.getResourceService().getAsset("sounds/Win_music.mp3", Music.class);
        music.setLooping(true);
        GameVolume.setMusicVolume(music, 0.13f);
        music.play();
      } else {
        Music music =
            ServiceLocator.getResourceService().getAsset("sounds/Death_music.ogg", Music.class);
        music.setLooping(false);
        GameVolume.setMusicVolume(music, 0.4f);
        music.play();
      }
    } catch (Exception e) {
      logger.warn("Could not play game end music: {}", e.getMessage());
    }
  }

  /**
   * Captures the gameplay frame right now (the moment the game actually ended) and shows it blurred
   * behind the panel. Must run here, not in buildActors()/create() — those fire once when the
   * screen loads, long before there's a real gameplay frame to capture. Only ever creates one
   * backdrop per screen; setState() can fire more than once (e.g. WIN then a later LOSE).
   */
  private void showBackdrop() {
    if (backdropEntity != null) {
      return;
    }
    BlurredBackdropDisplay backdrop = new BlurredBackdropDisplay(ScreenBlur.capture());
    backdropEntity = new Entity().addComponent(backdrop);
    ServiceLocator.getEntityService().register(backdropEntity);
    // Entity.create() runs components in hash order, not the order added, so the panel's own
    // root table (already on stage) needs to be explicitly brought back above the backdrop image.
    backdrop.toFront();
    root.toFront();
  }

  public String getTitleText() {
    return titleText;
  }

  public String getResultText() {
    return resultText;
  }

  TextBoxComponent getMessageBox() {
    return messageBox;
  }

  /**
   * Recreates the result message box for the current result text, mirroring how cutscenes rebuild
   * their textbox per scene. The box reveals characters over time using TextBoxComponent's own
   * typing effect. Transparent colors keep the panel's existing look (no nested scroll box).
   */
  private void rebuildMessageBox() {
    if (messageBox != null) {
      messageBox.dismiss();
      messageBox = null;
    }
    if (stage == null) {
      return;
    }
    messageBox =
        new TextBoxComponent(
            stage.getWidth() / 2f,
            stage.getHeight() * MESSAGE_Y_FRACTION,
            Color.BLACK,
            TRANSPARENT,
            TRANSPARENT,
            MESSAGE_CHARS_PER_SECOND,
            (int) (stage.getWidth() * MESSAGE_WIDTH_FRACTION),
            10,
            3,
            null,
            Align.center,
            List.of(resultText),
            List.of());
    messageBox.setExternallyControlled(true);
    messageBox.create();
  }

  /**
   * Keeps the message box anchored in the panel's message row, just above the restart button.
   * Tracking the button (rather than a fixed stage fraction) keeps the text clear of the title and
   * buttons on any resolution or with any message length. The box's bottom edge is pinned above the
   * button so taller (wrapped) messages grow upward, never over the buttons.
   */
  private void positionMessageBox() {
    if (messageBox == null || stage == null) {
      return;
    }
    if (restartBtn != null && restartBtn.getStage() != null) {
      Vector2 buttonTopCenter =
          restartBtn.localToStageCoordinates(
              new Vector2(restartBtn.getWidth() / 2f, restartBtn.getHeight()));
      messageBox.setPosition(
          buttonTopCenter.x, buttonTopCenter.y + MESSAGE_BUTTON_GAP, Align.bottom);
      return;
    }
    messageBox.setPosition(stage.getWidth() / 2f, stage.getHeight() * MESSAGE_Y_FRACTION);
  }

  public boolean isVisible() {
    return visible;
  }

  /** Hides the game-end panel so gameplay can resume after a checkpoint restart. */
  public void hide() {
    visible = false;
    if (messageBox != null) {
      messageBox.dismiss();
    }
    if (panel != null) {
      panel.setVisible(false);
    }
    if (root != null) {
      root.setVisible(false);
    }
    if (backdropEntity != null) {
      try {
        ServiceLocator.getEntityService().scheduleRemoval(backdropEntity);
      } catch (Exception e) {
        logger.warn("Could not remove game-end backdrop: {}", e.getMessage());
      }
      backdropEntity = null;
    }
    try {
      Music win = ServiceLocator.getResourceService().getAsset("sounds/Win_music.mp3", Music.class);
      win.stop();
    } catch (Exception ignored) {
      // Music may not be loaded in all screens/tests.
    }
    try {
      Music death =
          ServiceLocator.getResourceService().getAsset("sounds/Death_music.ogg", Music.class);
      death.stop();
    } catch (Exception ignored) {
      // Music may not be loaded in all screens/tests.
    }
    if (entity != null) {
      MainGameExitDisplay exitDisplay = entity.getComponent(MainGameExitDisplay.class);
      if (exitDisplay != null) {
        exitDisplay.setVisible(true);
      }
    }
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
    root = new Table();
    root.setFillParent(true);

    stack = new Stack();
    Texture backgroundTexture =
        ServiceLocator.getResourceService().getAsset("images/ui/scroll_bg.png", Texture.class);

    backgroundTable = new Table();
    background = new Image(backgroundTexture);
    backgroundTable.add(background);
    // root.add(stack).fill().expand();
    panel = new Table();
    panel.setVisible(visible);
    // panel.setBackground(getBackgroundDrawable());
    Value padding = Value.percentWidth(0.01f, root);

    titleLabel = new Label(titleText, skin);
    titleLabel.setFontScale(2f);
    titleLabel.setColor(Color.WHITE);

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
    restartBtn = new ImageButton(restartButtonStyle);
    restartBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("restart"));
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
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("mainMenu"));
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
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("exitGame"));
          }
        });

    panel.add(titleLabel).pad(padding).padBottom(TITLE_BUTTON_SPACING).row();
    panel.add(restartBtn).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(padding).row();
    panel.add(mainMenuBtn).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(padding).row();
    panel.add(exitGameBtn).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(padding).row();
    panel.pack();

    stack.add(backgroundTable);
    stack.add(panel);
    root.add(stack).width(Value.percentWidth(0.8f, root)).fillX().center();
    // Gate visibility on root, not just panel: everything in the stack (including the scroll
    // background) sits directly on the stage via root, so hiding only panel left the background
    // showing on its own before the game had actually ended.
    root.setVisible(visible);
    stage.addActor(root);
    root.invalidateHierarchy();
    root.layout();
    // No message box until the game actually ends: the box is a standalone stage actor that
    // reveals itself on its own draw loop, so creating it here would type the result text over
    // live gameplay from boot. setState() builds it when the game ends.
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (panel == null || !visible) {
      return;
    }
    positionMessageBox();
  }

  @Override
  public void dispose() {
    if (messageBox != null) {
      messageBox.dismiss();
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
}
