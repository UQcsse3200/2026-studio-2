package com.csse3200.game.ui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameEndDisplayTest {
  @BeforeEach
  void setUp() {
    Graphics graphics = mock(Graphics.class);
    when(graphics.getWidth()).thenReturn(800);
    when(graphics.getHeight()).thenReturn(600);
    when(graphics.getBackBufferWidth()).thenReturn(800);
    when(graphics.getBackBufferHeight()).thenReturn(600);
    Gdx.graphics = graphics;

    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);

    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class)))
        .thenReturn(mock(Texture.class));
    ServiceLocator.registerResourceService(resourceService);

    ServiceLocator.registerEntityService(new EntityService());
  }

  /** Registers the display on a UI entity (running create/buildActors) and returns it. */
  private GameEndDisplay createDisplay(GameEndState state) {
    GameEndDisplay display = new GameEndDisplay(state);
    Entity ui = new Entity().addComponent(display);
    ServiceLocator.getEntityService().register(ui);
    return display;
  }

  @Test
  void shouldRepresentWinState() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.WIN);

    assertEquals(GameEndState.WIN, display.getState());
    assertEquals("YOU WIN!", display.getTitleText());
    assertTrue(display.getResultText().contains("closer to home"));
  }

  @Test
  void shouldRepresentLoseState() {
    GameEndDisplay display = new GameEndDisplay(GameEndState.LOSE);

    assertEquals(GameEndState.LOSE, display.getState());
    assertEquals("GAME OVER!", display.getTitleText());
    assertTrue(display.getResultText().contains("Penelepe"));
  }

  @Test
  void shouldNotBuildMessageBoxUntilGameEnds() {
    GameEndDisplay display = createDisplay(GameEndState.LOSE);

    // The box is a standalone stage actor that reveals itself once drawn, so building it at boot
    // would type the result text over live gameplay. It must only exist after setState().
    assertNull(display.getMessageBox());
  }

  @Test
  void shouldRevealMessageOverTime() {
    GameEndDisplay display = createDisplay(GameEndState.LOSE);
    display.setState(GameEndState.LOSE);

    TextBoxComponent messageBox = display.getMessageBox();
    assertNotNull(messageBox);
    messageBox.revealCurrentPage();

    assertTrue(messageBox.isCurrentPageComplete());
  }

  @Test
  void shouldRebuildMessageBoxOnStateChange() {
    GameEndDisplay display = createDisplay(GameEndState.LOSE);
    display.setState(GameEndState.WIN);

    TextBoxComponent firstBox = display.getMessageBox();
    assertNotNull(firstBox);
    display.setState(GameEndState.LOSE);

    assertTrue(firstBox.isDismissed());
    TextBoxComponent secondBox = display.getMessageBox();
    assertNotNull(secondBox);
    assertNotSame(firstBox, secondBox);
    assertFalse(secondBox.isDismissed());
  }

  @Test
  void shouldDismissMessageBoxOnHide() {
    GameEndDisplay display = createDisplay(GameEndState.LOSE);
    display.setState(GameEndState.LOSE);

    display.hide();

    assertTrue(display.getMessageBox().isDismissed());
    assertFalse(display.isVisible());
  }

  @Test
  void shouldFallBackToFractionPositionWithoutRealStage() {
    GameEndDisplay display = createDisplay(GameEndState.LOSE);
    display.setState(GameEndState.LOSE);

    // Mock stage has no real actors, so the button anchor is skipped and drawing positions the
    // box at the stage-fraction fallback without throwing.
    assertDoesNotThrow(() -> display.draw(null));
    assertFalse(display.getMessageBox().isDismissed());
  }

  @Test
  void shouldAnchorMessageAboveRestartButtonOnRealStage() {
    RenderService realStageService = mock(RenderService.class);
    Stage realStage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    when(realStageService.getStage()).thenReturn(realStage);
    ServiceLocator.registerRenderService(realStageService);

    GameEndDisplay display = createDisplay(GameEndState.WIN);
    display.setState(GameEndState.WIN);
    display.draw(null); // positions the message box from the laid-out buttons

    com.badlogic.gdx.scenes.scene2d.ui.ImageButton restartBtn = getRestartButton(display);
    Vector2 buttonTopCenter =
        restartBtn.localToStageCoordinates(
            new Vector2(restartBtn.getWidth() / 2f, restartBtn.getHeight()));
    com.badlogic.gdx.scenes.scene2d.ui.Table messageTable = getMessageTable(display);

    // Bottom edge pinned just above the restart button: clear of both title and buttons.
    assertEquals(buttonTopCenter.y + 24f, messageTable.getY(), 1f);
    assertEquals(buttonTopCenter.x, messageTable.getX(), 1f);
  }

  private static com.badlogic.gdx.scenes.scene2d.ui.ImageButton getRestartButton(
      GameEndDisplay display) {
    try {
      java.lang.reflect.Field field = GameEndDisplay.class.getDeclaredField("restartBtn");
      field.setAccessible(true);
      return (com.badlogic.gdx.scenes.scene2d.ui.ImageButton) field.get(display);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static com.badlogic.gdx.scenes.scene2d.ui.Table getMessageTable(
      GameEndDisplay display) {
    try {
      java.lang.reflect.Field field =
          com.csse3200.game.components.TextBoxComponent.class.getDeclaredField("table");
      field.setAccessible(true);
      return (com.badlogic.gdx.scenes.scene2d.ui.Table) field.get(display.getMessageBox());
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
