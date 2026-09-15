package com.csse3200.game.cutscene;

import java.util.ArrayList;
import java.util.List;

/** Data loaded from a cutscene manifest. */
public class CutsceneDefinition {
  public float fadeDuration = 0.4f;
  public List<CutsceneScene> scenes = new ArrayList<>();
}
