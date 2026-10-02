package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TimingBarDisplayTests {

  TimingBarDisplay display;
  TimingBarLogic logic;
  RenderService mockRenderService;
  Stage mockStage;

  @BeforeEach
  void setup() {
    logic = mock(TimingBarLogic.class);
    display = new TimingBarDisplay(logic);

    mockStage = mock(Stage.class);
    mockRenderService = mock(RenderService.class);
    ServiceLocator.registerRenderService(mockRenderService);

    when(mockRenderService.getStage()).thenReturn(mockStage);
    when(mockStage.getWidth()).thenReturn(800f);
    when(mockStage.getHeight()).thenReturn(600f);
  }

  @Test
  void timingBarDisplayIsCreatedAndAddsAnActor() {
    when(logic.getMarkerX()).thenReturn(0f);
    display.create();
    verify(mockStage, atLeastOnce()).addActor(any(Actor.class));
  }
}
