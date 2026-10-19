package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.TopRightButtonDisplay;
import com.csse3200.game.ui.UiTestSupport;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExitButtonDisplayTest extends UiTestSupport {
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void sharesVisibilityLayoutAndCleanupWhileRetainingEachScreensButtonSize(boolean mainGame) {
    TopRightButtonDisplay display =
        mainGame ? new MainGameExitDisplay() : new CyclopsMinigameDisplay();
    display.setVisible(false); // Visibility can be requested before the actors exist.
    new Entity().addComponent(display);
    display.create();
    Table table = (Table) stage.getActors().first();
    table.validate();
    ImageButton button = (ImageButton) table.getChildren().first();
    assertEquals(mainGame ? 220f : 160f, button.getWidth());
    assertEquals(mainGame ? 80f : 56f, button.getHeight());
    assertEquals(mainGame ? 20f : 10f, stage.getWidth() - button.getRight());
    assertEquals(mainGame ? 90f : 10f, stage.getHeight() - button.getTop());
    assertEquals(2f, display.getZIndex());
    display.setVisible(false);
    assertFalse(table.isVisible());
    display.setVisible(true);
    assertTrue(table.isVisible());
    display.draw(batch);
    display.dispose();
    assertTrue(table.getChildren().isEmpty());
    verify(ServiceLocator.getRenderService()).unregister(display);
  }

  @Test
  void mainGameExitWaitsForClickSoundCompletionBeforeEmittingExit() {
    MainGameExitDisplay display = new MainGameExitDisplay();
    Entity entity = new Entity().addComponent(display);
    AtomicInteger exits = new AtomicInteger();
    entity.getEvents().addListener("exit", exits::incrementAndGet);
    AtomicReference<Runnable> afterSound = new AtomicReference<>();
    try (var sound = mockStatic(ButtonSound.class)) {
      sound
          .when(() -> ButtonSound.playClickThen(any(Runnable.class)))
          .thenAnswer(
              call -> {
                afterSound.set(call.getArgument(0));
                return null;
              });
      display.create();
      Table table = (Table) stage.getActors().first();
      table.getChildren().first().fire(new ChangeListener.ChangeEvent());
      assertEquals(0, exits.get());
      assertNotNull(afterSound.get());
      afterSound.get().run();
      assertEquals(1, exits.get());
      display.dispose();
    }
  }
}
