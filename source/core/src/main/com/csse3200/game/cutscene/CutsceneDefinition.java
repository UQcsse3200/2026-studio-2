package com.csse3200.game.cutscene;

import java.util.ArrayList;
import java.util.List;

/** Data loaded from a cutscene manifest. */
public class CutsceneDefinition {
  public float fadeDuration = 0.4f;

  /** Background music path, relative to assets. Defaults to the main menu track. */
  public String music = "sounds/Main_menu_sound.mp3";

  public List<CutsceneScene> scenes = new ArrayList<>();
}
