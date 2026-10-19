package com.csse3200.game.components.mainmenu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.UiTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainMenuDisplayTest extends UiTestSupport {
  // The background is drawn slightly larger than the window so its shake never shows an edge.
  private static final float OVERSCAN = 1.03f;

  private Graphics previousGraphics;
  private Graphics graphics;

  @BeforeEach
  void setUpGraphics() {
    previousGraphics = Gdx.graphics;
    graphics = mock(Graphics.class);
    when(graphics.getWidth()).thenReturn(1600);
    when(graphics.getHeight()).thenReturn(900);
    Gdx.graphics = graphics;
  }

  @AfterEach
  void restoreGraphics() {
    Gdx.graphics = previousGraphics;
  }

  @Test
  void shouldFitTheBackgroundToTheWindowAgainAfterAResizeWithoutDuplicatingActors() {
    MainMenuDisplay display = new MainMenuDisplay();
    register(new Entity().addComponent(display));
    int actorCount = stage.getActors().size;
    assertEquals(1600 * OVERSCAN, background(display).getWidth(), 0.01f);

    when(graphics.getWidth()).thenReturn(800);
    when(graphics.getHeight()).thenReturn(600);
    display.resize();

    assertEquals(actorCount, stage.getActors().size);
    assertEquals(800 * OVERSCAN, background(display).getWidth(), 0.01f);
    assertEquals(600 * OVERSCAN, background(display).getHeight(), 0.01f);
  }

  private Image background(MainMenuDisplay display) {
    return field(display, "backgroundImage", Image.class);
  }
}
