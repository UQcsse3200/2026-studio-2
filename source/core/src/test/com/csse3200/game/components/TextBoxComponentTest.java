package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TextBoxComponentTest {
  private SpriteBatch batch;

  @BeforeEach
  void beforeEach() {
    RenderService renderService = new RenderService();
    Stage stage = mock(Stage.class);
    renderService.setStage(stage);
    ServiceLocator.registerRenderService(renderService);

    Gdx.graphics = mock(com.badlogic.gdx.Graphics.class);
    when(Gdx.graphics.getDeltaTime()).thenReturn(0.1f);
    when(Gdx.graphics.getWidth()).thenReturn(800);
    when(Gdx.graphics.getHeight()).thenReturn(600);

    Gdx.input = mock(Input.class);
    when(Gdx.input.isKeyJustPressed(Input.Keys.TAB)).thenReturn(false);

    batch = mock(SpriteBatch.class);
  }

  private TextBoxComponent createTextBox(List<String> pages, boolean externallyControlled) {
    TextBoxComponent textBox =
        new TextBoxComponent(
            100,
            100,
            Color.BLACK,
            Color.TAN,
            Color.BROWN,
            30f,
            200,
            16,
            3,
            null,
            com.badlogic.gdx.utils.Align.center,
            pages);
    textBox.setExternallyControlled(externallyControlled);
    textBox.create();
    return textBox;
  }

  // --- advance() tests (externally controlled / cutscene mode) ---

  @Test
  void advanceShouldRevealPageWhenNotComplete() {
    TextBoxComponent textBox = createTextBox(List.of("Hello World", "Page 2"), true);
    textBox.render(batch); // populate fullContent

    assertFalse(textBox.isCurrentPageComplete());
    assertEquals(TextBoxComponent.AdvanceResult.REVEALED_PAGE, textBox.advance());
    assertTrue(textBox.isCurrentPageComplete());
  }

  @Test
  void advanceShouldMoveToNextPageAfterReveal() {
    TextBoxComponent textBox = createTextBox(List.of("Page 1", "Page 2", "Page 3"), true);
    textBox.render(batch);
    textBox.advance(); // reveal first page

    assertEquals(TextBoxComponent.AdvanceResult.NEXT_PAGE, textBox.advance());
    assertFalse(textBox.isOnLastPage()); // index 1 of [0,1,2] — not last
    assertTrue(textBox.isCurrentPageComplete()); // still revealed from previous page
  }

  @Test
  void advanceShouldReturnDismissedOnLastPageWhenExternallyControlled() {
    TextBoxComponent textBox = createTextBox(List.of("Only page"), true);
    textBox.render(batch);
    textBox.advance(); // reveal

    // Externally controlled: returns DISMISSED without calling dismiss()
    // (the cutscene screen manages the dismiss lifecycle)
    assertEquals(TextBoxComponent.AdvanceResult.DISMISSED, textBox.advance());
    assertFalse(textBox.isDismissed());
  }

  @Test
  void advanceShouldWalkThroughMultiplePagesThenDismiss() {
    // Long pages so a single render (0.1s * 30 chars/s = 3 chars) does NOT fully reveal them
    TextBoxComponent textBox =
        createTextBox(
            List.of("Page one content here", "Page two content here", "Page three content here"),
            true);
    textBox.render(batch);

    assertEquals(TextBoxComponent.AdvanceResult.REVEALED_PAGE, textBox.advance()); // reveal P1
    assertEquals(TextBoxComponent.AdvanceResult.NEXT_PAGE, textBox.advance()); // → P2
    assertFalse(textBox.isOnLastPage()); // P2 is index 1 of [0,1,2]

    textBox.render(batch); // re-populate fullContent for P2
    assertEquals(TextBoxComponent.AdvanceResult.REVEALED_PAGE, textBox.advance()); // reveal P2
    assertEquals(TextBoxComponent.AdvanceResult.NEXT_PAGE, textBox.advance()); // → P3
    assertTrue(textBox.isOnLastPage()); // P3 is index 2 of [0,1,2]

    textBox.render(batch); // re-populate fullContent for P3
    assertEquals(TextBoxComponent.AdvanceResult.REVEALED_PAGE, textBox.advance()); // reveal P3
    // Externally controlled: returns DISMISSED without calling dismiss()
    assertEquals(TextBoxComponent.AdvanceResult.DISMISSED, textBox.advance());
    assertFalse(textBox.isDismissed()); // cutscene screen manages dismiss lifecycle
  }

  @Test
  void advanceShouldDismissOnLastPageInLegacyMode() {
    TextBoxComponent textBox = createTextBox(List.of("Only page"), false);
    textBox.render(batch);
    textBox.advance(); // reveal

    assertEquals(TextBoxComponent.AdvanceResult.DISMISSED, textBox.advance());
    assertTrue(textBox.isDismissed());
  }

  @Test
  void advanceShouldReturnDismissedWhenAlreadyDismissed() {
    TextBoxComponent textBox = createTextBox(List.of("Page"), true);
    textBox.render(batch);
    textBox.advance(); // reveal
    textBox.advance(); // dismiss

    assertEquals(TextBoxComponent.AdvanceResult.DISMISSED, textBox.advance());
  }

  @Test
  void advanceShouldHandleSingleEmptyPage() {
    TextBoxComponent textBox = createTextBox(List.of(""), true);

    // Empty content: isCurrentPageComplete() is true because fullContent="" and revealedChars=0
    assertTrue(textBox.isOnLastPage());
    // Externally controlled: returns DISMISSED without calling dismiss()
    assertEquals(TextBoxComponent.AdvanceResult.DISMISSED, textBox.advance());
    assertFalse(textBox.isDismissed());
  }

  // --- revealCurrentPage() tests ---

  @Test
  void revealCurrentPageShouldCompleteImmediately() {
    TextBoxComponent textBox = createTextBox(List.of("A long text"), true);
    textBox.render(batch);

    assertFalse(textBox.isCurrentPageComplete());
    textBox.revealCurrentPage();
    assertTrue(textBox.isCurrentPageComplete());
  }

  // --- dismiss() tests ---

  @Test
  void dismissShouldBeIdempotent() {
    TextBoxComponent textBox = createTextBox(List.of("Page"), true);
    textBox.render(batch);

    textBox.dismiss();
    assertTrue(textBox.isDismissed());
    // Calling dismiss again should not throw
    textBox.dismiss();
    assertTrue(textBox.isDismissed());
  }

  // --- setOpacity() tests ---

  @Test
  void setOpacityShouldClampBetweenZeroAndOne() {
    TextBoxComponent textBox = createTextBox(List.of("Page"), true);
    textBox.render(batch);

    // Should not throw for out-of-range values
    textBox.setOpacity(-0.5f);
    textBox.setOpacity(1.5f);
    textBox.setOpacity(0.5f);
  }

  // --- state query tests ---

  @Test
  void isCurrentPageCompleteShouldReturnTrueForEmptyContent() {
    TextBoxComponent textBox = createTextBox(List.of(""), true);
    assertTrue(textBox.isCurrentPageComplete());
  }

  @Test
  void isOnLastPageShouldReturnTrueForSinglePage() {
    TextBoxComponent textBox = createTextBox(List.of("Only"), true);
    assertTrue(textBox.isOnLastPage());
  }

  @Test
  void isOnLastPageShouldReturnFalseWhenNotOnLast() {
    TextBoxComponent textBox = createTextBox(List.of("P1", "P2", "P3"), true);
    assertFalse(textBox.isOnLastPage());
  }

  @Test
  void isDismissedShouldBeFalseByDefault() {
    TextBoxComponent textBox = createTextBox(List.of("Page"), true);
    assertFalse(textBox.isDismissed());
  }

  @Test
  void shouldHandleNullPagesGracefully() {
    TextBoxComponent textBox = createTextBox(null, true);
    // Defaulted to a single empty page
    assertTrue(textBox.isOnLastPage());
    assertEquals(TextBoxComponent.AdvanceResult.DISMISSED, textBox.advance());
  }

  @Test
  void shouldHandleEmptyPagesListGracefully() {
    TextBoxComponent textBox = createTextBox(new ArrayList<>(), true);
    assertTrue(textBox.isOnLastPage());
    assertEquals(TextBoxComponent.AdvanceResult.DISMISSED, textBox.advance());
  }
}
