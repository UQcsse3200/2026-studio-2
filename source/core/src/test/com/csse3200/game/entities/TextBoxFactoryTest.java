package com.csse3200.game.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.entities.factories.TextBoxFactory;
import com.csse3200.game.files.FileLoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/**
 * Uses Mockito's static/constructor mocking to isolate TextBoxFactory from both file I/O
 * (FileLoader) and the real, Gdx-heavy TextBoxComponent.
 *
 * <p>Requires the "inline" mock maker (mockito-inline, or Mockito 5+ where it's the default) for
 * mockStatic/mockConstruction to work. If your build fails with a "MockMaker" error, add
 * `testImplementation "org.mockito:mockito-inline:<version>"` (Mockito &lt; 5) to build.gradle.
 */
class TextBoxFactoryTest {

  @Test
  void logsAndReturnsWithoutThrowing_whenConfigFailsToLoad() {
    TextBoxFactory factory = new TextBoxFactory();

    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      fileLoader
          .when(() -> FileLoader.readClass(eq(TextConfig.class), anyString()))
          .thenReturn(null);

      assertDoesNotThrow(() -> factory.createTextBox("configs/missing.json"));
    }
  }

  @Test
  void buildsAndCreatesTextBoxComponent_whenConfigLoadsSuccessfully() {
    TextBoxFactory factory = new TextBoxFactory();
    TextConfig config = new TextConfig();
    config.pages.set(0, "Hello, adventurer!");

    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class);
        MockedConstruction<TextBoxComponent> mockedComponent =
            mockConstruction(TextBoxComponent.class)) {

      fileLoader
          .when(() -> FileLoader.readClass(eq(TextConfig.class), anyString()))
          .thenReturn(config);

      factory.createTextBox("configs/textBoxes.json");

      assertEquals(1, mockedComponent.constructed().size());
      verify(mockedComponent.constructed().get(0)).create();
    }
  }

  @Test
  void doesNotPropagateExceptions_whenTextBoxComponentCreationFails() {
    // No Gdx game/render context is running in this unit test, so the real
    // TextBoxComponent.create() call inside the factory is expected to fail (e.g. a
    // NullPointerException from a missing Stage/Skin). This checks the factory's try/catch
    // swallows that instead of letting it escape and crash the caller (e.g. the terminal command).
    TextBoxFactory factory = new TextBoxFactory();
    TextConfig config = new TextConfig();

    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      fileLoader
          .when(() -> FileLoader.readClass(eq(TextConfig.class), anyString()))
          .thenReturn(config);

      assertDoesNotThrow(() -> factory.createTextBox("configs/textBoxes.json"));
    }
  }
}
