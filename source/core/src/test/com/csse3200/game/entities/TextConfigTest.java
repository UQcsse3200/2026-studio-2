package com.csse3200.game.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.entities.configs.TextConfig;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for TextConfig. None of this touches Gdx graphics/input, so it runs as a
 * plain JUnit test with no game harness required.
 */
class TextConfigTest {

  @Test
  void defaultsMatchExpectedScrollLook() {
    TextConfig config = new TextConfig();

    assertEquals(100, config.xPos);
    assertEquals(100, config.yPos);
    assertEquals("Color.BLACK", config.textColour);
    assertEquals("Color.TAN", config.backgroundColour);
    assertEquals("Color.BROWN", config.borderColour);
    assertEquals(30f, config.charsPerSecond);
    assertEquals(200, config.maxWidth);
    assertEquals(16, config.padding);
    assertEquals(3, config.borderThickness);
    assertEquals("fonts/scroll.fnt", config.fontPath);
    assertEquals("center", config.textAlignment);
    assertEquals(List.of("Test text"), config.pages);
  }

  @Test
  void pagesListIsMutable() {
    TextConfig config = new TextConfig();
    config.pages.add("Another page");
    assertEquals(2, config.pages.size());
  }

  @Test
  void getTextColourResolvesFullyQualifiedName() {
    TextConfig config = new TextConfig();
    config.textColour = "Color.WHITE";
    assertEquals(Color.WHITE, config.getTextColour());
  }

  @Test
  void getTextColourResolvesBareNameCaseInsensitive() {
    TextConfig config = new TextConfig();
    config.textColour = "white";
    assertEquals(Color.WHITE, config.getTextColour());
  }

  @Test
  void getBackgroundColourFallsBackOnUnknownName() {
    TextConfig config = new TextConfig();
    config.backgroundColour = "Color.NOT_A_REAL_COLOUR";
    assertEquals(Color.TAN, config.getBackgroundColour());
  }

  @Test
  void getBorderColourFallsBackOnNullOrBlank() {
    TextConfig config = new TextConfig();

    config.borderColour = null;
    assertEquals(Color.BROWN, config.getBorderColour());

    config.borderColour = "   ";
    assertEquals(Color.BROWN, config.getBorderColour());
  }

  @Test
  void getTextAlignmentResolvesKnownValues() {
    TextConfig config = new TextConfig();

    config.textAlignment = "left";
    assertEquals(Align.left, config.getTextAlignment());

    config.textAlignment = "right";
    assertEquals(Align.right, config.getTextAlignment());

    config.textAlignment = "center";
    assertEquals(Align.center, config.getTextAlignment());

    config.textAlignment = "CENTRE";
    assertEquals(Align.center, config.getTextAlignment());
  }

  @Test
  void getTextAlignmentFallsBackToCentreForUnknownOrMissingValues() {
    TextConfig config = new TextConfig();

    config.textAlignment = "diagonally";
    assertEquals(Align.center, config.getTextAlignment());

    config.textAlignment = null;
    assertEquals(Align.center, config.getTextAlignment());

    config.textAlignment = "";
    assertEquals(Align.center, config.getTextAlignment());
  }
}
