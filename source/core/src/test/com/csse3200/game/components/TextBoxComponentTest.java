package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for {@link TextBoxComponent}.
 *
 * <p>{@code UIComponent.create()} (the superclass) pulls its {@code Stage} from {@code
 * ServiceLocator.getRenderService().getStage()}, so a mocked {@link RenderService} is registered
 * before each test. {@code UIComponent.skin} is a {@code protected static final} field loaded
 * directly from {@code flat-earth/skin/flat-earth-ui.json} via {@code Gdx.files.internal} the first
 * time the class is touched - it can't be mocked from a test, so these tests rely on that real
 * asset being resolvable from the test run's working directory (the same way it must be for any
 * other test that exercises a {@code UIComponent} subclass).
 *
 * <p>Because most of {@code TextBoxComponent}'s state (pages, revealedChars, dismissed, etc.) is
 * private with no accessors, these tests use reflection to inspect it rather than changing the
 * production class.
 */
@ExtendWith(GameExtension.class)
class TextBoxComponentTest {

  private Graphics mockGraphics;
  private Input mockInput;

  @BeforeEach
  void setUp() {
    RenderService renderService = mock(RenderService.class);
    Stage stage = mock(Stage.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    // Mock Gdx.graphics / Gdx.input so tests control delta-time and key presses precisely.
    mockGraphics = mock(Graphics.class);
    mockInput = mock(Input.class);
    Gdx.graphics = mockGraphics;
    Gdx.input = mockInput;
    when(mockGraphics.getWidth()).thenReturn(800);
    when(mockGraphics.getHeight()).thenReturn(600);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  private TextBoxComponent makeComponent(List<String> pages, float charsPerSecond) {
    return new TextBoxComponent(
        10f,
        10f,
        Color.BLACK,
        Color.TAN,
        Color.BROWN,
        charsPerSecond,
        200,
        16,
        3,
        null, // no custom font -> falls back to the skin's default font
        com.badlogic.gdx.utils.Align.center,
        pages);
  }

  @SuppressWarnings("unchecked")
  private static <T> T getField(Object target, String name) {
    try {
      Field field = TextBoxComponent.class.getDeclaredField(name);
      field.setAccessible(true);
      return (T) field.get(target);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeDraw(TextBoxComponent component) {
    try {
      Method draw =
          TextBoxComponent.class.getDeclaredMethod(
              "draw", com.badlogic.gdx.graphics.g2d.SpriteBatch.class);
      draw.setAccessible(true);
      draw.invoke(component, (Object) null);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  // ---- Page list defaulting ----

  @Test
  void shouldDefaultToSingleEmptyPageWhenPagesIsNull() {
    TextBoxComponent component = makeComponent(null, 30f);
    List<String> pages = getField(component, "pages");
    assertEquals(Collections.singletonList(""), pages);
  }

  @Test
  void shouldDefaultToSingleEmptyPageWhenPagesIsEmpty() {
    TextBoxComponent component = makeComponent(List.of(), 30f);
    List<String> pages = getField(component, "pages");
    assertEquals(Collections.singletonList(""), pages);
  }

  @Test
  void shouldKeepProvidedPagesWhenNonEmpty() {
    List<String> providedPages = List.of("Page one", "Page two");
    TextBoxComponent component = makeComponent(providedPages, 30f);
    List<String> pages = getField(component, "pages");
    assertEquals(providedPages, pages);
  }

  // ---- Typewriter reveal ----

  @Test
  void shouldNotBuildActorsBeforeFirstDraw() {
    TextBoxComponent component = makeComponent(List.of("Hello world"), 10f);
    Table table = getField(component, "table");
    assertNull(table);
  }

  @Test
  void shouldGraduallyRevealCharactersOverTime() {
    TextBoxComponent component = makeComponent(List.of("Hello world"), 10f); // 10 chars/sec
    when(mockGraphics.getDeltaTime()).thenReturn(0.05f); // 50ms per frame -> 0.5 chars/frame
    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(false);

    component.create();
    invokeDraw(component);

    int revealedAfterOneFrame = getField(component, "revealedChars");
    assertTrue(revealedAfterOneFrame <= 1);

    // Advance ~1 more second total -> should reveal all 11 characters of "Hello world"
    for (int i = 0; i < 20; i++) {
      invokeDraw(component);
    }
    int revealedChars = getField(component, "revealedChars");
    assertEquals("Hello world".length(), revealedChars);
  }

  @Test
  void shouldNotShowTableWhenCurrentPageIsEmpty() {
    TextBoxComponent component = makeComponent(List.of(""), 10f);
    when(mockGraphics.getDeltaTime()).thenReturn(0.1f);
    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(false);

    component.create();
    invokeDraw(component);

    Table table = getField(component, "table");
    assertFalse(table.isVisible());
  }

  // ---- TAB behaviour ----

  @Test
  void firstTabPressShouldInstantlyRevealRestOfPage() {
    TextBoxComponent component = makeComponent(List.of("A longer line of dialogue text"), 5f);
    when(mockGraphics.getDeltaTime()).thenReturn(0.02f); // barely any time passed
    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(true);

    component.create();
    invokeDraw(component);

    int revealedChars = getField(component, "revealedChars");
    assertEquals("A longer line of dialogue text".length(), revealedChars);
    boolean dismissed = getField(component, "dismissed");
    assertFalse(dismissed);
  }

  @Test
  void tabOnLastFullyRevealedPageShouldDismissBox() {
    TextBoxComponent component = makeComponent(List.of("Only page"), 100f);
    when(mockGraphics.getDeltaTime()).thenReturn(1f); // fully reveal immediately
    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(false);

    component.create();
    invokeDraw(component); // fully reveals the only page

    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(true);
    invokeDraw(component); // TAB on a fully-revealed, last page -> dismiss

    boolean dismissed = getField(component, "dismissed");
    assertTrue(dismissed);

    Table table = getField(component, "table");
    assertFalse(table.isVisible());
  }

  @Test
  void tabOnFullyRevealedNonLastPageShouldAdvanceToNextPage() {
    TextBoxComponent component = makeComponent(List.of("Page one", "Page two"), 100f);
    when(mockGraphics.getDeltaTime()).thenReturn(1f);
    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(false);

    component.create();
    invokeDraw(component); // fully reveal page one

    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(true);
    invokeDraw(component); // TAB -> advance to page two, not dismissed

    int currentPageIndex = getField(component, "currentPageIndex");
    assertEquals(1, currentPageIndex);
    boolean dismissed = getField(component, "dismissed");
    assertFalse(dismissed);

    // Typing state should reset for the new page
    invokeDraw(component);
    String lastSourceContent = getField(component, "lastSourceContent");
    assertEquals("Page two", lastSourceContent);
  }

  @Test
  void drawShouldDoNothingOnceDismissed() {
    TextBoxComponent component = makeComponent(List.of("Only page"), 100f);
    when(mockGraphics.getDeltaTime()).thenReturn(1f);
    when(mockInput.isKeyJustPressed(Keys.TAB)).thenReturn(true);

    component.create();
    invokeDraw(component); // fully reveals and dismisses (only page, fully revealed by TAB)
    invokeDraw(component); // second call: dismissed should short-circuit, no exceptions

    boolean dismissed = getField(component, "dismissed");
    assertTrue(dismissed);
  }
}
