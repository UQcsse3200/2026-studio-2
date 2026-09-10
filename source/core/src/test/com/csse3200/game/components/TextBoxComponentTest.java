package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * NOTE ON TEST STRATEGY --------------------- TextBoxComponent is tightly coupled to LibGDX statics
 * (Gdx.graphics, Gdx.input, Gdx.files) and to scene2d (Stage/Skin via UIComponent.create()). Fully
 * exercising create()/draw() - the typewriter reveal timing, TAB-to-skip, TAB-to-advance-page,
 * TAB-to-dismiss-on-last-page behaviour, and the on-screen scroll rendering - needs a running (or
 * headless) Gdx app, which this project likely already provides via a shared test extension (e.g.
 * GameExtension) used by other *ComponentTest classes. If so, add
 * `@ExtendWith(GameExtension.class)` here and extend this class to cover that behaviour directly.
 *
 * <p>Without that harness, these tests are limited to what's reachable from the constructor alone
 * (which only touches Gdx.files, not graphics/input/scene2d), using a lightweight manual stub of
 * that one static. See TestPlan.md for the manual/integration test cases covering the rest of the
 * component's behaviour.
 */
class TextBoxComponentTest {

  private Files originalFiles;

  @BeforeEach
  void stubGdxFiles() {
    originalFiles = Gdx.files;
    Gdx.files = mock(Files.class);
  }

  @AfterEach
  void restoreGdxFiles() {
    Gdx.files = originalFiles;
  }

  private TextBoxComponent newComponent(String fontPath, List<String> pages) {
    return new TextBoxComponent(
        100f,
        100f,
        Color.BLACK,
        Color.TAN,
        Color.BROWN,
        30f,
        200,
        16,
        3,
        fontPath,
        Align.center,
        pages);
  }

  private Object getPrivateField(Object target, String fieldName) throws Exception {
    Field field = TextBoxComponent.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  @Test
  void nullPagesDefaultsToSingleEmptyPage() throws Exception {
    TextBoxComponent component = newComponent(null, null);

    @SuppressWarnings("unchecked")
    List<String> pages = (List<String>) getPrivateField(component, "pages");
    assertEquals(List.of(""), pages);
  }

  @Test
  void emptyPagesListDefaultsToSingleEmptyPage() throws Exception {
    TextBoxComponent component = newComponent(null, Collections.emptyList());

    @SuppressWarnings("unchecked")
    List<String> pages = (List<String>) getPrivateField(component, "pages");
    assertEquals(List.of(""), pages);
  }

  @Test
  void nullFontPathLeavesCustomFontNull() throws Exception {
    TextBoxComponent component = newComponent(null, List.of("Hello"));

    assertNull(getPrivateField(component, "customFont"));
    verifyNoInteractions(Gdx.files);
  }

  @Test
  void blankFontPathLeavesCustomFontNull() throws Exception {
    TextBoxComponent component = newComponent("   ", List.of("Hello"));

    assertNull(getPrivateField(component, "customFont"));
  }

  @Test
  void missingFontFileFallsBackToNullFontInsteadOfThrowing() throws Exception {
    when(Gdx.files.internal(anyString())).thenThrow(new GdxRuntimeException("File not found"));

    TextBoxComponent component =
        assertDoesNotThrow(() -> newComponent("fonts/does-not-exist.fnt", List.of("Hello")));

    assertNull(getPrivateField(component, "customFont"));
  }
}
