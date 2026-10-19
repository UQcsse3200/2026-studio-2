package com.csse3200.game.components.maingame;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UiTestSupport;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PauseButtonDisplayTest extends UiTestSupport {
  private static final String PAUSE_UP = "images/Buttons/pause_up_btn.png";
  private static final String PAUSE_DOWN = "images/Buttons/pause_down_btn.png";

  @ParameterizedTest
  @CsvSource({"true, true", "true, false", "false, true", "false, false"})
  void shouldUsePauseArtOnlyWhenBothImagesExist(boolean upExists, boolean downExists) {
    PauseButtonDisplay display = new PauseButtonDisplay(() -> {});
    new Entity().addComponent(display);
    Files originalFiles = Gdx.files;
    Files files = mock(Files.class);
    FileHandle up = mock(FileHandle.class);
    FileHandle down = mock(FileHandle.class);
    when(files.internal(PAUSE_UP)).thenReturn(up);
    when(files.internal(PAUSE_DOWN)).thenReturn(down);
    when(up.exists()).thenReturn(upExists);
    when(down.exists()).thenReturn(downExists);
    Gdx.files = files;
    try {
      boolean pauseArt = upExists && downExists;
      assertEquals(pauseArt, PauseButtonDisplay.hasPauseImages());
      assertArrayEquals(
          pauseArt ? new String[] {PAUSE_UP, PAUSE_DOWN} : new String[0],
          PauseButtonDisplay.extraTextures());
      display.create();
      verify(resources)
          .getAsset(pauseArt ? PAUSE_UP : "images/Buttons/exit_up_btn.png", Texture.class);
      verify(resources)
          .getAsset(pauseArt ? PAUSE_DOWN : "images/Buttons/exit_down_btn.png", Texture.class);
      display.dispose();
    } finally {
      Gdx.files = originalFiles;
    }
  }

  @Test
  void shouldKeepPauseLayoutVisibilityCleanupAndSoundCompletionAction() {
    AtomicInteger pauses = new AtomicInteger();
    PauseButtonDisplay display = new PauseButtonDisplay(pauses::incrementAndGet);
    display.setVisible(false);
    new Entity().addComponent(display);
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
      table.validate();
      ImageButton button = (ImageButton) table.getChildren().first();
      assertEquals(240f, button.getWidth());
      assertEquals(80f, button.getHeight());
      assertEquals(20f, stage.getWidth() - button.getRight());
      assertEquals(90f, stage.getHeight() - button.getTop());
      assertEquals(2f, display.getZIndex());
      display.setVisible(false);
      assertFalse(table.isVisible());
      display.setVisible(true);
      assertTrue(table.isVisible());
      button.fire(new ChangeListener.ChangeEvent());
      assertEquals(0, pauses.get());
      assertNotNull(afterSound.get());
      afterSound.get().run();
      assertEquals(1, pauses.get());
      display.draw(batch);
      display.dispose();
      assertTrue(table.getChildren().isEmpty());
      verify(ServiceLocator.getRenderService()).unregister(display);
    }
  }
}
