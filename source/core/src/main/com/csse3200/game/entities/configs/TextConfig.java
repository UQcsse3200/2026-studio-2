package com.csse3200.game.entities.configs;

import com.badlogic.gdx.graphics.Color;

/** Defines the configs to be loaded by TextBoxFactory */
public class TextConfig {
  // All values are default

  // positions
  public int xPos = 100;
  public int yPos = 100;

  // The box display
  public Color textColour = Color.WHITE;
  public Color backgroundColour = Color.BLACK;
  public Color borderColour = Color.WHITE;

  // Typewriter effect characters per second
  public float charsPerSecond = 30f;

  // For dynamic box sizing
  public int maxWidth = 200;
  public int padding = 16;
  public int borderThickness = 3;

  // The text to be displayed
  public String text = "Test text";
}
