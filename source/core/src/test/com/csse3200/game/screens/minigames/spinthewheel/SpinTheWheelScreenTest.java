package com.csse3200.game.screens.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelActions;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelDisplay;
import com.csse3200.game.components.minigames.spinthewheel.WheelConfig;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SpinTheWheelScreenTest {
  private final SpinTheWheelScreen screen = new SpinTheWheelScreen(mock(GdxGame.class));

  @Test
  void shouldLoadTheWheelsAssets() {
    assertArrayEquals(WheelConfig.TEXTURES, screen.getTextures());
    assertArrayEquals(WheelConfig.SOUNDS, screen.getSounds());
  }

  @Test
  void shouldBuildTheWheelUi() {
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);

    Entity ui = screen.createUI();

    assertNotNull(ui.getComponent(SpinTheWheelDisplay.class));
    assertNotNull(ui.getComponent(InputDecorator.class));
    assertNotNull(ui.getComponent(SpinTheWheelActions.class));
  }
}
