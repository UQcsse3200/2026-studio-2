package com.csse3200.game.screens;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ThankYouScreenTest {
  @Test
  void thankYouMessageIsAddedToTheStageOnEntry() {
    try (var batches = mockConstruction(SpriteBatch.class)) {
      ThankYouScreen screen =
          (ThankYouScreen) new GdxGame().createScreen(GdxGame.ScreenType.THANK_YOU);
      try {
        assertFalse(ServiceLocator.getRenderService().getStage().getActors().isEmpty());
      } finally {
        screen.dispose();
      }
    }
  }
}
