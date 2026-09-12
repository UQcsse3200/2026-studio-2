package com.csse3200.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core rendering system for the game. Controls the game's camera and runs rendering on all
 * renderables each frame.
 */
public class Renderer implements Disposable {
  private static final float GAME_SCREEN_WIDTH = 20f;

  /**
   * How much smaller the offscreen buffer is than the screen when blurring the paused game world.
   * Rendering small then stretching back up with linear filtering gives a cheap but convincing
   * blur, no custom shader needed.
   */
  private static final int BLUR_DOWNSCALE = 4;

  private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

  private CameraComponent camera;
  private float gameWidth;
  private SpriteBatch batch;
  private Stage stage;
  private RenderService renderService;
  private DebugRenderer debugRenderer;
  private FrameBuffer blurFrameBuffer;
  private final Matrix4 screenProjection = new Matrix4();

  /**
   * Create a new renderer with default settings
   *
   * @param camera camera to render to
   */
  public Renderer(CameraComponent camera) {
    SpriteBatch spriteBatch = new SpriteBatch();
    DebugRenderer debugRenderer = new DebugRenderer();
    debugRenderer.setActive(false);

    init(
        camera,
        GAME_SCREEN_WIDTH,
        spriteBatch,
        new Stage(new ScreenViewport(), spriteBatch),
        ServiceLocator.getRenderService(),
        debugRenderer);
  }

  /**
   * Create a renderer
   *
   * @param camera Camera to use for rendering.
   * @param gameWidth Desired game width in metres the screen should show. Height is then based on *
   *     the aspect ratio.
   * @param batch Batch to render to.
   * @param stage Scene2D stage for UI rendering
   * @param renderService Render service to use
   * @param debugRenderer Debug renderer to render
   */
  public Renderer(
      CameraComponent camera,
      float gameWidth,
      SpriteBatch batch,
      Stage stage,
      RenderService renderService,
      DebugRenderer debugRenderer) {
    init(camera, gameWidth, batch, stage, renderService, debugRenderer);
  }

  private void init(
      CameraComponent camera,
      float gameWidth,
      SpriteBatch batch,
      Stage stage,
      RenderService renderService,
      DebugRenderer debugRenderer) {

    this.camera = camera;
    this.gameWidth = gameWidth;
    this.batch = batch;
    this.stage = stage;
    this.renderService = renderService;
    this.debugRenderer = debugRenderer;

    renderService.setStage(stage);
    renderService.setDebug(debugRenderer);
    resizeCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  public CameraComponent getCamera() {
    return camera;
  }

  /** Render everything to the render service. */
  public void render() {
    Matrix4 projMatrix = camera.getProjectionMatrix();
    EntityService entityService = ServiceLocator.getEntityService();
    boolean paused = entityService != null && entityService.getPaused();

    if (paused) {
      renderWorldBlurred(projMatrix);
    } else {
      renderWorldSharp(projMatrix);
    }

    stage.act();
    stage.draw();
  }

  private void renderWorldSharp(Matrix4 projMatrix) {
    batch.setProjectionMatrix(projMatrix);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // The stage shares this batch and may leave its color/alpha tinted (e.g. a fading UI
    // actor) after its last draw call. Reset it before world rendering so that tint doesn't
    // bleed into the next frame.
    batch.setColor(1f, 1f, 1f, 1f);
    batch.begin();
    renderService.render(batch);
    batch.end();
    debugRenderer.render(projMatrix);
  }

  /**
   * Renders the game world into a small offscreen buffer, then stretches it back up onto the
   * screen with linear filtering. The UI stage (including the pause menu itself) is drawn
   * afterwards, on top, at full sharpness.
   */
  private void renderWorldBlurred(Matrix4 projMatrix) {
    ensureBlurFrameBuffer();

    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();

    blurFrameBuffer.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    batch.setProjectionMatrix(projMatrix);
    batch.setColor(1f, 1f, 1f, 1f);
    batch.begin();
    renderService.render(batch);
    batch.end();
    blurFrameBuffer.end();

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    screenProjection.setToOrtho2D(0, 0, screenWidth, screenHeight);
    batch.setProjectionMatrix(screenProjection);
    batch.begin();
    Texture blurredTexture = blurFrameBuffer.getColorBufferTexture();
    // FrameBuffer textures are stored upside-down relative to the screen, so flip on draw.
    batch.draw(
        blurredTexture,
        0,
        0,
        screenWidth,
        screenHeight,
        0,
        0,
        blurredTexture.getWidth(),
        blurredTexture.getHeight(),
        false,
        true);
    batch.end();
  }

  private void ensureBlurFrameBuffer() {
    int width = Math.max(1, Gdx.graphics.getWidth() / BLUR_DOWNSCALE);
    int height = Math.max(1, Gdx.graphics.getHeight() / BLUR_DOWNSCALE);

    if (blurFrameBuffer != null
        && blurFrameBuffer.getWidth() == width
        && blurFrameBuffer.getHeight() == height) {
      return;
    }

    if (blurFrameBuffer != null) {
      blurFrameBuffer.dispose();
    }

    blurFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    blurFrameBuffer
        .getColorBufferTexture()
        .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
  }

  /**
   * Resize the renderer to a new screen size.
   *
   * @param width new screen width
   * @param height new screen height
   */
  public void resize(int width, int height) {
    resizeCamera(width, height);
    resizeStage(width, height);
    logger.debug("Resizing to ({}x{})", width, height);
  }

  /**
   * @return The debug renderer attached to this renderer
   */
  public DebugRenderer getDebug() {
    return debugRenderer;
  }

  private void resizeCamera(int screenWidth, int screenHeight) {
    camera.resize(screenWidth, screenHeight, gameWidth);
  }

  private void resizeStage(int screenWidth, int screenHeight) {
    stage.getViewport().update(screenWidth, screenHeight, true);
  }

  @Override
  public void dispose() {
    if (blurFrameBuffer != null) {
      blurFrameBuffer.dispose();
    }
    stage.dispose();
    batch.dispose();
  }

  public Stage getStage() {
    return stage;
  }
}
