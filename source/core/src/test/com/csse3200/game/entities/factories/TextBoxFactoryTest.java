package com.csse3200.game.entities.factories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TextBoxFactoryTest {
  private RenderService renderService;

  @BeforeEach
  void setUp() {
    renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);
  }

  @Test
  void shouldCreateAndRegisterTextBoxFromConfig() {
    new TextBoxFactory().createTextBox("configs/textBoxes.json");

    verify(renderService).register(any(TextBoxComponent.class));
  }

  @Test
  void shouldNotCreateTextBoxWhenConfigDoesNotExist() {
    new TextBoxFactory().createTextBox("configs/does-not-exist.json");

    verifyNoInteractions(renderService);
  }

  @Test
  void shouldRegisterOnlyOneTextBoxForOneConfig() {
    new TextBoxFactory().createTextBox("configs/textBoxes.json");

    verify(renderService, org.mockito.Mockito.times(1)).register(any(TextBoxComponent.class));
  }
}
