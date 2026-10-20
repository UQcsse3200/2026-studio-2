package com.csse3200.game.entities.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.Json;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TextConfigTest {
  @ParameterizedTest
  @CsvSource({
    "'{\"xPos\":120,\"yPos\":-40}', 120, -40",
    "'{\"xPos\":120.5,\"yPos\":-40.25}', 120.5, -40.25",
    "'{}', 100, 100"
  })
  void retainsConfiguredUiCoordinatesAndLegacyDefaults(String json, float x, float y) {
    TextConfig config = new Json().fromJson(TextConfig.class, json);

    assertEquals(x, config.xPos, 0.001f);
    assertEquals(y, config.yPos, 0.001f);
    assertEquals("flat-earth/skin/fonts/alagard.fnt", config.fontPath);
  }
}
