package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TextBoxCommandTest {
  private RenderService renderService;

  @BeforeEach
  void setUp() {
    renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);
  }

  private static ArrayList<String> args(String... values) {
    return new ArrayList<>(List.of(values));
  }

  @Test
  void shouldResolveShorthandToPortraitsDirectory() {
    assertEquals(
        "images/portraits/test_portrait.jpeg", TextBoxCommand.resolvePortraitPath("test_portrait"));
  }

  @Test
  void shouldFallBackThroughExtensions() {
    assertEquals(
        "images/portraits/calypso_happy.png", TextBoxCommand.resolvePortraitPath("calypso_happy"));
  }

  @Test
  void shouldPassExplicitPathsThrough() {
    assertEquals(
        "images/cutscenes/cutscene1/scene1.jpeg",
        TextBoxCommand.resolvePortraitPath("images/cutscenes/cutscene1/scene1.jpeg"));
  }

  @Test
  void shouldReturnNullForUnknownOrBlankNames() {
    assertNull(TextBoxCommand.resolvePortraitPath("no_such_portrait"));
    assertNull(TextBoxCommand.resolvePortraitPath(""));
    assertNull(TextBoxCommand.resolvePortraitPath("   "));
    assertNull(TextBoxCommand.resolvePortraitPath(null));
    assertNull(TextBoxCommand.resolvePortraitPath("images/does-not-exist.jpeg"));
  }

  @Test
  void shouldLoadDefaultConfigWithoutArgs() {
    TextBoxCommand command = new TextBoxCommand("configs/textBoxes.json");

    assertTrue(command.action(args()));
    verify(renderService).register(any(TextBoxComponent.class));
  }

  @Test
  void shouldRejectPortraitWithoutName() {
    TextBoxCommand command = new TextBoxCommand("configs/textBoxes.json");

    assertFalse(command.action(args("portrait")));
    verify(renderService, never()).register(any(TextBoxComponent.class));
  }

  @Test
  void shouldRejectUnknownPortrait() {
    TextBoxCommand command = new TextBoxCommand("configs/textBoxes.json");

    assertFalse(command.action(args("portrait", "no_such_portrait")));
    verify(renderService, never()).register(any(TextBoxComponent.class));
  }

  @Test
  void shouldPreviewKnownPortrait() {
    TextBoxCommand command = new TextBoxCommand("configs/textBoxes.json");

    assertTrue(command.action(args("portrait", "test_portrait")));
    verify(renderService).register(any(TextBoxComponent.class));
  }
}
