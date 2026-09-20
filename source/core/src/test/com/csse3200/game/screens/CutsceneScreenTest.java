package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.cutscene.CutsceneDefinition;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.cutscene.CutsceneScene;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.extensions.GameExtension;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration tests for {@link CutsceneScreen}. Uses the real cutscene1 assets (JPEGs in
 * images/cutscenes/cutscene1/) and drives the state machine via {@code render()} with large deltas
 * to skip through fades instantly.
 *
 * <p>Requires the headless libGDX backend to support texture loading. Run locally with {@code
 * ./gradlew :core:test} if the CI environment does not support it.
 */
@ExtendWith(GameExtension.class)
class CutsceneScreenTest {

  @BeforeEach
  void beforeEach() {
    Gdx.gl20 = mock(GL20.class);
    Gdx.gl = Gdx.gl20;
    Gdx.graphics = mock(Graphics.class);
    when(Gdx.graphics.getWidth()).thenReturn(800);
    when(Gdx.graphics.getHeight()).thenReturn(600);
  }

  /** Builds a 2-scene cutscene with empty pages (advances immediately on TAB). */
  private CutsceneLoader.LoadedCutscene makeTwoSceneCutscene() {
    CutsceneDefinition def = new CutsceneDefinition();
    def.fadeDuration = 0.4f;

    CutsceneScene s1 = new CutsceneScene();
    s1.image = "scene1.jpeg";
    s1.text = new TextConfig();
    s1.text.pages = new ArrayList<>();

    CutsceneScene s2 = new CutsceneScene();
    s2.image = "scene2.jpeg";
    s2.text = new TextConfig();
    s2.text.pages = new ArrayList<>();

    def.scenes.add(s1);
    def.scenes.add(s2);

    return new CutsceneLoader.LoadedCutscene(
        "cutscene1",
        def,
        new String[] {
          "images/cutscenes/cutscene1/scene1.jpeg", "images/cutscenes/cutscene1/scene2.jpeg"
        });
  }

  /** Builds a 1-scene cutscene. */
  private CutsceneLoader.LoadedCutscene makeSingleSceneCutscene() {
    CutsceneDefinition def = new CutsceneDefinition();
    def.fadeDuration = 0.4f;

    CutsceneScene s1 = new CutsceneScene();
    s1.image = "scene1.jpeg";
    s1.text = new TextConfig();
    s1.text.pages = new ArrayList<>();

    def.scenes.add(s1);

    return new CutsceneLoader.LoadedCutscene(
        "cutscene1", def, new String[] {"images/cutscenes/cutscene1/scene1.jpeg"});
  }

  /**
   * Drives the screen through one scene cycle: render through fade-in to TEXT_ACTIVE, then TAB to
   * dismiss the textbox.
   */
  private void driveToTextActive(CutsceneScreen screen) {
    screen.render(10f); // FADE_IN → TEXT_ACTIVE
  }

  private void advanceViaTab() {
    // The InputService set up by CutsceneScreen is the current one on the ServiceLocator.
    // Dispatching keyDown here routes to the registered CutsceneInputComponent.
    com.csse3200.game.services.ServiceLocator.getInputService().keyDown(Keys.TAB);
  }

  @Test
  void shouldCompleteTwoSceneCutsceneAndSetDestination() {
    GdxGame game = mock(GdxGame.class);
    CutsceneLoader.LoadedCutscene cutscene = makeTwoSceneCutscene();
    SpriteBatch batch = mock(SpriteBatch.class);
    Stage stage = mock(Stage.class);

    CutsceneScreen screen =
        new CutsceneScreen(game, cutscene, GdxGame.ScreenType.MAIN_GAME, batch, stage);

    // Scene 0: fade in → TAB → fade text → fade to next
    driveToTextActive(screen);
    advanceViaTab();
    screen.render(10f); // FADE_TEXT → FADE_TO_NEXT
    screen.render(10f); // FADE_TO_NEXT → showScene(1) → FADE_IN

    // Scene 1 (last): fade in → TAB → fade text → fade to black → complete
    driveToTextActive(screen);
    advanceViaTab();
    screen.render(10f); // FADE_TEXT → FADE_TO_BLACK
    screen.render(10f); // FADE_TO_BLACK → COMPLETE → game.setScreen(...)

    verify(game).setScreen(GdxGame.ScreenType.MAIN_GAME);
  }

  @Test
  void shouldCompleteSingleSceneCutscene() {
    GdxGame game = mock(GdxGame.class);
    CutsceneLoader.LoadedCutscene cutscene = makeSingleSceneCutscene();
    SpriteBatch batch = mock(SpriteBatch.class);
    Stage stage = mock(Stage.class);

    CutsceneScreen screen =
        new CutsceneScreen(game, cutscene, GdxGame.ScreenType.LEVEL_1_GAME, batch, stage);

    driveToTextActive(screen);
    advanceViaTab();
    screen.render(10f); // FADE_TEXT → FADE_TO_BLACK
    screen.render(10f); // FADE_TO_BLACK → COMPLETE → game.setScreen(...)

    verify(game).setScreen(GdxGame.ScreenType.LEVEL_1_GAME);
  }

  @Test
  void shouldRenderWithoutCrashing() {
    GdxGame game = mock(GdxGame.class);
    CutsceneLoader.LoadedCutscene cutscene = makeSingleSceneCutscene();
    SpriteBatch batch = mock(SpriteBatch.class);
    Stage stage = mock(Stage.class);

    CutsceneScreen screen =
        new CutsceneScreen(game, cutscene, GdxGame.ScreenType.MAIN_GAME, batch, stage);

    // Should not throw
    screen.render(0.016f);
  }

  @Test
  void shouldIgnoreAdvanceDuringFadeIn() {
    GdxGame game = mock(GdxGame.class);
    CutsceneLoader.LoadedCutscene cutscene = makeSingleSceneCutscene();
    SpriteBatch batch = mock(SpriteBatch.class);
    Stage stage = mock(Stage.class);

    CutsceneScreen screen =
        new CutsceneScreen(game, cutscene, GdxGame.ScreenType.MAIN_GAME, batch, stage);

    // Still in FADE_IN — TAB should be ignored
    advanceViaTab();
    screen.render(10f); // now transitions FADE_IN → TEXT_ACTIVE

    // The textbox should still be active (not dismissed during FADE_IN)
    advanceViaTab(); // dismiss
    screen.render(10f); // FADE_TEXT → FADE_TO_BLACK
    screen.render(10f); // COMPLETE

    verify(game).setScreen(GdxGame.ScreenType.MAIN_GAME);
  }

  @Test
  void shouldDisposeWithoutError() {
    GdxGame game = mock(GdxGame.class);
    CutsceneLoader.LoadedCutscene cutscene = makeSingleSceneCutscene();
    SpriteBatch batch = mock(SpriteBatch.class);
    Stage stage = mock(Stage.class);

    CutsceneScreen screen =
        new CutsceneScreen(game, cutscene, GdxGame.ScreenType.MAIN_GAME, batch, stage);
    screen.render(0.016f);
    assertDoesNotThrow(screen::dispose);
  }
}
