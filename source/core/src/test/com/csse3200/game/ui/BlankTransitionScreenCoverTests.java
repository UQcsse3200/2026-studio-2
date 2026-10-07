package com.csse3200.game.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
public class BlankTransitionScreenCoverTests {

  BlankTransitionScreenCover transitionScreenCover;

  Table mockTable;
  RenderService mockRenderService;
  Stage mockStage;

  @BeforeEach
  void setup() {
    transitionScreenCover = new BlankTransitionScreenCover();
    mockTable = mock(Table.class);
    transitionScreenCover.table = mockTable;

    mockStage = mock(Stage.class);
    mockRenderService = mock(RenderService.class);
    ServiceLocator.registerRenderService(mockRenderService);

    when(mockRenderService.getStage()).thenReturn(mockStage);
    when(mockStage.getWidth()).thenReturn(800f);
    when(mockStage.getHeight()).thenReturn(600f);
  }

  @Test
  void blankTransitionScreenCoverIsCreatedAndAddsAnActor() {
    transitionScreenCover.create();
    verify(mockStage, atLeastOnce()).addActor(any(Actor.class));
  }

  @Test
  void blankTransitionScreenHidesWhenVisibleSetToFalse() {
    transitionScreenCover.setVisible(false);
    verify(mockTable).setVisible(false);
  }

  @Test
  void blankTransitionScreenShowsWhenVisibleSetToTrue() {
    transitionScreenCover.setVisible(true);
    verify(mockTable).setVisible(true);
  }
}
