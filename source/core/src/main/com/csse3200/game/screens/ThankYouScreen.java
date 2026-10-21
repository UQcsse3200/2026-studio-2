package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.input.CutsceneInputComponent;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shows a thank you message over a black screen before exiting to the main menu. */
public class ThankYouScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(ThankYouScreen.class);
  private final GdxGame game;
  private final ResourceService resourceService;
  private final RenderService renderService;
  private final SpriteBatch batch;
  private final Stage stage;
  private final TextBoxComponent textBox;
  private final CutsceneInputComponent input;

  public ThankYouScreen(GdxGame game) {
    this.game = game;
    batch = new SpriteBatch();
    stage = new Stage(new ScreenViewport(), batch);

    ServiceLocator.registerInputService(new InputService());
    resourceService = new ResourceService();
    ServiceLocator.registerResourceService(resourceService);
    renderService = new RenderService();
    ServiceLocator.registerRenderService(renderService);
    renderService.setStage(stage);

    resourceService.loadAll();

    textBox =
        new TextBoxComponent(
            new TextBoxComponent.Layout(0, 0, 600, 16, com.badlogic.gdx.utils.Align.center),
            new TextBoxComponent.Style(Color.WHITE, Color.BLACK, Color.WHITE, 3, null),
            20,
            new ArrayList<>(List.of("Thank you for playing our game")),
            null);
    textBox.setExternallyControlled(true);
    textBox.create();
    textBox.setBottomCentered(0.1f);

    input = new CutsceneInputComponent(this::onDismissed);
    input.create();
  }

  private void onDismissed() {
    TextBoxComponent.AdvanceResult result = textBox.advance();
    if (result == TextBoxComponent.AdvanceResult.DISMISSED) {
      game.transitionTo(GdxGame.ScreenType.MAIN_MENU);
    }
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
    renderService.render(batch);
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void dispose() {
    GdxGame.applyDefaultClearColor();
    if (input != null) {
      input.dispose();
    }
    if (textBox != null && !textBox.isDismissed()) {
      textBox.dismiss();
    }
    stage.dispose();
    batch.dispose();
    resourceService.dispose();
    renderService.dispose();
    ServiceLocator.clear();
  }
}
