package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.cutscene.CutsceneScene;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.input.CutsceneInputComponent;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An isolated screen for displaying an ordered cutscene before creating a level. */
public class CutsceneScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(CutsceneScreen.class);

  private enum State {
    FADE_IN,
    TEXT_ACTIVE,
    FADE_TEXT,
    FADE_TO_NEXT,
    FADE_TO_BLACK,
    COMPLETE
  }

  private final GdxGame game;
  private final GdxGame.ScreenType destination;
  private final CutsceneLoader.LoadedCutscene cutscene;
  private final ResourceService resourceService;
  private final RenderService renderService;
  private final SpriteBatch batch;
  private final Stage stage;
  private final Image sceneImage;
  private final Image fadeOverlay;
  private final Texture blackTexture;
  private final CutsceneInputComponent input;

  private Music music;
  private TextBoxComponent textBox;
  private State state = State.FADE_IN;
  private int sceneIndex;
  private float fadeTimer;

  public CutsceneScreen(
      GdxGame game, CutsceneLoader.LoadedCutscene cutscene, GdxGame.ScreenType destination) {
    this(game, cutscene, destination, new SpriteBatch());
    playMusic();
  }

  /** Package-private constructor that accepts a SpriteBatch for testability. */
  CutsceneScreen(
      GdxGame game,
      CutsceneLoader.LoadedCutscene cutscene,
      GdxGame.ScreenType destination,
      SpriteBatch batch) {
    this(game, cutscene, destination, batch, new Stage(new ScreenViewport(), batch));
  }

  /** Package-private constructor that accepts a SpriteBatch and Stage for testability. */
  CutsceneScreen(
      GdxGame game,
      CutsceneLoader.LoadedCutscene cutscene,
      GdxGame.ScreenType destination,
      SpriteBatch batch,
      Stage stage) {
    this.game = game;
    this.cutscene = cutscene;
    this.destination = destination;
    this.batch = batch;
    this.stage = stage;

    ServiceLocator.registerInputService(new InputService());
    resourceService = new ResourceService();
    ServiceLocator.registerResourceService(resourceService);
    renderService = new RenderService();
    ServiceLocator.registerRenderService(renderService);

    renderService.setStage(stage);

    resourceService.loadTextures(cutscene.getImagePaths());
    String musicPath = cutscene.getDefinition().music;
    if (musicPath != null && !musicPath.isBlank()) {
      resourceService.loadMusic(new String[] {musicPath});
    }
    resourceService.loadAll();

    sceneImage = new Image();
    sceneImage.setFillParent(true);
    sceneImage.setScaling(Scaling.fit);
    stage.addActor(sceneImage);

    blackTexture = createBlackTexture();
    fadeOverlay = new Image(new TextureRegionDrawable(new TextureRegion(blackTexture)));
    fadeOverlay.setFillParent(true);
    fadeOverlay.setColor(1f, 1f, 1f, 1f);
    stage.addActor(fadeOverlay);

    input = new CutsceneInputComponent(this::advanceRequested);
    input.create();
    showScene(0);
  }

  private Texture createBlackTexture() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  /** Starts the cutscene's background music, if one is configured and available. */
  private void playMusic() {
    String musicPath = cutscene.getDefinition().music;
    if (musicPath == null || musicPath.isBlank()) {
      return;
    }
    if (!resourceService.containsAsset(musicPath, Music.class)) {
      logger.debug("Cutscene music unavailable: {}", musicPath);
      return;
    }
    music = resourceService.getAsset(musicPath, Music.class);
    music.setLooping(true);
    music.setVolume(0.1f);
    music.play();
  }

  private void showScene(int index) {
    sceneIndex = index;
    sceneImage.setDrawable(
        new TextureRegionDrawable(
            new TextureRegion(
                resourceService.getAsset(cutscene.getImagePaths()[index], Texture.class))));

    if (textBox != null) {
      textBox.dismiss();
    }
    CutsceneScene scene = cutscene.getDefinition().scenes.get(index);
    TextConfig config = scene.text == null ? new TextConfig() : scene.text;
    textBox =
        new TextBoxComponent(
            config.xPos,
            config.yPos,
            config.getTextColour(),
            config.getBackgroundColour(),
            config.getBorderColour(),
            config.charsPerSecond,
            config.maxWidth,
            config.padding,
            config.borderThickness,
            config.fontPath,
            config.getTextAlignment(),
            config.pages);
    textBox.setExternallyControlled(true);
    textBox.create();
    fadeOverlay.toFront();
    fadeTimer = 0f;
    state = State.FADE_IN;
  }

  private void advanceRequested() {
    if (state != State.TEXT_ACTIVE) {
      return;
    }
    TextBoxComponent.AdvanceResult result = textBox.advance();
    if (result == TextBoxComponent.AdvanceResult.DISMISSED) {
      state = State.FADE_TEXT;
      fadeTimer = 0f;
    }
  }

  @Override
  public void render(float delta) {
    updateState(delta);
    if (state == State.COMPLETE) {
      return;
    }
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
    renderService.render(batch);
    stage.act(delta);
    stage.draw();
  }

  private void updateState(float delta) {
    float duration = cutscene.getDefinition().fadeDuration;
    switch (state) {
      case FADE_IN:
        fadeTimer += delta;
        setFadeAlpha(1f - Math.min(fadeTimer / duration, 1f));
        if (fadeTimer >= duration) {
          setFadeAlpha(0f);
          state = State.TEXT_ACTIVE;
        }
        break;
      case FADE_TO_NEXT:
        fadeTimer += delta;
        setFadeAlpha(Math.min(fadeTimer / duration, 1f));
        if (fadeTimer >= duration) {
          showScene(sceneIndex + 1);
        }
        break;
      case FADE_TEXT:
        fadeTimer += delta;
        if (textBox != null) {
          textBox.setOpacity(1f - Math.min(fadeTimer / duration, 1f));
        }
        if (fadeTimer >= duration) {
          if (textBox != null) {
            textBox.dismiss();
          }
          fadeTimer = 0f;
          state =
              sceneIndex == cutscene.getDefinition().scenes.size() - 1
                  ? State.FADE_TO_BLACK
                  : State.FADE_TO_NEXT;
        }
        break;
      case FADE_TO_BLACK:
        fadeTimer += delta;
        setFadeAlpha(Math.min(fadeTimer / duration, 1f));
        if (fadeTimer >= duration) {
          state = State.COMPLETE;
          input.dispose();
          // Fade the destination screen in from black instead of swapping instantly.
          game.transitionTo(destination);
        }
        break;
      case TEXT_ACTIVE:
      case COMPLETE:
        break;
    }
  }

  private void setFadeAlpha(float alpha) {
    fadeOverlay.setColor(1f, 1f, 1f, Math.max(0f, Math.min(1f, alpha)));
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void dispose() {
    logger.debug("Disposing cutscene screen");
    GdxGame.applyDefaultClearColor();
    input.dispose();
    if (textBox != null && !textBox.isDismissed()) {
      textBox.dismiss();
    }
    if (music != null) {
      music.stop();
    }
    stage.dispose();
    batch.dispose();
    blackTexture.dispose();
    resourceService.dispose();
    renderService.dispose();
    ServiceLocator.clear();
  }
}
