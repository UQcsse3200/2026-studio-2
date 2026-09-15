package com.csse3200.game.cutscene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class CutsceneLoaderTest {
  @Test
  void shouldLoadJpegFolderWithoutManifest() {
    CutsceneLoader.Result result = new CutsceneLoader().load("cutscene1");

    assertTrue(result.isSuccess());
    assertEquals(3, result.getCutscene().getImagePaths().length);
    assertTrue(result.getCutscene().getImagePaths()[0].endsWith("scene1.jpeg"));
  }

  @Test
  void shouldRejectUnsafeNames() {
    assertFalse(new CutsceneLoader().load("../cutscene1").isSuccess());
    assertFalse(new CutsceneLoader().load("/cutscene1").isSuccess());
    assertFalse(new CutsceneLoader().load("").isSuccess());
  }
}
