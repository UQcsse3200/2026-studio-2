package com.csse3200.game.cutscene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(GameExtension.class)
class CutsceneLoaderTest {
  @ParameterizedTest
  @CsvSource({
    "test-no-manifest, 2, a.jpeg",
    "cutscene1, 5, scene1.jpeg",
    "cutscene2, 6, scene1.jpeg",
    "cutscene3, 5, scene1.jpeg"
  })
  void shouldLoadCutsceneImages(String folder, int imageCount, String firstImage) {
    CutsceneLoader.Result result = new CutsceneLoader().load(folder);

    assertTrue(result.isSuccess());
    assertEquals(imageCount, result.getCutscene().getImagePaths().length);
    assertTrue(result.getCutscene().getImagePaths()[0].endsWith(firstImage));
  }

  @Test
  void shouldLoadConfiguredMusicFromManifest() {
    CutsceneLoader.Result result = new CutsceneLoader().load("cutscene1");

    assertTrue(result.isSuccess());
    assertEquals("sounds/Main_menu_sound.mp3", result.getCutscene().getDefinition().music);
  }

  @Test
  void shouldDefaultMusicWhenNotConfigured() {
    CutsceneLoader.Result result = new CutsceneLoader().load("test-no-manifest");

    assertTrue(result.isSuccess());
    assertEquals("sounds/Main_menu_sound.mp3", result.getCutscene().getDefinition().music);
  }

  @Test
  void shouldRejectUnsafeNames() {
    assertFalse(new CutsceneLoader().load("../cutscene1").isSuccess());
    assertFalse(new CutsceneLoader().load("/cutscene1").isSuccess());
    assertFalse(new CutsceneLoader().load("").isSuccess());
  }
}
